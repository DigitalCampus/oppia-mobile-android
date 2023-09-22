package org.digitalcampus.oppia.gamification

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.application.App
import org.digitalcampus.oppia.database.DbHelper
import org.digitalcampus.oppia.exception.WrongServerException
import org.joda.time.DateTime
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

object LeaderboardUtils {

    val TAG = LeaderboardUtils::class.simpleName

    @JvmStatic
    @Throws(JSONException::class, ParseException::class, WrongServerException::class)
    fun importLeaderboardJSON(ctx: Context, json: String): Int {
        val db = DbHelper.getInstance(ctx)
        var updatedPositions = 0
        val leaderboard = JSONObject(json)
        var server = leaderboard.getString("server")
        if (!server.endsWith("/")) {
            server = "$server/"
        }
        val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
        if (prefs.getString(PrefsActivity.PREF_SERVER, "") != server) {
            Log.d(TAG, "Leaderboard server doesn't match with current one: " +
                        prefs.getString(PrefsActivity.PREF_SERVER, "") + " - " + server)
            throw WrongServerException(server)
        }
        val lastUpdateStr = leaderboard.getString("generated_date")
        val sdt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()).parse(lastUpdateStr)
        val positions = leaderboard.getJSONArray("leaderboard")
        val lastUpdate = DateTime(sdt)
        for (i in 0 until positions.length()) {
            val pos = positions.getJSONObject(i)
            val updated = db.insertOrUpdateUserLeaderboard(
                pos.getString("username"),
                pos.getString("first_name") + " " + pos.getString("last_name"),
                pos.getInt("points"),
                lastUpdate,
                pos.getInt("position")
            )
            if (updated) {
                updatedPositions++
            }
        }
        Log.d(TAG, "leaderboard added:$updatedPositions")
        return updatedPositions
    }

    @JvmStatic
    fun shouldFetchLeaderboard(prefs: SharedPreferences): Boolean {
        val now = System.currentTimeMillis() / 1000
        val lastScan = prefs.getLong(PrefsActivity.PREF_LAST_LEADERBOARD_FETCH, 0)
        return lastScan + App.LEADERBOARD_FETCH_EXPIRATION <= now
    }

    @JvmStatic
    fun updateLeaderboardFetchTime(prefs: SharedPreferences) {
        Log.d(TAG, "Updating last leaderboard fetch to now")
        val now = System.currentTimeMillis() / 1000
        prefs.edit().putLong(PrefsActivity.PREF_LAST_LEADERBOARD_FETCH, now).apply()
    }
}