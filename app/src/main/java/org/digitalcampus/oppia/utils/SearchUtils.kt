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
package org.digitalcampus.oppia.utils

import android.content.Context
import android.util.Log

import org.digitalcampus.oppia.analytics.Analytics
import org.digitalcampus.oppia.database.DbHelper
import org.digitalcampus.oppia.exception.InvalidXMLException
import org.digitalcampus.oppia.model.CompleteCourse
import org.digitalcampus.oppia.model.Course
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader

object SearchUtils {
    val TAG = SearchUtils::class.simpleName

    @JvmStatic
    fun indexAddCourse(ctx: Context, course: CompleteCourse) {
        val activities = course.getActivities(course.courseId.toLong())
        val db = DbHelper.getInstance(ctx)

        db.beginTransaction()
        for (a in activities) {
            val langs = course.langs
            val fileContents = StringBuilder()
            for (l in langs) {
                var langContents = a.getFileContents(course.location, l.language) ?: continue
                // strip out all html tags from string (not needed for search)
                langContents = langContents.replace("<.*?>".toRegex(), "").trim()
                fileContents.append(langContents)
            }
            val act = db.getActivityByDigest(a.digest)
            if (act != null && fileContents.isNotEmpty()) {
                db.insertActivityIntoSearchTable(course.titleJSONString,
                        course.getSection(a.sectionId).titleJSONString,
                        a.titleJSONString,
                        act.dbId,
                        fileContents.toString())
            }
        }
        db.endTransaction(true)
    }

    fun indexAddCourse(ctx: Context, course: Course) {
        try {
            val cxr = CourseXMLReader(course.courseXMLLocation, course.courseId.toLong(), ctx)
            cxr.parse(CourseXMLReader.ParseMode.COMPLETE)
            indexAddCourse(ctx, cxr.getParsedCourse())
        } catch (e: InvalidXMLException) {
            // Ignore course
            Analytics.logException(e)
            Log.d(TAG, "InvalidXMLException:", e)
        }
    }

}