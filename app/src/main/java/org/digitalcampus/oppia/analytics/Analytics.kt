package org.digitalcampus.oppia.analytics

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.digitalcampus.mobile.learning.BuildConfig
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.application.SessionManager.getUsername

object Analytics {
    private const val ANALYTICS_LIBRARY_COUNTLY = "COUNTLY"

    private var analyticsEngine: BaseAnalytics? = null

    fun initializeAnalytics(appContext: Application) {
        if (analyticsEngine == null) {
            if (BuildConfig.ANALYTICS_LIBRARY == ANALYTICS_LIBRARY_COUNTLY) {
                analyticsEngine = CountlyAnalytics(appContext)
            } else {
                analyticsEngine = DefaultNoOpAnalytics(appContext)
            }
        }
    }

    private fun getPrefs(ctx: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(ctx)
    }

    fun shouldShowOptOutRationale(ctx: Context): Boolean {
        return !getPrefs(ctx).getBoolean(PrefsActivity.PREF_ANALYTICS_INITIAL_PROMPT, false)
    }

    @JvmStatic
    fun optOutRationaleShown(ctx: Context) {
        getPrefs(ctx).edit().putBoolean(PrefsActivity.PREF_ANALYTICS_INITIAL_PROMPT, true).apply()
    }

    @JvmStatic
    fun startTrackingIfEnabled(ctx: Context) {
        analyticsEngine?.run {
            if (isBugReportEnabled(ctx) || isTrackingEnabled(ctx)) {
                startTrackingSession()
                val username = getUsername(ctx)
                setUserId(username)
            }
        }
    }

    fun setUserId(username: String?) {
        analyticsEngine?.run {
            val username = if (username.isNullOrBlank()) "anon" else username
            setUserIdentifier(username)
        }
    }

    @JvmStatic
    fun enableTracking(ctx: Context) {
        analyticsEngine?.run {
            getPrefs(ctx).edit().putBoolean(PrefsActivity.PREF_ANALYTICS_ENABLED, true).apply()
            if (!isBugReportEnabled(ctx)) {
                startTrackingSession()
            } else {
                trackingConfigChanged()
            }
        }
    }

    @JvmStatic
    fun enableBugReport(ctx: Context) {
        analyticsEngine?.run {
            getPrefs(ctx).edit().putBoolean(PrefsActivity.PREF_BUG_REPORT_ENABLED, true).apply()
            if (!isTrackingEnabled(ctx)) {
                startTrackingSession()
            } else {
                trackingConfigChanged()
            }
        }
    }

    @JvmStatic
    fun disableTracking(ctx: Context) {
        analyticsEngine?.run {
            getPrefs(ctx).edit().putBoolean(PrefsActivity.PREF_ANALYTICS_ENABLED, false).apply()
            if (!isBugReportEnabled(ctx)) {
                stopTrackingSession()
            } else {
                trackingConfigChanged()
            }
        }
    }

    @JvmStatic
    fun disableBugReport(ctx: Context) {
        analyticsEngine?.run {
            getPrefs(ctx).edit().putBoolean(PrefsActivity.PREF_BUG_REPORT_ENABLED, false).apply()
            if (!isTrackingEnabled(ctx)) {
                stopTrackingSession()
            } else {
                trackingConfigChanged()
            }
        }
    }

    @JvmStatic
    fun logException(e: Exception) {
        analyticsEngine?.run {
            if(isBugReportEnabled()) {
                logHandledException(e)
            }
        }
    }

    @JvmStatic
    fun isTrackingEnabled(ctx: Context): Boolean {
        return getPrefs(ctx).getBoolean(PrefsActivity.PREF_ANALYTICS_ENABLED, false)
    }

    @JvmStatic
    fun isBugReportEnabled(ctx: Context): Boolean {
        return getPrefs(ctx).getBoolean(PrefsActivity.PREF_BUG_REPORT_ENABLED, false)
    }

    @JvmStatic
    fun trackViewOnStart(a: Activity) {
        analyticsEngine?.run {
            if(isAnalyticsEnabled()) {
                trackViewOnStart(a)
            }
        }
    }

    @JvmStatic
    fun trackViewOnStop(a: Activity) {
        analyticsEngine?.run {
            if(isAnalyticsEnabled()) {
                trackViewOnStop(a)
            }
        }
    }
}