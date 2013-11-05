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

package org.digitalcampus.oppia.widgets;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mquiz.MQuiz;
import org.digitalcampus.mquiz.model.QuizQuestion;
import org.digitalcampus.mquiz.model.questiontypes.Essay;
import org.digitalcampus.mquiz.model.questiontypes.Matching;
import org.digitalcampus.mquiz.model.questiontypes.MultiChoice;
import org.digitalcampus.mquiz.model.questiontypes.MultiSelect;
import org.digitalcampus.mquiz.model.questiontypes.Numerical;
import org.digitalcampus.mquiz.model.questiontypes.ShortAnswer;
import org.digitalcampus.oppia.activity.ModuleActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.gesture.QuizGestureDetector;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Module;
import org.digitalcampus.oppia.widgets.quiz.EssayWidget;
import org.digitalcampus.oppia.widgets.quiz.MatchingWidget;
import org.digitalcampus.oppia.widgets.quiz.MultiChoiceWidget;
import org.digitalcampus.oppia.widgets.quiz.MultiSelectWidget;
import org.digitalcampus.oppia.widgets.quiz.NumericalWidget;
import org.digitalcampus.oppia.widgets.quiz.QuestionWidget;
import org.digitalcampus.oppia.widgets.quiz.ShortAnswerWidget;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;

public class QuizWidget extends WidgetFactory {

	private static final String TAG = QuizWidget.class.getSimpleName();
	private SharedPreferences prefs;
	private android.app.Activity ctx;
	private MQuiz quiz;
	private QuestionWidget qw;
	public Button prevBtn;
	public Button nextBtn;
	private TextView qText;
	private String quizContent;
	private boolean isComplete = false;
	private Module module; 
	private long startTimestamp = System.currentTimeMillis()/1000;
	private long endTimestamp = System.currentTimeMillis()/1000;
	private boolean isBaselineActivity = false;
	
	private GestureDetector quizGestureDetector;
	private OnTouchListener quizGestureListener; 
	
	public QuizWidget(android.app.Activity context, Module module, Activity activity) {
		super(context, module, activity);
		this.ctx = context;
		this.module = module;
		this.startQuiz(activity);
	}

