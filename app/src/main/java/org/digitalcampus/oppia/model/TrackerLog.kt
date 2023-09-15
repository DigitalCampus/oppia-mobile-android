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

import org.digitalcampus.oppia.utils.DateUtils
import org.digitalcampus.oppia.utils.TextUtilsJava
import org.joda.time.DateTime

class TrackerLog {
    var datetime: DateTime? = null
    var id: Long = 0
    var digest: String? = null
    var content: String? = null
    var type: String? = null
    var isSubmitted = false
    var isCompleted = false
    var courseId: Long = 0
    var userId: Long = 0
    var event: String? = null
    var points = 0

    companion object {
        @JvmStatic
        fun asJSONCollectionString(trackerLogs: Collection<TrackerLog?>?): String {
            return "[" + TextUtilsJava.join(",", trackerLogs) + "]"
        }
    }

    fun getDateTimeString(): String {
        return DateUtils.DATETIME_FORMAT.print(datetime)
    }

    override fun toString(): String {
        return content!!
    }
}