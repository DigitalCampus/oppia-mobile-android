package org.digitalcampus.oppia.utils.ui;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import androidx.core.app.NotificationCompat;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;

import static android.content.Context.NOTIFICATION_SERVICE;

public class OppiaNotificationUtils {

    public static final String CHANNEL_INTERNAL_NOTIFICATIONS = "channel_internal_notifications";
    public static final int NOTIF_ID_SIMPLE_MESSAGE = 0;

    private OppiaNotificationUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void initializeOreoNotificationChannels(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            createChannel(context, CHANNEL_INTERNAL_NOTIFICATIONS,
                    R.string.channel_notif_internal_name,
                    R.string.channel_notif_internal_description);

        }

    }

    @SuppressLint("NewApi")
    private static void createChannel(Context context, String channelId, int nameStringId, int descriptionStringId) {

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

        if (notificationManager.getNotificationChannel(channelId) == null) {
            CharSequence name = context.getString(nameStringId);
            String description = context.getString(descriptionStringId);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static NotificationCompat.Builder getBaseBuilder(Context ctx, boolean setAutoCancel){
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(ctx, CHANNEL_INTERNAL_NOTIFICATIONS);
        notifBuilder.setSound(defaultSoundUri);
        notifBuilder.setAutoCancel(setAutoCancel);
        notifBuilder.setSmallIcon(R.drawable.ic_notification);
        notifBuilder.setColor(ContextCompat.getColor(ctx, R.color.theme_primary));

        return notifBuilder;
    }

    public static void sendNotification(Context ctx, int id, Notification notification){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean notificationsDisabled = prefs.getBoolean(PrefsActivity.PREF_DISABLE_NOTIFICATIONS, false);
        if(!notificationsDisabled) {
            NotificationManager notificationManager = (NotificationManager) ctx.getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(id, notification);
        }
    }

    public static void sendSimpleMessage(Context ctx, boolean setAutoCancel, String message){
        NotificationCompat.Builder mBuilder  = OppiaNotificationUtils.getBaseBuilder(ctx, setAutoCancel);
        mBuilder.setContentTitle(ctx.getString(R.string.app_name)).setContentText(message).build();
        OppiaNotificationUtils.sendNotification(ctx, NOTIF_ID_SIMPLE_MESSAGE, mBuilder.build());
    }
}
