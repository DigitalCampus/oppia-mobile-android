package org.digitalcampus.mtrain.widgets;

import org.digitalcampus.mquiz.MQuiz;
import org.digitalcampus.mquiz.model.QuizQuestion;
import org.digitalcampus.mquiz.model.questiontypes.Essay;
import org.digitalcampus.mquiz.model.questiontypes.Matching;
import org.digitalcampus.mquiz.model.questiontypes.MultiChoice;
import org.digitalcampus.mquiz.model.questiontypes.MultiSelect;
import org.digitalcampus.mquiz.model.questiontypes.Numerical;
import org.digitalcampus.mquiz.model.questiontypes.ShortAnswer;
import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.model.Activity;
import org.digitalcampus.mtrain.model.Module;
import org.digitalcampus.mtrain.widgets.mquiz.*;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MQuizWidget extends WidgetFactory{

	private static final String TAG = "QuizWidget";
	
	private Context ctx;
	private MQuiz mQuiz;
	private QuestionWidget qw;
	
	public MQuizWidget(Context context, Module module, Activity activity) {
		super(context, module, activity);
		this.ctx = context;
		View vv = super.getLayoutInflater().inflate(R.layout.widget_mquiz, null);
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
    	} else if(q instanceof Essay){
    		qw = new EssayWidget(this.ctx);
    	} else if(q instanceof MultiSelect){
    		qw = new MultiSelectWidget(this.ctx);
    	} else if(q instanceof ShortAnswer){
    		qw = new ShortAnswerWidget(this.ctx);
    	} else if(q instanceof Matching){
    		qw = new MatchingWidget(this.ctx);
    	} else if(q instanceof Numerical){
    		qw = new NumericalWidget(this.ctx);
    	} else {
    		Log.d(TAG,"Class for question type not found");
    		return;
    	}
		TextView qHint = (TextView) ((android.app.Activity) this.ctx).findViewById(R.id.questionhint);
    	if(q.getQhint().equals("")){
    		qHint.setVisibility(View.GONE);
    	} else {
    		qHint.setText(q.getQhint());
    		qHint.setVisibility(View.VISIBLE);
    	}
		qw.setQuestionResponses(q.getResponseOptions(), q.getUserResponses());
		this.setNav();
	}
	
	public void setNav(){
		
	}

}
