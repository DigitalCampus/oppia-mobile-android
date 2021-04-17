package org.digitalcampus.oppia.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import androidx.work.impl.utils.futures.SettableFuture;

import com.google.common.util.concurrent.ListenableFuture;

import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.listener.APIRequestFinishListener;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.task.FetchServerInfoTask;
import org.digitalcampus.oppia.task.SubmitQuizAttemptsTask;
import org.digitalcampus.oppia.task.SubmitTrackerMultipleTask;

import java.util.List;

@SuppressLint("RestrictedApi")
public class TrackerWorker extends ListenableWorker implements APIRequestFinishListener {


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

        pendingTasks = 3;

        // Update server info
        FetchServerInfoTask fetchServerInfoTask = new FetchServerInfoTask(getApplicationContext());
        fetchServerInfoTask.setAPIRequestFinishListener(this, "FetchServerInfoTask");
        fetchServerInfoTask.execute();
        Log.i(TAG, "updateTracking: FetchServerInfoTask executed");


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
            SubmitQuizAttemptsTask omSubmitQuizAttemptsTask = new SubmitQuizAttemptsTask(getApplicationContext());
            omSubmitQuizAttemptsTask.setAPIRequestFinishListener(this, "SubmitQuizAttemptsTask");
            omSubmitQuizAttemptsTask.execute(unsent);
        } 


        // Attention! if more tasks are added, remember to update pendingTasks method variable
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
