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
import org.digitalcampus.oppia.api.Paths
import org.digitalcampus.oppia.application.App
import org.digitalcampus.oppia.exception.CourseNotFoundException
import org.digitalcampus.oppia.exception.GamificationEventNotFound
import org.digitalcampus.oppia.model.coursecomplete.AllActivities
import org.digitalcampus.oppia.model.coursecomplete.AllQuizzes
import org.digitalcampus.oppia.model.coursecomplete.AllQuizzesPlusPercent
import org.digitalcampus.oppia.model.coursecomplete.FinalQuiz
import org.digitalcampus.oppia.utils.TextUtilsJava
import org.digitalcampus.oppia.utils.storage.Storage
import java.io.File
import java.io.Serializable

open class Course : MultiLangInfoModel, Serializable {

    companion object {
        @JvmField
        val TAG = Course::class.simpleName
        private const val serialVersionUID = 4412987572522420704L

        const val SEQUENCING_MODE_NONE = "none"
        const val SEQUENCING_MODE_SECTION = "section"
        const val SEQUENCING_MODE_COURSE = "course"

        const val COURSE_COMPLETE_ALL_ACTIVITIES = "all_activities"
        const val COURSE_COMPLETE_ALL_QUIZZES = "all_quizzes"
        const val COURSE_COMPLETE_FINAL_QUIZ = "final_quiz"
        const val COURSE_COMPLETE_ALL_QUIZZES_PLUS_PERCENT = "all_quizzes_plus_percent"

        const val STATUS_LIVE = "live"
        const val STATUS_DRAFT = "draft"
        const val STATUS_ARCHIVED = "archived"
        const val STATUS_NEW_DOWNLOADS_DISABLED = "new_downloads_disabled"
        const val STATUS_READ_ONLY = "read_only"

        const val JSON_PROPERTY_DESCRIPTION = "description"
        const val JSON_PROPERTY_TITLE = "title"
        const val JSON_PROPERTY_SHORTNAME = "shortname"
        const val JSON_PROPERTY_VERSION = "version"
        const val JSON_PROPERTY_URL = "url"
        const val JSON_PROPERTY_AUTHOR = "author"
        const val JSON_PROPERTY_USERNAME = "username"
        const val JSON_PROPERTY_ORGANISATION = "organisation"
        const val JSON_PROPERTY_STATUS = "status"
        const val JSON_PROPERTY_RESTRICTED = "restricted"
        const val JSON_PROPERTY_RESTRICTED_COHORTS = "cohorts"

        @JvmStatic
        fun getLocalFilename(shortname: String, versionID: Double?): String {
            return shortname + "-" + String.format("%.0f", versionID) + ".zip"
        }
    }

    var courseId = 0
    private var shortname: String? = null
    var versionId: Double = 0.0
    var status = STATUS_LIVE
    var isInstalled = false
    var isToUpdate = false
    var isToDelete = false
    var downloadUrl: String? = null
    var imageFile: String? = null
    var media: MutableList<Media> = ArrayList()
    var metaPages: List<CourseMetaPage> = ArrayList()
    var priority = 0
    var noActivities = 0
    var noActivitiesCompleted = 0
    var sequencingMode = SEQUENCING_MODE_NONE
    private var gamificationEvents: List<GamificationEvent> = ArrayList()
    var isRestricted = false
    var restrictedCohorts: List<Int>? = ArrayList()
    private var root: String? = null

    constructor() {}
    constructor(root: String?) {
        this.root = root
    }

    @Throws(CourseNotFoundException::class)
    fun validate(): Boolean {
        val courseXML = File(courseXMLLocation)
        return if (!courseXML.exists()) {
            throw CourseNotFoundException()
        } else {
            true
        }
    }

    fun getImageFileFromRoot(): String {
        return this.root + File.separator + Storage.APP_COURSES_DIR_NAME + File.separator + getShortname() + File.separator + imageFile
    }

    fun getTrackerLogUrl(): String {
        return String.format(Paths.COURSE_ACTIVITY_PATH, getShortname())
    }

    // prevent divide by zero errors
    fun getProgressPercent(): Float {
        // prevent divide by zero errors
        return if (noActivities != 0) {
            noActivitiesCompleted.toFloat() * 100f / noActivities.toFloat()
        } else {
            0f
        }
    }

    fun getShortname(): String? {
        return shortname?.lowercase()
    }

    fun setShortname(shortname: String) {
        this.shortname = shortname.lowercase()
    }

    fun getLocation(): String {
        return this.root + File.separator + Storage.APP_COURSES_DIR_NAME + File.separator + getShortname() + File.separator
    }

    val courseXMLLocation: String
        get() = getLocation() + App.COURSE_XML

    fun getMetaPage(id: Int): CourseMetaPage? {
        return metaPages.find { it.id == id }
    }

    fun setGamificationEvents(events: List<GamificationEvent>) {
        gamificationEvents = events
    }

    @Throws(GamificationEventNotFound::class)
    fun findGamificationEvent(event: String): GamificationEvent {
        return gamificationEvents.find { it.event  == event }
            ?: throw GamificationEventNotFound(event)
    }

    fun isComplete(ctx: Context?, user: User?, criteria: String?, percent: Int): Boolean {
        return when (criteria) {
            COURSE_COMPLETE_ALL_ACTIVITIES -> AllActivities.isComplete(ctx, courseId, user)
            COURSE_COMPLETE_ALL_QUIZZES -> AllQuizzes.isComplete(ctx, courseId, user)
            COURSE_COMPLETE_FINAL_QUIZ -> FinalQuiz.isComplete(ctx, courseId, user)
            COURSE_COMPLETE_ALL_QUIZZES_PLUS_PERCENT -> AllQuizzesPlusPercent.isComplete(ctx, courseId, user, percent )
            else -> false
        }
    }

    fun hasStatus(status: String?): Boolean {
        return TextUtilsJava.equals(this.status, status)
    }
}