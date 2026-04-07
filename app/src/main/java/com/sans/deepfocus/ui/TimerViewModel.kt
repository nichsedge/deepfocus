package com.sans.deepfocus.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sans.deepfocus.analytics.AnalyticsProvider
import com.sans.deepfocus.domain.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class TimerViewModel(private val timerManager: TimerManager) : ViewModel() {
    private val analytics = AnalyticsProvider.get()
    val remainingTime: StateFlow<Long> = timerManager.remainingTime
    val elapsedTime: StateFlow<Long> = timerManager.elapsedTime
    val sessionState: StateFlow<SessionState> = timerManager.sessionState
    val sessionMode: StateFlow<SessionMode> = timerManager.sessionMode

    fun setMode(mode: SessionMode) {
        analytics.trackEvent("change_mode", mapOf("mode" to mode.name))
        timerManager.setMode(mode)
    }

    fun start() {
        analytics.trackEvent("timer_start", mapOf("mode" to sessionMode.value.name))
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
    }

    fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }
}
