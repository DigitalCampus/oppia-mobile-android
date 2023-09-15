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
package org.digitalcampus.oppia.model

import android.content.SharedPreferences
import android.util.Log
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.application.App
import java.io.Serializable

class Media : Serializable {

    companion object {
        private const val serialVersionUID = -7381597814535579028L
        val TAG = Media::class.simpleName
        @JvmStatic
        fun shouldScanMedia(prefs: SharedPreferences): Boolean {
            val now = System.currentTimeMillis() / 1000
            val lastScan = prefs.getLong(PrefsActivity.PREF_LAST_MEDIA_SCAN, 0)
            return lastScan + App.MEDIA_SCAN_TIME_LIMIT <= now
        }

        @JvmStatic
        fun resetMediaScan(prefs: SharedPreferences) {
            Log.d(TAG, "Resetting last media scan")
            prefs.edit().putLong(PrefsActivity.PREF_LAST_MEDIA_SCAN, 0).apply()
        }

        @JvmStatic
        fun updateMediaScan(prefs: SharedPreferences) {
            Log.d(TAG, "Updating last media scan to now")
            val now = System.currentTimeMillis() / 1000
            prefs.edit().putLong(PrefsActivity.PREF_LAST_MEDIA_SCAN, now).apply()
        }
    }

    var filename: String? = null
    var downloadUrl: String? = null
    var digest: String? = null
    var length = 0
    var fileSize = 0.0
    private var courses: ArrayList<Course>? = null
    val downloaded = 0

    constructor() {
        courses = ArrayList()
    }

    constructor(filename: String?, length: Int) {
        this.filename = filename
        this.length = length
    }

    fun getCourses(): List<Course>? {
        return courses
    }

    //ONLY FOR UI PURPOSES
    var isDownloading = false
    private var failed = false
    var progress = 0

    fun hasFailed(): Boolean {
        return failed
    }

    fun setFailed(failed: Boolean) {
        this.failed = failed
    }
}