package org.digitalcampus.oppia.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.responses.CourseServer;
import org.digitalcampus.oppia.model.responses.CoursesServerResponse;

import java.util.ArrayList;
import java.util.List;

public class CourseUtils {



    public static void setSyncStatus(SharedPreferences prefs, List<Course> courses, Double fromTimestamp) {

        if (courses == null || courses.isEmpty()) {
            return;
        }

        String coursesCachedStr = prefs.getString(PrefsActivity.PREF_SERVER_COURSES_CACHE, null);
        if (coursesCachedStr != null) {
            CoursesServerResponse coursesServerResponse = new Gson().fromJson(
                    coursesCachedStr, CoursesServerResponse.class);

            for (Course course : courses) {
                checkUpdateOrDeleteStatus(course, coursesServerResponse.getCourses(), fromTimestamp);
            }

        }
    }

    private static void checkUpdateOrDeleteStatus(Course course, List<CourseServer> coursesServer, Double fromTimestamp) {

        for (CourseServer courseServer : coursesServer) {

            if (fromTimestamp != null && courseServer.getVersion() <= fromTimestamp) {
                continue;
            }

            if (TextUtils.equals(course.getShortname(), courseServer.getShortname())) {
                boolean toUpdate = course.getVersionId() < courseServer.getVersion();
                course.setToUpdate(toUpdate);
                course.setToDelete(false);
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
            if (TextUtils.equals(course.getShortname(), courseServer.getShortname())) {
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

}
