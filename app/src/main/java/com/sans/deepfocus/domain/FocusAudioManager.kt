package com.sans.deepfocus.domain

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.*

class FocusAudioManager(private val context: Context) {
    private var exoPlayer: ExoPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var fadeJob: Job? = null

    private var isMuted = false
    private var currentUri: String? = null

    fun playSound(uriString: String) {
        if (currentUri == uriString && exoPlayer?.isPlaying == true) return
        currentUri = uriString
        
        stopSound()
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            val uri = when {
                uriString.startsWith("asset:///") -> android.net.Uri.parse(uriString)
                uriString.startsWith("/") -> android.net.Uri.fromFile(java.io.File(uriString))
                else -> android.net.Uri.parse(uriString)
            }
            val mediaItem = MediaItem.fromUri(uri)
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
            volume = 0f // Start at 0 for fade in
            play()
        }
        if (!isMuted) fadeIn()
    }

    fun setMuted(muted: Boolean) {
        isMuted = muted
        exoPlayer?.volume = if (muted) 0f else 1f
    }

    private fun fadeIn() {
        fadeJob?.cancel()
        fadeJob = scope.launch {
            val steps = 15
            val stepDuration = 100L
            val volumeIncrement = 1f / steps
            for (i in 1..steps) {
                exoPlayer?.volume = i * volumeIncrement
                delay(stepDuration)
            }
            exoPlayer?.volume = 1f
        }
    }

    fun stopWithFade() {
        fadeJob?.cancel()
        fadeJob = scope.launch {
            val steps = 15
            val stepDuration = 100L
            val currentVolume = exoPlayer?.volume ?: 0f
            val volumeDecrement = currentVolume / steps
            for (i in 1..steps) {
                exoPlayer?.volume = currentVolume - (i * volumeDecrement)
                delay(stepDuration)
            }
            stopSound()
        }
    }

    fun stopSound() {
        fadeJob?.cancel()
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
    }

    fun release() {
        stopSound()
        scope.cancel()
    }
}
