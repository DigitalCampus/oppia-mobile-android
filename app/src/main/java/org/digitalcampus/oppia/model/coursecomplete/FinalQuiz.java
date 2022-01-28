package org.digitalcampus.oppia.model.coursecomplete;

import android.content.Context;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.ActivityNotFoundException;
import org.digitalcampus.oppia.model.User;


public class FinalQuiz {

    public static boolean isComplete(Context ctx, int courseId, User user) {

        DbHelper db = DbHelper.getInstance(ctx);
        // Get final quiz digest
        try {
            String digest = db.getCourseFinalQuizDigest(courseId);
            return db.activityCompleted(courseId, digest, user.getUserId());
        } catch (ActivityNotFoundException anfe) {
            return false;
        }
    }
}
