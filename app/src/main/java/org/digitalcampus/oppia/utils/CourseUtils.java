package org.digitalcampus.oppia.utils;

import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.responses.CourseServer;
import org.digitalcampus.oppia.model.responses.CoursesServerResponse;

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
                course.setToDelete(courseServer.isDraft());
                if (course.getVersionId() < courseServer.getVersion()) {
                    course.setToUpdate(true);
                }

                return;
            }

            // If this line is reached is  because this course is not in the server list yet.
            course.setToDelete(true);
        }


    }
}
