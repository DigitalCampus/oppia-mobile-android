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
public class UserCohortsCheckerWorker extends ListenableWorker {

    public static final String TAG = UserCohortsCheckerWorker.class.getSimpleName();
    private SettableFuture<Result> future;

    public UserCohortsCheckerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public UserCohortsChecksWorkerManager getWorkerManager() {
        return new UserCohortsChecksWorkerManager(getApplicationContext());
    }

    @SuppressLint("RestrictedApi")
    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        Log.i(TAG, "startWork: UserCohortsChecksWorker");

        future = SettableFuture.create();

        UserCohortsChecksWorkerManager userCohortsChecksWorkerManager = getWorkerManager();
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
