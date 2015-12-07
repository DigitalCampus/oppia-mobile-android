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

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;


public class AdminSecurityManager {

    public interface AuthListener{
        void onPermissionGranted();
    }

    public static boolean isAdminProtectionEnabled(Context ctx){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        return prefs.getBoolean(PrefsActivity.PREF_ADMIN_PROTECTION, false);
    }

    public static boolean isActionProtected(Context ctx, int actionId) {

        if (!isAdminProtectionEnabled(ctx)) return false;

        switch (actionId){
            case R.id.course_context_delete: return MobileLearning.ADMIN_PROTECT_COURSE_DELETE;
            case R.id.course_context_reset: return MobileLearning.ADMIN_PROTECT_COURSE_RESET;
            case R.id.course_context_update_activity: return MobileLearning.ADMIN_PROTECT_COURSE_UPDATE;
            case R.id.menu_download: return MobileLearning.ADMIN_PROTECT_COURSE_INSTALL;
            case R.id.menu_settings: return MobileLearning.ADMIN_PROTECT_SETTINGS;
            default: return false;
        }
    }

    public static boolean checkAdminPassword(Context ctx, String pass) {

        if (!isAdminProtectionEnabled(ctx)) return false;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String adminPass = prefs.getString(PrefsActivity.PREF_ADMIN_PASSWORD, "");

        return ((pass != null) && (adminPass.equals("") || adminPass.equals(pass)));
    }
}
