package org.digitalcampus.oppia.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.listener.APIRequestFinishListener;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.model.responses.CourseServer;
import org.digitalcampus.oppia.model.responses.CoursesServerResponse;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.CourseUtils;
import org.digitalcampus.oppia.utils.ui.OppiaNotificationUtils;

import java.util.List;

import javax.inject.Inject;

/**
 * Class isolated for unit testing purposes
 */
public class CoursesChecksWorkerManager implements APIRequestFinishListener, APIRequestListener {

    public static final String TAG = CoursesChecksWorkerManager.class.getSimpleName();

    private static final int ID_NOTIF_TO_UPDATE = 1;
    private static final int ID_NOTIF_TO_DELETE = 2;
    private static final int ID_NOTIF_NEW_COURSES = 3;

    private Context context;
    private int pendingChecks;
    private OnFinishListener onFinishListener;

    @Inject
    CoursesRepository coursesRepository;

    @Inject
    User user;

    @Inject
    SharedPreferences prefs;

    public CoursesChecksWorkerManager(Context context) {
        this.context = context;
        initializeDaggerBase();
    }

    private void initializeDaggerBase() {
        App app = (App) context.getApplicationContext();
        app.getComponent().inject(this);
    }

    public interface OnFinishListener {
        void onFinish(String message);
    }

    public void setOnFinishListener(OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }

    private boolean isUserLoggedIn() {
        return user != null && !TextUtils.isEmpty(user.getUsername());
    }

    public void startChecks() {


        if (isUserLoggedIn()) {

            pendingChecks = 2;

            checkNoCoursesInstalled();
            checkCoursesUpdates();

        } else {

            if (onFinishListener != null) {
                onFinishListener.onFinish(getString(R.string.user_not_logged_in));
            }
        }

    }

    public void checkNoCoursesInstalled() {

        if (!isUserLoggedIn()) {
            // Duplicate user logged in check needed to test individually
            return;
        }

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

        onRequestFinish(null);
    }


    public void checkCoursesUpdates() {

        if (!isUserLoggedIn()) {
            // Duplicate user logged in check needed to test individually
            return;
        }

        APIUserRequestTask task = new APIUserRequestTask(context);
        String url = Paths.SERVER_COURSES_PATH;
        task.setAPIRequestListener(this);
        task.setAPIRequestFinishListener(this, "APIUserRequestTask");
        task.execute(url);

    }


