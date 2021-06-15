package org.digitalcampus.oppia.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.App;
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

import okhttp3.Request;

public class CoursesNotCompletedReminderWorker extends Worker {

    private static final String TAG = CoursesNotCompletedReminderWorker.class.getSimpleName();

    public CoursesNotCompletedReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        if (!SessionManager.isLoggedIn(getApplicationContext())) {
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
        DbHelper db = DbHelper.getInstance(getApplicationContext());
        User user = null;
        try {
            user = db.getUser(SessionManager.getUsername(getApplicationContext()));
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String criteria = prefs.getString(PrefsActivity.PREF_BADGE_AWARD_CRITERIA, null);
        int percent = prefs.getInt(PrefsActivity.PREF_BADGE_AWARD_CRITERIA_PERCENT, 0);

        DbHelper db = DbHelper.getInstance(getApplicationContext());
        User user = null;
        try {
            user = db.getUser(SessionManager.getUsername(getApplicationContext()));
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }

        if (criteria == null || user == null) {
            // This should not happen
            throw new IllegalStateException("Wrong data");
        }

        List<Course> courses = db.getAllCourses();

        for (Course course : courses) {
            if (!course.isComplete(getApplicationContext(), user, criteria, percent)) {
                return false;
            }
        }

        return true;
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
