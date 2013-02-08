package org.digitalcampus.mobile.learning.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class TrackerStartServiceReceiver extends BroadcastReceiver {

	public final static String TAG = TrackerStartServiceReceiver.class.getSimpleName();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean backgroundData = prefs.getBoolean("prefBackgroundDataConnect", true);
		Intent service = new Intent(context, TrackerService.class);
		
		Bundle tb = new Bundle();
		tb.putBoolean("backgroundData", backgroundData);
		service.putExtras(tb);
		
		context.startService(service);
	}
}