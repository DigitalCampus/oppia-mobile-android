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

package org.digitalcampus.oppia.utils.storage;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.listener.StorageAccessListener;

import java.io.File;

public class InternalStorageStrategy implements StorageAccessStrategy{

    @Override
    public boolean updateStorageLocation(Context ctx) {
        String location = this.getStorageLocation(ctx);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PrefsActivity.PREF_STORAGE_LOCATION, location);
        editor.commit();
        return true;
    }

    @Override
    public boolean updateStorageLocation(Context ctx, String mount) {
        updateStorageLocation(ctx);
        return true;
    }

    @Override
    public String getStorageLocation(Context ctx) {
        File location = ctx.getFilesDir();
        return location.toString();
    }

    @Override
    public boolean isStorageAvailable() {
        //Internal storage is always available :)
        return true;
    }

    @Override
    public boolean needsUserPermissions(Context ctx) { return false; }
    @Override
    public void askUserPermissions(Activity activity, StorageAccessListener listener) {
        listener.onAccessGranted(true);
    }

    @Override
    public String getStorageType() {
        return PrefsActivity.STORAGE_OPTION_INTERNAL;
    }
}
