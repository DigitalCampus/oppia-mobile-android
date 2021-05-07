package org.digitalcampus.oppia.analytics;

import android.app.Activity;
import android.content.Context;

import org.digitalcampus.mobile.learning.BuildConfig;

import java.util.HashMap;
import java.util.Map;

import ly.count.android.sdk.Countly;
import ly.count.android.sdk.CountlyConfig;
import ly.count.android.sdk.DeviceId;

public class CountlyAnalytics extends BaseAnalytics {

    public CountlyAnalytics(Context ctx) {
        super(ctx);
    }

    @Override
    protected void startTrackingSession() {
        CountlyConfig config = new CountlyConfig(ctx, BuildConfig.COUNTLY_APP_KEY, BuildConfig.COUNTLY_SERVER_URL);
        config.setLoggingEnabled(Analytics.isTrackingEnabled(ctx));
        config.setViewTracking(Analytics.isTrackingEnabled(ctx));
        if (Analytics.isBugReportEnabled(ctx)) {
            config.enableCrashReporting();
        }
        config.setIdMode(DeviceId.Type.OPEN_UDID);
        Countly.sharedInstance().init(config);
    }

    @Override
    protected void stopTrackingSession() {
        Countly.sharedInstance().halt();
    }

    @Override
    protected void trackingConfigChanged() {
        //As we cannot update configuration on the run, we need to restart the service
        stopTrackingSession();
        startTrackingSession();
    }

    @Override
    public void setUserIdentifier(String username) {
        if (isBugReportEnabled() || isAnalyticsEnabled()) {
            Map<String, String> userFields = new HashMap<>();
            userFields.put("username", username);
            Countly.userData.setUserData(userFields);
            Countly.userData.save();
        }
    }

    @Override
    public void trackViewOnStart(Activity activity) {
        Countly.sharedInstance().onStart(activity);
    }

    @Override
    public void trackViewOnStop(Activity activity) {
        Countly.sharedInstance().onStop();
    }

    @Override
    protected void logHandledException(Exception e) {
        Countly.sharedInstance().crashes().recordHandledException(e);
    }
}
