package org.digitalcampus.oppia.model.coursecomplete

import android.content.Context
import org.digitalcampus.oppia.database.DbHelper
import org.digitalcampus.oppia.exception.ActivityNotFoundException
import org.digitalcampus.oppia.model.User

object FinalQuiz {
    fun isComplete(ctx: Context?, courseId: Int, user: User?): Boolean {
        val db = DbHelper.getInstance(ctx)
        // Get final quiz digest
        return try {
            val digest = db.getCourseFinalQuizDigest(courseId.toLong())
            db.activityCompleted(courseId, digest, user!!.userId)
        } catch (anfe: ActivityNotFoundException) {
            false
        }
    }
}