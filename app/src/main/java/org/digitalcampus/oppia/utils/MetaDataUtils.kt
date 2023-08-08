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
package org.digitalcampus.oppia.utils

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.BatteryManager
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Log
import androidx.preference.PreferenceManager
import org.digitalcampus.mobile.learning.BuildConfig
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.analytics.Analytics
import org.digitalcampus.oppia.application.App
import org.digitalcampus.oppia.utils.ConnectionUtils.Companion.isNetworkConnected
import org.digitalcampus.oppia.utils.ConnectionUtils.Companion.isOnWifi
import org.json.JSONException
import org.json.JSONObject

class MetaDataUtils(private val ctx: Context) {

    companion object {
        val TAG = MetaDataUtils::class.simpleName
    }

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx)

    private val networkProvider: String
        get() {
            val manager = ctx.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return manager.networkOperatorName
        }

    private val appInstanceId: String?
        get() = App.getPrefs(ctx).getString(PrefsActivity.PREF_APP_INSTANCE_ID, "no-id")

    private val manufacturerModel: String
        get() {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return if (model.startsWith(manufacturer)) {
                model
            } else {
                "$manufacturer $model"
            }
        }

    // Error checking that probably isn't needed but I added just in case.
    private val batteryLevel: Float
        get() {
            val batteryIntent = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

            // Error checking that probably isn't needed but I added just in case.
            return if (level == -1 || scale == -1) {
                50.0f
            } else {
                (level.toFloat() / scale.toFloat()) * 100.0f
            }
        }

    fun saveMetaData(json: JSONObject) {
        try {
            val metadata = json.getJSONObject("metadata")
            saveMetaData(metadata, prefs)
        } catch (e: JSONException) {
            Analytics.logException(e)
            Log.d(TAG, "JSONException: ", e)
        }
    }

    private fun saveMetaData(metadata: JSONObject, prefs: SharedPreferences) {
        val editor = prefs.edit()
        val keys: Iterator<*> = metadata.keys()
        while (keys.hasNext()) {
            val key = keys.next() as String
            editor.putBoolean(getMetadataPref(key), metadata.getBoolean(key))
        }
        editor.apply()
    }

    fun getMetadataPref(metadataKey: String): String {
        return "${PrefsActivity.PREF_METADATA}_$metadataKey"
    }

    val metaData: JSONObject
        get() {
            val json = JSONObject()
            return getMetaData(json)
        }

    fun getMetaData(_json: JSONObject?): JSONObject {
        var json = _json
        if (json == null) {
            json = JSONObject()
        }
        try {
            if (prefs.getBoolean(getMetadataPref(PrefsActivity.PREF_METADATA_NETWORK), BuildConfig.METADATA_INCLUDE_NETWORK)) {
                json.put("network", networkProvider)
            }
            if (prefs.getBoolean(getMetadataPref(PrefsActivity.PREF_METADATA_APP_INSTANCE_ID), BuildConfig.METADATA_INCLUDE_APP_INSTANCE_ID)) {
                json.put("appInstanceId", appInstanceId)
            }
            if (prefs.getBoolean(getMetadataPref(PrefsActivity.PREF_METADATA_MANUFACTURER_MODEL), BuildConfig.METADATA_INCLUDE_MANUFACTURER_MODEL)) {
                json.put("manufacturermodel", manufacturerModel)
            }
            if (prefs.getBoolean(getMetadataPref(PrefsActivity.PREF_METADATA_WIFI_ON), BuildConfig.METADATA_INCLUDE_WIFI_ON)) {
                json.put("wifion", isOnWifi(ctx))
            }
            if (prefs.getBoolean(getMetadataPref(PrefsActivity.PREF_METADATA_NETWORK_CONNECTED), BuildConfig.METADATA_INCLUDE_NETWORK_CONNECTED)) {
                json.put("netconnected", isNetworkConnected(ctx))
            }
            if (prefs.getBoolean(getMetadataPref(PrefsActivity.PREF_METADATA_BATTERY_LEVEL), BuildConfig.METADATA_INCLUDE_BATTERY_LEVEL)) {
                json.put("battery", batteryLevel.toDouble())
            }
        } catch (e: JSONException) {
            Analytics.logException(e)
        }
        return json
    }
}