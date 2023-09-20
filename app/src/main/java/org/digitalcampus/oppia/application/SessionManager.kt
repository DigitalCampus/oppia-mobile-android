/*
 * This file is part of OppiaMobile - https://digital-campus.org/
 *
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */
package org.digitalcampus.oppia.application

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.analytics.Analytics
import org.digitalcampus.oppia.application.App.Companion.db
import org.digitalcampus.oppia.application.App.Companion.loadDefaultPreferenceValues
import org.digitalcampus.oppia.database.DbHelper
import org.digitalcampus.oppia.exception.UserNotFoundException
import org.digitalcampus.oppia.listener.PreloadAccountsListener
import org.digitalcampus.oppia.model.User
import org.digitalcampus.oppia.model.db_model.UserPreference
import org.digitalcampus.oppia.task.PreloadAccountsTask
import org.digitalcampus.oppia.utils.TextUtilsJava
import org.digitalcampus.oppia.utils.storage.Storage.getStorageLocationRoot
import java.io.File
import java.util.Arrays

object SessionManager {

    val TAG = SessionManager::class.simpleName
    const val ACCOUNTS_CSV_FILENAME = "oppia_accounts.csv"
    private const val APIKEY_VALID = "prefApiKeyInvalid"

    private val USER_STRING_PREFS = listOf(
        PrefsActivity.PREF_PHONE_NO,
        PrefsActivity.PREF_CONTENT_LANGUAGE,
        PrefsActivity.PREF_NO_SCHEDULE_REMINDERS,
        PrefsActivity.PREF_TEXT_SIZE,
        PrefsActivity.PREF_GAMIFICATION_POINTS_ANIMATION,
        PrefsActivity.PREF_DURATION_GAMIFICATION_POINTS_VIEW
    ) // todo persist reminder prefs

    private val USER_BOOLEAN_PREFS = listOf(
        PrefsActivity.PREF_SHOW_SCHEDULE_REMINDERS,
        PrefsActivity.PREF_SHOW_COURSE_DESC,
        PrefsActivity.PREF_SHOW_PROGRESS_BAR,
        PrefsActivity.PREF_SHOW_SECTION_NOS,
        PrefsActivity.PREF_HIGHLIGHT_COMPLETED,
        PrefsActivity.PREF_DISABLE_NOTIFICATIONS,
        PrefsActivity.PREF_COURSES_REMINDER_ENABLED,
        PrefsActivity.PREF_SHOW_GAMIFICATION_EVENTS,
        PrefsActivity.PREF_BUG_REPORT_ENABLED,
        PrefsActivity.PREF_ANALYTICS_ENABLED,
        PrefsActivity.PREF_ANALYTICS_INITIAL_PROMPT
    )

    private const val STR_FALSE = "false"
    private const val STR_TRUE = "true"

    @JvmStatic
    fun isLoggedIn(ctx: Context): Boolean {
        val username = getUsername(ctx)
        return !TextUtilsJava.isEmpty(username)
    }

    // For testing, to be able to pass mocked prefs
    @JvmStatic
    fun isLoggedIn(prefs: SharedPreferences): Boolean {
        val username = getUsernameFromPrefs(prefs)
        return !TextUtilsJava.isEmpty(username)
    }

    private fun getUsernameFromPrefs(prefs: SharedPreferences): String {
        return prefs.getString(PrefsActivity.PREF_USER_NAME, "")  ?: ""
    }

