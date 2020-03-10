package org.digitalcampus.oppia.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import androidx.work.impl.utils.futures.SettableFuture;

import com.google.common.util.concurrent.ListenableFuture;
import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.DownloadActivity;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.APIRequestFinishListener;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.FetchServerInfoTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.SubmitQuizAttemptsTask;
import org.digitalcampus.oppia.task.SubmitTrackerMultipleTask;
import org.digitalcampus.oppia.utils.ui.OppiaNotificationUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressLint("RestrictedApi")
public class TrackerWorker extends ListenableWorker implements APIRequestFinishListener, APIRequestListener {


    private static final String TAG = TrackerWorker.class.getSimpleName();
    private SettableFuture<Result> future;
    private int pendingTasks;

    public TrackerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {

        Log.i(TAG, "startWork");

        future = SettableFuture.create();

        boolean isLoggedIn = SessionManager.isLoggedIn(getApplicationContext());
        if (isLoggedIn) {
            updateTracking();
        } else {
            Log.i(TAG, "startWork: user not logged in. exiting TrakerWorker");
            future.set(Result.success());
        }

        return future;
    }

    @Override
    public void onStopped() {
        super.onStopped();
        Log.i(TAG, "onStopped");
    }

    public void updateTracking() {

        pendingTasks = 4;

        // Update server info
        FetchServerInfoTask fetchServerInfoTask = new FetchServerInfoTask(getApplicationContext());
        fetchServerInfoTask.setAPIRequestFinishListener(this, "FetchServerInfoTask");
        fetchServerInfoTask.execute();
        Log.i(TAG, "updateTracking: FetchServerInfoTask executed");

        // check for updated courses
        // should only do this once a day or so....
        SharedPreferences prefs = App.getPrefs(getApplicationContext());
        long lastRun = prefs.getLong("lastCourseUpdateCheck", 0);
        long now = System.currentTimeMillis() / 1000;
        if ((lastRun + (TimeUnit.HOURS.toSeconds(12))) < now) {
            APIUserRequestTask task = new APIUserRequestTask(getApplicationContext());
            Payload p = new Payload(Paths.SERVER_COURSES_PATH);
            task.setAPIRequestListener(this);
            task.setAPIRequestFinishListener(this, "APIUserRequestTask");
            task.execute(p);

            prefs.edit().putLong("lastCourseUpdateCheck", now).apply();
        } else {
            onRequestFinish(null);
        } 

        // send activity trackers
        Log.d(TAG, "Submitting trackers multiple task");
        SubmitTrackerMultipleTask omSubmitTrackerMultipleTask = new SubmitTrackerMultipleTask(getApplicationContext());
        omSubmitTrackerMultipleTask.setAPIRequestFinishListener(this, "SubmitTrackerMultipleTask");
        omSubmitTrackerMultipleTask.execute();


        // send quiz results
        Log.d(TAG, "Submitting quiz task");
        DbHelper db = DbHelper.getInstance(getApplicationContext());
        List<QuizAttempt> unsent = db.getUnsentQuizAttempts();

        if (unsent.isEmpty()) {
            onRequestFinish(null);
        } else {
            Payload p2 = new Payload(unsent);
            SubmitQuizAttemptsTask omSubmitQuizAttemptsTask = new SubmitQuizAttemptsTask(getApplicationContext());
            omSubmitQuizAttemptsTask.setAPIRequestFinishListener(this, "SubmitQuizAttemptsTask");
            omSubmitQuizAttemptsTask.execute(p2);
        } 


        // Attention! if more tasks are added, remember to update pendingTasks method variable
    }

    @Override
    public void apiRequestComplete(Payload response) {
        boolean updateAvailable = false;
        try {

            JSONObject json = new JSONObject(response.getResultResponse());
            Log.d(TAG, json.toString(4));
            DbHelper db = DbHelper.getInstance(getApplicationContext());
            for (int i = 0; i < (json.getJSONArray("courses").length()); i++) {
                JSONObject jsonObj = (JSONObject) json.getJSONArray("courses").get(i);
                String shortName = jsonObj.getString("shortname");
                Double version = jsonObj.getDouble("version");

                if (db.toUpdate(shortName, version)) {
                    updateAvailable = true;
                }
            }

        } catch (JSONException e) {
            Mint.logException(e);
            Log.d(TAG, "JSON error: ", e);
        }

        if (updateAvailable) {
            Intent resultIntent = new Intent(getApplicationContext(), DownloadActivity.class);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder = OppiaNotificationUtils.getBaseBuilder(getApplicationContext(), true);
            mBuilder
                    .setContentTitle(getString(R.string.notification_course_update_title))
                    .setContentText(getString(R.string.notification_course_update_text))
                    .setContentIntent(resultPendingIntent);
            int mId = 001;

            OppiaNotificationUtils.sendNotification(getApplicationContext(), mId, mBuilder.build());
        }
    }

    private String getString(int stringId) {
        return getApplicationContext().getString(stringId);
    }


    @Override
    public void onRequestFinish(String idRequest) {

        pendingTasks--;

        Log.i(TAG, "onRequestFinish: pendingTasks: " + pendingTasks);

        if (pendingTasks == 0) {
            future.set(Result.success());
        }
    }

    @Override
    public void apiKeyInvalidated() {
        SessionManager.logoutCurrentUser(getApplicationContext());
    }

}
