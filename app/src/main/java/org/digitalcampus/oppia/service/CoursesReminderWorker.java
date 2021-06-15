package org.digitalcampus.oppia.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
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
        builder.setContentTitle(getApplicationContext().getString(R.string.courses_reminder_notif_title));
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(getApplicationContext().getString(R.string.courses_reminder_notif_text)));
        builder.setContentText(getApplicationContext().getString(R.string.courses_reminder_notif_text));
        builder.setContentIntent(OppiaNotificationUtils.getMainActivityPendingIntent(getApplicationContext()));

        builder.addAction(0, getApplicationContext().getString(R.string.courses_reminder_settings),
                OppiaNotificationUtils.getActivityPendingIntent(getApplicationContext(), PrefsActivity.class, null));

        OppiaNotificationUtils.sendNotification(getApplicationContext(),
                OppiaNotificationUtils.NOTIF_ID_COURSES_REMINDER, builder.build());

    }
}
