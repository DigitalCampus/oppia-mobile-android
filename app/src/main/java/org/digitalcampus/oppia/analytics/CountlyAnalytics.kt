package org.digitalcampus.oppia.analytics

import android.app.Activity
import android.app.Application
import android.content.Context
import ly.count.android.sdk.Countly
import ly.count.android.sdk.CountlyConfig
import ly.count.android.sdk.DeviceId
import org.digitalcampus.mobile.learning.BuildConfig

class CountlyAnalytics(appContext: Application) : BaseAnalytics(appContext) {
    override fun startTrackingSession() {
        val config = CountlyConfig(appContext, BuildConfig.COUNTLY_APP_KEY.toString(), BuildConfig.COUNTLY_SERVER_URL)
        config.setLoggingEnabled(Analytics.isTrackingEnabled(appContext))
        config.setViewTracking(Analytics.isTrackingEnabled(appContext))
        if (Analytics.isBugReportEnabled(appContext)) {
            config.enableCrashReporting()
        }
        config.setIdMode(DeviceId.Type.OPEN_UDID)
        Countly.sharedInstance().init(config)
    }

    override fun stopTrackingSession() {
        Countly.sharedInstance().halt()
    }

    override fun trackingConfigChanged() {
        //As we cannot update configuration on the run, we need to restart the service
        stopTrackingSession()
        startTrackingSession()
    }

    override fun setUserIdentifier(username: String) {
        if (isBugReportEnabled() || isAnalyticsEnabled()) {
            val userFields = mapOf("username" to username)
            Countly.userData.setUserData(userFields)
            Countly.userData.save()
        }
    }

    override fun trackViewOnStart(activity: Activity) {
        Countly.sharedInstance().onStart(activity)
    }

    override fun trackViewOnStop(activity: Activity) {
        Countly.sharedInstance().onStop()
    }

    override fun logHandledException(e: Exception) {
        Countly.sharedInstance().crashes().recordHandledException(e)
    }
}