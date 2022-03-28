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

import android.app.Activity;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.fragments.PasswordDialogFragment;

import javax.inject.Inject;


public class AdminSecurityManager {

    public static final String TAG = AdminSecurityManager.class.getSimpleName();

    @Inject
    SharedPreferences prefs;

    Activity context;

    public interface AuthListener{
        void onPermissionGranted();
    }

    public AdminSecurityManager(Activity context) {
        this.context = context;
        initializeDaggerBase();
    }

    public static AdminSecurityManager with(Activity context) {
        return new AdminSecurityManager(context);
    }

    private void initializeDaggerBase() {
        App app = (App) context.getApplication();
        app.getComponent().inject(this);
    }

    public boolean isAdminProtectionEnabled(){
        return prefs.getBoolean(PrefsActivity.PREF_ADMIN_PROTECTION, false);
    }

    public void checkAdminPermission(int actionId, AdminSecurityManager.AuthListener authListener){
        if (isActionProtected(actionId)) {
            promptAdminPassword(authListener);
        }
        else{
            //If the admin password is not needed, we simply call the listener method
            authListener.onPermissionGranted();
        }
    }

    public void promptAdminPassword(AdminSecurityManager.AuthListener authListener){
        PasswordDialogFragment passDialog = new PasswordDialogFragment();
        passDialog.setListener(authListener);
        passDialog.show(context.getFragmentManager(), TAG);
    }

    public boolean isActionProtected(int actionId) {

        if (!isAdminProtectionEnabled()) return false;

        // Only for automated testing. Only way I could "mock" BuildConfig fields
        if(testForzeActionProtected()) return getTestActionProtectedValue();

        switch (actionId){
            case R.id.course_context_delete: return App.ADMIN_PROTECT_COURSE_DELETE;
            case R.id.course_context_reset: return App.ADMIN_PROTECT_COURSE_RESET;
            case R.id.course_context_update_activity: return App.ADMIN_PROTECT_COURSE_UPDATE;
            case R.id.menu_download: return App.ADMIN_PROTECT_COURSE_INSTALL;
            case R.id.menu_settings: return App.ADMIN_PROTECT_SETTINGS;
            case R.id.menu_sync: return App.ADMIN_PROTECT_ACTIVITY_SYNC;
            case R.id.action_export_activity: return App.ADMIN_PROTECT_ACTIVITY_EXPORT;

            default: return false;
        }
    }

    public boolean isPreferenceProtected(String preferenceKey) {

        if (TextUtils.equals(preferenceKey, PrefsActivity.PREF_ADMIN_PROTECTION)
            || TextUtils.equals(preferenceKey, PrefsActivity.PREF_ADMIN_PASSWORD)) {
            return true;
        }

        if (!isAdminProtectionEnabled()) return false;

        // Only for automated testing. Only way I could "mock" BuildConfig fields
        if(testForzeActionProtected()) {
            return getTestActionProtectedValue();
        }

        switch (preferenceKey){

            case PrefsActivity.PREF_SERVER: return App.ADMIN_PROTECT_SERVER;
            case PrefsActivity.PREF_ADVANCED_SCREEN: return App.ADMIN_PROTECT_ADVANCED_SETTINGS;
            case PrefsActivity.PREF_SECURITY_SCREEN: return App.ADMIN_PROTECT_SECURITY_SETTINGS;

            case PrefsActivity.PREF_DISABLE_NOTIFICATIONS: return App.ADMIN_PROTECT_NOTIFICATIONS;
            case PrefsActivity.PREF_COURSES_REMINDER_ENABLED: return App.ADMIN_PROTECT_ENABLE_REMINDER_NOTIFICATIONS;
            case PrefsActivity.PREF_COURSES_REMINDER_INTERVAL: return App.ADMIN_PROTECT_REMINDER_INTERVAL;
            case PrefsActivity.PREF_COURSES_REMINDER_DAYS: return App.ADMIN_PROTECT_REMINDER_DAYS;
            case PrefsActivity.PREF_COURSES_REMINDER_TIME: return App.ADMIN_PROTECT_REMINDER_TIME;

            default: return false;
        }
    }


    public boolean testForzeActionProtected() {
        String actionProtected = prefs.getString(PrefsActivity.PREF_TEST_ACTION_PROTECTED, null);
        return  actionProtected != null;
    }

    private boolean getTestActionProtectedValue() {
        boolean actionProtected = Boolean.parseBoolean(prefs.getString(PrefsActivity.PREF_TEST_ACTION_PROTECTED, null));
        return actionProtected;
    }

    public boolean checkAdminPassword(String pass) {

        if (!isAdminProtectionEnabled()) return false;
        String adminPass = prefs.getString(PrefsActivity.PREF_ADMIN_PASSWORD, "");

        return ((pass != null) && (adminPass.equals("") || adminPass.equals(pass)));
    }
}