    @Override
    public void apiRequestComplete(Payload response) {

        if (response.isResult()) {

            // TODO oppia-577 remove
            response.setResultResponse("{\"courses\":[{\"resource_uri\":\"/api/v2/course/73/\",\"id\":73,\"version\":20160220235615,\"title\":{\"en\":\"Antenatal Care Part 2\"},\"description\":{\"en\":\"ANC HEAT Module Part 2, full content, designed for use in all countries\"},\"shortname\":\"anc2-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/73/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/76/\",\"id\":76,\"version\":20150221000028,\"title\":{\"en\":\"Communicable Diseases Part 1\"},\"description\":{\"en\":\"CD HEAT Module Part 1, full content, designed for use in all countries\"},\"shortname\":\"cd1-all\",\"priority\":0,\"is_draft\":true,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/76/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/59/\",\"id\":59,\"version\":20150218143333,\"title\":{\"en\":\"Communicable Diseases Part 2 - Ethiopia (Full)\"},\"description\":{\"en\":\"CD HEAT Module Part 2, full content, designed for use in Ethiopia\"},\"shortname\":\"cd2-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/59/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/60/\",\"id\":60,\"version\":20150218154124,\"title\":{\"en\":\"Communicable Diseases Part 3 - Ethiopia (Full)\"},\"description\":{\"en\":\"CD HEAT Module Part 3, full content, designed for use in Ethiopia\"},\"shortname\":\"cd3-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/60/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/78/\",\"id\":78,\"version\":20150221000542,\"title\":{\"en\":\"Communicable Diseases Part 3\"},\"description\":{\"en\":\"CD HEAT Module Part 3, full content, designed for use in all countries\"},\"shortname\":\"cd3-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/78/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/61/\",\"id\":61,\"version\":20150218161834,\"title\":{\"en\":\"Communicable Diseases Part 4 - Ethiopia (Full)\"},\"description\":{\"en\":\"CD HEAT Module Part 4, full content, designed for use in Ethiopia\"},\"shortname\":\"cd4-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/61/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/79/\",\"id\":79,\"version\":20150221001243,\"title\":{\"en\":\"Communicable Diseases Part 4\"},\"description\":{\"en\":\"CD HEAT Module Part 4, full content, designed for use in all countries\"},\"shortname\":\"cd4-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/79/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/57/\",\"id\":57,\"version\":20150218162046,\"title\":{\"en\":\"Family Planning - Ethiopia (Full)\"},\"description\":{\"en\":\"FP HEAT Module, full content, designed for use in Ethiopia\"},\"shortname\":\"fp-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/57/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/74/\",\"id\":74,\"version\":20150221001535,\"title\":{\"en\":\"Family Planning\"},\"description\":{\"en\":\"FP HEAT Module, full content, designed for use in all countries\"},\"shortname\":\"fp-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/74/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/65/\",\"id\":65,\"version\":20150218163637,\"title\":{\"en\":\"Health Education, Advocacy and Community Mobilisation Part 1 - Ethiopia (Full)\"},\"description\":{\"en\":\"HEACM HEAT Module Part 1, full content, designed for use in Ethiopia\"},\"shortname\":\"heacm1-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/65/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/80/\",\"id\":80,\"version\":20150221124051,\"title\":{\"en\":\"Health Education, Advocacy and Community Mobilisation Part 1\"},\"description\":{\"en\":\"HEACM HEAT Module Part 1, full content, designed for use in all countries\"},\"shortname\":\"heacm1-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/80/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/66/\",\"id\":66,\"version\":20150218163819,\"title\":{\"en\":\"Health Education, Advocacy and Community Mobilisation Part 2 - Ethiopia (Full)\"},\"description\":{\"en\":\"HEACM HEAT Module Part 2, full content, designed for use in Ethiopia\"},\"shortname\":\"heacm2-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/66/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/81/\",\"id\":81,\"version\":20150221124256,\"title\":{\"en\":\"Health Education, Advocacy and Community Mobilisation Part 2\"},\"description\":{\"en\":\"HEACM HEAT Module Part 2, full content, designed for use in all countries\"},\"shortname\":\"heacm2-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/81/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/69/\",\"id\":69,\"version\":20150218164216,\"title\":{\"en\":\"Health Management, Ethics and Research - Ethiopia (Full)\"},\"description\":{\"en\":\"HMER HEAT Module, full content, designed for use in Ethiopia\"},\"shortname\":\"hmer-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/69/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/86/\",\"id\":86,\"version\":20150221124624,\"title\":{\"en\":\"Health Management, Ethics and Research\"},\"description\":{\"en\":\"HMER HEAT Module, full content, designed for use in all countries\"},\"shortname\":\"hmer-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/86/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/62/\",\"id\":62,\"version\":20150219124016,\"title\":{\"en\":\"Hygiene and Environmental Health Part 1 - Ethiopia (Full)\"},\"description\":{\"en\":\"HEH HEAT Module Part 1, full content, designed for use in Ethiopia\"},\"shortname\":\"heh1-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/62/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/82/\",\"id\":82,\"version\":20150221124854,\"title\":{\"en\":\"Hygiene and Environmental Health Part 1\"},\"description\":{\"en\":\"HEH HEAT Module Part 1, full content, designed for use in all countries\"},\"shortname\":\"heh1-all\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/82/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"resource_uri\":\"/api/v2/course/63/\",\"id\":63,\"version\":20150219125505,\"title\":{\"en\":\"Hygiene and Environmental Health Part 2 - Ethiopia (Full)\"},\"description\":{\"en\":\"HEH HEAT Module Part 2, full content, designed for use in Ethiopia\"},\"shortname\":\"heh2-et\",\"priority\":0,\"is_draft\":false,\"url\":\"https://staging.oppia-mobile.org/api/v2/course/63/download/\",\"author\":\"Alex Little\",\"username\":\"alex\",\"organisation\":\"Digital Campus\"},{\"author\":\"Alex Little\",\"description\":{\"en\":\"HEH HEAT Module Part 2, full content, designed for use in all countries\"},\"id\":83,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/83/\",\"shortname\":\"heh2-all\",\"title\":{\"en\":\"Hygiene and Environmental Health Part 2\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/83/download/\",\"username\":\"alex\",\"version\":20150221125250},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IM HEAT Module, full content, designed for use in Ethiopia\"},\"id\":42,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/42/\",\"shortname\":\"im-et\",\"title\":{\"en\":\"Immunization - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/42/download/\",\"username\":\"alex\",\"version\":20150219125724},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IM HEAT Module, full content, designed for use in all countries (may require localisation)\"},\"id\":50,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/50/\",\"shortname\":\"im-all\",\"title\":{\"en\":\"Immunization\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/50/download/\",\"username\":\"alex\",\"version\":20150221141604},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IMNCI HEAT Module Part 1, full content, designed for use in Ethiopia\"},\"id\":45,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/45/\",\"shortname\":\"imnci1-et\",\"title\":{\"en\":\"Integrated Management of Newborn and Childhood Illness Part 1 - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/45/download/\",\"username\":\"alex\",\"version\":20150219125811},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IMNCI HEAT Module Part 1, full content, designed for use in all countries\"},\"id\":47,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/47/\",\"shortname\":\"imnci1-all\",\"title\":{\"en\":\"Integrated Management of Newborn and Childhood Illness Part 1\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/47/download/\",\"username\":\"alex\",\"version\":20150221141640},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IMNCI HEAT Module Part 2, full content, designed for use in Ethiopia\"},\"id\":44,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/44/\",\"shortname\":\"imnci2-et\",\"title\":{\"en\":\"Integrated Management of Newborn and Childhood Illness Part 2 - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/44/download/\",\"username\":\"alex\",\"version\":20150219130627},{\"author\":\"Alex Little\",\"description\":{\"en\":\"IMNCI HEAT Module Part 2, full content, designed for use in all countries\"},\"id\":49,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/49/\",\"shortname\":\"imnci2-all\",\"title\":{\"en\":\"Integrated Management of Newborn and Childhood Illness Part 2\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/49/download/\",\"username\":\"alex\",\"version\":20150221142445},{\"author\":\"Alex Little\",\"description\":{\"en\":\"LDC HEAT Module, full content, designed for use in Ethiopia\"},\"id\":38,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/38/\",\"shortname\":\"ldc-et\",\"title\":{\"en\":\"Labour and Delivery Care - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/38/download/\",\"username\":\"alex\",\"version\":20150219133612},{\"author\":\"Alex Little\",\"description\":{\"en\":\"LDC HEAT Module, full content, designed for use in all countries\"},\"id\":37,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/37/\",\"shortname\":\"ldc-all\",\"title\":{\"en\":\"Labour and Delivery Care\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/37/download/\",\"username\":\"alex\",\"version\":20150221151347},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NCD HEAT Module Part 1, full content, designed for use in Ethiopia\"},\"id\":67,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/67/\",\"shortname\":\"ncd1-et\",\"title\":{\"en\":\"Non-Communicable Diseases, Emergency Care and Mental Health Part 1 - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/67/download/\",\"username\":\"alex\",\"version\":20150219134008},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NCD HEAT Module Part 1, full content, designed for use in all countries\"},\"id\":84,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/84/\",\"shortname\":\"ncd1-all\",\"title\":{\"en\":\"Non-Communicable Diseases, Emergency Care and Mental Health Part 1\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/84/download/\",\"username\":\"alex\",\"version\":20150221151807},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NCD HEAT Module Part 2, full content, designed for use in Ethiopia\"},\"id\":68,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/68/\",\"shortname\":\"ncd2-et\",\"title\":{\"en\":\"Non-Communicable Diseases, Emergency Care and Mental Health Part 2 - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/68/download/\",\"username\":\"alex\",\"version\":20150219134457},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NCD HEAT Module Part 2, full content, designed for use in all countries\"},\"id\":85,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/85/\",\"shortname\":\"ncd2-all\",\"title\":{\"en\":\"Non-Communicable Diseases, Emergency Care and Mental Health Part 2\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/85/download/\",\"username\":\"alex\",\"version\":20150221152035},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NUT HEAT Module, full content, designed for use in Ethiopia\"},\"id\":43,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/43/\",\"shortname\":\"nut-et\",\"title\":{\"en\":\"Nutrition - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/43/download/\",\"username\":\"alex\",\"version\":20150219140806},{\"author\":\"Alex Little\",\"description\":{\"en\":\"NUT HEAT Module, full content, designed for use in all countries (may require localisation)\"},\"id\":48,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/48/\",\"shortname\":\"nut-all\",\"title\":{\"en\":\"Nutrition\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/48/download/\",\"username\":\"alex\",\"version\":20150221152458},{\"author\":\"Alex Little\",\"description\":{\"en\":\"PNC HEAT Module, full content, designed for use in Ethiopia\"},\"id\":41,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/41/\",\"shortname\":\"pnc-et\",\"title\":{\"en\":\"Postnatal Care - Ethiopia (Full)\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/41/download/\",\"username\":\"alex\",\"version\":20150219143153},{\"author\":\"Alex Little\",\"description\":{\"en\":\"PNC HEAT Module, full content, designed for use in all countries\"},\"id\":46,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/46/\",\"shortname\":\"pnc-all\",\"title\":{\"en\":\"Postnatal Care\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/46/download/\",\"username\":\"alex\",\"version\":20150221152551},{\"author\":\"Alex Little\",\"description\":{\"en\":null},\"id\":122,\"is_draft\":false,\"organisation\":\"Digital Campus\",\"priority\":0,\"resource_uri\":\"/api/v2/course/122/\",\"shortname\":\"draft-test\",\"title\":{\"en\":\"Reference course 1 - reference\"},\"url\":\"https://staging.oppia-mobile.org/api/v2/course/122/download/\",\"username\":\"alex\",\"version\":20210413132552}],\"meta\":{\"limit\":1000,\"next\":null,\"offset\":0,\"previous\":null,\"total_count\":37}}");

            try {


                CoursesServerResponse coursesServerResponse = new Gson().fromJson(
                        response.getResultResponse(), CoursesServerResponse.class);

                double mostRecentVersionTimestamp = getMostRecentVersionTimestamp(coursesServerResponse);
                long mostRecentVersionTimestampLong = Double.doubleToRawLongBits(mostRecentVersionTimestamp);

                prefs.edit()
                        .putLong(PrefsActivity.PREF_LAST_COURSE_VERSION_TIMESTAMP_CHECKED, mostRecentVersionTimestampLong)
                        .putLong(PrefsActivity.PREF_LAST_COURSES_CHECKS_SUCCESSFUL, System.currentTimeMillis())
                        .putString(PrefsActivity.PREF_SERVER_COURSES_CACHE, response.getResultResponse())
                        .commit();

                groupCoursesBySyncStatusAndNotify();


            } catch (JsonSyntaxException e) {
                Mint.logException(e);
                Log.d(TAG, "JSON error: ", e);
            }

        }
    }

