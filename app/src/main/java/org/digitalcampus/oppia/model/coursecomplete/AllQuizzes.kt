package org.digitalcampus.oppia.model.coursecomplete

import android.content.Context
import org.digitalcampus.oppia.database.DbHelper
import org.digitalcampus.oppia.model.User

object AllQuizzes {
    fun isComplete(ctx: Context?, courseId: Int, user: User?): Boolean {
        val db = DbHelper.getInstance(ctx)
        // Get all the digests for this course
        val quizzes = db.getCourseQuizzes(courseId.toLong())

        // check that the user has completed every activity
        for (activity in quizzes) {
            if (!db.activityCompleted(courseId, activity.digest, user!!.userId)) {
                return false
            }
        }
        return true
    }
}