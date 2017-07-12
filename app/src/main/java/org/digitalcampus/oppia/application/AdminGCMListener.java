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

package org.digitalcampus.oppia.application;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.StartUpActivity;
import org.digitalcampus.oppia.utils.ui.OppiaNotificationUtils;

@SuppressLint("NewApi")
public class AdminGCMListener extends GcmListenerService {

    private static final String TAG = AdminGCMListener.class.getSimpleName();
    private static final int NOTIFICATION_ID = 100;
    private static final String MESSAGE_TYPE = "type";
    private static final String MESSAGE_ACTION = "action";
    private static final String MESSAGE_PASSWORD = "password";
    private static final String TYPE_ADMIN = "admin";
    private static final String ACTION_DISABLE_CAMERA = "disable_camera";
    private static final String ACTION_ENABLE_CAMERA = "enable_camera";
    private static final String ACTION_PASSWORD_LOCK = "password_lock";

    @Override
    public void onMessageReceived(String from, Bundle messageData) {
        Log.d(TAG, "Push message received from: " + from);

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            String type = messageData.getString(MESSAGE_TYPE);
            if ((type != null) && (type.equals(TYPE_ADMIN))){

                if (!BuildConfig.FLAVOR.equals("admin")) {
                    //Is not the admin-flavor app (we don't have the permission, would produce crash)
                    Log.d(TAG, "Device Administration is disabled :(");
                    return;
                }

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                boolean adminEnabled = prefs.getBoolean(PrefsActivity.PREF_REMOTE_ADMIN, false);
                //First, we need to check if admin option is enabled
                if (!adminEnabled){
                    Log.d(TAG, "Device Administration is disabled :(");
                    return;
                }

                String action = messageData.getString(MESSAGE_ACTION);
                Log.d(TAG, "Remote admin action: " + action);
                if (ACTION_DISABLE_CAMERA.equals(action)){
                    ComponentName adminReceiver = new ComponentName(this, AdminReceiver.class);
                    DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    policyManager.setCameraDisabled(adminReceiver, true);
                    sendNotification(getString(R.string.notification_remote_admin_camera_disabled));
                }
                else if (ACTION_ENABLE_CAMERA.equals(action)){
                    ComponentName adminReceiver = new ComponentName(this, AdminReceiver.class);
                    DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    policyManager.setCameraDisabled(adminReceiver, false);
                    sendNotification(getString(R.string.notification_remote_admin_camera_enabled));
                }
                else if (ACTION_PASSWORD_LOCK.equals(action)){
                    String password = messageData.getString(MESSAGE_PASSWORD);
                    if ((password != null) && !password.equals("")){
                        DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                        policyManager.resetPassword(password, DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
                        policyManager.lockNow();
                    }
                }
            }

        }

    }

    private void sendNotification(String message) {
        Intent intent = new Intent(this, StartUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder notifBuilder = OppiaNotificationUtils.getBaseBuilder(this, true);
        notifBuilder
                .setContentTitle(getString(R.string.notification_remote_admin_title))
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .build();

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        OppiaNotificationUtils.sendNotification(this, notificationManager, NOTIFICATION_ID, notifBuilder.build());
    }
}
