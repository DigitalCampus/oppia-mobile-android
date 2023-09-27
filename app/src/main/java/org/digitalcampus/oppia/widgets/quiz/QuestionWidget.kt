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
import android.content.SharedPreferences
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.preference.PreferenceManager
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.quiz.model.Response
import org.digitalcampus.oppia.activity.PrefsActivity
import java.util.Locale

abstract class QuestionWidget {

    protected lateinit var ctx: Context
    protected lateinit var view: View
    protected lateinit var prefs: SharedPreferences
    protected lateinit var currentUserLang: String

    constructor() {}

    // Abstract methods
    abstract fun setQuestionResponses(responses: MutableList<Response>, currentAnswers: List<String>)
    abstract fun setQuestionResponses(currentAnswers: List<String>)
    abstract fun getQuestionResponses(responses: MutableList<Response>) : List<String>
    abstract fun getQuestionResponses() : List<String>

    protected constructor(activity: Activity, v: View, container: ViewGroup, layout: Int) {
        ctx = ContextThemeWrapper(activity, R.style.Oppia_Theme)
        prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        currentUserLang = prefs.getString(PrefsActivity.PREF_CONTENT_LANGUAGE, Locale.getDefault().language) ?: Locale.getDefault().language
        view = v
        val ll = v.findViewById<LinearLayout>(R.id.quiz_response_widget)
        ll.removeAllViews()
        val localInflater = (ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).cloneInContext(ctx)
        val vv = localInflater.inflate(layout, container, false)
        ll.addView(vv)
    }

    protected fun setResponseMarginInLayoutParams(params: LinearLayout.LayoutParams) {
        params.setMargins(
            0,
            ctx.resources.getDimension(R.dimen.quiz_response_margin).toInt(),
            0,
            0
        )
    }
}