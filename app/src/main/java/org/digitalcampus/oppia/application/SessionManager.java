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
import android.preference.PreferenceManager;
import android.util.Pair;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.listener.PreloadAccountsListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.PreloadAccountsTask;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SessionManager {

    public static final String ACCOUNTS_CSV_FILENAME = "oppia_accounts.csv";
    public static final String APIKEY_VALID = "prefApiKeyInvalid";

    public static boolean isLoggedIn(Context ctx) {
        String username = getUsername(ctx);
        return !username.trim().equals("");
    }

    private static String getUsernameFromPrefs(SharedPreferences prefs){
        return prefs.getString(PrefsActivity.PREF_USER_NAME, "");
    }

    public static String getUserDisplayName(Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String username = getUsernameFromPrefs(prefs);

        DbHelper db = DbHelper.getInstance(ctx);
        try {
            User u = db.getUser(username);
            return u.getDisplayName();

        } catch (UserNotFoundException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static String getUsername(Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return getUsernameFromPrefs(prefs);
    }

    public static long getUserId(Context ctx){
        return DbHelper.getInstance(ctx).getUserId(getUsername(ctx));
    }

    public static void loginUser(Context ctx, User user){

        //To ensure that userPrefs get saved, we force the logout of the current user
        logoutCurrentUser(ctx);

        String username = user.getUsername();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PrefsActivity.PREF_USER_NAME, username);
        editor.putString(PrefsActivity.PREF_PHONE_NO, user.getPhoneNo());
        editor.putBoolean(PrefsActivity.PREF_SCORING_ENABLED, user.isScoringEnabled());
        editor.putBoolean(PrefsActivity.PREF_BADGING_ENABLED, user.isBadgingEnabled());

        loadUserPrefs(ctx, username, editor);
        setUserApiKeyValid(ctx, user, true);
        Mint.setUserIdentifier(username);
        editor.apply();
    }

    public static void logoutCurrentUser(Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        String username = getUsernameFromPrefs(prefs);

        //If there was a logged in user, we save her Preferences in the DB
        if (!username.equals("")){
            saveUserPrefs(ctx, username, prefs);
        }

        //Logout the user (unregister from Preferences)
        editor.putString(PrefsActivity.PREF_USER_NAME, "");
        Mint.setUserIdentifier("anon");
        editor.apply();
    }

    private static void saveUserPrefs(Context ctx, String username, SharedPreferences prefs){

        ArrayList<Pair<String, String>> userPrefs = new ArrayList<>();

        for (String prefID : PrefsActivity.USER_STRING_PREFS){
            String prefValue =  prefs.getString(prefID, "");
            if (!prefValue.equals("")){
                Pair<String, String> userPref = new Pair<>(prefID, prefValue);
                userPrefs.add(userPref);
            }
        }

        for (String prefID : PrefsActivity.USER_BOOLEAN_PREFS){
            if (prefs.contains(prefID)){
                boolean prefValue =  prefs.getBoolean(prefID, false);
                Pair<String, String> userPref = new Pair<>(prefID, prefValue?"true":"false");
                userPrefs.add(userPref);
            }
        }

        DbHelper db = DbHelper.getInstance(ctx);
        db.insertUserPreferences(username, userPrefs);
    }

    //Warning: this method doesn't call prefs.apply()
    private static void loadUserPrefs(Context ctx, String username, SharedPreferences.Editor prefsEditor){

        DbHelper db = DbHelper.getInstance(ctx);
        List<Pair<String, String>> userPrefs = db.getUserPreferences(username);

        ArrayList<String> prefsToSave = new ArrayList<>();
        prefsToSave.addAll(PrefsActivity.USER_BOOLEAN_PREFS);
        prefsToSave.addAll(PrefsActivity.USER_STRING_PREFS);

        for (Pair<String, String> pref : userPrefs){
            String prefKey = pref.first;
            String prefValue = pref.second;
            if (PrefsActivity.USER_STRING_PREFS.contains(prefKey)){
                prefsEditor.putString(prefKey, prefValue);
            }
            else if (PrefsActivity.USER_BOOLEAN_PREFS.contains(prefKey)){
                prefsEditor.putBoolean(prefKey, "true".equals(prefValue));
            }
            prefsToSave.remove(prefKey);
        }

        //If there were prefKeys not previously saved, we clear them from the SharedPreferences
        if (prefsToSave.size()>0){
            for (String pref : prefsToSave) prefsEditor.remove(pref);
            //Then we set the default values again (only empty values, will not overwrite the others)
            PreferenceManager.setDefaultValues(ctx, R.xml.prefs, true);
        }
    }

    public static void setUserApiKeyValid(Context ctx, User user, boolean valid){
        ArrayList<Pair<String, String>> userPrefs = new ArrayList<>();
        Pair<String, String> userPref = new Pair<>(APIKEY_VALID, valid?"true":"false");
        userPrefs.add(userPref);

        DbHelper.getInstance(ctx).insertUserPreferences(user.getUsername(), userPrefs);
    }

    public static boolean isUserApiKeyValid(Context ctx){
        if (isLoggedIn(ctx)){
            String user = getUsername(ctx);
            return isUserApiKeyValid(ctx, user);
        }
        return true;
    }

    public static boolean isUserApiKeyValid(Context ctx, String username){
        DbHelper db = DbHelper.getInstance(ctx);
        String prefValue = db.getUserPreference(username, APIKEY_VALID);
        return (prefValue == null || "true".equals(prefValue));
    }

    public static void preloadUserAccounts(Context ctx, PreloadAccountsListener listener){
        File csvAccounts = new File(Storage.getStorageLocationRoot(ctx) + File.separator + ACCOUNTS_CSV_FILENAME);
        if (csvAccounts.exists()){
            Payload payload = new Payload();
            PreloadAccountsTask task = new PreloadAccountsTask(ctx);
            task.setPreloadAccountsListener(listener);
            task.execute(payload);
        }
        else{
            listener.onPreloadAccountsComplete(null);
        }
    }

}
