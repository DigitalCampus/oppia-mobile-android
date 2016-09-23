package org.digitalcampus.oppia.model;

import android.content.Context;

import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;

import java.util.ArrayList;

public class CoursesRepository {

    public ArrayList<Course> getCourses(Context ctx, long userId){
        DbHelper db = DbHelper.getInstance(ctx);
        return db.getCourses(userId);
    }
}
