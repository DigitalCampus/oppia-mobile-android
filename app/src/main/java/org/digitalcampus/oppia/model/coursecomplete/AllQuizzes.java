package org.digitalcampus.oppia.model.coursecomplete;

import android.content.Context;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.User;

import java.util.List;

public class AllQuizzes {

    public static boolean isComplete(Context ctx, int course_id, User user){

        DbHelper db = DbHelper.getInstance(ctx);
        // Get all the digests for this course
        List<Activity> quizzes = db.getCourseQuizzes(course_id);

        // check that the user has completed every activity
        for (Activity activity: quizzes){
            if (!db.activityCompleted(course_id, activity.getDigest(), user.getUserId())){
                return false;
            }
        }
        return true;
    }
}
