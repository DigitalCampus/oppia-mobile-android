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
package org.digitalcampus.oppia.utils.storage

import android.content.Context
import android.os.Environment
import android.util.Log
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.utils.TextUtilsJava
import org.digitalcampus.oppia.utils.storage.ExternalStorageState.Companion.getExternalStorageState
import java.io.File

class ExternalStorageStrategy : StorageAccessStrategy {
    companion object {
        val TAG = ExternalStorageStrategy::class.simpleName
    }

    override fun getStorageLocation(ctx: Context): String? {
        val external = StorageUtils.getExternalMemoryDrive(ctx)
        return external?.path
    }

    override fun isStorageAvailable(ctx: Context): Boolean {
        val externalStorageLocation = getStorageLocation(ctx)
        if (TextUtilsJava.isEmpty(externalStorageLocation)) {
            return false
        }
        val cardStatus = getExternalStorageState(File(externalStorageLocation))
        return if (cardStatus == Environment.MEDIA_REMOVED
                || cardStatus == Environment.MEDIA_UNMOUNTABLE
                || cardStatus == Environment.MEDIA_UNMOUNTED
                || cardStatus == Environment.MEDIA_MOUNTED_READ_ONLY
                || cardStatus == Environment.MEDIA_SHARED) {
            Log.d(TAG, "card status: $cardStatus")
            false
        } else {
            true
        }
    }

    override fun getStorageType(): String {
        return PrefsActivity.STORAGE_OPTION_EXTERNAL
    }
}