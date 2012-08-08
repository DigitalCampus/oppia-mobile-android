package org.digitalcampus.mtrain.widgets;

import org.digitalcampus.mquiz.MQuiz;
import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.model.Activity;
import org.digitalcampus.mtrain.model.Module;

import android.content.Context;
import android.util.Log;
import android.view.View;

public class QuizWidget extends WidgetFactory{

	private static final String TAG = "QuizWidget";
	
	private Context ctx;
	private MQuiz mQuiz;
	
	public QuizWidget(Context context, Module module, Activity activity) {
		super(context, module, activity);
		this.ctx = context;
		View vv = super.getLayoutInflater().inflate(R.layout.widget_quiz, null);
		super.getLayout().addView(vv);
		
		// TODO error check that content is in the hashmap
		String quiz = activity.getActivity().get("content");
		mQuiz = new MQuiz();
		mQuiz.load(quiz);
	}

}
