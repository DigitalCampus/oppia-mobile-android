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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.digitalcampus.oppia.activity.PrefsActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ExternalStorageStrategy implements StorageAccessStrategy{

    public static final String TAG = FileUtils.class.getSimpleName();
    private static String internalPath;

    //@Override
    public void updateStorageLocation(Context ctx){

        String location = null;
        //If no mount argument passed, we set the default external mount
        DeviceFile external = StorageUtils.getExternalMemoryDrive();
        if (external != null && external.canWrite()){
            location = external.getPath();
        }
        else{
            DeviceFile internal = StorageUtils.getInternalMemoryDrive();
            if (internal != null && internal.canWrite()){
                location = internal.getPath();

            }
        }
        if (location != null){
            location += getInternalBasePath(ctx);
            updateLocationPreference(ctx, location);
        }
    }

    //@Override
    public void updateStorageLocation(Context ctx, String mount) {

        if ((mount == null ) || mount.equals("")){
            updateStorageLocation(ctx);
            return;
        }
        String currentLocation = this.getStorageLocation(ctx);
        if (currentLocation.startsWith(mount)){ return; }

        String location = mount + getInternalBasePath(ctx);
        updateLocationPreference(ctx, location);
    }

    //@Override
    public String getStorageLocation(Context ctx){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String location = prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, "");

        if ((location == null) || location.equals("")){
            //If location is not set yet, update it and get it again
            updateStorageLocation(ctx);
            location = prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, "");
        }

        return location;
    }

    //@Override
    public boolean isStorageAvailable(Context ctx) {
        String cardStatus = Environment.getExternalStorageState();
        if (cardStatus.equals(Environment.MEDIA_REMOVED)
                || cardStatus.equals(Environment.MEDIA_UNMOUNTABLE)
                || cardStatus.equals(Environment.MEDIA_UNMOUNTED)
                || cardStatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)
                || cardStatus.equals(Environment.MEDIA_SHARED)) {
            Log.d(TAG, "card status: " + cardStatus);
            return false;
        }
        else{
            return true;
        }
    }

    //@Override
    public String getStorageType() {
        return PrefsActivity.STORAGE_OPTION_EXTERNAL;
    }

    private void updateLocationPreference(Context ctx, String location){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PrefsActivity.PREF_STORAGE_LOCATION, location);
        editor.commit();
    }

    private static String getInternalBasePath(Context ctx){
        if (internalPath == null){
            String packageName = ctx.getPackageName();
            // internalPath: /Android/data/{{packageName}}/files
            internalPath = File.separator + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "files";
        }
        return internalPath;
    }
}
