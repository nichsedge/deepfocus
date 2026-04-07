package com.sans.deepfocus.analytics

import android.util.Log

/**
 * Interface for tracking analytics events across the app.
 */
interface AnalyticsTracker {
    fun trackScreen(screenName: String)
    fun trackEvent(eventName: String, params: Map<String, Any> = emptyMap())
}

/**
 * Implementation that logs events to Logcat. Useful for development and tracking without a backend.
 */
class LoggerAnalytics : AnalyticsTracker {
    private val tag = "DeepFocusAnalytics"

    override fun trackScreen(screenName: String) {
        Log.d(tag, "Screen Viewed: $screenName")
    }

    override fun trackEvent(eventName: String, params: Map<String, Any>) {
        val paramsString = params.entries.joinToString(", ") { "${it.key}=${it.value}" }
        Log.d(tag, "Event: $eventName | Params: [$paramsString]")
    }
}

/**
 * Singleton-ish provider for analytics
 */
object AnalyticsProvider {
    private var instance: AnalyticsTracker = LoggerAnalytics()

    fun get(): AnalyticsTracker = instance

    fun setInstance(tracker: AnalyticsTracker) {
        instance = tracker
    }
}
