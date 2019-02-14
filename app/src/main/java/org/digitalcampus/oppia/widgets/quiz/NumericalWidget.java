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
import java.util.Iterator;
import java.util.List;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.model.Response;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class NumericalWidget extends TextInputQuizWidget {

	public static final String TAG = NumericalWidget.class.getSimpleName();
	
	public NumericalWidget(Activity activity, View v, ViewGroup container) {
		super(activity, v, container, R.layout.widget_quiz_numerical);
	}


	@Override
	public void setQuestionResponses(List<Response> responses, List<String> currentAnswers) {
		EditText et = view.findViewById(R.id.responsetext);
		Iterator<String> itr = currentAnswers.iterator(); 
		while(itr.hasNext()) {
		    String answer = itr.next();
		    et.setText(answer);
		}
        hideOnFocusLoss(et);

	}
	
	public List<String> getQuestionResponses(List<Response> responses){
		EditText et = view.findViewById(R.id.responsetext);
		if(et.getText().toString().trim().equals("")){
			return new ArrayList<>();
		} else {
			List<String> response = new ArrayList<>();
			response.add(et.getText().toString().trim());
			return response;
		}
	}

}