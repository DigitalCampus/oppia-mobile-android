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
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.quiz.Quiz
import org.digitalcampus.mobile.quiz.model.Response

class MatchingWidget(activity: Activity, v: View, container: ViewGroup) :
    QuestionWidget(activity, v, container, R.layout.widget_quiz_matching) {

    val TAG = MatchingWidget::class.simpleName

    private var responseLayouts: Array<LinearLayout?>? = null

    override fun setQuestionResponses(currentAnswers: List<String>) {
        // not used for this widget
    }

    override fun setQuestionResponses(responses: MutableList<Response>, currentAnswers: List<String>) {
        val responsesLL = view.findViewById<LinearLayout>(R.id.questionresponses)
        responsesLL.removeAllViews()

        // this could be tidied up - to use ArrayAdapters/Lists
        val possibleAnswers = HashMap<String, String>()
        val possibleAnswersShuffle = ArrayList<String?>()
        var noresponses = 0
        for (r in responses) {
            val temp = r.getTitle(currentUserLang).split(Quiz.MATCHING_REGEX.toRegex()).toTypedArray()
            if (temp[0] != "") {
                noresponses++
            }
            possibleAnswers[temp[0].trim()] = temp[1].trim()
            possibleAnswersShuffle.add(temp[1].trim())
        }
        val responseIt: Iterator<Map.Entry<String, String>> = possibleAnswers.entries.iterator()
        var counter = 0
        responseLayouts = arrayOfNulls(noresponses)

        while (responseIt.hasNext()) {
            val (key) = responseIt.next()
            // only add if there is question text
            if (key != "") {
                val responseLayout = LinearLayout(ctx)
                responseLayout.orientation = LinearLayout.VERTICAL
                val tv = TextView(ctx)
                tv.text = key
                tv.setTextColor(ctx.resources.getColor(R.color.text_dark))
                tv.setPaddingRelative(0, 0, 0, 8)
                val spinner = Spinner(ctx)
                val responseAdapter: ArrayAdapter<String> = object : ArrayAdapter<String>(ctx, R.layout.custom_spinner_item) {
                        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                            val view = super.getDropDownView(position, convertView, parent)
                            val finalView = view as TextView
                            view.post { finalView.isSingleLine = false }
                            return view
                        }
                    }
                responseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = responseAdapter

                responseAdapter.add("")
                possibleAnswersShuffle.shuffle()
                for (s in possibleAnswersShuffle) {
                    responseAdapter.add(s)
                }

                responseLayout.addView(tv)
                responseLayout.addView(spinner)

                // set the selected item based on current responses
                for (s in currentAnswers) {
                    val temp = s.split(Quiz.MATCHING_REGEX.toRegex()).toTypedArray()
                    if (temp[0].trim() == key) {
                        val i = responseAdapter.getPosition(temp[1].trim())
                        spinner.setSelection(i)
                    }
                }
                responsesLL.addView(responseLayout)
                responseLayouts?.set(counter, responseLayout)
                counter++
            }
        }
    }

    override fun getQuestionResponses(responses: MutableList<Response>): List<String> {
        val userResponses: MutableList<String> = ArrayList()
        for (ll in responseLayouts!!) {
            val tv = ll?.getChildAt(0) as TextView
            val sp = ll.getChildAt(1) as Spinner
            if (sp.selectedItem.toString().trim() != "") {
                val response = tv.text.toString().trim() + Quiz.MATCHING_SEPARATOR + sp.selectedItem.toString().trim()
                userResponses.add(response)
            }
        }
        if (userResponses.isEmpty()) {
            return ArrayList()
        }

        return userResponses
    }

    override fun getQuestionResponses(): List<String> {
        return ArrayList()
    }

}