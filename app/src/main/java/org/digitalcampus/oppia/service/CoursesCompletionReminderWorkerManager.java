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
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.model.ActivityLogRepository;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.TrackerLogRepository;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.DateUtils;
import org.digitalcampus.oppia.utils.ui.OppiaNotificationUtils;
import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.List;

import androidx.work.ListenableWorker.Result;

import javax.inject.Inject;

public class CoursesCompletionReminderWorkerManager {

    private static final String TAG = CoursesCompletionReminderWorkerManager.class.getSimpleName();
    private final Context context;

    public static final int PERIOD_DAYS_REMINDER = 7;

    @Inject
    TrackerLogRepository trackerLogRepository;

    @Inject
    CoursesRepository coursesRepository;

    @Inject
    User user;

    @Inject
    SharedPreferences prefs;

    public CoursesCompletionReminderWorkerManager(@NonNull Context context) {
        this.context = context;
        initializeDaggerBase();
    }

    private void initializeDaggerBase() {
        App app = (App) context.getApplicationContext();
        app.getComponent().inject(this);
    }

    public Result checkCompletionReminder() {
        if (!isUserLoggedIn()) {
            return Result.success();
        }

        try {

            boolean anyActivityLastWeek = checkActivityLastWeek();
            boolean allCoursesCompleted = checkAllCoursesCompleted();
            if (!anyActivityLastWeek && !allCoursesCompleted) {
                showReminderNotification();
            }
        } catch (Exception e) {
            return Result.failure();
        }

        return Result.success();
    }

    private boolean isUserLoggedIn() {
        return user != null && !TextUtils.isEmpty(user.getUsername());
    }


    private boolean checkActivityLastWeek() throws Exception {

        String datetimeString = trackerLogRepository.getLastTrackerDatetime(context);

        if (!TextUtils.isEmpty(datetimeString)) {
            DateTime datetime = DateUtils.DATETIME_FORMAT.parseDateTime(datetimeString);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -PERIOD_DAYS_REMINDER);

            Log.i(TAG, "checkActivityLastWeek: last Tuesday date: "
                    + DateUtils.DATE_FORMAT.print(calendar.getTimeInMillis()));

            return datetime.isAfter(calendar.getTimeInMillis());

        }

        return false;
    }

    private boolean checkAllCoursesCompleted() throws IllegalStateException {

        String criteria = prefs.getString(PrefsActivity.PREF_BADGE_AWARD_CRITERIA, null);
        int percent = prefs.getInt(PrefsActivity.PREF_BADGE_AWARD_CRITERIA_PERCENT, 0);

        if (criteria == null || user == null) {
            // This should not happen
            throw new IllegalStateException("Wrong data");
        }

        List<Course> courses = coursesRepository.getCourses(context);

        for (Course courseDB : courses) {
            Course course = coursesRepository.getCourse(context, courseDB.getCourseId(), user.getUserId());
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
