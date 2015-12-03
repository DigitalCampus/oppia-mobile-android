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

import com.splunk.mint.Mint;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.model.User;

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

        loadUserPrefs(username, prefs);
        Mint.setUserIdentifier(username);
        editor.apply();
    }

    public static void logoutCurrentUser(Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        String username = getUsernameFromPrefs(prefs);

        //If there was a logged in user, we save her Preferences in the DB
        if (!username.equals("")){
            saveUserPrefs(username, prefs);
        }

        //Logout the user (unregister from Preferences)
        editor.putString(PrefsActivity.PREF_USER_NAME, "");
        Mint.setUserIdentifier("anon");
        editor.apply();
    }

    //Warning: this method doesn't call prefs.apply()
    private static void saveUserPrefs(String user, SharedPreferences prefs){

    }

    //Warning: this method doesn't call prefs.apply()
    private static void loadUserPrefs(String user, SharedPreferences prefs){

    }


}
