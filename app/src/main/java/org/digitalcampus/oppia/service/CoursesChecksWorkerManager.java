package org.digitalcampus.oppia.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.DownloadActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.fragments.CoursesListFragment;
import org.digitalcampus.oppia.listener.APIRequestFinishListener;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.model.responses.CourseServer;
import org.digitalcampus.oppia.model.responses.CoursesServerResponse;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.CourseUtils;
import org.digitalcampus.oppia.utils.ui.OppiaNotificationUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import androidx.core.app.NotificationCompat;

/**
 * Class isolated for unit testing purposes
 */
public class CoursesChecksWorkerManager implements APIRequestFinishListener, APIRequestListener {

    public static final String TAG = CoursesChecksWorkerManager.class.getSimpleName();

    private Context context;
    private int pendingChecks;
    private OnFinishListener onFinishListener;

    @Inject
    CoursesRepository coursesRepository;

    @Inject
    User user;

    @Inject
    SharedPreferences prefs;
    private List<Course> coursesInstalled;

    public CoursesChecksWorkerManager(Context context) {
        this.context = context;
        initializeDaggerBase();
    }

    private void initializeDaggerBase() {
        App app = (App) context.getApplicationContext();
        app.getComponent().inject(this);
    }

    public interface OnFinishListener {
        void onFinish(String message);
    }

    public void setOnFinishListener(OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }

    private boolean isUserLoggedIn() {
        return user != null && !TextUtils.isEmpty(user.getUsername());
    }

    public void startChecks() {


        if (isUserLoggedIn()) {

            pendingChecks = 2;

            coursesInstalled = coursesRepository.getCourses(context);

            checkNoCoursesInstalled();
            checkCoursesUpdates();

        } else {

            if (onFinishListener != null) {
                onFinishListener.onFinish(getString(R.string.user_not_logged_in));
            }
        }

    }


    public void checkNoCoursesInstalled() {

        if (!isUserLoggedIn()) {
            // Duplicate user logged in check needed to test individually
            return;
        }

        List<Course> courses = coursesRepository.getCourses(context);
        if (courses.size() < App.DOWNLOAD_COURSES_DISPLAY) {

            NotificationCompat.Builder mBuilder = OppiaNotificationUtils.getBaseBuilder(context, true);
            mBuilder
                    .setContentTitle(context.getString(R.string.notification_course_download_title))
                    .setContentText(context.getString(R.string.notification_course_download_text))
                    .setContentIntent(OppiaNotificationUtils.getActivityPendingIntent(context, TagSelectActivity.class, null));

            OppiaNotificationUtils.sendNotification(context, OppiaNotificationUtils.NOTIF_ID_COURSES_NOT_INSTALLED, mBuilder.build());
        }

        onRequestFinish(null);
    }


    public void checkCoursesUpdates() {

        if (!isUserLoggedIn()) {
            // Duplicate user logged in check needed to test individually
            return;
        }

        APIUserRequestTask task = new APIUserRequestTask(context);
        String url = Paths.SERVER_COURSES_PATH;
        task.setAPIRequestListener(this);
        task.setAPIRequestFinishListener(this, "APIUserRequestTask");
        task.execute(url);

    }


    @Override
    public void apiRequestComplete(BasicResult result) {

        if (result.isSuccess()) {

            try {

                CoursesServerResponse coursesServerResponse = new Gson().fromJson(
                        result.getResultMessage(), CoursesServerResponse.class);

                prefs.edit()
                        .putLong(PrefsActivity.PREF_LAST_COURSES_CHECKS_SUCCESSFUL_TIME, System.currentTimeMillis())
                        .putString(PrefsActivity.PREF_SERVER_COURSES_CACHE, result.getResultMessage())
                        .commit();


                checkUpdatedOrDeletedCoursesAndNotify();
                checkNewCoursesAndNotify(coursesServerResponse.getCourses());


                double mostRecentVersionTimestamp = getMostRecentVersionTimestamp(coursesServerResponse.getCourses());
                long mostRecentVersionTimestampLong = (long) mostRecentVersionTimestamp;

                Log.i(TAG, "apiRequestComplete: mostRecentVersionTimestampLong: " + mostRecentVersionTimestampLong);

                prefs.edit()
                        .putLong(PrefsActivity.PREF_LAST_COURSE_VERSION_TIMESTAMP_CHECKED, mostRecentVersionTimestampLong)
                        .commit();

                final long lastNewCourseSeenTimestamp = prefs.getLong(PrefsActivity.PREF_LAST_NEW_COURSE_SEEN_TIMESTAMP, 0);
                if (lastNewCourseSeenTimestamp == 0) {
                    prefs.edit().putLong(PrefsActivity.PREF_LAST_NEW_COURSE_SEEN_TIMESTAMP, mostRecentVersionTimestampLong).commit();
                }

            } catch (JsonSyntaxException e) {
                Analytics.logException(e);
                Log.d(TAG, "JSON error: ", e);
            }

        }
    }

