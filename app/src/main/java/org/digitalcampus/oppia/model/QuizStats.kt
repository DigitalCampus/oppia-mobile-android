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

import android.util.Log
import java.io.Serializable

// TODO: Convert to data class when all references are migrated to kotlin
class QuizStats : Serializable {
    var digest: String? = null
    var numAttempts = 0
    var maxScore = -1f
    var userScore = -1f
    var averageScore = -1f
    var isPassed = false
    var quizTitle: String? = null
    var sectionTitle: String? = null

    companion object {
        @JvmField
        val TAG = QuizStats::class.simpleName
    }

    fun isAttempted(): Boolean {
        return numAttempts > 0
    }

    fun getPercent(): Int {
        Log.d(TAG, "userScore:$userScore")
        Log.d(TAG, "maxScore:$maxScore")
        val percent = (userScore * 100.0f / maxScore).toInt()
        Log.d(TAG, "percent:$percent")
        return percent
    }

    fun getAveragePercent(): Int {
        return (averageScore * 100.0f / maxScore).toInt()
    }

}