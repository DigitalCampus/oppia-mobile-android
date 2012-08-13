package org.digitalcampus.mtrain.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TrackerStartServiceReceiver extends BroadcastReceiver {

	public final static String TAG = "TrackerStartServiceReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent service = new Intent(context, TrackerService.class);
		context.startService(service);
		Log.d(TAG, "onReceive running");
	}
}