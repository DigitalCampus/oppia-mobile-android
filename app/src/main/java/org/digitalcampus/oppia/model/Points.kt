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
import org.joda.time.DateTime
import java.util.Locale

class Points {
    var dateTime: DateTime? = null
        private set
    var event: String? = null
    var description: String? = null
    var pointsAwarded = 0

    fun getDateDayMonth(): String {
        return DateUtils.DATE_FORMAT_DAY_MONTH.print(dateTime)
    }

    fun getTimeHoursMinutes(): String {
        return DateUtils.TIME_FORMAT_HOURS_MINUTES.print(dateTime)
    }

    fun setDateTime(date: String?) {
        dateTime = DateUtils.DATETIME_FORMAT.parseDateTime(date)
    }

    fun getDescriptionPrettified(): String? {
        return description?.let { capitalize(it).replace("_", " ") }
    }

    private fun capitalize(str: String): String {
        return str.substring(0, 1).uppercase(Locale.getDefault()) + str.substring(1)
    }
}