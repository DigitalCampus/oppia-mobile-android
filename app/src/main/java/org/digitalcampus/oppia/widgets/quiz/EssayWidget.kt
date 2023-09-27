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
package org.digitalcampus.oppia.widgets.quiz

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import org.digitalcampus.mobile.learning.R

class EssayWidget(activity: Activity, v: View, container: ViewGroup) :
    TextInputQuizWidget(activity, v, container, R.layout.widget_quiz_shortanswer) {

    val TAG = ShortAnswerWidget::class.simpleName

    override fun setQuestionResponses(currentAnswers: List<String>) {
        // not used for this widget
    }

    override fun getQuestionResponses(): List<String> {
        return ArrayList()
    }
}