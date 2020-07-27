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
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import androidx.preference.PreferenceManager;
import androidx.documentfile.provider.DocumentFile;
import android.util.Log;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.listener.StorageAccessListener;

import java.io.File;


public class ExternalStorageStrategy implements StorageAccessStrategy{

    public static final String TAG = ExternalStorageStrategy.class.getSimpleName();
    private static String internalPath;

    @Override
    public boolean updateStorageLocation(Context ctx){

        String location = null;
        //If no mount argument passed, we set the default external mount
        DeviceFile external = StorageUtils.getExternalMemoryDrive(ctx);
        Log.d(TAG, "External drive: " + (external==null?"null!":external.getPath()));
        if (external != null && external.canWrite()){
            location = external.getPath();
            if (!location.contains(getInternalBasePath(ctx))){
                File destPath = new File(external.getPath() + getInternalBasePath(ctx));
                if (!destPath.canWrite()){
                    Log.d(TAG, "External SD(" + external.getPath() + ") available, but no write permissions");
                    setPermissionsNeeded(ctx, true);
                }
            }
        }


        //If there is no external storage available, we try the internal one
        if (location == null){
            DeviceFile internal = StorageUtils.getInternalMemoryDrive();
            Log.d(TAG, "External non-removable: " + (internal==null?"null!":internal.getPath()));
            if (internal != null && internal.canWrite()){
                location = internal.getPath();
            }
        }
        if (location != null){
            if (!location.contains(getInternalBasePath(ctx)))
                location += getInternalBasePath(ctx);
            updateLocationPreference(ctx, location);
        }
        //It was successfull if we found a writable external location
        return (location != null);
    }

    @Override
    public boolean updateStorageLocation(Context ctx, String mount) {

        if ((mount == null ) || mount.equals("")){
            return updateStorageLocation(ctx);
        }
        String currentLocation = this.getStorageLocation(ctx);
        if (currentLocation.startsWith(mount)){ return true; }

        String location = mount + getInternalBasePath(ctx);
        updateLocationPreference(ctx, location);
        
        return true;
    }

    @Override
    public String getStorageLocation(Context ctx){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String location = prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, "");

        if (location.equals("")){
            //If location is not set yet, update it and get it again
            updateStorageLocation(ctx);
            location = prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, "");
        }
        return location;
    }

    @Override
    public boolean isStorageAvailable() {
        String cardStatus = ExternalStorageState.getExternalStorageState();
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

    @Override
    public boolean needsUserPermissions(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (prefs.contains(PrefsActivity.STORAGE_NEEDS_PERMISSIONS)){
            return prefs.getBoolean(PrefsActivity.STORAGE_NEEDS_PERMISSIONS, false);
        }

        // If by some reason the value is not set yet (coming from previous installation)
        String currentLocation = this.getStorageLocation(ctx);
        File currentPath = new File(currentLocation);
        if (!currentPath.canWrite()){
            setPermissionsNeeded(ctx, true);
            return true;
        }
        else{
            setPermissionsNeeded(ctx, false);
            return false;
        }

    }

    @Override
    public void askUserPermissions(final Activity act, final StorageAccessListener listener) {

        final FragmentManager fragManager = act.getFragmentManager();
        GrantStorageAccessFragment f = new GrantStorageAccessFragment();
        f.setListener(new GrantStorageAccessFragment.AccessGrantedListener() {
            @Override
            public void onAccessGranted(Uri pathAccessGranted) {
                if (pathAccessGranted == null){
                    listener.onAccessGranted(false);
                    return;
                }
                String[] treePath = pathAccessGranted.getPath().split(":");
                if ((treePath.length > 1)){
                    //The user didn't select the root directory or selected the internal storage
                    listener.onAccessGranted(false);
                }
                else{
                    Log.d(TAG, "Creating Oppia folders in SD-card with granted access");
                    DocumentFile rootDir = DocumentFile.fromTreeUri(act, pathAccessGranted);
                    DocumentFile tempDir = null;
                    if (rootDir.isDirectory()){
                        tempDir = rootDir;
                        String[] dirs = getInternalBasePath(act).split(File.separator);
                        for (int i=1; i<dirs.length; i++) {
                            DocumentFile childDir = tempDir.findFile(dirs[i]);
                            if((childDir!=null) && childDir.exists() && childDir.isDirectory()){
                                tempDir = childDir;
                            }
                            else{
                                tempDir = tempDir.createDirectory(dirs[i]);
                                if (tempDir == null) break;
                            }

                        }
                    }
                    //Everything is correct if we were able to create all the tree directory
                    setPermissionsNeeded(act, false);
                    listener.onAccessGranted( (tempDir != null) );
                }
            }
        });

        //We add the fragment so the intent gets launched
        FragmentTransaction fragmentTransaction = fragManager.beginTransaction();
        fragmentTransaction.add(f, GrantStorageAccessFragment.FRAGMENT_TAG);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    public String getStorageType() {
        return PrefsActivity.STORAGE_OPTION_EXTERNAL;
    }

    private void updateLocationPreference(Context ctx, String location){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        prefs.edit().putString(PrefsActivity.PREF_STORAGE_LOCATION, location).apply();
    }

    public static String getInternalBasePath(Context ctx){
        if (internalPath == null){
            String packageName = ctx.getPackageName();
            // internalPath: /Android/data/{{packageName}}/files
            internalPath = File.separator + "Android" + File.separator + "data" + File.separator + packageName + File.separator + "files";
        }
        return internalPath;
    }

    protected static void setPermissionsNeeded(Context ctx, boolean permissionsNeeded){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        prefs.edit().putBoolean(PrefsActivity.STORAGE_NEEDS_PERMISSIONS, permissionsNeeded).apply();
    }

    public static boolean needsUserPermissions(Context ctx, String location){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            //Only in versions >= Lollipop we need to check write permissions
            return false;
        }

        DeviceFile internal = StorageUtils.getInternalMemoryDrive();
        if (internal.getPath().equals(location)){
            return false;
        }
        else{
            File destPath = new File(location + getInternalBasePath(ctx));
            if (!destPath.canWrite()){
                Log.d(TAG, "External SD(" + location + ") available, but no write permissions");
                return true;
            }
        }
        return false;
    }
}
