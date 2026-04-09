package com.sans.deepfocus.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sans.deepfocus.data.AppDatabase
import com.sans.deepfocus.domain.FocusAudioManager
import com.sans.deepfocus.domain.SessionState
import com.sans.deepfocus.domain.TimerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class FocusService : Service() {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private lateinit var timerManager: TimerManager
    private lateinit var audioManager: FocusAudioManager

    private val NOTIFICATION_ID = 1
    private val CHANNEL_ID = "focus_session_channel"

    inner class LocalBinder : Binder() {
        fun getService(): FocusService = this@FocusService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder = binder


    override fun onCreate() {
        super.onCreate()
        val database = AppDatabase.getInstance(this)
        timerManager = TimerManager.getInstance(database.sessionDao())
        audioManager = FocusAudioManager(this)

        val prefs = getSharedPreferences("deepfocus_prefs", Context.MODE_PRIVATE)
        audioManager.setMuted(prefs.getBoolean("is_muted", false))

        createNotificationChannel()

        observeSessionAndSound(database)
        observeMuteState(prefs)
    }

    private fun observeSessionAndSound(database: AppDatabase) {
        scope.launch {
            kotlinx.coroutines.flow.combine(
                timerManager.sessionState,
                database.soundDao().getSelectedSound()
            ) { state, sound -> state to sound }
                .collect { (state, sound) ->
                    if (state == SessionState.RUNNING) {
                        if (sound != null && sound.uri.isNotEmpty()) {
                            audioManager.playSound(sound.uri)
                        } else {
                            audioManager.stopWithFade()
                        }
                    } else if (state == SessionState.PAUSED || state == SessionState.IDLE) {
                        audioManager.stopWithFade()
                    }
                }
        }
    }

    private fun observeMuteState(prefs: android.content.SharedPreferences) {
        scope.launch {
            val listener =
                android.content.SharedPreferences.OnSharedPreferenceChangeListener { p, key ->
                    if (key == "is_muted") {
                        audioManager.setMuted(p.getBoolean(key, false))
                    }
                }
            prefs.registerOnSharedPreferenceChangeListener(listener)
            try {
                awaitCancellation()
            } finally {
                prefs.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Focus Session",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val mode = intent?.getStringExtra("MODE") ?: "Focus"

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focusing in $mode mode")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        return START_STICKY
    }

    fun stopSession() {
        timerManager.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        audioManager.release()
    }
}