    private void groupCoursesBySyncStatusAndNotify() {

        double lastVersionTimestampChecked = Double.longBitsToDouble(
                prefs.getLong(PrefsActivity.PREF_LAST_COURSE_VERSION_TIMESTAMP_CHECKED, 0));

        // TODO oppia-577 remove
        lastVersionTimestampChecked = 0;

        List<Course> courses = coursesRepository.getCourses(context);
        CourseUtils.setSyncStatus(prefs, courses, lastVersionTimestampChecked);

        int toUpdateCount = 0;
        int toDeleteCount = 0;

        for (Course course : courses) {
            if (course.isToDelete()) {
                toDeleteCount++;
            } else if (course.isToUpdate()) {
                toUpdateCount++;
            }
        }

        if (toUpdateCount > 0) {
            showToUpdateNotification(toUpdateCount);
        }

        if (toDeleteCount > 0) {
            showToDeleteNotification(toDeleteCount);
        }

    }

    private void showToUpdateNotification(int toUpdateCount) {

        Intent resultIntent = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String contentText = context.getResources().getQuantityString(
                R.plurals.notification_courses_to_update_text, toUpdateCount, toUpdateCount);

        NotificationCompat.Builder mBuilder = OppiaNotificationUtils.getBaseBuilder(context, true);
        mBuilder
                .setContentTitle(getString(R.string.notification_courses_to_update_title))
                .setContentText(contentText)
                .setContentIntent(resultPendingIntent);

        OppiaNotificationUtils.sendNotification(context, ID_NOTIF_TO_UPDATE, mBuilder.build());
    }