    private void checkUpdatedOrDeletedCoursesAndNotify() {

        double lastVersionTimestampChecked = Double.longBitsToDouble(
                prefs.getLong(PrefsActivity.PREF_LAST_COURSE_VERSION_TIMESTAMP_CHECKED, 0));

        Log.i(TAG, "checkUpdatedOrDeletedCoursesAndNotify: lastVersionTimestampChecked: " + lastVersionTimestampChecked);

        if (lastVersionTimestampChecked == 0) {
            // First time app starts must not notify any "new courses"
            return;
        }

        CourseUtils.setSyncStatus(prefs, coursesInstalled, lastVersionTimestampChecked);

        int toUpdateCount = 0;

        for (Course course : coursesInstalled) {
            if (course.isToUpdate()) {
                toUpdateCount++;
            }
        }

        Log.i(TAG, "checkUpdatedOrDeletedCoursesAndNotify: toUpdateCount: " + toUpdateCount);

        if (toUpdateCount > 0) {
            showToUpdateNotification(toUpdateCount);
            context.sendBroadcast(new Intent(CoursesListFragment.ACTION_COURSES_UPDATES));
        }


    }

    private void checkNewCoursesAndNotify(List<CourseServer> coursesServer) {

        Set<String> newCoursesNotified = prefs.getStringSet(PrefsActivity.PREF_NEW_COURSES_LIST_NOTIFIED, null);

        Log.i(TAG, "checkNewCoursesAndNotify: newCoursesNotified count: " +
                (newCoursesNotified != null ? newCoursesNotified.size()+"" : "null"));

        if (newCoursesNotified == null) {

            // First time. No need to notify courses, just save them all
            Set<String> notInstalledCoursesShortnames = new HashSet<>();
            for (CourseServer courseServer : coursesServer) {
                notInstalledCoursesShortnames.add(courseServer.getShortname());
            }

            prefs.edit().putStringSet(PrefsActivity.PREF_NEW_COURSES_LIST_NOTIFIED, notInstalledCoursesShortnames).commit();

        } else {

            List<CourseServer> notInstalledCourses = CourseUtils.getNotInstalledCourses(prefs, coursesInstalled);

            Set<String> notInstalledAndNotNotifiedCourses = new HashSet<>();
            for (CourseServer courseServer : notInstalledCourses) {
                if (!newCoursesNotified.contains(courseServer.getShortname())) {
                    notInstalledAndNotNotifiedCourses.add(courseServer.getShortname());
                }
            }

            Log.i(TAG, "checkNewCoursesAndNotify: notInstalledAndNotNotifiedCourses: " + notInstalledAndNotNotifiedCourses);

            if (!notInstalledAndNotNotifiedCourses.isEmpty()) {
                showNewCoursesNotification(notInstalledAndNotNotifiedCourses.size());
            }

            newCoursesNotified.addAll(notInstalledAndNotNotifiedCourses);
            prefs.edit().putStringSet(PrefsActivity.PREF_NEW_COURSES_LIST_NOTIFIED, newCoursesNotified).commit();
        }

    }

    private void showNewCoursesNotification(int newCoursesCount) {

        Bundle extras = new Bundle();
        extras.putInt(DownloadActivity.EXTRA_MODE, DownloadActivity.MODE_NEW_COURSES);

        String contentText = context.getResources().getQuantityString(
                R.plurals.notification_new_courses_text, newCoursesCount, newCoursesCount);

        NotificationCompat.Builder mBuilder = OppiaNotificationUtils.getBaseBuilder(context, true);
        mBuilder
                .setContentTitle(getString(R.string.notification_new_courses_title))
                .setContentText(contentText)
                .setContentIntent(OppiaNotificationUtils.getActivityPendingIntent(context, DownloadActivity.class, extras));

        OppiaNotificationUtils.sendNotification(context, OppiaNotificationUtils.NOTIF_ID_NEW_COURSES, mBuilder.build());
    }

    private void showToUpdateNotification(int toUpdateCount) {

        String contentText = context.getResources().getQuantityString(
                R.plurals.notification_courses_to_update_text, toUpdateCount, toUpdateCount);

        NotificationCompat.Builder mBuilder = OppiaNotificationUtils.getBaseBuilder(context, true);
        mBuilder
                .setContentTitle(getString(R.string.notification_courses_to_update_title))
                .setContentText(contentText)
                .setContentIntent(OppiaNotificationUtils.getMainActivityPendingIntent(context));

        OppiaNotificationUtils.sendNotification(context, OppiaNotificationUtils.NOTIF_ID_TO_UPDATE, mBuilder.build());
    }

    private double getMostRecentVersionTimestamp(List<CourseServer> coursesServer) {
        double mostRecentVersionTimestamp = 0;
        for (CourseServer course : coursesServer) {
            mostRecentVersionTimestamp = Math.max(mostRecentVersionTimestamp, course.getVersion());
        }
        return mostRecentVersionTimestamp;
    }

    private String getString(int stringId) {
        return context.getString(stringId);
    }

    @Override
    public void onRequestFinish(String idRequest) {

        pendingChecks--;
        Log.i(TAG, "onRequestFinish: pendingChecks: " + pendingChecks);

        if ((pendingChecks == 0) && (onFinishListener != null)){
            onFinishListener.onFinish(null);
        }
    }

    @Override
    public void apiKeyInvalidated() {
        SessionManager.logoutCurrentUser(context);
    }
}
