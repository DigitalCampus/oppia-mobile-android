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
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.quiz.model.QuizQuestion
import org.digitalcampus.mobile.quiz.model.Response
import org.digitalcampus.oppia.utils.UIUtils.getFromHtmlAndTrim

class MultiSelectWidget(activity: Activity, v: View, container: ViewGroup, private val question: QuizQuestion)
    : QuestionWidget(activity, v, container, R.layout.widget_quiz_multiselect) {

    val TAG = MultiSelectWidget::class.simpleName

    private var responsesLL: LinearLayout? = null

    override fun setQuestionResponses(currentAnswers: List<String>) {
        // not used for this widget
    }

    override fun setQuestionResponses(responses: MutableList<Response>, currentAnswers: List<String>) {
        responsesLL = view.findViewById(R.id.questionresponses)
        responsesLL?.removeAllViews()

        val shuffle = question.getProp("shuffleanswers")
        if (shuffle != null && shuffle == "1") {
            responses.shuffle()
        }

        for (r in responses) {
            val chk = CheckBox(ctx)
            chk.text = getFromHtmlAndTrim(r.getTitle(currentUserLang))
            val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setResponseMarginInLayoutParams(params)
            responsesLL?.addView(chk, params)
            for (a in currentAnswers) {
                if (a == r.getTitle(currentUserLang)) {
                    chk.isChecked = true
                }
            }
        }
    }

    override fun getQuestionResponses(responses: MutableList<Response>): List<String> {
        val count = responsesLL?.childCount ?: 0
        val responsesSelected: MutableList<String> = ArrayList()
        for (i in 0 until count) {
            val cb = responsesLL?.getChildAt(i) as CheckBox
            val responseText = responses[i].getTitle(currentUserLang)
            if (cb.isChecked) {
                responsesSelected.add(responseText)
                Log.d(TAG, "User selected: " + cb.text.toString())
            } else {
                responsesSelected.remove(responseText)
            }
        }
        return responsesSelected
    }

    override fun getQuestionResponses(): List<String> {
        return ArrayList()
    }
}