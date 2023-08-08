package org.digitalcampus.oppia.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import org.digitalcampus.mobile.learning.BuildConfig
import org.digitalcampus.oppia.activity.PrefsActivity

class ReminderLogHelper(context: Context) {

    companion object {
        private const val MAX_LOG_ENTRIES = 30
        private const val SEPARATOR_LOG_ENTRIES = "\n\n--------------\n\n"
    }

    private val prefs: SharedPreferences

    init {
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun saveLogEntry(type: String, content: String) {
        if (BuildConfig.DEBUG) {
            val previousLog = previousLogLimited
            val logEntry = "--- $type ---\n${DateUtils.DATETIME_FORMAT.print(System.currentTimeMillis())}\n$content"
            prefs.edit().putString(PrefsActivity.PREF_REMINDERS_LOG, logEntry + SEPARATOR_LOG_ENTRIES + previousLog).apply()
        }
    }

    private val previousLogLimited: String
        get() {
            val log : String = prefs.getString(PrefsActivity.PREF_REMINDERS_LOG, "") ?: ""
            val logItems = log.split(SEPARATOR_LOG_ENTRIES.toRegex()).toTypedArray()
            var logLimited = ""
            if (logItems.size > MAX_LOG_ENTRIES) {
                for (i in 0 until MAX_LOG_ENTRIES) {
                    logLimited += logItems[i] + SEPARATOR_LOG_ENTRIES
                }
            } else {
                logLimited = log
            }
            return logLimited
        }

    val log: String
        get() = prefs.getString(PrefsActivity.PREF_REMINDERS_LOG, "") ?: ""
}