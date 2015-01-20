package org.digitalcampus.oppia.utils.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.digitalcampus.oppia.activity.PrefsActivity;

import java.io.File;

public class InternalStorageStrategy implements StorageAccessStrategy{

    //@Override
    public void updateStorageLocation(Context ctx) {
        String location = this.getStorageLocation(ctx);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PrefsActivity.PREF_STORAGE_LOCATION, location);
        editor.commit();
    }

    //@Override
    public String getStorageLocation(Context ctx) {
        File location = ctx.getFilesDir();
        return location.toString();
    }

    //@Override
    public boolean isStorageAvailable(Context ctx) {
        //Internal storage is always available :)
        return true;
    }

    //@Override
    public String getStorageType() {
        return PrefsActivity.STORAGE_OPTION_INTERNAL;
    }
}
