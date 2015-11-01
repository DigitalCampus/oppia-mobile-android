package org.digitalcampus.oppia.application;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.android.gms.gcm.GcmListenerService;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.StartUpActivity;
import org.digitalcampus.oppia.utils.ui.OppiaNotificationBuilder;

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
            if (type == null) return;
            else if (type.equals(TYPE_ADMIN)){

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

        NotificationCompat.Builder notifBuilder = OppiaNotificationBuilder.getBaseBuilder(this, true);
        notifBuilder
            .setContentTitle(getString(R.string.notification_remote_admin_title))
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent).setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .build();

        notifBuilder.setSmallIcon(R.drawable.ic_notification);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int color = 0;
            /*if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                color = getResources().getColor(R.color.highlight_light, null);
            }
            else{*/
                color = getResources().getColor(R.color.highlight_light);
            //}
            notifBuilder.setColor(color);
        }
        else{
            notifBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), MobileLearning.APP_LOGO));
        }

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notifBuilder.build());
    }
}
