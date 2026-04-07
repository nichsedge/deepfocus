package com.sans.deepfocus.domain

import com.sans.deepfocus.data.SessionDao
import com.sans.deepfocus.data.SessionEntity
import com.sans.deepfocus.analytics.AnalyticsProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SessionMode { POMODORO, STOPWATCH }
enum class SessionState { IDLE, RUNNING, PAUSED }

class TimerManager(private val coroutineScope: CoroutineScope, private val sessionDao: SessionDao) {
    private var actualStartTime: Long = 0
    private var startTime: Long = 0
    private var pausedTime: Long = 0
    private var initialDuration: Long = 0
    private var job: Job? = null

    private val _remainingTime = MutableStateFlow(0L)
    val remainingTime = _remainingTime.asStateFlow()

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime = _elapsedTime.asStateFlow()

    private val _sessionState = MutableStateFlow(SessionState.IDLE)
    val sessionState = _sessionState.asStateFlow()

    private val _sessionMode = MutableStateFlow(SessionMode.POMODORO)
    val sessionMode = _sessionMode.asStateFlow()

    init {
        _remainingTime.value = 25 * 60 * 1000L
    }

    fun setMode(mode: SessionMode) {
        if (_sessionState.value == SessionState.IDLE) {
            _sessionMode.value = mode
            if (mode == SessionMode.POMODORO) {
                _remainingTime.value = 25 * 60 * 1000L
            } else {
                _elapsedTime.value = 0L
            }
        }
    }

    fun start(mode: SessionMode? = null, durationMs: Long = 25 * 60 * 1000L) {
        mode?.let { _sessionMode.value = it }
        initialDuration = if (_sessionMode.value == SessionMode.POMODORO) durationMs else 0
        actualStartTime = System.currentTimeMillis()
        startTime = actualStartTime
        pausedTime = 0
        _sessionState.value = SessionState.RUNNING
        startTicking()
    }

    fun pause() {
        if (_sessionState.value == SessionState.RUNNING) {
            pausedTime += System.currentTimeMillis() - startTime
            job?.cancel()
            _sessionState.value = SessionState.PAUSED
        }
    }

    fun resume() {
        if (_sessionState.value == SessionState.PAUSED) {
            startTime = System.currentTimeMillis()
            _sessionState.value = SessionState.RUNNING
            startTicking()
        }
    }

    fun stop() {
        job?.cancel()
        val duration = _elapsedTime.value
        if (duration > 60 * 1000) { // Only save sessions longer than 1 minute
            saveSession(duration)
        }
        _sessionState.value = SessionState.IDLE
        _remainingTime.value = 0
        _elapsedTime.value = 0
    }

    private fun saveSession(duration: Long) {
        coroutineScope.launch(Dispatchers.IO) {
            sessionDao.insertSession(
                SessionEntity(
                    startTime = actualStartTime,
                    endTime = System.currentTimeMillis(),
                    duration = duration,
                    mode = _sessionMode.value.name
                )
            )
        }
    }

    private fun startTicking() {
        job?.cancel()
        job = coroutineScope.launch {
            while (isActive) {
                val currentElapsed = pausedTime + (System.currentTimeMillis() - startTime)
                _elapsedTime.value = currentElapsed

                if (_sessionMode.value == SessionMode.POMODORO) {
                    val remaining = initialDuration - currentElapsed
                    if (remaining <= 0) {
                        _remainingTime.value = 0
                        _sessionState.value = SessionState.IDLE
                        AnalyticsProvider.get().trackEvent("session_completed", mapOf(
                            "mode" to _sessionMode.value.name,
                            "duration" to initialDuration
                        ))
                        cancel()
                    } else {
                        _remainingTime.value = remaining
                    }
                }
                delay(100) 
            }
        }
    }
}
