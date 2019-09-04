package org.digitalcampus.oppia.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import androidx.work.impl.utils.futures.SettableFuture;

import com.google.common.util.concurrent.ListenableFuture;

public class TrackerWorker extends ListenableWorker {

    private final String TAG = "TrackerWorker";

    public TrackerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    @SuppressLint("RestrictedApi")
    public ListenableFuture<Result> startWork() {

        boolean backgroundData = getInputData().getBoolean("backgroundData", true);


        final SettableFuture<Result> future = SettableFuture.create();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Log.i(TAG, "run: postDelayed2");
                future.set(Result.success());
            }
        }, 2000);

//        bgtask {
//
//            future.set(Result.success());
//
//        }

//        CallbackToFutureAdapter.getFuture(new CallbackToFutureAdapter.Resolver<Object>() {
//            @Nullable
//            @Override
//            public Object attachCompleter(@NonNull CallbackToFutureAdapter.Completer<Object> completer) throws Exception {
//                return null;
//            }
//        });

        return future;
    }

}
