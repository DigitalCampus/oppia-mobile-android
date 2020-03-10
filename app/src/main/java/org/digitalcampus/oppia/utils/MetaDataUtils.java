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
package org.digitalcampus.oppia.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.BatteryManager;
import android.os.Build;
import androidx.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.App;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class MetaDataUtils {

    public static final String TAG = MetaDataUtils.class.getSimpleName();
    private String networkProvider;
    private Context ctx;
    private SharedPreferences prefs;

    public MetaDataUtils(Context ctx) {
        this.ctx = ctx;
        setPrefs(PreferenceManager.getDefaultSharedPreferences(ctx));
        TelephonyManager manager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (manager != null) {
            networkProvider = manager.getNetworkOperatorName();
        }
    }

    public void setPrefs(SharedPreferences prefs){
        this.prefs = prefs;
    }

    private String getNetworkProvider() {
        return networkProvider;
    }

    private String getDeviceId() {
        return Settings.Secure.ANDROID_ID;
    }

    private String getSimSerial() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return model;
        } else {
            return manufacturer + " " + model;
        }
    }

    private float getBatteryLevel() {
        Intent batteryIntent = ctx.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if (level == -1 || scale == -1) {
            return 50.0f;
        }

        return ((float) level / (float) scale) * 100.0f;
    }

    public void saveMetaData(JSONObject metadata, SharedPreferences prefs) throws JSONException {
        Editor editor = prefs.edit();
        Iterator<?> keys = metadata.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            editor.putBoolean(getMetadataPref(key), metadata.getBoolean(key));
        }
        editor.apply();
    }

    public String getMetadataPref(String metadataKey){
        return PrefsActivity.PREF_METADATA + "_" + metadataKey;
    }

    public JSONObject getMetaData() throws JSONException{
        JSONObject json = new JSONObject();
        return getMetaData(json);
    }

    public JSONObject getMetaData(JSONObject json) throws JSONException {

        if (json == null){
            json = new JSONObject();
        }

        if (prefs.getBoolean(getMetadataPref(PrefsActivity.PREF_METADATA_NETWORK), App.METADATA_INCLUDE_NETWORK)) {
            json.put("network", this.getNetworkProvider());
        }
        if (prefs.getBoolean(getMetadataPref(PrefsActivity.PREF_METADATA_DEVICE_ID), App.METADATA_INCLUDE_DEVICE_ID)) {
            json.put("deviceid", this.getDeviceId());
        }
        if (prefs.getBoolean(getMetadataPref(PrefsActivity.PREF_METADATA_SIM_SERIAL), App.METADATA_INCLUDE_SIM_SERIAL)) {
            json.put("simserial", this.getSimSerial());
        }
        if (prefs.getBoolean(getMetadataPref(PrefsActivity.PREF_METADATA_WIFI_ON), App.METADATA_INCLUDE_WIFI_ON)) {
            json.put("wifion", ConnectionUtils.isOnWifi(ctx));
        }
        if (prefs.getBoolean(getMetadataPref(PrefsActivity.PREF_METADATA_NETWORK_CONNECTED), App.METADATA_INCLUDE_NETWORK_CONNECTED)) {
            json.put("netconnected", ConnectionUtils.isNetworkConnected(ctx));
        }
        if (prefs.getBoolean(getMetadataPref(PrefsActivity.PREF_METADATA_BATTERY_LEVEL), App.METADATA_INCLUDE_BATTERY_LEVEL)) {
            json.put("battery", this.getBatteryLevel());
        }
        if (prefs.getBoolean(getMetadataPref(PrefsActivity.PREF_METADATA_GPS), App.METADATA_INCLUDE_GPS)) {
            json.put("gps", "0,0");
        }
        return json;
    }
}
