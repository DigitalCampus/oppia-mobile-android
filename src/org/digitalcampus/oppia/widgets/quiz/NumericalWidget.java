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
import android.widget.EditText;

public class NumericalWidget extends QuestionWidget {

	public static final String TAG = NumericalWidget.class.getSimpleName();
	
	public NumericalWidget(Activity activity, View v, ViewGroup container) {
		init(activity, container, R.layout.widget_quiz_numerical, v);
	}


	@Override
	public void setQuestionResponses(List<Response> responses, List<String> currentAnswers) {
		EditText et = (EditText) view.findViewById(R.id.responsetext);
		Iterator<String> itr = currentAnswers.iterator(); 
		while(itr.hasNext()) {
		    String answer = itr.next(); 
		    et.setText(answer);
		} 
	}
	
	public List<String> getQuestionResponses(List<Response> responses){
		EditText et = (EditText) view.findViewById(R.id.responsetext);
		if(et.getText().toString().equals("")){
			return null;
		} else {
			List<String> response = new ArrayList<String>();
			response.add(et.getText().toString());
			return response;
		}
	}

}