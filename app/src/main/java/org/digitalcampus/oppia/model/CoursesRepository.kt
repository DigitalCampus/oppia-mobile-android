package org.digitalcampus.oppia.model

import android.content.Context
import org.digitalcampus.oppia.application.SessionManager
import org.digitalcampus.oppia.database.DbHelper

class CoursesRepository {
    fun getCourses(ctx: Context?): List<Course> {
        val db = DbHelper.getInstance(ctx)
        val userId = db.getUserId(SessionManager.getUsername(ctx))
        return db.getCoursesForUser(userId)
    }

    fun getActivityByDigest(ctx: Context?, digest: String?): Activity {
        val db = DbHelper.getInstance(ctx)
        return db.getActivityByDigest(digest)
    }

    fun getCourse(ctx: Context?, courseID: Long, userID: Long): Course {
        val db = DbHelper.getInstance(ctx)
        return db.getCourseWithProgress(courseID, userID)
    }

    fun getCourseByShortname(ctx: Context?, shortname: String?, userID: Long): Course? {
        val db = DbHelper.getInstance(ctx)
        val courseId = db.getCourseIdByShortname(shortname, userID)
        return if (courseId != -1L) {
            db.getCourseWithProgress(courseId, userID)
        } else null
    }
}