package org.digitalcampus.oppia.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.oppia.activity.PrefsActivity;

public class ReminderLogHelper {

    private final static int MAX_LOG_ENTRIES = 30;
    private static final String SEPARATOR_LOG_ENTRIES = "\n\n--------------\n\n";

    private final SharedPreferences prefs;

    public ReminderLogHelper(Context context) {
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void saveLogEntry(String type, String content) {

        if (BuildConfig.DEBUG) {
            String previousLog = getPreviousLogLimited();
            String logEntry = "--- " + type + " ---" + "\n" + DateUtils.DATETIME_FORMAT.print(System.currentTimeMillis()) + "\n" + content;
            prefs.edit().putString(PrefsActivity.PREF_REMINDERS_LOG, logEntry + SEPARATOR_LOG_ENTRIES + previousLog).commit();
        }

    }

    private String getPreviousLogLimited() {
        String log = prefs.getString(PrefsActivity.PREF_REMINDERS_LOG, "");
        String[] logItems = log.split(SEPARATOR_LOG_ENTRIES);
        String logLimited = "";
        if (logItems.length > MAX_LOG_ENTRIES) {
            for (int i = 0; i < MAX_LOG_ENTRIES; i++) {
                logLimited += logItems[i] + SEPARATOR_LOG_ENTRIES;
            }
        } else {
            logLimited = log;
        }
        return logLimited;
    }

    public String getLog() {
        return prefs.getString(PrefsActivity.PREF_REMINDERS_LOG, "");
    }
}
