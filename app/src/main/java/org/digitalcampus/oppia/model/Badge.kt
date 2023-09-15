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

class Badge {
    private var datetime: DateTime? = null
    var description: String? = null
    var icon: String? = null
    var certificatePdf: String? = null

    companion object {
        const val BADGE_CRITERIA_ALL_ACTIVITIES = "all_activities"
        const val BADGE_CRITERIA_ALL_QUIZZES = "all_quizzes"
        const val BADGE_CRITERIA_FINAL_QUIZ = "final_quiz"
        const val BADGE_CRITERIA_ALL_QUIZZES_PERCENT = "all_quizzes_plus_percent"
    }

    constructor() {}
    constructor(datetime: DateTime?, description: String?) {
        this.datetime = datetime
        this.description = description
    }

    fun getDateAsString(): String? {
        return DateUtils.DATE_FORMAT.print(datetime)
    }

    fun setDateTime(date: String?) {
        datetime = DateUtils.DATETIME_FORMAT.parseDateTime(date)
    }

}