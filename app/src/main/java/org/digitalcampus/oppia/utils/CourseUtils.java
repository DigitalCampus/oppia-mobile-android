package org.digitalcampus.oppia.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.preference.PreferenceManager;

import com.google.gson.Gson;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.responses.CourseServer;
import org.digitalcampus.oppia.model.responses.CoursesServerResponse;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class CourseUtils {


    /**
     * Utility method to set both sync status (to update or to delete) and course status (draft, live...)
     * based on courses info cache
     *
     * @param prefs         SharedPreferences to get the cache data (passed by parameter to be mockable for tests)
     * @param courses       Installed courses in local database
     * @param fromTimestamp if not null, courses with version timestamp prior to this parameter are ignored
     */
    public static void refreshStatuses(SharedPreferences prefs, List<Course> courses, Double fromTimestamp) {

        if (courses == null || courses.isEmpty()) {
            return;
        }

        String coursesCachedStr = prefs.getString(PrefsActivity.PREF_SERVER_COURSES_CACHE, null);
        if (coursesCachedStr != null) {
            CoursesServerResponse coursesServerResponse = new Gson().fromJson(
                    coursesCachedStr, CoursesServerResponse.class);

            for (Course course : courses) {
                checkCourseStatuses(course, coursesServerResponse.getCourses(), fromTimestamp);
            }

        } else {
            for (Course course : courses) {
                course.setToUpdate(false);
                course.setToDelete(false);
                course.setStatus(Course.STATUS_LIVE);
            }
        }
    }

    private static void checkCourseStatuses(Course course, List<CourseServer> coursesServer, Double fromTimestamp) {

        for (CourseServer courseServer : coursesServer) {

            if (fromTimestamp != null && courseServer.getVersion() <= fromTimestamp) {
                continue;
            }

            if (TextUtilsJava.equals(course.getShortname(), courseServer.getShortname())) {
                boolean toUpdate = course.getVersionId() < courseServer.getVersion();
                course.setToUpdate(toUpdate);
                course.setToDelete(false);
                course.setStatus(courseServer.getStatus());
                return;
            }

        }

        // If this line is reached is  because this course is not in the server list yet.
        course.setToDelete(true);

    }

    public static List<CourseServer> getNotInstalledCourses(SharedPreferences prefs, List<Course> coursesInstalled) {

        List<CourseServer> notInstalledCourses = null;

        String coursesCachedStr = prefs.getString(PrefsActivity.PREF_SERVER_COURSES_CACHE, null);

        if (coursesCachedStr != null) {
            CoursesServerResponse coursesServerResponse = new Gson().fromJson(
                    coursesCachedStr, CoursesServerResponse.class);

            List<CourseServer> coursesServer = coursesServerResponse.getCourses();
            notInstalledCourses = new ArrayList<>();
            for (CourseServer courseServer : coursesServer) {
                if (!isCourseInstalled(courseServer, coursesInstalled)) {
                    notInstalledCourses.add(courseServer);
                }
            }
        }

        return notInstalledCourses;
    }

    private static boolean isCourseInstalled(CourseServer courseServer, List<Course> coursesInstalled) {
        for (Course course : coursesInstalled) {
            if (TextUtilsJava.equals(course.getShortname(), courseServer.getShortname())) {
                return true;
            }
        }
        return false;
    }

    public static void updateCourseActivitiesWordCount(Context ctx, CompleteCourse course) {

        DbHelper db = DbHelper.getInstance(ctx);
        List<Activity> activities = course.getActivities(course.getCourseId());

        for (Activity a : activities) {
            ArrayList<Lang> langs = (ArrayList<Lang>) course.getLangs();
            int wordCount = 0;

            for (Lang l : langs) {
                String langContents = a.getFileContents(course.getLocation(), l.getLanguage());
                if (langContents == null) {
                    continue;
                }
                // strip out all html tags from string (not needed for search nor wordcount)
                langContents = langContents.replaceAll("\\<.*?\\>", "").trim();
                int langWordCount = langContents.split("\\s+").length;
                // We keep the highest wordcount among the different languages
                wordCount = Math.max(wordCount, langWordCount);
            }

            Activity act = db.getActivityByDigest(a.getDigest());
            if ((act != null) && (wordCount > 0)) {
                db.updateActivityWordCount(act, wordCount);
            }

        }
    }

    public static boolean isReadOnlyCourse(Context context, String activityDigest) {
        return isReadOnlyCourse(context, activityDigest, PreferenceManager.getDefaultSharedPreferences(context));

    }

    public static boolean isReadOnlyCourse(Context context, String activityDigest, SharedPreferences prefs) {
        DbHelper db = DbHelper.getInstance(context);
        Activity activity = db.getActivityByDigest(activityDigest);
        Course course = db.getCourse(activity.getCourseId());

        String coursesCachedStr = prefs.getString(PrefsActivity.PREF_SERVER_COURSES_CACHE, null);
        if (coursesCachedStr != null) {
            CoursesServerResponse coursesServerResponse = new Gson().fromJson(
                    coursesCachedStr, CoursesServerResponse.class);

            for (CourseServer courseServer : coursesServerResponse.getCourses()) {
                if (TextUtilsJava.equals(courseServer.getShortname(), course.getShortname())) {
                    return courseServer.hasStatus(Course.STATUS_READ_ONLY);
                }
            }
        }

        return false;
    }

    public static void refreshCachedData(Context context, Course course) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String coursesCachedStr = prefs.getString(PrefsActivity.PREF_SERVER_COURSES_CACHE, null);
        if (coursesCachedStr != null) {
            CoursesServerResponse coursesServerResponse = new Gson().fromJson(
                    coursesCachedStr, CoursesServerResponse.class);

            if (coursesServerResponse != null) {
                for (CourseServer courseServer : coursesServerResponse.getCourses()) {
                    if (TextUtilsJava.equals(courseServer.getShortname(), course.getShortname())) {
                        refreshCachedStatus(course, courseServer.getStatus());
                        refreshCachedCohorts(course, courseServer.isRestricted(), courseServer.getRestrictedCohorts());
                    }
                }
            }
        }
    }

    private static void refreshCachedStatus(Course course, String newStatus) {
        course.setStatus(newStatus);
    }

    private static void refreshCachedCohorts(Course course, boolean isRestricted, List<Integer> cohorts) {
        course.setRestricted(isRestricted);
        course.setRestrictedCohorts(cohorts);
    }

    public static List<Integer> parseCourseCohortsFromJSONArray(JSONArray cohortsJson) throws JSONException {
        List<Integer> cohorts = new ArrayList<>();
        for (int i = 0; i < cohortsJson.length(); i++) {
            cohorts.add(cohortsJson.getInt(i));
        }
        return cohorts;
    }

    public static void updateCourseCache(Context context, SharedPreferences prefs, ApiEndpoint apiEndpoint) {
        APIUserRequestTask task = new APIUserRequestTask(context, apiEndpoint);
        String url = Paths.SERVER_COURSES_PATH;
        task.setAPIRequestListener(new APIRequestListener() {
            @Override
            public void apiRequestComplete(BasicResult result) {

                if (result.isSuccess()) {
                    prefs.edit()
                            .putLong(PrefsActivity.PREF_LAST_COURSES_CHECKS_SUCCESSFUL_TIME, System.currentTimeMillis())
                            .putString(PrefsActivity.PREF_SERVER_COURSES_CACHE, result.getResultMessage())
                            .commit();
                }
            }

            @Override
            public void apiKeyInvalidated() {

            }
        });
        task.execute(url);
    }
}
