package org.digitalcampus.oppia.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.ui.OppiaNotificationUtils;

import java.util.List;

import javax.inject.Inject;

public class NoCourseDownloadedManager {

    public static final String TAG = NoCourseDownloadedManager.class.getSimpleName();
    
    private Context context;
    
    @Inject
    CoursesRepository coursesRepository;

    @Inject
    User user;

    public NoCourseDownloadedManager(Context context) {
        this.context = context;
        initializeDaggerBase();
    }

    private void initializeDaggerBase() {
        App app = (App) context.getApplicationContext();
        app.getComponent().inject(this);
    }

    public void checkNoCoursesNotification() {

        if (isUserLoggedIn()) {
            checkSendNoCourseNotification();
        } else {
            Log.i(TAG, "startWork: user not logged in. exiting NoCourseDownloadWorker");
        }
    }

    private boolean isUserLoggedIn() {
        return user != null && !TextUtils.isEmpty(user.getUsername());
    }


    private void checkSendNoCourseNotification() {
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
