package org.digitalcampus.oppia.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ListenableWorker.Result;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.StartUpActivity;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.fragments.prefs.NotificationsPrefsFragment;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.TrackerLogRepository;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.DateUtils;
import org.digitalcampus.oppia.utils.ReminderLogHelper;
import org.digitalcampus.oppia.utils.ui.OppiaNotificationUtils;
import org.joda.time.DateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class CoursesCompletionReminderWorkerManager {

    private static final String TAG = CoursesCompletionReminderWorkerManager.class.getSimpleName();
    public static final String EXTRA_GO_TO_NOTIFICATIONS_SETTINGS = "extra_go_to_notifications_settings";
    private final Context context;

    public static final int WEEK_DAYS_NUM = 7;
    public static final int ONE_DAY_NUM = 1;

    @Inject
    TrackerLogRepository trackerLogRepository;

    @Inject
    CoursesRepository coursesRepository;

//    @Inject
//    User user; #reminders-multi-user

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

        ReminderLogHelper reminderLogHelper = new ReminderLogHelper(context);
        String logEntry = "";

//        if (!isUserLoggedIn()) { #reminders-multi-user
//            logEntry += "User not logged in";
//            reminderLogHelper.saveLogEntry("WORKER STARTED", "\nConfiguration: \n" + getConfiguration() + "\n\n" + logEntry);
//            return Result.success();
//        }


        try {

            boolean anyActivityDone = checkActivityDone();
            boolean allCoursesCompleted = checkAllCoursesCompleted();
            boolean notify = !anyActivityDone && !allCoursesCompleted;
            if (notify) {
                showReminderNotification();
            }
            logEntry += "Notification displayed: " + notify + "\n"
                    + "anyActivityDone: " + anyActivityDone + "\n"
                    + "allCoursesCompleted: " + allCoursesCompleted;
        } catch (Exception e) {
            logEntry = "Error: " + e.getMessage();
            return Result.failure();
        } finally {
            reminderLogHelper.saveLogEntry("WORKER STARTED", "Configuration: \n" + getConfiguration() + "\n\n" + logEntry);
        }


        return Result.success();
    }

    private String getConfiguration() {
        boolean enabled = prefs.getBoolean(PrefsActivity.PREF_COURSES_REMINDER_ENABLED, true);
        String interval = prefs.getString(PrefsActivity.PREF_COURSES_REMINDER_INTERVAL, context.getString(R.string.prefCoursesReminderIntervalDefault));
        String time = prefs.getString(PrefsActivity.PREF_COURSES_REMINDER_TIME, context.getString(R.string.prefCoursesReminderTimeDefault));
        Set<String> dayCodes = prefs.getStringSet(PrefsActivity.PREF_COURSES_REMINDER_DAYS,
                new HashSet<>(Arrays.asList(context.getResources().getStringArray(R.array.days_of_week_values_default))));
        return String.format("Enabled: %s\nInterval: %s\nTime: %s\nDays: %s", enabled, interval, time, NotificationsPrefsFragment.getWeekDaysNames(context, dayCodes));
    }

