package org.digitalcampus.oppia.analytics;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.digitalcampus.oppia.activity.PrefsActivity;

import androidx.preference.PreferenceManager;

public abstract class BaseAnalytics {

    protected final Context ctx;

    protected abstract void startTrackingSession();
    protected abstract void stopTrackingSession();
    protected abstract void trackingConfigChanged();
    protected abstract void logHandledException(Exception e);
    public abstract void setUserIdentifier(String username);
    public abstract void trackViewOnStart(Activity activity);
    public abstract void trackViewOnStop(Activity activity);

    public BaseAnalytics(Context ctx){
        this.ctx = ctx;
    }

    public boolean isBugReportEnabled(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean(PrefsActivity.PREF_BUG_REPORT_ENABLED, false);
    }

    public boolean isAnalyticsEnabled(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean(PrefsActivity.PREF_ANALYTICS_ENABLED, false);
    }

}
