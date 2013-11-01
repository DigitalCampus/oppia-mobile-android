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

package org.digitalcampus.oppia.service;

import org.digitalcampus.mobile.learning.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class TrackerStartServiceReceiver extends BroadcastReceiver {

	public final static String TAG = TrackerStartServiceReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context ctx, Intent intent) {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		boolean backgroundData = prefs.getBoolean(ctx.getString(R.string.prefs_background_data_connect), true);
		Intent service = new Intent(ctx, TrackerService.class);
		
		Bundle tb = new Bundle();
		tb.putBoolean("backgroundData", backgroundData);
		service.putExtras(tb);
		
		ctx.startService(service);
	}
}