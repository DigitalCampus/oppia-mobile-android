/* 
 * This file is part of OppiaMobile - http://oppia-mobile.org/
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

import java.util.Iterator;

import org.digitalcampus.mobile.learning.R;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

public class MetaDataUtils {

	public static final String TAG = MetaDataUtils.class.getSimpleName();
	private String networkProvider;
	private String deviceId;
	private String simSerial;
	private Context ctx;
	  
	public MetaDataUtils(Context ctx){
		this.ctx = ctx;
		TelephonyManager manager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
		networkProvider = manager.getNetworkOperatorName();
		deviceId = manager.getDeviceId();
		simSerial = manager.getSimSerialNumber();
	}
	
	private String getNetworkProvider() {
		return networkProvider;
	}

	private String getDeviceId() {
		return deviceId;
	}

	private String getSimSerial() {
		return simSerial;
	}

	private float getBatteryLevel() {
	    Intent batteryIntent = ctx.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	    int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
	    int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

	    // Error checking that probably isn't needed but I added just in case.
	    if(level == -1 || scale == -1) {
	        return 50.0f;
	    }

	    return ((float)level / (float)scale) * 100.0f; 
	}
	
	public void saveMetaData(JSONObject metadata, SharedPreferences prefs) throws JSONException{
		Editor editor = prefs.edit();
		Iterator<?> keys = metadata.keys();
		while( keys.hasNext() ){
            String key = (String) keys.next();
            //Log.d(TAG,key + ": " + metadata.getBoolean(key));
            editor.putBoolean(ctx.getString(R.string.prefs_metadata) + "_" + key, metadata.getBoolean(key));
        }
		editor.commit();
	}
	
	public JSONObject getMetaData(JSONObject json) throws JSONException{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		if(prefs.getBoolean(ctx.getString(R.string.prefs_metadata) + "_NETWORK", false)){
			json.put("network",this.getNetworkProvider());
		}
		if(prefs.getBoolean(ctx.getString(R.string.prefs_metadata) + "_DEVICE_ID", false)){
			json.put("deviceid",this.getDeviceId());
		}
		if(prefs.getBoolean(ctx.getString(R.string.prefs_metadata) + "_SIM_SERIAL", false)){
			json.put("simserial",this.getSimSerial());
		}
		if(prefs.getBoolean(ctx.getString(R.string.prefs_metadata) + "_WIFI_ON", false)){
			json.put("wifion",ConnectionUtils.isOnWifi(ctx));
		}
		if(prefs.getBoolean(ctx.getString(R.string.prefs_metadata) + "_NETWORK_CONNECTED", false)){
			json.put("netconnected",ConnectionUtils.isNetworkConnected(ctx));
		}
		if(prefs.getBoolean(ctx.getString(R.string.prefs_metadata) + "_BATTERY_LEVEL", false)){
			json.put("battery",this.getBatteryLevel());
		}
		if(prefs.getBoolean(ctx.getString(R.string.prefs_metadata) + "_GPS", false)){
			json.put("gps","0,0");
		}
		return json;
	}
}
