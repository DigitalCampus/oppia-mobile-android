package org.digitalcampus.oppia.analytics;

import android.content.Context;
import android.content.SharedPreferences;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.SessionManager;

import androidx.preference.PreferenceManager;

public class Analytics {

    private static volatile BaseAnalytics analytics;

    public static void initializeAnalytics(Context ctx){
        if (analytics == null) {
            analytics = new MintAnalytics(ctx);
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
        if (analytics.isBugReportEnabled() || analytics.isAnalyticsEnabled()){
            analytics.startTrackingSession();
            String username = SessionManager.getUsername(ctx);
            Analytics.setUserId(username);
        }
    }

    public static void setUserId(String username){
        analytics.setUserIdentifier(username.equals("") ? "anon" : username);
    }

    public static void enableTracking(Context ctx){
        getPrefs(ctx).edit().putBoolean(PrefsActivity.PREF_ANALYTICS_ENABLED, true).apply();
        analytics.startTrackingSession();
    }

    public static void enableBugReport(Context ctx){
        getPrefs(ctx).edit().putBoolean(PrefsActivity.PREF_BUG_REPORT_ENABLED, true).apply();
        analytics.startTrackingSession();
    }

    public static void disableTracking(Context ctx){
        getPrefs(ctx).edit().putBoolean(PrefsActivity.PREF_ANALYTICS_ENABLED, false).apply();
        analytics.stopTrackingSession();
    }

    public static void logException(Exception e){
        if (analytics.isBugReportEnabled()){
            analytics.logHandledException(e);
        }
    }

}
