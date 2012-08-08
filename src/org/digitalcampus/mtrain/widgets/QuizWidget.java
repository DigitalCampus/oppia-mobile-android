package org.digitalcampus.mtrain.widgets;

import org.digitalcampus.mquiz.MQuiz;
import org.digitalcampus.mquiz.model.QuizQuestion;
import org.digitalcampus.mquiz.model.questiontypes.MultiChoice;
import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.model.Activity;
import org.digitalcampus.mtrain.model.Module;
import org.digitalcampus.mtrain.widgets.mquiz.*;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

public class QuizWidget extends WidgetFactory{

	private static final String TAG = "QuizWidget";
	
	private Context ctx;
	private MQuiz mQuiz;
	private QuestionWidget qw;
	
	public QuizWidget(Context context, Module module, Activity activity) {
		super(context, module, activity);
		this.ctx = context;
		View vv = super.getLayoutInflater().inflate(R.layout.widget_quiz, null);
		super.getLayout().addView(vv);
		
		// TODO error check that "content" is in the hashmap
		String quiz = activity.getActivity().get("content");
		mQuiz = new MQuiz();
		mQuiz.load(quiz);
		
		this.showQuestion();
	}
	
	public void showQuestion(){
		QuizQuestion q = mQuiz.getCurrentQuestion();
		TextView qText = (TextView) ((android.app.Activity) this.ctx).findViewById(R.id.questiontext);
		qText.setText(q.getQtext());
		
		if(q instanceof MultiChoice){
    		qw = new MultiChoiceWidget(this.ctx);
    	}
		
		qw.setQuestionResponses(q.getResponseOptions(), q.getUserResponses());
		this.setNav();
	}
	
	public void setNav(){
		
	}

}