//    private boolean isUserLoggedIn() { #reminders-multi-user
//        return user != null && !TextUtils.isEmpty(user.getUsername());
//    }


    private boolean checkActivityDone() throws Exception {

        String datetimeString = trackerLogRepository.getLastTrackerDatetime(context);

        if (!TextUtils.isEmpty(datetimeString)) {
            DateTime datetime = DateUtils.DATETIME_FORMAT.parseDateTime(datetimeString);

            String interval = prefs.getString(PrefsActivity.PREF_COURSES_REMINDER_INTERVAL, context.getString(R.string.prefCoursesReminderIntervalDefault));

            int daysBefore = TextUtils.equals(interval, context.getString(R.string.interval_weekly_value)) ? WEEK_DAYS_NUM : ONE_DAY_NUM;
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -daysBefore);
            // Not necessary to set the time in this calendar. This worker will be launched at the time configured.

            Log.i(TAG, "checkActivityLastWeek: last Tuesday date: "
                    + DateUtils.DATE_FORMAT.print(calendar.getTimeInMillis()));

            return datetime.isAfter(calendar.getTimeInMillis());

        }

        return false;
    }

    private boolean checkAllCoursesCompleted() throws IllegalStateException {

        String criteria = prefs.getString(PrefsActivity.PREF_BADGE_AWARD_CRITERIA, null);
        int percent = prefs.getInt(PrefsActivity.PREF_BADGE_AWARD_CRITERIA_PERCENT, 0);

        if (criteria == null/* || user == null*/) { // #reminders-multi-user
            // This should not happen
            throw new IllegalStateException("Wrong data");
        }

        List<Course> courses = coursesRepository.getCourses(context);

        List<User> users = DbHelper.getInstance(context).getAllUsers(); // #reminders-multi-user

        for (User user : users) {
            for (Course courseDB : courses) {
                Course course = coursesRepository.getCourse(context, courseDB.getCourseId(), user.getUserId());
                if (!course.isComplete(context, user, criteria, percent)) {
                    return false;
                }
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
        builder.setContentIntent(OppiaNotificationUtils.getActivityPendingIntent(context, StartUpActivity.class, null));

        Bundle extras = new Bundle();
        extras.putBoolean(EXTRA_GO_TO_NOTIFICATIONS_SETTINGS, true);
        builder.addAction(0, context.getString(R.string.courses_reminder_settings),
                OppiaNotificationUtils.getActivityPendingIntent(context, PrefsActivity.class, extras));

        OppiaNotificationUtils.sendNotification(context,
                OppiaNotificationUtils.NOTIF_ID_COURSES_REMINDER, builder.build());

    }

    public static void configureCoursesCompletionReminderWorker(Context context) {
        configureCoursesCompletionReminderWorker(context, ExistingPeriodicWorkPolicy.REPLACE);
    }

    public static void configureCoursesCompletionReminderWorker(Context context, ExistingPeriodicWorkPolicy policy) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean enabled = prefs.getBoolean(PrefsActivity.PREF_COURSES_REMINDER_ENABLED, true);
        if (enabled) {
            scheduleCoursesCompletionReminderWorker(context, policy);
        } else {
            cancelAllReminderWorkers(context);
        }
    }

    public static void cancelAllReminderWorkers(Context context) {

        String[] allDaysWeek = context.getResources().getStringArray(R.array.days_of_week_values);
        for (String dayOfWeek : allDaysWeek) {
            cancelWeeklyReminderWorker(context, dayOfWeek);
        }
    }

    private static void scheduleCoursesCompletionReminderWorker(Context context, ExistingPeriodicWorkPolicy policy) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        Set<String> defaultReminderDays = new HashSet<>(Arrays.asList(
                context.getResources().getStringArray(R.array.days_of_week_values_default)));
        Set<String> daysReminder = prefs.getStringSet(PrefsActivity.PREF_COURSES_REMINDER_DAYS, defaultReminderDays);
        String time = prefs.getString(PrefsActivity.PREF_COURSES_REMINDER_TIME, context.getString(R.string.prefCoursesReminderTimeDefault));

        String[] allDaysWeek = context.getResources().getStringArray(R.array.days_of_week_values);
        for (String dayOfWeek : allDaysWeek) {
            if (daysReminder.contains(dayOfWeek)) {
                scheduleWeeklyReminderWorker(context, dayOfWeek, time, policy);
            } else {
                cancelWeeklyReminderWorker(context, dayOfWeek);
            }
        }

    }

    /**
     * Same process for weekly or daily intervals. The difference is inside the worker when reminder criteria is processed.
     */
    private static void scheduleWeeklyReminderWorker(Context context, String dayOfWeek, String time, ExistingPeriodicWorkPolicy policy) {

        Calendar calendarTime = Calendar.getInstance();
        try {
            Date timeParsed = new SimpleDateFormat("HH:mm").parse(time);
            calendarTime.setTime(timeParsed);
        } catch (ParseException e) {
            Analytics.logException(e);
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Integer.parseInt(dayOfWeek));
        calendar.set(Calendar.HOUR_OF_DAY, calendarTime.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MINUTE, calendarTime.get(Calendar.MINUTE));
        calendar.set(Calendar.SECOND, 0);

        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DAY_OF_MONTH, 7);
        }

        long delayFromNow = calendar.getTimeInMillis() - System.currentTimeMillis();

        PeriodicWorkRequest coursesCompletionReminder = new PeriodicWorkRequest.Builder(CoursesCompletionReminderWorker.class,
                CoursesCompletionReminderWorkerManager.WEEK_DAYS_NUM, TimeUnit.DAYS)
                .setInitialDelay(delayFromNow, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                App.WORK_COURSES_NOT_COMPLETED_REMINDER_ + dayOfWeek, policy, coursesCompletionReminder);

    }

    private static void cancelWeeklyReminderWorker(Context context, String dayOfWeek) {
        ((App) context.getApplicationContext()).cancelWorks(App.WORK_COURSES_NOT_COMPLETED_REMINDER_ + dayOfWeek);
    }

}
