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
import org.digitalcampus.oppia.database.DbHelper
import org.digitalcampus.oppia.service.courseinstall.CourseInstallerService
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class CourseInstallViewAdapter(root: String?) : Course(root) {
    //Extension for UI purposes
    var isDownloading = false
    var isInstalling = false
    var progress = 0
    private var authorUsername: String? = null
    private var authorName: String? = null
    var organisationName: String? = null

    val isToInstall: Boolean
        get() = !isInstalled || isToUpdate

    val isInProgress: Boolean
        get() = isInstalling || isDownloading

    fun setAuthorUsername(authorUsername: String?) {
        this.authorUsername = authorUsername
    }

    fun setAuthorName(authorName: String?) {
        this.authorName = authorName
    }

    val displayAuthorName: String?
        get() {
            return authorName
        }

    companion object {
        private const val serialVersionUID = -4251898143809197224L

        const val SERVER_COURSES_NAME = "courses"


        @Throws(JSONException::class)
        fun parseCoursesJSON(
            ctx: Context?, json: JSONObject?, location: String?, onlyAddUpdates: Boolean
        ): List<CourseInstallViewAdapter> {
            val downloadingCourses = CourseInstallerService.getTasksDownloading() as ArrayList<String?>
            val courses = ArrayList<CourseInstallViewAdapter>()
            json?.getJSONArray(SERVER_COURSES_NAME)?.let { coursesArray ->
                for (i in 0 until coursesArray.length()) {
                    val jsonObj = coursesArray.getJSONObject(i)
                    val course = CourseInstallViewAdapter(location)

                    val titles = jsonObj.getJSONObject(JSON_PROPERTY_TITLE).toLangList()
                    course.setTitles(titles)

                    val descriptions = jsonObj.optJSONObject(JSON_PROPERTY_DESCRIPTION)?.toLangList()
                    if (descriptions != null) {
                        course.setDescriptions(descriptions)
                    }

                    course.setShortname(jsonObj.getString(JSON_PROPERTY_SHORTNAME))
                    course.versionId = jsonObj.getDouble(JSON_PROPERTY_VERSION)
                    course.downloadUrl = jsonObj.getString(JSON_PROPERTY_URL)

                    jsonObj.optBoolean(JSON_PROPERTY_RESTRICTED).let { courseRestricted ->
                        course.isRestricted = courseRestricted
                        if (courseRestricted) {
                            val restrictedCohorts = jsonObj.getJSONArray(JSON_PROPERTY_RESTRICTED_COHORTS)
                                .toIntegerList()
                            course.restrictedCohorts = restrictedCohorts
                        }
                    }

                    course.status = jsonObj.optString(JSON_PROPERTY_STATUS, "")
                    course.setAuthorName(jsonObj.optString(JSON_PROPERTY_AUTHOR, ""))
                    course.setAuthorUsername(jsonObj.optString(JSON_PROPERTY_USERNAME, ""))

                    val db = DbHelper.getInstance(ctx)
                    course.isInstalled = db.isInstalled(course.getShortname())
                    course.isToUpdate = db.toUpdate(course.getShortname(), course.versionId)
                    course.organisationName = jsonObj.optString(JSON_PROPERTY_ORGANISATION, "")
                    course.isDownloading = downloadingCourses.contains(course.downloadUrl) == true

                    if (!onlyAddUpdates || course.isToUpdate) {
                        courses.add(course)
                    }
                }
            }
            return courses
        }

        private fun JSONObject.toLangList(): MutableList<Lang> {
            val langList = mutableListOf<Lang>()
            keys().forEach { key ->
                val value = optString(key)
                if (value.isNotEmpty()) {
                    langList.add(Lang(key, value))
                }
            }
            return langList
        }

        private fun JSONArray.toIntegerList(): MutableList<Int> {
            val integerList = mutableListOf<Int>()
            for (i in 0 until length()) {
                integerList.add(optInt(i, 0))
            }
            return integerList
        }
    }
}