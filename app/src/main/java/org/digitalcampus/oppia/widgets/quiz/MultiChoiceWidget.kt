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
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.quiz.model.QuizQuestion
import org.digitalcampus.mobile.quiz.model.Response
import org.digitalcampus.oppia.utils.UIUtils.getFromHtmlAndTrim

class MultiChoiceWidget(activity: Activity, v: View, container: ViewGroup, private val question: QuizQuestion)
    : QuestionWidget(activity, v, container, R.layout.widget_quiz_multichoice) {

    val TAG = MultiChoiceWidget::class.simpleName

    override fun setQuestionResponses(currentAnswers: List<String>) {
        // not used for this widget
    }

    override fun setQuestionResponses(responses: MutableList<Response>, currentAnswers: List<String>) {
        val responsesLL = view.findViewById<LinearLayout>(R.id.questionresponses)
        responsesLL.removeAllViews()
        val responsesRG = RadioGroup(ctx)
        responsesRG.id = R.id.multichoiceRadioGroup
        responsesLL.addView(responsesRG)
        val shuffle = question.getProp("shuffleanswers")
        if (shuffle != null && shuffle == "1") {
            responses.shuffle()
        }
        var id = 1000 + 1
        for (r in responses) {
            val rb = RadioButton(ctx)
            val params = RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setResponseMarginInLayoutParams(params)
            rb.id = id
            rb.text = getFromHtmlAndTrim(r.getTitle(currentUserLang))
            responsesRG.addView(rb, params)
            for (answer in currentAnswers) {
                if (answer == r.getTitle(currentUserLang)) {
                    rb.isChecked = true
                }
            }
            id++
        }
    }

    override fun getQuestionResponses(responses: MutableList<Response>): List<String> {
        val responsesRG = view.findViewById<RadioGroup>(R.id.multichoiceRadioGroup)
        val resp = responsesRG.checkedRadioButtonId
        val rb = responsesRG.findViewById<View>(resp)
        val idx = responsesRG.indexOfChild(rb)
        val response: MutableList<String> = ArrayList()
        if (idx >= 0) {
            response.add(responses[idx].getTitle(currentUserLang))
        }
        return response
    }

    override fun getQuestionResponses(): List<String> {
        return ArrayList()
    }

}