package org.digitalcampus.oppia.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.listener.APIRequestFinishListener;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.ui.OppiaNotificationUtils;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressLint("RestrictedApi")
public class CoursesCheckingsWorker extends ListenableWorker {

    public static final String TAG = CoursesCheckingsWorker.class.getSimpleName();
    private SettableFuture<Result> future;

    public CoursesCheckingsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @SuppressLint("RestrictedApi")
    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        Log.i(TAG, "startWork: CoursesCheckingsWorker");

        future = SettableFuture.create();

        CoursesCheckingsWorkerManager coursesCheckingsWorkerManager = new CoursesCheckingsWorkerManager(getApplicationContext());
        coursesCheckingsWorkerManager.setOnFinishListener(message -> future.set(Result.success()));
        coursesCheckingsWorkerManager.startCheckings();


        return future;
    }


    @Override
    public void onStopped() {
        super.onStopped();
        Log.i(TAG, "onStopped");
    }


}
