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
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.oppia.activity.AppActivity
import org.digitalcampus.oppia.api.ApiEndpoint
import org.digitalcampus.oppia.api.Paths
import org.digitalcampus.oppia.listener.APIRequestListener
import org.digitalcampus.oppia.task.APIUserRequestTask
import org.json.JSONException
import org.json.JSONObject

class TagRepository {

    companion object {
        val TAG = TagRepository::class.simpleName
        private const val JSON_PROPERTY_TAGS = "tags"
        private const val JSON_PROPERTY_NAME = "name"
        private const val JSON_PROPERTY_ID = "id"
        private const val JSON_PROPERTY_COUNT = "count"
        private const val JSON_PROPERTY_DESCRIPTION = "description"
        private const val JSON_PROPERTY_ICON = "icon"
        private const val JSON_PROPERTY_HIGHLIGHT = "highlight"
        private const val JSON_PROPERTY_ORDER_PRIORITY = "order_priority"
        private const val JSON_PROPERTY_COUNT_NEW_DOWNLOADS_ENABLED = "count_new_downloads_enabled"
        private const val JSON_PROPERTY_COURSE_STATUSES = "course_statuses"
    }

    fun getTagList(ctx: Context, api: ApiEndpoint?) {
        (ctx as AppActivity).showProgressDialog(ctx.getString(R.string.loading))
        val task = APIUserRequestTask(ctx, api)
        val url = Paths.SERVER_TAG_PATH
        task.setAPIRequestListener(ctx as APIRequestListener)
        task.execute(url)
    }

    @Throws(JSONException::class)
    fun refreshTagList(
        tags: MutableList<Tag?>,
        json: JSONObject,
        installedCoursesNames: List<String>
    ) {
        for (i in 0 until json.getJSONArray(JSON_PROPERTY_TAGS).length()) {
            val jsonObj = json.getJSONArray(JSON_PROPERTY_TAGS)[i] as JSONObject
            val t = Tag()
            t.name = jsonObj.getString(JSON_PROPERTY_NAME)
            t.id = jsonObj.getInt(JSON_PROPERTY_ID)
            t.setCount(jsonObj.getInt(JSON_PROPERTY_COUNT))

            // Description
            if (jsonObj.has(JSON_PROPERTY_DESCRIPTION) && !jsonObj.isNull(JSON_PROPERTY_DESCRIPTION)) {
                t.description = jsonObj.getString(JSON_PROPERTY_DESCRIPTION)
            }

            // icon
            if (jsonObj.has(JSON_PROPERTY_ICON) && !jsonObj.isNull(JSON_PROPERTY_ICON)) {
                t.icon = jsonObj.getString(JSON_PROPERTY_ICON)
            }

            // highlight
            if (jsonObj.has(JSON_PROPERTY_HIGHLIGHT) && !jsonObj.isNull(JSON_PROPERTY_HIGHLIGHT)) {
                t.isHighlight = jsonObj.getBoolean(JSON_PROPERTY_HIGHLIGHT)
            }

            // order priority
            if (jsonObj.has(JSON_PROPERTY_ORDER_PRIORITY) && !jsonObj.isNull(JSON_PROPERTY_ORDER_PRIORITY)) {
                t.orderPriority = jsonObj.getInt(JSON_PROPERTY_ORDER_PRIORITY)
            }

            // Count new downloads enabled
            if (jsonObj.has(JSON_PROPERTY_COUNT_NEW_DOWNLOADS_ENABLED) && !jsonObj.isNull(JSON_PROPERTY_COUNT_NEW_DOWNLOADS_ENABLED)) {
                t.countNewDownloadEnabled = jsonObj.getInt(JSON_PROPERTY_COUNT_NEW_DOWNLOADS_ENABLED)
            }

            if (jsonObj.has(JSON_PROPERTY_COURSE_STATUSES) && !jsonObj.isNull(JSON_PROPERTY_COURSE_STATUSES)) {
                t.countAvailable = t.countNewDownloadEnabled
                val jObjCourseStatuses = jsonObj.getJSONObject(JSON_PROPERTY_COURSE_STATUSES)
                val keys = jObjCourseStatuses.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val value = jObjCourseStatuses.getString(key)
                    if (isCourseInstalled(key, installedCoursesNames)) {
                        if (Course.STATUS_NEW_DOWNLOADS_DISABLED == value) {
                            t.incrementCountAvailable()
                        }
                    } else {
                        if (Course.STATUS_READ_ONLY == value) {
                            t.decrementCountAvailable()
                        }
                    }
                }
            } else {
                t.countAvailable = if (t.countNewDownloadEnabled > -1) t.countNewDownloadEnabled else t.getCount()
            }
            if (t.countAvailable > 0) {
                tags.add(t)
            }
        }
    }

    private fun isCourseInstalled(name: String, installedCoursesNames: List<String>): Boolean {
        return installedCoursesNames.contains(name)
    }
}