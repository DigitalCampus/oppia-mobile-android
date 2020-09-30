package org.digitalcampus.oppia.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import androidx.work.impl.utils.futures.SettableFuture;

import com.google.common.util.concurrent.ListenableFuture;

public class NoCourseDownloadedWorker extends ListenableWorker {

    public static final String TAG = NoCourseDownloadedWorker.class.getSimpleName() ;
    private SettableFuture<Result> future;

    public NoCourseDownloadedWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @SuppressLint("RestrictedApi")
    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        Log.i(TAG, "startWork: NoCourseDownloadWorker");

        future = SettableFuture.create();

        new NoCourseDownloadedManager(getApplicationContext()).checkNoCoursesNotification();

        future.set(Result.success());

        return future;
    }

    @Override
    public void onStopped() {
        super.onStopped();
        Log.i(TAG, "onStopped");
    }

}
