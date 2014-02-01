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

package org.digitalcampus.oppia.adapter;

import java.util.ArrayList;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.QuizFeedback;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class QuizFeedbackAdapter extends ArrayAdapter<QuizFeedback> {

	public static final String TAG = QuizFeedbackAdapter.class.getSimpleName();

	private final Context ctx;
	private final ArrayList<QuizFeedback> quizFeedbackList;
	
	public QuizFeedbackAdapter(Activity context, ArrayList<QuizFeedback> quizFeedbackList) {
		super(context, R.layout.widget_quiz_feedback_row, quizFeedbackList);
		this.ctx = context;
		this.quizFeedbackList = quizFeedbackList;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.widget_quiz_feedback_row, parent, false);
	    QuizFeedback qf = quizFeedbackList.get(position);
	    
	    TextView qqt = (TextView) rowView.findViewById(R.id.quiz_question_text);
	    qqt.setText(qf.getQuestionText());
	    
	    TextView qqurt = (TextView) rowView.findViewById(R.id.quiz_question_user_response_text);
	    String userResponseText = "";
	    for (int i=0; i<qf.getUserResponse().size();i++){
	    	userResponseText += qf.getUserResponse().get(i);
	    	if (i+1<qf.getUserResponse().size()){
	    		userResponseText += "\n";
	    	}
	    }
	    qqurt.setText(userResponseText);
	    
	    TextView qqft = (TextView) rowView.findViewById(R.id.quiz_question_user_feedback_text);
	    if (qf.getFeedbackText() != null && !qf.getFeedbackText().equals("")){
	    	qqft.setText(qf.getFeedbackText());
	    } else {
	    	qqft.setVisibility(View.GONE);
	    	TextView qqftitle = (TextView) rowView.findViewById(R.id.quiz_question_user_feedback_title);
	    	qqftitle.setVisibility(View.GONE);
	    }
	    
		// set image
		ImageView iv = (ImageView) rowView.findViewById(R.id.quiz_question_feedback_image);
		if (qf.getScore() >= MobileLearning.QUIZ_PASS_THRESHOLD){
			iv.setImageResource(R.drawable.quiz_tick);
		} else {
			iv.setImageResource(R.drawable.quiz_cross);
		}
		
		
	    return rowView;
	}
}
