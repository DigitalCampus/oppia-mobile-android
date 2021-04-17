package org.digitalcampus.oppia.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.listener.APIRequestFinishListener;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.model.responses.CourseServer;
import org.digitalcampus.oppia.model.responses.CoursesServerResponse;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.CourseUtils;
import org.digitalcampus.oppia.utils.ui.OppiaNotificationUtils;

import java.util.List;

import javax.inject.Inject;

/**
 * Class isolated for unit testing purposes
 */
public class CoursesChecksWorkerManager implements APIRequestFinishListener, APIRequestListener {

    public static final String TAG = CoursesChecksWorkerManager.class.getSimpleName();

    private static final int ID_NOTIF_TO_UPDATE = 1;
    private static final int ID_NOTIF_TO_DELETE = 2;
    private static final int ID_NOTIF_NEW_COURSES = 3;

    private Context context;
    private int pendingChecks;
    private OnFinishListener onFinishListener;

    @Inject
    CoursesRepository coursesRepository;

    @Inject
    User user;

    @Inject
    SharedPreferences prefs;

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
        if (courses.size() < App.DOWNLOAD_COURSES_DISPLAY){
            Intent resultIntent = new Intent(context, TagSelectActivity.class);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder = OppiaNotificationUtils.getBaseBuilder(context, true);
            mBuilder
                    .setContentTitle(context.getString(R.string.notification_course_download_title))
                    .setContentText(context.getString(R.string.notification_course_download_text))
                    .setContentIntent(resultPendingIntent);
            int mId = 002;

            OppiaNotificationUtils.sendNotification(context, mId, mBuilder.build());
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
    public void apiRequestComplete(Payload response) {

        if (response.isResult()) {

            try {


                CoursesServerResponse coursesServerResponse = new Gson().fromJson(
                        response.getResultResponse(), CoursesServerResponse.class);

                double mostRecentVersionTimestamp = getMostRecentVersionTimestamp(coursesServerResponse);
                long mostRecentVersionTimestampLong = Double.doubleToRawLongBits(mostRecentVersionTimestamp);

                prefs.edit()
                        .putLong(PrefsActivity.PREF_LAST_COURSE_VERSION_TIMESTAMP_CHECKED, mostRecentVersionTimestampLong)
                        .putLong(PrefsActivity.PREF_LAST_COURSES_CHECKS_SUCCESSFUL, System.currentTimeMillis())
                        .putString(PrefsActivity.PREF_SERVER_COURSES_CACHE, response.getResultResponse())
                        .commit();

                groupCoursesBySyncStatusAndNotify();


            } catch (JsonSyntaxException e) {
                Mint.logException(e);
                Log.d(TAG, "JSON error: ", e);
            }

        }
    }

    private void groupCoursesBySyncStatusAndNotify() {

        double lastVersionTimestampChecked = Double.longBitsToDouble(
                prefs.getLong(PrefsActivity.PREF_LAST_COURSE_VERSION_TIMESTAMP_CHECKED, 0));

        // TODO oppia-577 remove
        lastVersionTimestampChecked = 0;

        List<Course> courses = coursesRepository.getCourses(context);
        CourseUtils.setSyncStatus(prefs, courses, lastVersionTimestampChecked);

        int toUpdateCount = 0;
        int toDeleteCount = 0;

        for (Course course : courses) {
            if (course.isToDelete()) {
                toDeleteCount++;
            } else if (course.isToUpdate()) {
                toUpdateCount++;
            }
        }

        if (toUpdateCount > 0) {
            showToUpdateNotification(toUpdateCount);
        }

        if (toDeleteCount > 0) {
            showToDeleteNotification(toDeleteCount);
        }

    }

    private void showToUpdateNotification(int toUpdateCount) {

        Intent resultIntent = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String contentText = context.getResources().getQuantityString(
                R.plurals.notification_courses_to_update_text, toUpdateCount, toUpdateCount);

        NotificationCompat.Builder mBuilder = OppiaNotificationUtils.getBaseBuilder(context, true);
        mBuilder
                .setContentTitle(getString(R.string.notification_courses_to_update_title))
                .setContentText(contentText)
                .setContentIntent(resultPendingIntent);

        OppiaNotificationUtils.sendNotification(context, ID_NOTIF_TO_UPDATE, mBuilder.build());
    }

    private void showToDeleteNotification(int toDeleteCount) {

        Intent resultIntent = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String contentText = context.getResources().getQuantityString(
                R.plurals.notification_courses_to_delete_text, toDeleteCount, toDeleteCount);

        NotificationCompat.Builder mBuilder = OppiaNotificationUtils.getBaseBuilder(context, true);
        mBuilder
                .setContentTitle(getString(R.string.notification_courses_to_delete_title))
                .setContentText(contentText)
                .setContentIntent(resultPendingIntent);

        OppiaNotificationUtils.sendNotification(context, ID_NOTIF_TO_DELETE, mBuilder.build());
    }

    private double getMostRecentVersionTimestamp(CoursesServerResponse coursesServerResponse) {
        double mostRecentVersionTimestamp = 0;
        for(CourseServer course : coursesServerResponse.getCourses()) {
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

        if (pendingChecks == 0) {
            if (onFinishListener != null) {
                onFinishListener.onFinish(null);
            }
        }
    }

    @Override
    public void apiKeyInvalidated() {
        SessionManager.logoutCurrentUser(context);
    }
}
