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

    fun playSound(assetPath: String) {
        stopSound()
        exoPlayer = ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri("asset:///$assetPath")
            setMediaItem(mediaItem)
            repeatMode = Player.REPEAT_MODE_ONE
            prepare()
            volume = 0f
            play()
        }
        fadeIn()
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
