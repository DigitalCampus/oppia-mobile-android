package org.digitalcampus.mtrain.service;

import java.util.Calendar;

import org.digitalcampus.mtrain.model.Module;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class TrackerScheduleReceiver extends BroadcastReceiver {

	public static final String TAG = "TrackerScheduleReceiver";

	// Restart service every 1 hour
	private static final long REPEAT_TIME = 1000 * 3600;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "running onReceive service");
		AlarmManager service = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent i = new Intent(context, TrackerStartServiceReceiver.class);
		
		PendingIntent pending = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
		Calendar cal = Calendar.getInstance();
		
		// Start 30 seconds after boot completed
		cal.add(Calendar.SECOND, 30);
		//
		// every 1 hour
		// InexactRepeating allows Android to optimize the energy consumption
		service.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), REPEAT_TIME, pending);

	}
}
