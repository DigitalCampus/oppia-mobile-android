package org.digitalcampus.oppia.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import androidx.work.ListenableWorker;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.DateUtils;
import org.digitalcampus.oppia.utils.ui.OppiaNotificationUtils;
import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.List;

import androidx.work.ListenableWorker.Result;

public class CoursesCompletionReminderWorkerManager {

    private static final String TAG = CoursesCompletionReminderWorkerManager.class.getSimpleName();
    private final Context context;

    public CoursesCompletionReminderWorkerManager(@NonNull Context context) {
        this.context = context;
    }

    public Result checkCompletionReminder() {
        if (!SessionManager.isLoggedIn(context)) {
            return Result.success();
        }

        try {

            boolean anyActivityLastWeek = checkActivityLastWeek();
            boolean allCoursesCompleted = checkAllCoursesCompleted();
            if (!anyActivityLastWeek && !allCoursesCompleted) {
                showReminderNotification();
            }
        } catch (IllegalStateException e) {
            return Result.failure();
        }

        return Result.success();
    }
    


    private boolean checkActivityLastWeek() {
        DbHelper db = DbHelper.getInstance(context);
        User user = null;
        try {
            user = db.getUser(SessionManager.getUsername(context));
        } catch (UserNotFoundException e) {
            e.printStackTrace();
            throw new IllegalStateException("Wrong data");
        }

        String datetimeString = db.getLastTrackerDatetime(user.getUserId());
        if (!TextUtils.isEmpty(datetimeString)) {
            DateTime datetime = DateUtils.DATETIME_FORMAT.parseDateTime(datetimeString);

            // Calculate delay for last Tuesday at 10am (default date for these reminders)
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
            calendar.set(Calendar.HOUR_OF_DAY, 10);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);

            int todayDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

            if (calendar.get(Calendar.DAY_OF_WEEK) == todayDayOfWeek) {
                calendar.add(Calendar.DAY_OF_MONTH, -7);
            }

            Log.i(TAG, "checkActivityLastWeek: last Tuesday date: "
                    + DateUtils.DATE_FORMAT.print(calendar.getTimeInMillis()));

            return datetime.isAfter(calendar.getTimeInMillis());

        }

        return false;
    }

    private boolean checkAllCoursesCompleted() throws IllegalStateException {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String criteria = prefs.getString(PrefsActivity.PREF_BADGE_AWARD_CRITERIA, null);
        int percent = prefs.getInt(PrefsActivity.PREF_BADGE_AWARD_CRITERIA_PERCENT, 0);

        DbHelper db = DbHelper.getInstance(context);
        User user = null;
        try {
            user = db.getUser(SessionManager.getUsername(context));
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }

        if (criteria == null || user == null) {
            // This should not happen
            throw new IllegalStateException("Wrong data");
        }

        List<Course> courses = db.getAllCourses();

        for (Course course : courses) {
            if (!course.isComplete(context, user, criteria, percent)) {
                return false;
            }
        }

        return true;
    }

    private void showReminderNotification() {
        NotificationCompat.Builder builder = OppiaNotificationUtils.getBaseBuilder(context, true);
        builder.setContentTitle(context.getString(R.string.courses_reminder_notif_title));
        builder.setStyle(new NotificationCompat.BigTextStyle()
                .bigText(context.getString(R.string.courses_reminder_notif_text)));
        builder.setContentText(context.getString(R.string.courses_reminder_notif_text));
        builder.setContentIntent(OppiaNotificationUtils.getMainActivityPendingIntent(context));

        builder.addAction(0, context.getString(R.string.courses_reminder_settings),
                OppiaNotificationUtils.getActivityPendingIntent(context, PrefsActivity.class, null));

        OppiaNotificationUtils.sendNotification(context,
                OppiaNotificationUtils.NOTIF_ID_COURSES_REMINDER, builder.build());

    }

}
