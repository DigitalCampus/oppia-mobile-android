package org.digitalcampus.oppia.analytics;

import android.content.Context;
import android.content.SharedPreferences;

import org.digitalcampus.oppia.activity.PrefsActivity;

import androidx.preference.PreferenceManager;

public abstract class BaseAnalytics {

    protected final Context ctx;

    protected abstract void startTrackingSession();
    protected abstract void stopTrackingSession();
    protected abstract void logHandledException(Exception e);
    public abstract void setUserIdentifier(String username);

    public BaseAnalytics(Context ctx){
        this.ctx = ctx;
    }

    public boolean isEnabled(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean(PrefsActivity.PREF_ANALYTICS_ENABLED, false);
    }

}
