package org.digitalcampus.oppia.model.coursecomplete

import android.content.Context
import org.digitalcampus.oppia.database.DbHelper
import org.digitalcampus.oppia.model.User

object AllQuizzesPlusPercent {
    val TAG = AllQuizzesPlusPercent::class.simpleName

    fun isComplete(ctx: Context?, courseId: Int, user: User?, percent: Int): Boolean {
        val db = DbHelper.getInstance(ctx)

        // check all the quizzes complete
        val quizzes = db.getCourseQuizzes(courseId.toLong())

        // check that the user has completed every activity
        for (activity in quizzes) {
            if (!db.activityCompleted(courseId, activity.digest, user!!.userId)) {
                return false
            }
        }
        var totalNonQuizActivities = 0
        var activitiesNonQuizCompleted = 0

        // check no of other activities completed
        val activities = db.getCourseActivities(courseId.toLong())
        for (activity in activities) {
            if (activity.actType == "quiz") {
                // skip to next in loop
                continue
            }
            totalNonQuizActivities++
            if (db.activityCompleted(courseId, activity.digest, user!!.userId)) {
                activitiesNonQuizCompleted++
            }
        }
        if (totalNonQuizActivities == 0) {
            return true
        }
        val percentComplete = activitiesNonQuizCompleted * 100 / totalNonQuizActivities
        return percentComplete >= percent
    }
}