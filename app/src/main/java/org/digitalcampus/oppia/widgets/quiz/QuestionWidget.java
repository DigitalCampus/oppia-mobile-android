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

package org.digitalcampus.oppia.widgets.quiz;

import java.util.List;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.model.Response;
import org.digitalcampus.oppia.activity.PrefsActivity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public abstract class QuestionWidget {

	protected Context ctx;
	protected View view;
	protected SharedPreferences prefs;
	protected String currentUserLang;

	public QuestionWidget() {}

	// Abstract methods
	public abstract void setQuestionResponses(List<Response> responses, List<String> currentAnswers);
	public abstract void setQuestionResponses(List<String> currentAnswers);

	public abstract List<String> getQuestionResponses(List<Response> responses);
	public abstract List<String> getQuestionResponses();

	protected QuestionWidget(Activity activity, View v, ViewGroup container, int layout){
		ctx = new ContextThemeWrapper(activity, R.style.Oppia_Theme);
		prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		currentUserLang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
		view = v;

		LinearLayout ll = v.findViewById(R.id.quiz_response_widget);
		ll.removeAllViews();
		LayoutInflater localInflater = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).cloneInContext(ctx);
		View vv = localInflater.inflate(layout, container, false);
		ll.addView(vv);
	}

	protected void setResponseMarginInLayoutParams(LinearLayout.LayoutParams params){
		params.setMargins(0, (int) ctx.getResources().getDimension(R.dimen.quiz_response_margin), 0, 0);
	}

}
