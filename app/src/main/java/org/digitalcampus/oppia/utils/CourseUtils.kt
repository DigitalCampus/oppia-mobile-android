package org.digitalcampus.oppia.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.api.ApiEndpoint
import org.digitalcampus.oppia.api.Paths
import org.digitalcampus.oppia.database.DbHelper
import org.digitalcampus.oppia.listener.APIRequestListener
import org.digitalcampus.oppia.model.CompleteCourse
import org.digitalcampus.oppia.model.Course
import org.digitalcampus.oppia.model.Lang
import org.digitalcampus.oppia.model.responses.CourseServer
import org.digitalcampus.oppia.model.responses.CoursesServerResponse
import org.digitalcampus.oppia.task.APIUserRequestTask
import org.digitalcampus.oppia.task.result.BasicResult
import org.json.JSONArray
import org.json.JSONException
import kotlin.math.max

object CourseUtils {
    /**
     * Utility method to set both sync status (to update or to delete) and course status (draft, live...)
     * based on courses info cache
     *
     * @param prefs         SharedPreferences to get the cache data (passed by parameter to be mockable for tests)
     * @param courses       Installed courses in local database
     * @param fromTimestamp if not null, courses with version timestamp prior to this parameter are ignored
     */
    @JvmStatic
    fun refreshStatuses(prefs: SharedPreferences, courses: List<Course>, fromTimestamp: Double?) {
        if (courses.isEmpty()) {
            return
        }
        val coursesCachedStr = prefs.getString(PrefsActivity.PREF_SERVER_COURSES_CACHE, null)
        if (coursesCachedStr != null) {
            val coursesServerResponse = Gson().fromJson(
                    coursesCachedStr, CoursesServerResponse::class.java)
            for (course in courses) {
                coursesServerResponse.courses?.let {
                    checkCourseStatuses(course, it, fromTimestamp)
                }
            }
        } else {
            for (course in courses) {
                course.isToUpdate = false
                course.isToDelete = false
                course.status = Course.STATUS_LIVE
            }
        }
    }

    private fun checkCourseStatuses(course: Course, coursesServer: List<CourseServer>, fromTimestamp: Double?) {
        for (courseServer in coursesServer) {
            if (fromTimestamp != null && courseServer.version <= fromTimestamp) {
                continue
            }
            if (TextUtilsJava.equals(course.getShortname(), courseServer.shortname)) {
                val toUpdate = course.versionId < courseServer.version
                course.isToUpdate = toUpdate
                course.isToDelete = false
                course.status = courseServer.status
                return
            }
        }

        // If this line is reached is  because this course is not in the server list yet.
        course.isToDelete = true
    }

    @JvmStatic
    fun getNotInstalledCourses(prefs: SharedPreferences, coursesInstalled: List<Course>): List<CourseServer?>? {
        var notInstalledCourses: MutableList<CourseServer?>? = null
        val coursesCachedStr = prefs.getString(PrefsActivity.PREF_SERVER_COURSES_CACHE, null)
        if (coursesCachedStr != null) {
            val coursesServerResponse = Gson().fromJson(
                    coursesCachedStr, CoursesServerResponse::class.java)
            val coursesServer = coursesServerResponse.courses
            notInstalledCourses = ArrayList()
            if (coursesServer != null) {
                for (courseServer in coursesServer) {
                    if (!isCourseInstalled(courseServer, coursesInstalled)) {
                        notInstalledCourses.add(courseServer)
                    }
                }
            }
        }
        return notInstalledCourses
    }

