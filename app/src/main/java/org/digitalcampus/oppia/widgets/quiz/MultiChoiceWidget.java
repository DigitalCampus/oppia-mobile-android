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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MultiChoiceWidget extends QuestionWidget{

	public static final String TAG = MultiChoiceWidget.class.getSimpleName();

	private QuizQuestion question;
	
	public MultiChoiceWidget(Activity activity, View v, ViewGroup container, QuizQuestion q) {
		super(activity, v, container, R.layout.widget_quiz_multichoice);
		this.question = q;
	}

	@Override
	public void setQuestionResponses(List<String> currentAnswers) {
		// not used for this widget
	}

	@Override
	public void setQuestionResponses(List<Response> responses, List<String> currentAnswer) {
		LinearLayout responsesLL = view.findViewById(R.id.questionresponses);
    	responsesLL.removeAllViews();
    	RadioGroup responsesRG = new RadioGroup(ctx);
    	responsesRG.setId(R.id.multichoiceRadioGroup);
    	responsesLL.addView(responsesRG);
		String shuffle = question.getProp("shuffleanswers");
		if ((shuffle != null) && shuffle.equals("1")){
			Collections.shuffle(responses);
		}

    	int id = 1000+1;
    	for (Response r : responses){
    		RadioButton rb = new RadioButton(ctx);
			RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
			setResponseMarginInLayoutParams(params);
    		rb.setId(id);
			rb.setText(r.getTitle(currentUserLang));
			responsesRG.addView(rb, params);
            for (String answer : currentAnswer) {
                if (answer.equals(r.getTitle(currentUserLang))){
                    rb.setChecked(true);
                }
            }
			id++;
    	}
		
	}
	
	public List<String> getQuestionResponses(List<Response> responses){
		RadioGroup responsesRG = view.findViewById(R.id.multichoiceRadioGroup);
		int resp = responsesRG.getCheckedRadioButtonId();
    	View rb = responsesRG.findViewById(resp);
    	int idx = responsesRG.indexOfChild(rb);
    	if (idx >= 0){
    		List<String> response = new ArrayList<>();
			response.add(responses.get(idx).getTitle(currentUserLang));
    		return response;
    	}
		return new ArrayList<>();
	}

	@Override
	public List<String> getQuestionResponses() {
		return new ArrayList<>();
	}

}
