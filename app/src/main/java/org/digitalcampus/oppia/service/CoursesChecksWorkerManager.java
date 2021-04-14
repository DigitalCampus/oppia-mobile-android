package org.digitalcampus.oppia.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.DownloadActivity;
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
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.ui.OppiaNotificationUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import javax.inject.Inject;

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
        boolean updateAvailable = false;

        if (response.isResult()) {

            double lastVersionTimestampChecked = Double.longBitsToDouble(
                    prefs.getLong(PrefsActivity.PREF_LAST_COURSE_VERSION_TIMESTAMP_CHECKED, 0));

            double mostRecentVersionTimestamp = 0;

            try {

                JSONObject json = new JSONObject(response.getResultResponse());
                Log.d(TAG, json.toString(4));
                DbHelper db = DbHelper.getInstance(context);
                for (int i = 0; i < (json.getJSONArray("courses").length()); i++) {
                    JSONObject jsonObj = (JSONObject) json.getJSONArray("courses").get(i);
                    String shortName = jsonObj.getString("shortname");
                    Double version = jsonObj.getDouble("version");

                    if (version <= lastVersionTimestampChecked) {
                        continue;
                    }

                    if (db.toUpdate(shortName, version)) {
                        updateAvailable = true;
                    }

                    mostRecentVersionTimestamp = Math.max(mostRecentVersionTimestamp, version);
                }

            } catch (JSONException e) {
                Mint.logException(e);
                Log.d(TAG, "JSON error: ", e);
            }

            if (updateAvailable) {
                Intent resultIntent = new Intent(context, DownloadActivity.class);
                PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder mBuilder = OppiaNotificationUtils.getBaseBuilder(context, true);
                mBuilder
                        .setContentTitle(getString(R.string.notification_course_update_title))
                        .setContentText(getString(R.string.notification_course_update_text))
                        .setContentIntent(resultPendingIntent);
                int mId = 001;

                OppiaNotificationUtils.sendNotification(context, mId, mBuilder.build());
            }

            long mostRecentVersionTimestampLong = Double.doubleToRawLongBits(mostRecentVersionTimestamp);
            prefs.edit()
                    .putLong(PrefsActivity.PREF_LAST_COURSE_VERSION_TIMESTAMP_CHECKED, mostRecentVersionTimestampLong)
                    .putLong(PrefsActivity.PREF_LAST_COURSES_CHECKS_SUCCESSFUL, System.currentTimeMillis())
                    .putString(PrefsActivity.PREF_SERVER_COURSES_CACHE, response.getResultResponse())
                    .apply();


        }
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
