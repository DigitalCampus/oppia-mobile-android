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
package org.digitalcampus.oppia.application

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.analytics.Analytics
import org.digitalcampus.oppia.database.DbHelper
import org.digitalcampus.oppia.gamification.Gamification
import org.digitalcampus.oppia.model.GamificationEvent
import org.digitalcampus.oppia.utils.MetaDataUtils
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID

class Tracker(private val ctx: Context) {

    companion object {
        val TAG = Tracker::class.simpleName
        const val SEARCH_TYPE = "search"
        const val MISSING_MEDIA_TYPE = "missing_media"
        const val DOWNLOAD_TYPE = "download"
    }

    fun saveTracker(courseId: Int, digest: String?, data: JSONObject, type: String, completed: Boolean, gamificationEvent: GamificationEvent) {
        // add tracker UUID
        val guid = UUID.randomUUID()
        try {
            data.put("uuid", guid.toString())
        } catch (e: JSONException) {
            Analytics.logException(e)
            Log.d(TAG, "error with uuid: ", e)
        }
        val db = DbHelper.getInstance(ctx)
        db.insertTracker(
            courseId,
            digest,
            data.toString(),
            type,
            completed,
            gamificationEvent.event,
            gamificationEvent.points
        )
        val editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit()
        val now = System.currentTimeMillis() / 1000
        editor.putLong(PrefsActivity.PREF_TRIGGER_POINTS_REFRESH, now).apply()
    }

    fun saveTracker(courseId: Int, digest: String, data: JSONObject, completed: Boolean, gamificationEvent: GamificationEvent) {
        saveTracker(courseId, digest, data, "", completed, gamificationEvent)
    }

    fun saveSearchTracker(searchTerm: String, count: Int) {
        try {
            val searchData = MetaDataUtils(ctx).metaData
            searchData.put("query", searchTerm)
            searchData.put("results_count", count)
            saveTracker(
                0,
                "",
                searchData,
                SEARCH_TYPE,
                true,
                Gamification.GAMIFICATION_SEARCH_PERFORMED
            )
        } catch (e: JSONException) {
            Analytics.logException(e)
            Log.d(TAG, "Errors saving search tracker: ", e)
        }
    }

    fun saveMissingMediaTracker(courseId: Int, mediaDigest: String, filename: String) {
        try {
            val missingMedia = MetaDataUtils(ctx).metaData
            missingMedia.put("filename", filename)
            missingMedia.put("media_digest", mediaDigest)
            saveTracker(
                courseId,
                "",
                missingMedia,
                MISSING_MEDIA_TYPE,
                false,
                Gamification.GAMIFICATION_MEDIA_MISSING
            )
        } catch (e: JSONException) {
            Analytics.logException(e)
            Log.d(TAG, "Error saving missing media tracker: ", e)
        }
    }

    fun saveRegisterTracker() {
        val registerData = MetaDataUtils(ctx).metaData
        saveTracker(
            0,
            "",
            registerData,
            Gamification.EVENT_NAME_REGISTER,
            true,
            Gamification.GAMIFICATION_REGISTER
        )
    }
}