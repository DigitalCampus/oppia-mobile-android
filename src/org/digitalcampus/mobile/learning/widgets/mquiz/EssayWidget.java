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

package org.digitalcampus.mobile.learning.widgets.mquiz;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.digitalcampus.mquiz.model.Response;
import org.digitalcampus.mobile.learning.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

public class EssayWidget extends QuestionWidget {

	public static final String TAG = EssayWidget.class.getSimpleName();
	
	private Context ctx;
	
	public EssayWidget(Context context) {
		this.ctx = context;
		
		LinearLayout ll = (LinearLayout) ((Activity) ctx).findViewById(R.id.quizResponseWidget);
		ll.removeAllViews();
		LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vv = vi.inflate(R.layout.widget_mquiz_essay, null);
		ll.addView(vv);
	}

	public void setQuestionResponses(List<Response> responses, List<String> currentAnswers) {
		EditText et = (EditText) ((Activity) ctx).findViewById(R.id.responsetext);
		Iterator<String> itr = currentAnswers.iterator(); 
		while(itr.hasNext()) {
		    String answer = itr.next(); 
		    et.setText(answer);
		} 
	}
	
	public List<String> getQuestionResponses(List<Response> responses){
		EditText et = (EditText) ((Activity) ctx).findViewById(R.id.responsetext);
		if(et.getText().toString().equals("")){
			return null;
		} else {
			List<String> response = new ArrayList<String>();
			response.add(et.getText().toString());
			return response;
		}
	}

}
