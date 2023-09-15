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
import org.digitalcampus.oppia.utils.DateUtils
import org.digitalcampus.oppia.utils.TextUtilsJava
import org.joda.time.DateTime
import java.io.Serializable
import kotlin.math.roundToInt

class QuizAttempt : Serializable {
    var datetime: DateTime? = null
    var id: Long = 0
    var data: String? = null
    var activityDigest: String? = null
    var isSent = false
    var courseId: Long = 0
    var userId: Long = 0
    var score = 0f
    var maxscore = 0f
    var isPassed = false
    var user: User? = null
    var event: String? = null
    var points = 0
    var timetaken: Long = 0
    var type: String? = null
    var courseTitle: String? = null
    var quizTitle: String? = null
    var sectionTitle: String? = null

    companion object {
        @JvmField
        val TAG = QuizAttempt::class.simpleName
        const val TYPE_QUIZ = "quiz"
        const val TYPE_FEEDBACK = "feedback"

        @JvmStatic
        fun asJSONCollectionString(quizAttempts: Collection<QuizAttempt>): String {
            val jsonQuizAttempts = ArrayList<String?>()
            for (qa in quizAttempts) {
                if (qa.data != null) {
                    jsonQuizAttempts.add(qa.data)
                }
            }
            return "[" + TextUtilsJava.join(",", jsonQuizAttempts) + "]"
        }
    }

    fun setDateTimeFromString(date: String?) {
        datetime = DateUtils.DATETIME_FORMAT.parseDateTime(date)
    }

    fun getDateTimeString(): String {
        return DateUtils.DATETIME_FORMAT.print(datetime)
    }

    fun getScoreAsPercent(): Float {
        return score * 100 / maxscore
    }

    fun getScorePercentLabel(): String {
        return getScoreAsPercent().roundToInt().toString() + "%"
    }

    fun getHumanTimetaken(): String {
        val minutes = timetaken / 60
        val seconds = timetaken % 60
        return String.format("%d min %ds", minutes, seconds)
    }

    fun getDisplayTitle(ctx: Context): String {
        return if (sectionTitle == null || quizTitle == null) {
            ctx.getString(R.string.quiz_attempts_unknown_quiz)
        } else "$sectionTitle > $quizTitle"
    }

}