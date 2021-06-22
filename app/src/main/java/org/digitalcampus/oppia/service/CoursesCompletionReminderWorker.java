package org.digitalcampus.oppia.service;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class CoursesCompletionReminderWorker extends Worker {

    private static final String TAG = CoursesCompletionReminderWorker.class.getSimpleName();

    public CoursesCompletionReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {

        return new CoursesCompletionReminderWorkerManager(getApplicationContext()).checkCompletionReminder();
    }

}
