package com.sans.deepfocus.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.sans.deepfocus.R
import com.sans.deepfocus.domain.FocusAudioManager
import com.sans.deepfocus.domain.TimerManager
import com.sans.deepfocus.domain.SessionMode
import com.sans.deepfocus.data.AppDatabase
import kotlinx.coroutines.*

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
        timerManager = TimerManager(scope, database.sessionDao())
        audioManager = FocusAudioManager(this)
        createNotificationChannel()
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

    fun startSession(mode: String, durationMs: Long) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Focusing in $mode mode")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .build()
        
        startForeground(NOTIFICATION_ID, notification)
        timerManager.start(if (mode == "POMODORO") SessionMode.POMODORO else SessionMode.STOPWATCH, durationMs)
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
