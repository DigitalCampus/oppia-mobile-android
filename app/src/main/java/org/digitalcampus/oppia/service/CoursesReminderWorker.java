package org.digitalcampus.oppia.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.utils.ui.OppiaNotificationUtils;

public class CoursesReminderWorker extends Worker {

    public CoursesReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        if (!SessionManager.isLoggedIn(getApplicationContext())) {
            return Result.success();
        }

        boolean anyActivityLastWeek = checkActivityLastWeek();
        boolean allCoursesNotCompleted = checkAllCoursesCompleted();
        if (!anyActivityLastWeek && !allCoursesNotCompleted) {
            showReminderNotification();
        }

        return Result.success();
    }


    private boolean checkActivityLastWeek() {
        return false;
    }

    private boolean checkAllCoursesCompleted() {
        return false;
    }

    private void showReminderNotification() {
        NotificationCompat.Builder builder = OppiaNotificationUtils.getBaseBuilder(getApplicationContext(), true);
        builder.setContentTitle("Courses reminder");
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText("You have not completed all your courses yet.\nGo to your courses"));
        builder.setContentIntent(OppiaNotificationUtils.getMainActivityPendingIntent(getApplicationContext()));



// Create an Intent for the activity you want to start
        Intent resultIntent = new Intent(getApplicationContext(), PrefsActivity.class);
// Create the TaskStackBuilder and add the intent, which inflates the back stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(getApplicationContext());
        stackBuilder.addNextIntentWithParentStack(resultIntent);
// Get the PendingIntent containing the entire back stack
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(0, "Reminder settings", resultPendingIntent);
//
//        builder.addAction(0, "Go to your courses", reminderSettingsPendingIntent);

        OppiaNotificationUtils.sendNotification(getApplicationContext(),
                OppiaNotificationUtils.NOTIF_ID_COURSES_REMINDER, builder.build());

    }
}
