package org.digitalcampus.oppia.widgets.quiz;

import java.util.List;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.model.Response;

import android.app.Activity;
import android.content.Context;
import android.widget.LinearLayout;

public class DescriptionWidget extends QuestionWidget{

	public static final String TAG = DescriptionWidget.class.getSimpleName();
	private Context ctx;
	
	public DescriptionWidget(Context context) {
		this.ctx = context;
		
		LinearLayout ll = (LinearLayout) ((Activity) ctx).findViewById(R.id.quizResponseWidget);
		ll.removeAllViews();
	}
	
	@Override
	public void setQuestionResponses(List<Response> responses, List<String> currentAnswers) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<String> getQuestionResponses(List<Response> responses) {
		// TODO Auto-generated method stub
		return null;
	}

}
