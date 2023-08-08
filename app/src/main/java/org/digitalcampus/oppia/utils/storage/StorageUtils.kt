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
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.analytics.Analytics
import org.digitalcampus.oppia.application.App
import java.io.IOException
import java.nio.charset.StandardCharsets

object StorageUtils {

    val TAG = StorageUtils::class.simpleName

    @JvmStatic
    fun getInternalMemoryDrive(ctx: Context): DeviceFile? {
        return getFirstDeviceIfRemovable(ctx, false)
    }

    fun getExternalMemoryDrive(ctx: Context): DeviceFile? {
        return getFirstDeviceIfRemovable(ctx, true)
    }

    private fun getFirstDeviceIfRemovable(ctx: Context, removable: Boolean): DeviceFile? {
        val dirs = ctx.getExternalFilesDirs(null)
        for (dir in dirs) {
            if (dir != null && Environment.isExternalStorageRemovable(dir) == removable) {
                return DeviceFile(dir)
            }
        }
        return null
    }

    @JvmStatic
    fun getStorageList(ctx: Context): List<StorageLocationInfo> {
        val list = mutableListOf<StorageLocationInfo>()
        val internalStorage = getInternalMemoryDrive(ctx)
        val externalStorage = getExternalMemoryDrive(ctx)
        internalStorage?.let {
            val internal = StorageLocationInfo(PrefsActivity.STORAGE_OPTION_INTERNAL,
                    internalStorage.path, false, false, 1)
            list.add(internal)
        }
        externalStorage?.let {
            if (externalStorage.canWrite()) {
                val external = StorageLocationInfo(PrefsActivity.STORAGE_OPTION_EXTERNAL,
                        externalStorage.path, false, true, 1)
                list.add(external)
            }
        }
        return list
    }

    @JvmStatic
    fun readFileFromAssets(ctx: Context, filename: String): String? {
        try {
            ctx.resources.assets.open(filename).use { isStream ->
                val size =  isStream.available()
                val buffer = ByteArray(size)
                isStream.read(buffer)
                return String(buffer, StandardCharsets.UTF_8)
            }
        } catch (e: IOException) {
            Analytics.logException(e)
            return null
        }
    }

    @JvmStatic
    fun saveStorageData(context: Context, storageType: String) {
        val editor = App.getPrefs(context).edit()
        editor.putString(PrefsActivity.PREF_STORAGE_OPTION, storageType)
        editor.apply()
    }
}