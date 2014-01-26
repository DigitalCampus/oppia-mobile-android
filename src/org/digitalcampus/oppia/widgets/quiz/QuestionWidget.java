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

import java.util.List;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.model.Response;

import android.app.Activity;
import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public abstract class QuestionWidget {

	protected Context ctx;
	protected View view;
	
	// Abstract methods
	public abstract void setQuestionResponses(List<Response> responses, List<String> currentAnswers);

	public abstract List<String> getQuestionResponses(List<Response> responses);
	
	protected void init(Activity activity, ViewGroup container, int layout, View v){
		this.ctx = new ContextThemeWrapper(activity, R.style.Oppia_Theme);
		this.view = v;
		
		LinearLayout ll = (LinearLayout) v.findViewById(R.id.quiz_response_widget);
		ll.removeAllViews();
		LayoutInflater localInflater = ((LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).cloneInContext(ctx);
		View vv = localInflater.inflate(layout ,container, false);
		ll.addView(vv);
	}

}
