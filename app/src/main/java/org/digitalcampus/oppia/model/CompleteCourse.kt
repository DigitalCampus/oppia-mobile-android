/*
 * This file is part of OppiaMobile - https://digital-campus.org/
 *
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */
package org.digitalcampus.oppia.model

import android.content.Context
import org.digitalcampus.oppia.application.SessionManager
import org.digitalcampus.oppia.database.DbHelper
import org.digitalcampus.oppia.utils.xmlreaders.IMediaXMLHandler

class CompleteCourse : Course, IMediaXMLHandler {
    var baselineActivities = ArrayList<Activity>()
    var sections = ArrayList<Section>()
    var gamification = ArrayList<GamificationEvent>()

    constructor() : super("") {}
    constructor(root: String?) : super(root) {}

    fun getSection(order: Int): Section? {
        return sections.find { it.order == order }
    }

    fun getActivityByDigest(digest: String): Activity? {
        return sections.flatMap { it.activities }.find { it.digest == digest }
    }

    fun getSectionByActivityDigest(digest: String): Section? {
        return sections.find { section -> section.activities.any { it.digest == digest } }
    }

    fun updateCourseActivity(ctx: Context?) {
        val db = DbHelper.getInstance(ctx)
        val userId = db.getUserId(SessionManager.getUsername(ctx))
        for (section in sections) {
            if (section.isProtectedByPassword) {
                section.isUnlocked = db.sectionUnlocked(courseId.toLong(), section.order, userId)
            }
            for (activity in section.activities) {
                activity.completed = db.activityCompleted(courseId, activity.digest, userId)
            }
        }
        for (activity in baselineActivities) {
            activity.isAttempted = db.activityAttempted(courseId.toLong(), activity.digest, userId)
        }
    }

    fun getActivities(courseId: Long): List<Activity> {
        val activities = ArrayList<Activity>()
        for (section in sections) {
            for (act in section.activities) {
                act.courseId = courseId
                activities.add(act)
            }
        }
        return activities
    }

    override val courseMedia: MutableList<Media>
        get() = media
}