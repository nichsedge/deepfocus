package com.sans.deepfocus.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sans.deepfocus.analytics.AnalyticsProvider
import com.sans.deepfocus.data.SoundDao
import com.sans.deepfocus.data.SoundEntity
import com.sans.deepfocus.domain.*
import android.content.Intent
import android.provider.OpenableColumns
import com.sans.deepfocus.service.FocusService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class TimerViewModel(
    application: Application,
    private val timerManager: TimerManager,
    private val soundDao: SoundDao,
    private val tagDao: com.sans.deepfocus.data.TagDao
) : AndroidViewModel(application) {
    private val analytics = AnalyticsProvider.get()
    val sessionState: StateFlow<SessionState> = timerManager.sessionState
    val sessionMode: StateFlow<SessionMode> = timerManager.sessionMode
    
    val displayTime: StateFlow<String> = combine(
        timerManager.sessionMode,
        timerManager.remainingTime,
        timerManager.elapsedTime
    ) { mode, remaining, elapsed ->
        if (mode == SessionMode.POMODORO) {
            formatTime(remaining)
        } else {
            formatTime(elapsed)
        }
    }.distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), formatTime(timerManager.remainingTime.value))

    private val prefs = application.getSharedPreferences("deepfocus_prefs", android.content.Context.MODE_PRIVATE)
    private val _isMuted = MutableStateFlow(prefs.getBoolean("is_muted", false))
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    fun toggleMute() {
        val newState = !isMuted.value
        _isMuted.value = newState
        prefs.edit().putBoolean("is_muted", newState).apply()
        analytics.trackEvent("toggle_mute", mapOf("muted" to newState))
    }

    val pomodoroDuration: StateFlow<Long> = timerManager.pomodoroDuration

    fun setPomodoroDuration(minutes: Int) {
        val durationMs = minutes * 60 * 1000L
        timerManager.setPomodoroDuration(durationMs)
        analytics.trackEvent("set_pomodoro_duration", mapOf("minutes" to minutes))
    }

    val availableSounds: StateFlow<List<SoundEntity>> = soundDao.getAllSounds()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedSound: StateFlow<SoundEntity?> = soundDao.getSelectedSound()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val availableTags: StateFlow<List<com.sans.deepfocus.data.TagEntity>> = tagDao.getAllTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedTag: StateFlow<String?> = timerManager.selectedTag

    fun selectTag(tagName: String?) {
        timerManager.setSelectedTag(tagName)
        analytics.trackEvent("select_tag", mapOf("tag" to (tagName ?: "none")))
    }

    fun addTag(name: String) {
        viewModelScope.launch {
            tagDao.insertTag(com.sans.deepfocus.data.TagEntity(name))
            analytics.trackEvent("add_tag", mapOf("tag_name" to name))
        }
    }

    fun deleteTag(tag: com.sans.deepfocus.data.TagEntity) {
        viewModelScope.launch {
            if (timerManager.selectedTag.value == tag.name) {
                timerManager.setSelectedTag(null)
            }
            tagDao.deleteTag(tag)
            analytics.trackEvent("delete_tag", mapOf("tag_name" to tag.name))
        }
    }

    fun setMode(mode: SessionMode) {
        analytics.trackEvent("change_mode", mapOf("mode" to mode.name))
        timerManager.setMode(mode)
    }

    fun start() {
        analytics.trackEvent("timer_start", mapOf("mode" to sessionMode.value.name))
        
        val intent = Intent(getApplication(), FocusService::class.java).apply {
            putExtra("MODE", sessionMode.value.name)
            putExtra("DURATION", if (sessionMode.value == SessionMode.POMODORO) pomodoroDuration.value else 0L)
        }
        getApplication<Application>().startForegroundService(intent)
        
        timerManager.start()
    }

    fun pause() {
        analytics.trackEvent("timer_pause")
        timerManager.pause()
    }

    fun resume() {
        analytics.trackEvent("timer_resume")
        timerManager.resume()
    }

    fun stop() {
        analytics.trackEvent("timer_stop")
        timerManager.stop()
        getApplication<Application>().stopService(Intent(getApplication(), FocusService::class.java))
    }

    fun selectSound(sound: SoundEntity) {
        viewModelScope.launch {
            soundDao.selectSound(sound.id)
            analytics.trackEvent("select_sound", mapOf("sound_name" to sound.name))
        }
    }

    fun uploadCustomSound(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                
                // Extract filename
                var displayName = "Custom Sound"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameIndex != -1) {
                        displayName = cursor.getString(nameIndex)
                    }
                }
                
                // Remove extension for display name
                val name = displayName.substringBeforeLast(".")
                
                val soundsDir = File(context.filesDir, "custom_sounds")
                if (!soundsDir.exists()) soundsDir.mkdirs()

                val fileName = "custom_${System.currentTimeMillis()}.mp3"
                val destFile = File(soundsDir, fileName)

                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }

                val sound = SoundEntity(
                    name = name,
                    uri = destFile.absolutePath,
                    isCustom = true
                )
                soundDao.insertSound(sound)
                analytics.trackEvent("upload_sound", mapOf("sound_name" to name))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteSound(sound: SoundEntity) {
        if (!sound.isCustom) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                File(sound.uri).delete()
                soundDao.deleteSound(sound)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
