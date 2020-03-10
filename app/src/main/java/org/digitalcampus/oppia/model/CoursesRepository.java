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
}
