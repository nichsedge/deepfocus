package com.sans.deepfocus.domain

import com.sans.deepfocus.analytics.AnalyticsProvider
import com.sans.deepfocus.data.SessionDao
import com.sans.deepfocus.data.SessionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class SessionMode { POMODORO, STOPWATCH }
enum class SessionState { IDLE, RUNNING, PAUSED }

class TimerManager private constructor(private val sessionDao: SessionDao) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        @Volatile
        private var INSTANCE: TimerManager? = null

        fun getInstance(sessionDao: SessionDao): TimerManager {
            return INSTANCE ?: synchronized(this) {
                val instance = TimerManager(sessionDao)
                INSTANCE = instance
                instance
            }
        }
    }

    private var actualStartTime: Long = 0
    private var startTime: Long = 0
    private var pausedTime: Long = 0
    private var initialDuration: Long = 0
    private var job: Job? = null
    private val _pomodoroDuration = MutableStateFlow(25 * 60 * 1000L)
    val pomodoroDuration = _pomodoroDuration.asStateFlow()

    private val _remainingTime = MutableStateFlow(0L)
    val remainingTime = _remainingTime.asStateFlow()

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime = _elapsedTime.asStateFlow()

    private val _sessionState = MutableStateFlow(SessionState.IDLE)
    val sessionState = _sessionState.asStateFlow()

    private val _sessionMode = MutableStateFlow(SessionMode.POMODORO)
    val sessionMode = _sessionMode.asStateFlow()

    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag = _selectedTag.asStateFlow()

    fun setSelectedTag(tag: String?) {
        _selectedTag.value = tag
    }

    init {
        _remainingTime.value = _pomodoroDuration.value
    }

    fun setPomodoroDuration(durationMs: Long) {
        _pomodoroDuration.value = durationMs
        if (_sessionMode.value == SessionMode.POMODORO && _sessionState.value == SessionState.IDLE) {
            _remainingTime.value = _pomodoroDuration.value
        }
    }

    fun setMode(mode: SessionMode) {
        if (_sessionState.value == SessionState.IDLE) {
            _sessionMode.value = mode
            if (mode == SessionMode.POMODORO) {
                _remainingTime.value = _pomodoroDuration.value
            } else {
                _elapsedTime.value = 0L
            }
        }
    }

    fun start(mode: SessionMode? = null, durationMs: Long? = null) {
        mode?.let { _sessionMode.value = it }
        val finalDuration = durationMs ?: _pomodoroDuration.value
        initialDuration = if (_sessionMode.value == SessionMode.POMODORO) finalDuration else 0
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
                    mode = _sessionMode.value.name,
                    tag = _selectedTag.value
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
                        val finalElapsed = currentElapsed.coerceAtLeast(initialDuration)
                        if (finalElapsed > 60 * 1000) { // Only save sessions longer than 1 minute
                            saveSession(finalElapsed)
                        }
                        _remainingTime.value = 0
                        _elapsedTime.value = 0
                        _sessionState.value = SessionState.IDLE
                        AnalyticsProvider.get().trackEvent(
                            "session_completed", mapOf(
                                "mode" to _sessionMode.value.name,
                                "duration" to finalElapsed
                            )
                        )
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
