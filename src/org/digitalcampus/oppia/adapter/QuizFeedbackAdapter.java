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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.oppia.model.QuizFeedback;

import java.util.ArrayList;

public class QuizFeedbackAdapter extends ArrayAdapter<QuizFeedback> {

	public static final String TAG = QuizFeedbackAdapter.class.getSimpleName();

	private final Context ctx;
	private final ArrayList<QuizFeedback> quizFeedbackList;
	
	public QuizFeedbackAdapter(Activity context, ArrayList<QuizFeedback> quizFeedbackList) {
		super(context, R.layout.widget_quiz_feedback_row, quizFeedbackList);
		this.ctx = context;
		this.quizFeedbackList = quizFeedbackList;
	}

    static class QuizFeedbackViewHolder{
        TextView quizQuestion;
        TextView quizUserResponse;
        TextView quizFeedbackTitle;
        TextView quizFeedbackText;
        ImageView quizFeedbackIcon;
    }
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

        QuizFeedbackViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView  = inflater.inflate(R.layout.widget_quiz_feedback_row, parent, false);
            viewHolder = new QuizFeedbackViewHolder();
            viewHolder.quizQuestion = (TextView) convertView.findViewById(R.id.quiz_question_text);
            viewHolder.quizUserResponse = (TextView) convertView.findViewById(R.id.quiz_question_user_response_text);
            viewHolder.quizFeedbackText = (TextView) convertView.findViewById(R.id.quiz_question_user_feedback_text);
            viewHolder.quizFeedbackTitle = (TextView) convertView.findViewById(R.id.quiz_question_user_feedback_title);
            viewHolder.quizFeedbackIcon = (ImageView) convertView.findViewById(R.id.quiz_question_feedback_image);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (QuizFeedbackViewHolder) convertView.getTag();
        }

	    QuizFeedback qf = quizFeedbackList.get(position);
        String userResponseText = "";
        for (int i=0; i<qf.getUserResponse().size();i++){
            userResponseText += qf.getUserResponse().get(i);
            if (i+1<qf.getUserResponse().size()){
                userResponseText += "\n";
            }
        }

        viewHolder.quizQuestion.setText(qf.getQuestionText());
        viewHolder.quizUserResponse.setText(userResponseText);

	    if (qf.getFeedbackText() != null && !qf.getFeedbackText().equals("")){
            viewHolder.quizFeedbackText.setText(qf.getFeedbackText());
	    } else {
            //If there's no feedback to show, hide both text and title
            viewHolder.quizFeedbackTitle.setVisibility(View.GONE);
            viewHolder.quizFeedbackText.setVisibility(View.GONE);
	    }

        viewHolder.quizFeedbackIcon.setImageResource(
                (qf.getScore() >= Quiz.QUIZ_QUESTION_PASS_THRESHOLD)?
                R.drawable.quiz_tick:
                R.drawable.quiz_cross
        );
		
	    return convertView;
	}
}
