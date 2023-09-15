package org.digitalcampus.oppia.model.coursecomplete

import android.content.Context
import org.digitalcampus.oppia.database.DbHelper
import org.digitalcampus.oppia.model.User

object AllActivities {
    fun isComplete(ctx: Context?, courseId: Int, user: User?): Boolean {
        val db = DbHelper.getInstance(ctx)
        // Get all the digests for this course
        val activities = db.getCourseActivities(courseId.toLong())

        // check that the user has completed every activity
        for (activity in activities) {
            if (!db.activityCompleted(courseId, activity.digest, user!!.userId)) {
                return false
            }
        }
        return true
    }
}