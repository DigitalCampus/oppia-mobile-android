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
public class CoursesChecksWorker extends ListenableWorker {

    public static final String TAG = CoursesChecksWorker.class.getSimpleName();
    private SettableFuture<Result> future;

    public CoursesChecksWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @SuppressLint("RestrictedApi")
    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        Log.i(TAG, "startWork: CoursesChecksWorker");

        future = SettableFuture.create();

        CoursesChecksWorkerManager coursesChecksWorkerManager = new CoursesChecksWorkerManager(getApplicationContext());
        coursesChecksWorkerManager.setOnFinishListener(message -> future.set(Result.success()));
        coursesChecksWorkerManager.startChecks();


        return future;
    }


    @Override
    public void onStopped() {
        super.onStopped();
        Log.i(TAG, "onStopped");
    }


}
