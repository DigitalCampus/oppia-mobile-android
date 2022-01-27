package org.digitalcampus.oppia.analytics;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.SessionManager;

import androidx.preference.PreferenceManager;

public class Analytics {

    public static final String ANALYTICS_LIBRARY_MINT = "MINT";
    public static final String ANALYTICS_LIBRARY_COUNTLY = "COUNTLY";

    private static volatile BaseAnalytics analyticsEngine;

    public static void initializeAnalytics(Context ctx){
        if (analyticsEngine == null) {
            if (BuildConfig.ANALYTICS_LIBRARY.equals(ANALYTICS_LIBRARY_MINT)){
                analyticsEngine = new MintAnalytics(ctx);
            }
            else if (BuildConfig.ANALYTICS_LIBRARY.equals(ANALYTICS_LIBRARY_COUNTLY)){
                analyticsEngine = new CountlyAnalytics(ctx);
            }
            else{
                analyticsEngine = new DefaultNoOpAnalytics(ctx);
            }
        }
    }

    private static SharedPreferences getPrefs(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static boolean shouldShowOptOutRationale(Context ctx){
        return !getPrefs(ctx).getBoolean(PrefsActivity.PREF_ANALYTICS_INITIAL_PROMPT, false);
    }

    public static void optOutRationaleShown(Context ctx){
        getPrefs(ctx).edit().putBoolean(PrefsActivity.PREF_ANALYTICS_INITIAL_PROMPT, true).apply();
    }

    public static void startTrackingIfEnabled(Context ctx){
        if (isBugReportEnabled(ctx) || isTrackingEnabled(ctx)){
            analyticsEngine.startTrackingSession();
            String username = SessionManager.getUsername(ctx);
            Analytics.setUserId(username);
        }
    }

    public static void setUserId(String username){
        analyticsEngine.setUserIdentifier(username.equals("") ? "anon" : username);
    }

    public static void enableTracking(Context ctx){
        getPrefs(ctx).edit().putBoolean(PrefsActivity.PREF_ANALYTICS_ENABLED, true).apply();
        if (!isBugReportEnabled(ctx)){
            analyticsEngine.startTrackingSession();
        }
        else{
            analyticsEngine.trackingConfigChanged();
        }
    }

    public static void enableBugReport(Context ctx){
        getPrefs(ctx).edit().putBoolean(PrefsActivity.PREF_BUG_REPORT_ENABLED, true).apply();
        if (!isTrackingEnabled(ctx)){
            analyticsEngine.startTrackingSession();
        }
        else{
            analyticsEngine.trackingConfigChanged();
        }
    }

    public static void disableTracking(Context ctx){
        getPrefs(ctx).edit().putBoolean(PrefsActivity.PREF_ANALYTICS_ENABLED, false).apply();
        if (!isBugReportEnabled(ctx)){
            analyticsEngine.stopTrackingSession();
        }
        else{
            analyticsEngine.trackingConfigChanged();
        }
    }

    public static void disableBugReport(Context ctx){
        getPrefs(ctx).edit().putBoolean(PrefsActivity.PREF_BUG_REPORT_ENABLED, false).apply();
        if (!isTrackingEnabled(ctx)){
            analyticsEngine.stopTrackingSession();
        }
        else{
            analyticsEngine.trackingConfigChanged();
        }
    }

    public static void logException(Exception e){
        if (analyticsEngine.isBugReportEnabled()){
            analyticsEngine.logHandledException(e);
        }
    }

    public static boolean isTrackingEnabled(Context ctx){
        return getPrefs(ctx).getBoolean(PrefsActivity.PREF_ANALYTICS_ENABLED, false);
    }

    public static boolean isBugReportEnabled(Context ctx){
        return getPrefs(ctx).getBoolean(PrefsActivity.PREF_BUG_REPORT_ENABLED, false);
    }

    public static void trackViewOnStart(Activity a){
        if (analyticsEngine.isAnalyticsEnabled()){
            analyticsEngine.trackViewOnStart(a);
        }
    }

    public static void trackViewOnStop(Activity a){
        if (analyticsEngine.isAnalyticsEnabled()){
            analyticsEngine.trackViewOnStop(a);
        }
    }


}