    @JvmStatic
    fun getUserDisplayName(ctx: Context): String? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
        val username = getUsernameFromPrefs(prefs)
        val db = DbHelper.getInstance(ctx)
        return try {
            val u = db.getUser(username)
            u.getDisplayName()
        } catch (e: UserNotFoundException) {
            Log.d(TAG, "User not found: ", e)
            null
        }
    }

    @JvmStatic
    fun getUsername(ctx: Context?): String? {
        if (ctx == null) {
            return ""
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
        return getUsernameFromPrefs(prefs)
    }

    @JvmStatic
    fun getUserId(ctx: Context): Long {
        return DbHelper.getInstance(ctx).getUserId(getUsername(ctx))
    }

    @JvmStatic
    fun loginUser(ctx: Context, user: User) {
        //To ensure that userPrefs get saved, we force the logout of the current user
        logoutCurrentUser(ctx)
        val username = user.username
        val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
        val editor = prefs.edit()
        editor.putString(PrefsActivity.PREF_USER_NAME, username)
        editor.putString(PrefsActivity.PREF_PHONE_NO, user.phoneNo)
        editor.putBoolean(PrefsActivity.PREF_SCORING_ENABLED, user.isScoringEnabled)
        editor.putBoolean(PrefsActivity.PREF_BADGING_ENABLED, user.isBadgingEnabled)
        loadUserPrefs(ctx, username, editor)
        setUserApiKeyValid(user, true)
        Analytics.setUserId(username)
        editor.apply()

//        CoursesCompletionReminderWorkerManager.configureCoursesCompletionReminderWorker(ctx); #reminders-multi-user
    }

    @JvmStatic
    fun logoutCurrentUser(ctx: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
        val editor = prefs.edit()
        val username = getUsernameFromPrefs(prefs)

        //If there was a logged in user, we save her Preferences in the DB
        if (!TextUtilsJava.isEmpty(username)) {
            saveUserPrefs(username, prefs)
        }

//        OppiaNotificationUtils.cancelAllUserNotifications(ctx); #reminders-multi-user
//        CoursesCompletionReminderWorkerManager.cancelAllReminderWorkers(ctx);  #reminders-multi-user

        //Logout the user (unregister from Preferences)
        editor.putString(PrefsActivity.PREF_USER_NAME, "")
        Analytics.setUserId("")
        editor.apply()
    }

    private fun saveUserPrefs(username: String, prefs: SharedPreferences) {
        val userPreferences: MutableList<UserPreference> = ArrayList()
        for (prefID in USER_STRING_PREFS) {
            val prefValue = prefs.getString(prefID, "")
            if (!TextUtilsJava.isEmpty(prefValue)) {
                userPreferences.add(UserPreference(username, prefID, prefValue!!))
            }
        }
        for (prefID in USER_BOOLEAN_PREFS) {
            if (prefs.contains(prefID)) {
                val prefValue = prefs.getBoolean(prefID, false)
                userPreferences.add(
                    UserPreference(username, prefID, if (prefValue) STR_TRUE else STR_FALSE )
                )
            }
        }
        db!!.userPreferenceDao().insertAll(userPreferences)
    }

    //Warning: this method doesn't call prefs.apply()
    private fun loadUserPrefs(ctx: Context?, username: String?, prefsEditor: SharedPreferences.Editor) {
        val userPrefs = db!!.userPreferenceDao().getAllForUser(username)
        val prefsToSave = ArrayList<String>()
        prefsToSave.addAll(USER_STRING_PREFS)
        prefsToSave.addAll(USER_BOOLEAN_PREFS)
        for (userPref in userPrefs) {
            val prefKey = userPref.preference
            val prefValue = userPref.value
            if (USER_STRING_PREFS.contains(prefKey)) {
                prefsEditor.putString(prefKey, prefValue)
            } else if (USER_BOOLEAN_PREFS.contains(prefKey)) {
                prefsEditor.putBoolean(prefKey, STR_TRUE == prefValue)
            }
            prefsToSave.remove(prefKey)
        }

        //If there were prefKeys not previously saved, we clear them from the SharedPreferences
        if (prefsToSave.isNotEmpty()) {
            for (pref in prefsToSave) prefsEditor.remove(pref)
            //Then we set the default values again (only empty values, will not overwrite the others)
            loadDefaultPreferenceValues(ctx, true)
        }
    }

    @JvmStatic
    fun setUserApiKeyValid(user: User, valid: Boolean) {
        val userPreference = UserPreference(user.username!!, APIKEY_VALID, if (valid) STR_TRUE else STR_FALSE)
        db!!.userPreferenceDao().insert(userPreference)
    }

    @JvmStatic
    fun invalidateAllApiKeys(ctx: Context) {
        for (u in DbHelper.getInstance(ctx).allUsers) {
            setUserApiKeyValid(u, false)
        }
    }

    @JvmStatic
    fun invalidateCurrentUserApiKey(ctx: Context) {
        try {
            val u = DbHelper.getInstance(ctx).getUser(getUsername(ctx))
            setUserApiKeyValid(u, false)
        } catch (e: UserNotFoundException) {
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun isUserApiKeyValid(prefs: SharedPreferences): Boolean {
        if (isLoggedIn(prefs)) {
            val user = getUsernameFromPrefs(prefs)
            return isUserApiKeyValid(user)
        }
        return true
    }

    @JvmStatic
    fun isUserApiKeyValid(username: String?): Boolean {
        val userPref = db!!.userPreferenceDao().getUserPreference(username, APIKEY_VALID)
        return userPref == null || STR_TRUE == userPref.value
    }

    @JvmStatic
    fun areAllApiKeysInvalid(ctx: Context?): Boolean {
        val users = DbHelper.getInstance(ctx).allUsers
        for (u in users) {
            if (isUserApiKeyValid(u.username)) {
                return false
            }
        }
        return true
    }

    @JvmStatic
    fun preloadUserAccounts(ctx: Context, listener: PreloadAccountsListener) {
        val csvAccounts = File(getStorageLocationRoot(ctx) + File.separator + ACCOUNTS_CSV_FILENAME)
        if (csvAccounts.exists()) {
            val task = PreloadAccountsTask(ctx)
            task.setPreloadAccountsListener(listener)
            task.execute()
        } else {
            listener.onPreloadAccountsComplete(null)
        }
    }
}