    private void showToDeleteNotification(int toDeleteCount) {

        Intent resultIntent = new Intent(context, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String contentText = context.getResources().getQuantityString(
                R.plurals.notification_courses_to_delete_text, toDeleteCount, toDeleteCount);

        NotificationCompat.Builder mBuilder = OppiaNotificationUtils.getBaseBuilder(context, true);
        mBuilder
                .setContentTitle(getString(R.string.notification_courses_to_delete_title))
                .setContentText(contentText)
                .setContentIntent(resultPendingIntent);

        OppiaNotificationUtils.sendNotification(context, ID_NOTIF_TO_DELETE, mBuilder.build());
    }

    private double getMostRecentVersionTimestamp(CoursesServerResponse coursesServerResponse) {
        double mostRecentVersionTimestamp = 0;
        for(CourseServer course : coursesServerResponse.getCourses()) {
            mostRecentVersionTimestamp = Math.max(mostRecentVersionTimestamp, course.getVersion());
        }
        return mostRecentVersionTimestamp;
    }

    private String getString(int stringId) {
        return context.getString(stringId);
    }

    @Override
    public void onRequestFinish(String idRequest) {

        pendingChecks--;

        Log.i(TAG, "onRequestFinish: pendingChecks: " + pendingChecks);

        if (pendingChecks == 0) {
            if (onFinishListener != null) {
                onFinishListener.onFinish(null);
            }
        }
    }

    @Override
    public void apiKeyInvalidated() {
        SessionManager.logoutCurrentUser(context);
    }
}
