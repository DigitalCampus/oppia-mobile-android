package org.digitalcampus.oppia.application;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.task.RegisterDeviceRemoteAdminTask;


public class AdminReceiver extends DeviceAdminReceiver {
    static final String TAG = AdminReceiver.class.getSimpleName();

    /** Called when this application is approved to be a device administrator. */
    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        Toast.makeText(context, "Admin enabled", Toast.LENGTH_LONG).show();
        Log.d(TAG, "Device Administration enabled");

        SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(context);
        //When the remote admin is enabled, we register the device in the admin panel
        new RegisterDeviceRemoteAdminTask(context, prefs).execute();
    }

    /** Called when this application is no longer the device administrator. */
    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        Toast.makeText(context, "Admin disabled", Toast.LENGTH_LONG).show();
        Log.d(TAG, "Device Administration disabled");

        SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PrefsActivity.PREF_REMOTE_ADMIN, false);
        editor.commit();
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        super.onPasswordChanged(context, intent);
        Log.d(TAG, "onPasswordChanged");
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        super.onPasswordFailed(context, intent);
        Log.d(TAG, "onPasswordFailed");
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        super.onPasswordSucceeded(context, intent);
        Log.d(TAG, "onPasswordSucceeded");
    }

}
