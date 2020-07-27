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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.Response;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.core.text.HtmlCompat;

public class MultiSelectWidget extends QuestionWidget {

	public static final String TAG = MultiSelectWidget.class.getSimpleName();
	private LinearLayout responsesLL;
	private QuizQuestion question;
	
	public MultiSelectWidget(Activity activity, View v, ViewGroup container, QuizQuestion q) {
		super(activity, v, container, R.layout.widget_quiz_multiselect);
		this.question = q;
	}

	@Override
	public void setQuestionResponses(List<String> currentAnswers) {
		// not used for this widget
	}

	@Override
	public void setQuestionResponses(List<Response> responses, List<String> currentAnswer) {
		responsesLL = view.findViewById(R.id.questionresponses);
    	responsesLL.removeAllViews();

		String shuffle = question.getProp("shuffleanswers");
		if ((shuffle != null) && shuffle.equals("1")){
			Collections.shuffle(responses);
		}

    	for (Response r : responses){
    		CheckBox chk= new CheckBox(ctx);
			chk.setText(HtmlCompat.fromHtml(r.getTitle(currentUserLang), HtmlCompat.FROM_HTML_MODE_LEGACY));
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
			setResponseMarginInLayoutParams(params);
			responsesLL.addView(chk, params);
			for (String a : currentAnswer) {
				if (a.equals(r.getTitle(currentUserLang))) {
					chk.setChecked(true);
				}
			}
    	}	
	}

	@Override
	public List<String> getQuestionResponses(List<Response> responses) {
		int count = responsesLL.getChildCount();
		List<String> response = new ArrayList<>();
		for (int i=0; i<count; i++) {
			CheckBox cb = (CheckBox) responsesLL.getChildAt(i);
			if(cb.isChecked()){
				response.add(cb.getText().toString());
			}
		}

		if(response.isEmpty()){
			return new ArrayList<>();
		} else {
			return response;
		}
	}

	@Override
	public List<String> getQuestionResponses() {
		return new ArrayList<>();
	}

}
