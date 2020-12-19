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
    public String getStorageLocation(Context ctx) {
        DeviceFile internal = StorageUtils.getInternalMemoryDrive(ctx);
        return internal != null ? internal.getPath() : null;
    }

    @Override
    public boolean isStorageAvailable(Context ctx) {
        return true;
    }

    @Override
    public String getStorageType() {
        return PrefsActivity.STORAGE_OPTION_INTERNAL;
    }
}
