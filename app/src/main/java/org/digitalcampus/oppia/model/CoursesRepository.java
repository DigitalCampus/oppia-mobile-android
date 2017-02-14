package org.digitalcampus.oppia.model;

import android.content.Context;

import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;

import java.util.ArrayList;

public class CoursesRepository {

    public ArrayList<Course> getCourses(Context ctx){
        DbHelper db = DbHelper.getInstance(ctx);
        long userId = db.getUserId(SessionManager.getUsername(ctx));
        return db.getCourses(userId);
    }
}