	public QuizWidget(android.app.Activity context, Module module, Activity activity, HashMap<String, Object> config) {
		super(context, module, activity);
		this.ctx = context;
		this.module = module;
		this.setWidgetConfig(config);
		this.startQuiz(activity);
	}
	
	
	private void startQuiz(Activity activity){
		this.prefs = PreferenceManager.getDefaultSharedPreferences(this.ctx);
		
		quizGestureDetector = new GestureDetector((android.app.Activity) this.ctx, new QuizGestureDetector((ModuleActivity) this.ctx));
		quizGestureListener = new OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				try {
					//  for some reason unless this is in a try/catch block it will fail with NullPointerException
					return quizGestureDetector.onTouchEvent(event);
				} catch (Exception e){
					return false;
				}
			}
		};
		View vv = super.getLayoutInflater().inflate(R.layout.widget_quiz, null);
		
		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		super.getLayout().addView(vv);
		vv.setLayoutParams(lp);

		ScrollView sv = (ScrollView) this.ctx.findViewById(R.id.quizScrollView);
		sv.setOnTouchListener(quizGestureListener);

		prevBtn = (Button) ((android.app.Activity) this.ctx).findViewById(R.id.mquiz_prev_btn);
		nextBtn = (Button) ((android.app.Activity) this.ctx).findViewById(R.id.mquiz_next_btn);
		qText = (TextView) ((android.app.Activity) this.ctx).findViewById(R.id.questiontext);
		
		if(this.quiz == null){
			quizContent = activity.getContents(prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage()));
			this.quiz = new MQuiz();
			this.quiz.load(quizContent);
		}

		this.showQuestion();
	}
	
	public void showQuestion() {
		QuizQuestion q = this.quiz.getCurrentQuestion();
		qText.setVisibility(View.VISIBLE);
		qText.setText(q.getTitle());

		if (q instanceof MultiChoice) {
			qw = new MultiChoiceWidget(this.ctx);
		} else if (q instanceof Essay) {
			qw = new EssayWidget(this.ctx);
		} else if (q instanceof MultiSelect) {
			qw = new MultiSelectWidget(this.ctx);
		} else if (q instanceof ShortAnswer) {
			qw = new ShortAnswerWidget(this.ctx);
		} else if (q instanceof Matching) {
			qw = new MatchingWidget(this.ctx);
		} else if (q instanceof Numerical) {
			qw = new NumericalWidget(this.ctx);
		} else {
			Log.d(TAG, "Class for question type not found");
			return;
		}
		qw.setQuestionResponses(q.getResponseOptions(), q.getUserResponses());
		this.setProgress();
		this.setNav();
	}

	private void setNav() {
		nextBtn.setVisibility(View.VISIBLE);
		prevBtn.setVisibility(View.VISIBLE);
		
		if (this.quiz.hasPrevious()) {
			prevBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					// save answer
					saveAnswer();

					if (QuizWidget.this.quiz.hasPrevious()) {
						QuizWidget.this.quiz.movePrevious();
						showQuestion();
					}
				}
			});
			prevBtn.setEnabled(true);
		} else {
			prevBtn.setEnabled(false);
		}

		
		nextBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// save answer
				if (saveAnswer()) {
					String feedback = QuizWidget.this.quiz.getCurrentQuestion().getFeedback();
					if (!feedback.equals("") && !isBaselineActivity) {
						showFeedback(feedback);
					} else if (QuizWidget.this.quiz.hasNext()) {
						QuizWidget.this.quiz.moveNext();
						showQuestion();
					} else {
						showResults();
					}
				} else {
					CharSequence text = ((android.app.Activity) ctx).getString(R.string.widget_mquiz_noanswergiven);
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(ctx, text, duration);
					toast.show();
				}
			}
		});

		// set label on next button
		if (quiz.hasNext()) {
			nextBtn.setText(((android.app.Activity) ctx).getString(R.string.widget_mquiz_next));
		} else {
			nextBtn.setText(((android.app.Activity) ctx).getString(R.string.widget_mquiz_getresults));
		}
	}

	private void setProgress() {
		TextView progress = (TextView) ((android.app.Activity) this.ctx).findViewById(R.id.mquiz_progress);
		progress.setText(((android.app.Activity) this.ctx).getString(R.string.widget_mquiz_progress,
				quiz.getCurrentQuestionNo(), quiz.getTotalNoQuestions()));

	}

	private boolean saveAnswer() {
		List<String> answers = qw.getQuestionResponses(quiz.getCurrentQuestion().getResponseOptions());
		if (answers != null) {
			quiz.getCurrentQuestion().setUserResponses(answers);
			return true;
		}
		return false;
	}

	private void showFeedback(String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder((android.app.Activity) this.ctx);
		builder.setTitle(ctx.getString(R.string.feedback));
		builder.setMessage(msg);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1) {
				if (QuizWidget.this.quiz.hasNext()) {
					QuizWidget.this.quiz.moveNext();
					showQuestion();
				} else {
					showResults();
				}
			}
		});
		builder.show();
	}

	private void showResults() {
		quiz.mark();
		float percent = quiz.getUserscore() * 100 / quiz.getMaxscore();
		
		// log the activity as complete
		isComplete = true;
		
		// make end time
		endTimestamp = System.currentTimeMillis()/1000;
		
		// save results ready to send back to the mquiz server
		String data = quiz.getResultObject().toString();
		DbHelper db = new DbHelper(ctx);
		db.insertMQuizResult(data, module.getModId());
		db.close();
		Log.d(TAG,data);
		Log.d(TAG,"id: " + module.getModId());
		
		LinearLayout responsesLL = (LinearLayout) ((android.app.Activity) ctx).findViewById(R.id.quizResponseWidget);
    	responsesLL.removeAllViews();
		nextBtn.setVisibility(View.GONE);
		prevBtn.setVisibility(View.GONE);
		qText.setVisibility(View.GONE);
		
		// set page heading
		TextView progress = (TextView) ((android.app.Activity) this.ctx).findViewById(R.id.mquiz_progress);
		progress.setText(((android.app.Activity) this.ctx).getString(R.string.widget_mquiz_results));
		
		// show final score
		TextView intro = new TextView(this.ctx);
		intro.setText(((android.app.Activity) this.ctx).getString(R.string.widget_mquiz_results_intro));
		intro.setGravity(Gravity.CENTER);
		intro.setTextSize(20);
		responsesLL.addView(intro);
		
		TextView score = new TextView(this.ctx);
		score.setText(((android.app.Activity) this.ctx).getString(R.string.widget_mquiz_results_score,percent));
		score.setTextSize(60);
		score.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		score.setGravity(Gravity.CENTER);
		score.setPadding(0, 20, 0, 20);
		responsesLL.addView(score);
		
		Button restartBtn = new Button(this.ctx);
		restartBtn.setText(((android.app.Activity) this.ctx).getString(R.string.widget_mquiz_results_restart));
		restartBtn.setTextSize(20);
		restartBtn.setTypeface(Typeface.DEFAULT_BOLD);
		restartBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				restart();
			}
		});
		
		responsesLL.addView(restartBtn);
	}

	private void restart() {
		quiz = new MQuiz();
		quiz.load(quizContent);
		startTimestamp = System.currentTimeMillis()/1000;
		endTimestamp = System.currentTimeMillis()/1000;
		this.showQuestion();
	}
	
	public boolean activityHasTracker(){
		return isComplete;
	}
	
	public boolean activityCompleted(){
		if(isComplete){
			quiz.mark();
			float percent = quiz.getUserscore() * 100 / quiz.getMaxscore();
			if (percent > 99){
				return true;
			}
		}
		return false;
	}
	
	public long getTimeTaken(){
		return (endTimestamp - startTimestamp);
	}

	public JSONObject getTrackerData(){
		JSONObject obj = new JSONObject();
		try {
			obj.put("timetaken", this.getTimeTaken());
			String lang = prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage());
			obj.put("lang", lang);
			obj.put("quiz_id", quiz.getID());
			obj.put("instance_id", quiz.getInstanceID());
			quiz.mark();
			float percent = quiz.getUserscore() * 100 / quiz.getMaxscore();
			obj.put("score", percent);
		} catch (JSONException e) {
			e.printStackTrace();
			BugSenseHandler.sendException(e);
		}
		
		return obj;
	}
	
	@Override
	public String getContentToRead() {
		return "";
	}

	@Override
	public void setStartTime(long startTime) {
		this.startTimestamp = startTime;
		
	}

	@Override
	public long getStartTime() {
		return this.startTimestamp;
	}

	
	@Override
	public void setReadAloud(boolean reading){
		//do nothing
	}
	
	@Override
	public boolean getReadAloud(){
		return false;
	}

	public MQuiz getQuiz() {
		return this.quiz;
	}

	private void setQuiz(MQuiz quiz) {
		this.quiz = quiz;
	}

	@Override
	public void setBaselineActivity(boolean baseline) {
		this.isBaselineActivity = baseline;
	}

	@Override
	public boolean isBaselineActivity() {
		return this.isBaselineActivity;
	}

	@Override
	public HashMap<String, Object> getWidgetConfig() {
		HashMap<String, Object> config = new HashMap<String, Object>();
		this.saveAnswer();
		config.put("quiz", this.getQuiz());
		return config;
	}

	@Override
	public void setWidgetConfig(HashMap<String, Object> config) {
		if (config.containsKey("quiz")){
			this.setQuiz((MQuiz) config.get("quiz"));
		}
		
	}
}
