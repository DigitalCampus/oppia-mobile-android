package org.digitalcampus.oppia.utils.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import org.digitalcampus.oppia.activity.PrefsActivity;

import java.io.File;


public class ExternalStorageStrategy implements StorageAccessStrategy{

    public static final String TAG = FileUtils.class.getSimpleName();

    @Override
    public void updateStorageLocation(Context ctx) {
        String location = this.getStorageLocation(ctx);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PrefsActivity.PREF_STORAGE_LOCATION, location);
        editor.commit();
    }

    @Override
    public String  getStorageLocation(Context ctx){
        File[] dirs = ContextCompat.getExternalFilesDirs(ctx, null);

        if (dirs.length > 0){
            String location = dirs[dirs.length-1].toString();
            return location;
        }
        else{
            return "";
        }
        /*
        COMMENTED THIS PART ABOUT CUSTOM LOCATIONS
        //get from prefs
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String location = prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, "");
        // if location not set - then set it to first of dirs
        if (location.equals("") && dirs.length > 0){
            location = dirs[dirs.length-1].toString();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PrefsActivity.PREF_STORAGE_LOCATION, location);
            editor.commit();
        }
        */

    }

    @Override
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

    @Override
    public String getStorageType() {
        return PrefsActivity.STORAGE_OPTION_EXTERNAL;
    }
}
