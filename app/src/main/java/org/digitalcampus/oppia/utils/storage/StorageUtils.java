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
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.App;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;


public class StorageUtils {

    public static final String TAG = StorageUtils.class.getSimpleName();

    private StorageUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static DeviceFile getInternalMemoryDrive(Context ctx) {
        return getFirstDeviceIfRemovable(ctx, false);
    }

    public static DeviceFile getExternalMemoryDrive(Context ctx) {
        return getFirstDeviceIfRemovable(ctx, true);
    }

    private static DeviceFile getFirstDeviceIfRemovable(Context ctx, boolean removable) {

        File[] dirs = ctx.getExternalFilesDirs(null);
        for (File dir : dirs) {
            if (dir != null && Environment.isExternalStorageRemovable(dir) == removable) {
                return new DeviceFile(dir);
            }
        }

        return null;
    }

    public static List<StorageLocationInfo> getStorageList(Context ctx) {

        List<StorageLocationInfo> list = new ArrayList<>();
        DeviceFile internalStorage = getInternalMemoryDrive(ctx);
        DeviceFile externalStorage = getExternalMemoryDrive(ctx);

        StorageLocationInfo internal = new StorageLocationInfo(PrefsActivity.STORAGE_OPTION_INTERNAL,
                internalStorage.getPath(), false, false, 1);
        list.add(internal);

        if (externalStorage != null && externalStorage.canWrite()) {
            StorageLocationInfo external = new StorageLocationInfo(PrefsActivity.STORAGE_OPTION_EXTERNAL,
                    externalStorage.getPath(), false, true, 1);
            list.add(external);
        }

        return list;
    }


    public static String readFileFromAssets(Context ctx, String filename) {

        try (InputStream is = ctx.getResources().getAssets().open(filename)) {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            String content = new String(buffer, StandardCharsets.UTF_8);
            return content;
        } catch (IOException e) {
            Mint.logException(e);
            return null;
        }
    }

    public static void saveStorageData(Context context, String storageType) {

        SharedPreferences.Editor editor = App.getPrefs(context).edit();
        editor.putString(PrefsActivity.PREF_STORAGE_OPTION, storageType);
        editor.apply();
    }
}
