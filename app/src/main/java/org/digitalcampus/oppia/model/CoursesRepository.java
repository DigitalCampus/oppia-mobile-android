package org.digitalcampus.oppia.model;

import android.content.Context;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;

import java.util.List;

public class CoursesRepository {

    public List<Course> getCourses(Context ctx){
        DbHelper db = DbHelper.getInstance(ctx);
        long userId = db.getUserId(SessionManager.getUsername(ctx));
        return db.getCoursesForUser(userId);
    }

    public Activity getActivityByDigest(Context ctx, String digest){
        DbHelper db = DbHelper.getInstance(ctx);
        return db.getActivityByDigest(digest);
    }

    public Course getCourse(Context ctx, long courseID, long userID){
        DbHelper db = DbHelper.getInstance(ctx);
        return db.getCourse(courseID, userID);
    }

    public Course getCourseByShortname(Context ctx, String shortname, long userID){
        DbHelper db = DbHelper.getInstance(ctx);
        long courseId = db.getCourseIdByShortname(shortname);
        if (courseId != -1) {
            return db.getCourse(courseId, userID);
        }
        return null;
    }
}
