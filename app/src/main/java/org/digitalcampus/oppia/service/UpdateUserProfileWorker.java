package org.digitalcampus.oppia.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import androidx.work.impl.utils.futures.SettableFuture;

import com.google.common.util.concurrent.ListenableFuture;

@SuppressLint("RestrictedApi")
public class UpdateUserProfileWorker extends ListenableWorker {

    public static final String TAG = UpdateUserProfileWorker.class.getSimpleName();
    private SettableFuture<Result> future;

    public UpdateUserProfileWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public UpdateUserProfileWorkerManager getWorkerManager() {
        return new UpdateUserProfileWorkerManager(getApplicationContext());
    }

    @SuppressLint("RestrictedApi")
    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        Log.i(TAG, "startWork: UserCohortsChecksWorker");

        future = SettableFuture.create();

        UpdateUserProfileWorkerManager userCohortsChecksWorkerManager = getWorkerManager();
        userCohortsChecksWorkerManager.setOnFinishListener(result -> future.set(result));
        userCohortsChecksWorkerManager.startChecks();

//        future.set(Result.success());

        return future;
    }


    @Override
    public void onStopped() {
        super.onStopped();
        Log.i(TAG, "onStopped");
    }

}
