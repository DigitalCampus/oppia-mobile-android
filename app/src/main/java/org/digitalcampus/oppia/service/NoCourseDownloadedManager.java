package org.digitalcampus.oppia.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.utils.ui.OppiaNotificationUtils;

import java.util.List;

import javax.inject.Inject;

public class NoCourseDownloadedManager {

    private final String TAG = this.getClass().getSimpleName();
    
    private Context context;
    
    @Inject
    CoursesRepository coursesRepository;

    public NoCourseDownloadedManager(Context context) {
        this.context = context;
        initializeDaggerBase();
    }

    private void initializeDaggerBase() {
        App app = (App) context.getApplicationContext();
        app.getComponent().inject(this);
    }

    public void checkNoCoursesNotification() {

        boolean isLoggedIn = SessionManager.isLoggedIn(context);
        if (isLoggedIn) {
            checkSendNoCourseNotification();
        } else {
            Log.i(TAG, "startWork: user not logged in. exiting NoCourseDownloadWorker");
        }
    }


    private void checkSendNoCourseNotification() {
//        DbHelper db = DbHelper.getInstance(getApplicationContext());
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

    }
}
