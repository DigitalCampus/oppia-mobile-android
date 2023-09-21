package org.digitalcampus.oppia.analytics

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
import org.digitalcampus.oppia.activity.PrefsActivity

abstract class BaseAnalytics (protected val appContext: Application) {
    abstract fun startTrackingSession()
    abstract fun stopTrackingSession()
    abstract fun trackingConfigChanged()
    abstract fun logHandledException(e: Exception)
    abstract fun setUserIdentifier(username: String)
    abstract fun trackViewOnStart(activity: Activity)
    abstract fun trackViewOnStop(activity: Activity)

    fun isBugReportEnabled(): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)
        return prefs.getBoolean(PrefsActivity.PREF_BUG_REPORT_ENABLED, false)
    }

    fun isAnalyticsEnabled(): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)
        return prefs.getBoolean(PrefsActivity.PREF_ANALYTICS_ENABLED, false)
    }
}