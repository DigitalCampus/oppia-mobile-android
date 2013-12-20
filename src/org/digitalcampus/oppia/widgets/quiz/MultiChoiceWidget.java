/* 
 * This file is part of OppiaMobile - http://oppia-mobile.org/
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
import java.util.Iterator;
import java.util.List;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.model.Response;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class MultiChoiceWidget extends QuestionWidget{

	public static final String TAG = MultiChoiceWidget.class.getSimpleName();
	
	public MultiChoiceWidget(Activity activity, View v, ViewGroup container) {
		init(activity,container,R.layout.widget_quiz_multichoice,v);
	}

	public void setQuestionResponses(List<Response> responses, List<String> currentAnswer) {
		LinearLayout responsesLL = (LinearLayout) view.findViewById(R.id.questionresponses);
    	responsesLL.removeAllViews();
    	RadioGroup responsesRG = new RadioGroup(ctx);
    	// TODO change to use getchild views (like the MultiSelect)
    	responsesRG.setId(234523465);
    	responsesLL.addView(responsesRG);
    	int id = 1000;
    	for (Response r : responses){
    		RadioButton rb = new RadioButton(ctx);
    		rb.setId(id);
			rb.setText(r.getTitle());
			responsesRG.addView(rb);
			Iterator<String> itr = currentAnswer.iterator();
			while(itr.hasNext()) {
				String answer = itr.next(); 
				if (r.getTitle() == answer){
					rb.setChecked(true);
				}
			}
			id++;
    	}
		
	}
	
	public List<String> getQuestionResponses(List<Response> responses){
		// TODO change to use getchild views (like the MultiSelect)
		RadioGroup responsesRG = (RadioGroup) view.findViewById(234523465);
		int resp = responsesRG.getCheckedRadioButtonId();
    	View rb = responsesRG.findViewById(resp);
    	int idx = responsesRG.indexOfChild(rb);
    	if (idx >= 0){
    		List<String> response = new ArrayList<String>();
			response.add(responses.get(idx).getTitle());
    		return response;
    	}
    	return null;
	}

}
