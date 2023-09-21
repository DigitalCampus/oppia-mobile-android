package org.digitalcampus.oppia.analytics

import android.app.Activity
import android.app.Application
import android.content.Context
import android.util.Log

class DefaultNoOpAnalytics(appContext: Application) : BaseAnalytics(appContext) {

    val TAG = DefaultNoOpAnalytics::class.simpleName

    override fun startTrackingSession() {
        Log.d(TAG, "Starting tracking session")
    }

    override fun stopTrackingSession() {
        Log.d(TAG, "Stop tracking session")
    }

    override fun setUserIdentifier(username: String) {
        Log.d(TAG, "Set tracking session user: $username")
    }

    override fun trackingConfigChanged() {}
    override fun trackViewOnStart(activity: Activity) {}
    override fun trackViewOnStop(activity: Activity) {}
    override fun logHandledException(e: Exception) {}
}