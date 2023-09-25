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
import android.content.Context
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.quiz.model.Response

abstract class TextInputQuizWidget(activity: Activity, v: View, container: ViewGroup, layout: Int) : QuestionWidget(activity, v, container, layout) {

    protected fun hideOnFocusLoss(et: EditText) {
        val ofcListener: OnFocusChangeListener = ResponseTextFocusChangeListener()
        et.onFocusChangeListener = ofcListener
    }

    private inner class ResponseTextFocusChangeListener : OnFocusChangeListener {
        override fun onFocusChange(v: View, hasFocus: Boolean) {
            if (v.id == R.id.responsetext && !hasFocus) {
                val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
            }
        }
    }

    override fun setQuestionResponses(responses: MutableList<Response>, currentAnswers: List<String>) {
        val et = view.findViewById<EditText>(R.id.responsetext)
        val itr = currentAnswers.iterator()
        while (itr.hasNext()) {
            val answer = itr.next()
            et.setText(answer)
        }
        hideOnFocusLoss(et)
    }

    override fun getQuestionResponses(responses: MutableList<Response>): List<String> {
        val et = view.findViewById<EditText>(R.id.responsetext)
        return if (et.text.toString().trim() == "") {
            ArrayList()
        } else {
            val response: MutableList<String> = ArrayList()
            response.add(et.text.toString().trim())
            response
        }
    }
}