    private fun isCourseInstalled(courseServer: CourseServer, coursesInstalled: List<Course>): Boolean {
        for (course in coursesInstalled) {
            if (TextUtilsJava.equals(course.getShortname(), courseServer.shortname)) {
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun updateCourseActivitiesWordCount(ctx: Context?, course: CompleteCourse) {
        val db = DbHelper.getInstance(ctx)
        val activities = course.getActivities(course.courseId.toLong())
        for (a in activities) {
            val langs = course.getLangs() as ArrayList<Lang>
            var wordCount = 0
            for (l in langs) {
                var langContents = a.getFileContents(course.getLocation(), l.language) ?: continue
                // strip out all html tags from string (not needed for search nor wordcount)
                langContents = langContents.replace("<.*?>".toRegex(), "").trim()
                val langWordCount = langContents.split("\\s+".toRegex()).size
                // We keep the highest wordcount among the different languages
                wordCount = max(wordCount, langWordCount)
            }
            val act = db.getActivityByDigest(a.digest)
            if (act != null && wordCount > 0) {
                db.updateActivityWordCount(act, wordCount)
            }
        }
    }

    fun isReadOnlyCourse(context: Context?, activityDigest: String?): Boolean {
        return isReadOnlyCourse(context, activityDigest, PreferenceManager.getDefaultSharedPreferences(context!!))
    }

    @JvmStatic
    fun isReadOnlyCourse(context: Context?, activityDigest: String?, prefs: SharedPreferences): Boolean {
        val db = DbHelper.getInstance(context)
        val activity = db.getActivityByDigest(activityDigest)
        val course = db.getCourse(activity.courseId)
        val coursesCachedStr = prefs.getString(PrefsActivity.PREF_SERVER_COURSES_CACHE, null)
        if (coursesCachedStr != null) {
            val coursesServerResponse = Gson().fromJson(
                    coursesCachedStr, CoursesServerResponse::class.java)
            for (courseServer in coursesServerResponse.courses!!) {
                if (TextUtilsJava.equals(courseServer.shortname, course.getShortname())) {
                    return courseServer.hasStatus(Course.STATUS_READ_ONLY)
                }
            }
        }
        return false
    }

    @JvmStatic
    fun refreshCachedData(context: Context?, course: Course) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context!!)
        val coursesCachedStr = prefs.getString(PrefsActivity.PREF_SERVER_COURSES_CACHE, null)
        if (coursesCachedStr != null) {
            val coursesServerResponse = Gson().fromJson(
                    coursesCachedStr, CoursesServerResponse::class.java)
            if (coursesServerResponse != null) {
                for (courseServer in coursesServerResponse.courses!!) {
                    if (TextUtilsJava.equals(courseServer.shortname, course.getShortname())) {
                        refreshCachedStatus(course, courseServer.status)
                        refreshCachedCohorts(course, courseServer.isRestricted, courseServer.restrictedCohorts)
                    }
                }
            }
        }
    }

    private fun refreshCachedStatus(course: Course, newStatus: String) {
        course.status = newStatus
    }

    private fun refreshCachedCohorts(course: Course, isRestricted: Boolean, cohorts: List<Int>?) {
        course.isRestricted = isRestricted
        course.restrictedCohorts = cohorts
    }

    @JvmStatic
    @Throws(JSONException::class)
    fun parseCourseCohortsFromJSONArray(cohortsJson: JSONArray): List<Int> {
        val cohorts: MutableList<Int> = ArrayList()
        for (i in 0 until cohortsJson.length()) {
            cohorts.add(cohortsJson.getInt(i))
        }
        return cohorts
    }

    @JvmStatic
    fun updateCourseCache(context: Context?, prefs: SharedPreferences, apiEndpoint: ApiEndpoint?) {
        val task = APIUserRequestTask(context, apiEndpoint)
        val url = Paths.SERVER_COURSES_PATH
        task.setAPIRequestListener(object : APIRequestListener {
            override fun apiRequestComplete(result: BasicResult) {
                if (result.isSuccess) {
                    prefs.edit()
                            .putLong(PrefsActivity.PREF_LAST_COURSES_CHECKS_SUCCESSFUL_TIME, System.currentTimeMillis())
                            .putString(PrefsActivity.PREF_SERVER_COURSES_CACHE, result.resultMessage)
                            .apply()
                }
            }

            override fun apiKeyInvalidated() {}
        })
        task.execute(url)
    }
}