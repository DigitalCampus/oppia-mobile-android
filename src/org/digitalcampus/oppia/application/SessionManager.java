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
