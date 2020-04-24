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

package org.digitalcampus.oppia.application;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.splunk.mint.Mint;
import com.splunk.mint.MintLogLevel;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.listener.PreloadAccountsListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.model.db_model.UserPreference;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.PreloadAccountsTask;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.preference.PreferenceManager;

import static org.digitalcampus.oppia.activity.PrefsActivity.PREF_PHONE_NO;

public class SessionManager {

    public static final String TAG = SessionManager.class.getSimpleName();

    public static final String ACCOUNTS_CSV_FILENAME = "oppia_accounts.csv";
    public static final String APIKEY_VALID = "prefApiKeyInvalid";

    private static final List<String> USER_STRING_PREFS = Arrays.asList(
            PrefsActivity.PREF_PHONE_NO,
            PrefsActivity.PREF_LANGUAGE,
            PrefsActivity.PREF_NO_SCHEDULE_REMINDERS,
            PrefsActivity.PREF_TEXT_SIZE,
            PrefsActivity.PREF_GAMIFICATION_POINTS_ANIMATION,
            PrefsActivity.PREF_DURATION_GAMIFICATION_POINTS_VIEW);

    private static final List<String> USER_BOOLEAN_PREFS = Arrays.asList(
            PrefsActivity.PREF_SHOW_SCHEDULE_REMINDERS,
            PrefsActivity.PREF_SHOW_COURSE_DESC,
            PrefsActivity.PREF_SHOW_PROGRESS_BAR,
            PrefsActivity.PREF_SHOW_SECTION_NOS,
            PrefsActivity.PREF_HIGHLIGHT_COMPLETED,
            PrefsActivity.PREF_DISABLE_NOTIFICATIONS,
            PrefsActivity.PREF_SHOW_GAMIFICATION_EVENTS);

    private static final String STR_FALSE = "false";
    private static final String STR_TRUE = "true";

    private SessionManager() {
        throw new IllegalStateException("Utility class");
    }
    public static boolean isLoggedIn(Context ctx) {
        String username = getUsername(ctx);
        return !username.trim().equals("");
    }

    private static String getUsernameFromPrefs(SharedPreferences prefs) {
        return prefs.getString(PrefsActivity.PREF_USER_NAME, "");
    }

    public static String getUserDisplayName(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String username = getUsernameFromPrefs(prefs);

        DbHelper db = DbHelper.getInstance(ctx);
        try {
            User u = db.getUser(username);
            return u.getDisplayName();

        } catch (UserNotFoundException e) {
            Mint.logEvent(e.getMessage(), MintLogLevel.Info);
            Log.d(TAG, "User not found: ", e);
            return null;
        }

    }

    public static String getUsername(Context ctx) {
        if (ctx == null) {
            return "";
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return getUsernameFromPrefs(prefs);
    }

    public static long getUserId(Context ctx) {
        return DbHelper.getInstance(ctx).getUserId(getUsername(ctx));
    }

    public static void loginUser(Context ctx, User user) {

        //To ensure that userPrefs get saved, we force the logout of the current user
        logoutCurrentUser(ctx);

        String username = user.getUsername();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PrefsActivity.PREF_USER_NAME, username);
        editor.putString(PREF_PHONE_NO, user.getPhoneNo());
        editor.putBoolean(PrefsActivity.PREF_SCORING_ENABLED, user.isScoringEnabled());
        editor.putBoolean(PrefsActivity.PREF_BADGING_ENABLED, user.isBadgingEnabled());

        loadUserPrefs(ctx, username, editor);
        setUserApiKeyValid(user, true);
        Mint.setUserIdentifier(username);
        editor.apply();
    }

    public static void logoutCurrentUser(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        String username = getUsernameFromPrefs(prefs);

        //If there was a logged in user, we save her Preferences in the DB
        if (!TextUtils.isEmpty(username)) {
            saveUserPrefs(username, prefs);
        }

        //Logout the user (unregister from Preferences)
        editor.putString(PrefsActivity.PREF_USER_NAME, "");
        Mint.setUserIdentifier("anon");
        editor.apply();
    }

    private static void saveUserPrefs(String username, SharedPreferences prefs) {

        List<UserPreference> userPreferences = new ArrayList<>();

        for (String prefID : USER_STRING_PREFS) {
            String prefValue = prefs.getString(prefID, "");
            if (!TextUtils.isEmpty(prefValue)) {
                userPreferences.add(new UserPreference(username, prefID, prefValue));
            }
        }

        for (String prefID : USER_BOOLEAN_PREFS) {
            if (prefs.contains(prefID)) {
                boolean prefValue = prefs.getBoolean(prefID, false);
                userPreferences.add(new UserPreference(username, prefID, prefValue ? STR_TRUE : STR_FALSE));
            }
        }

        App.getDb().userPreferenceDao().insertAll(userPreferences);
    }

    //Warning: this method doesn't call prefs.apply()
    private static void loadUserPrefs(Context ctx, String username, SharedPreferences.Editor prefsEditor) {

        List<UserPreference> userPrefs = App.getDb().userPreferenceDao().getAllForUser(username);

        ArrayList<String> prefsToSave = new ArrayList<>();
        prefsToSave.addAll(USER_STRING_PREFS);
        prefsToSave.addAll(USER_BOOLEAN_PREFS);

        for (UserPreference userPref : userPrefs) {
            String prefKey = userPref.getPreference();
            String prefValue = userPref.getValue();
            if (USER_STRING_PREFS.contains(prefKey)) {
                prefsEditor.putString(prefKey, prefValue);
            } else if (USER_BOOLEAN_PREFS.contains(prefKey)) {
                prefsEditor.putBoolean(prefKey, STR_TRUE.equals(prefValue));
            }
            prefsToSave.remove(prefKey);
        }

        //If there were prefKeys not previously saved, we clear them from the SharedPreferences
        if (!prefsToSave.isEmpty()) {
            for (String pref : prefsToSave) prefsEditor.remove(pref);
            //Then we set the default values again (only empty values, will not overwrite the others)
            App.loadDefaultPreferenceValues(ctx, true);
        }
    }

    public static void setUserApiKeyValid(User user, boolean valid) {
        ArrayList<Pair<String, String>> userPrefs = new ArrayList<>();
        Pair<String, String> userPref = new Pair<>(APIKEY_VALID, valid ? STR_TRUE : STR_FALSE);
        userPrefs.add(userPref);

        UserPreference userPreference = new UserPreference(user.getUsername(), APIKEY_VALID, valid ? STR_TRUE : STR_FALSE);
        App.getDb().userPreferenceDao().insert(userPreference);
    }

    public static boolean isUserApiKeyValid(Context ctx) {
        if (isLoggedIn(ctx)) {
            String user = getUsername(ctx);
            return isUserApiKeyValid(user);
        }
        return true;
    }

    public static boolean isUserApiKeyValid(String username) {

        UserPreference userPref = App.getDb().userPreferenceDao().getUserPreference(username, APIKEY_VALID);
        return (userPref == null || STR_TRUE    .equals(userPref.getValue()));
    }

    public static void preloadUserAccounts(Context ctx, PreloadAccountsListener listener) {
        File csvAccounts = new File(Storage.getStorageLocationRoot(ctx) + File.separator + ACCOUNTS_CSV_FILENAME);
        if (csvAccounts.exists()) {
            Payload payload = new Payload();
            PreloadAccountsTask task = new PreloadAccountsTask(ctx);
            task.setPreloadAccountsListener(listener);
            task.execute(payload);
        } else {
            listener.onPreloadAccountsComplete(null);
        }
    }

}
