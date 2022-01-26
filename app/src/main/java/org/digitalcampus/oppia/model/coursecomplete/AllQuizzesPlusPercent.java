package org.digitalcampus.oppia.model.coursecomplete;

import android.content.Context;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.User;

import java.util.List;

public class AllQuizzesPlusPercent {

    public static final String TAG = AllQuizzesPlusPercent.class.getSimpleName();

    public static boolean isComplete(Context ctx, int courseId, User user, int percent){

        DbHelper db = DbHelper.getInstance(ctx);

        // check all the quizzes complete
        List<Activity> quizzes = db.getCourseQuizzes(courseId);

        // check that the user has completed every activity
        for (Activity activity: quizzes){
            if (!db.activityCompleted(courseId, activity.getDigest(), user.getUserId())){
                return false;
            }
        }

        int totalNonQuizActivities = 0;
        int activitiesNonQuizCompleted = 0;

        // check no of other activities completed
        List<Activity> activities = db.getCourseActivities(courseId);
        for (Activity activity: activities){
            if(activity.getActType().equals("quiz")){
                // skip to next in loop
                continue;
            }
            totalNonQuizActivities++;
            if (db.activityCompleted(courseId, activity.getDigest(), user.getUserId())){
                activitiesNonQuizCompleted++;
            }
        }

        if (totalNonQuizActivities == 0){
            return true;
        }
        int percentComplete = activitiesNonQuizCompleted * 100 / totalNonQuizActivities;
        return percentComplete >= percent;
    }
}
