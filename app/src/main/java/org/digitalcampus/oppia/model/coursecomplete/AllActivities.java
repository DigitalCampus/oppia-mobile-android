package org.digitalcampus.oppia.model.coursecomplete;

import android.content.Context;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.User;

import java.util.List;

public class AllActivities {

    public static boolean isComplete(Context ctx, int courseId, User user){

        DbHelper db = DbHelper.getInstance(ctx);
        // Get all the digests for this course
        List<Activity> activities = db.getCourseActivities(courseId);

        // check that the user has completed every activity
        for (Activity activity: activities){
            if (!db.activityCompleted(courseId, activity.getDigest(), user.getUserId())){
                return false;
            }
        }
        return true;
    }
}
