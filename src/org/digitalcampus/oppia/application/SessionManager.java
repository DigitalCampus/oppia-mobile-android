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
import org.digitalcampus.oppia.model.User;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {

    public static boolean isLoggedIn(Context ctx) {
        String username = getUsername(ctx);
        return !username.trim().equals("");
    }

    private static String getUsernameFromPrefs(SharedPreferences prefs){
        return prefs.getString(PrefsActivity.PREF_USER_NAME, "");
    }

    public static String getUsername(Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return getUsernameFromPrefs(prefs);
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

        DbHelper db = new DbHelper(ctx);
        db.insertUserPreferences(username, userPrefs);
        DatabaseManager.getInstance().closeDatabase();

    }

    //Warning: this method doesn't call prefs.apply()
    private static void loadUserPrefs(Context ctx, String username, SharedPreferences.Editor prefsEditor){

        DbHelper db = new DbHelper(ctx);
        List<Pair<String, String>> userPrefs = db.getUserPreferences(username);
        DatabaseManager.getInstance().closeDatabase();

        for (Pair<String, String> pref : userPrefs){
            String prefKey = pref.first;
            String prefValue = pref.second;
            if (PrefsActivity.USER_STRING_PREFS.contains(prefKey)){
                prefsEditor.putString(prefKey, prefValue);
            }
            else if (PrefsActivity.USER_BOOLEAN_PREFS.contains(prefKey)){
                prefsEditor.putBoolean(prefKey, "true".equals(prefValue));
            }
        }

    }


}
