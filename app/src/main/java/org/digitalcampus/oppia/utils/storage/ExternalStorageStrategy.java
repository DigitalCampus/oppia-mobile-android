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
import android.os.Environment;

import androidx.preference.PreferenceManager;
import androidx.documentfile.provider.DocumentFile;

import android.text.TextUtils;
import android.util.Log;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.listener.StorageAccessListener;

import java.io.File;


public class ExternalStorageStrategy implements StorageAccessStrategy {

    public static final String TAG = ExternalStorageStrategy.class.getSimpleName();

    @Override
    public String getStorageLocation(Context ctx) {
        DeviceFile external = StorageUtils.getExternalMemoryDrive(ctx);
        return external != null ? external.getPath() : null;
    }

    @Override
    public boolean isStorageAvailable(Context ctx) {
        String externalStorageLocation = getStorageLocation(ctx);
        if (TextUtils.isEmpty(externalStorageLocation)) {
            return false;
        }

        String cardStatus = ExternalStorageState.getExternalStorageState(new File(externalStorageLocation));
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
//        return StorageUtils.getExternalMemoryDrive(ctx, true) != null;
    }

    @Override
    public String getStorageType() {
        return PrefsActivity.STORAGE_OPTION_EXTERNAL;
    }

}
