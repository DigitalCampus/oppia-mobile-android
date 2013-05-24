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

package org.digitalcampus.mobile.learning.widgets;

import java.util.List;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.application.DbHelper;
import org.digitalcampus.mobile.learning.model.Activity;
import org.digitalcampus.mobile.learning.model.Module;
import org.digitalcampus.mobile.learning.widgets.mquiz.EssayWidget;
import org.digitalcampus.mobile.learning.widgets.mquiz.MatchingWidget;
import org.digitalcampus.mobile.learning.widgets.mquiz.MultiChoiceWidget;
import org.digitalcampus.mobile.learning.widgets.mquiz.MultiSelectWidget;
import org.digitalcampus.mobile.learning.widgets.mquiz.NumericalWidget;
import org.digitalcampus.mobile.learning.widgets.mquiz.QuestionWidget;
import org.digitalcampus.mobile.learning.widgets.mquiz.ShortAnswerWidget;
import org.digitalcampus.mquiz.MQuiz;
import org.digitalcampus.mquiz.model.QuizQuestion;
import org.digitalcampus.mquiz.model.questiontypes.Essay;
import org.digitalcampus.mquiz.model.questiontypes.Matching;
import org.digitalcampus.mquiz.model.questiontypes.MultiChoice;
import org.digitalcampus.mquiz.model.questiontypes.MultiSelect;
import org.digitalcampus.mquiz.model.questiontypes.Numerical;
import org.digitalcampus.mquiz.model.questiontypes.ShortAnswer;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;

public class MQuizWidget extends WidgetFactory {

	private static final String TAG = MQuizWidget.class.getSimpleName();
	private SharedPreferences prefs;
	private android.app.Activity ctx;
	private MQuiz mQuiz;
	private QuestionWidget qw;
	public Button prevBtn;
	public Button nextBtn;
	private TextView qText;
	private String quizContent;
	private boolean isComplete = false;
	private Module module; 
	private long startTimestamp = System.currentTimeMillis()/1000;
	private long endTimestamp = System.currentTimeMillis()/1000;
	
	public MQuizWidget(android.app.Activity context, Module module, Activity activity) {
		super(context, module, activity);
		this.ctx = context;
		this.module = module;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		View vv = super.getLayoutInflater().inflate(R.layout.widget_mquiz, null);
		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		super.getLayout().addView(vv);
		vv.setLayoutParams(lp);

		prevBtn = (Button) ((android.app.Activity) this.ctx).findViewById(R.id.mquiz_prev_btn);
		nextBtn = (Button) ((android.app.Activity) this.ctx).findViewById(R.id.mquiz_next_btn);
		qText = (TextView) ((android.app.Activity) this.ctx).findViewById(R.id.questiontext);
		
		quizContent = activity.getContents(prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage()));
		mQuiz = new MQuiz();
		mQuiz.load(quizContent);

		this.showQuestion();
	}

	public void showQuestion() {
		QuizQuestion q = mQuiz.getCurrentQuestion();
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
		
		if (mQuiz.hasPrevious()) {
			prevBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					// save answer
					saveAnswer();

					if (mQuiz.hasPrevious()) {
						mQuiz.movePrevious();
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
					String feedback = mQuiz.getCurrentQuestion().getFeedback();
					if (!feedback.equals("")) {
						showFeedback(feedback);
					} else if (mQuiz.hasNext()) {
						mQuiz.moveNext();
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
		if (mQuiz.hasNext()) {
			nextBtn.setText(((android.app.Activity) ctx).getString(R.string.widget_mquiz_next));
		} else {
			nextBtn.setText(((android.app.Activity) ctx).getString(R.string.widget_mquiz_getresults));
		}
	}

	private void setProgress() {
		TextView progress = (TextView) ((android.app.Activity) this.ctx).findViewById(R.id.mquiz_progress);
		progress.setText(((android.app.Activity) this.ctx).getString(R.string.widget_mquiz_progress,
				mQuiz.getCurrentQuestionNo(), mQuiz.getTotalNoQuestions()));

	}

	private boolean saveAnswer() {
		List<String> answers = qw.getQuestionResponses(mQuiz.getCurrentQuestion().getResponseOptions());
		if (answers != null) {
			mQuiz.getCurrentQuestion().setUserResponses(answers);
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
				if (mQuiz.hasNext()) {
					mQuiz.moveNext();
					showQuestion();
				} else {
					showResults();
				}
			}
		});
		builder.show();
	}

	private void showResults() {
		mQuiz.mark();
		float percent = mQuiz.getUserscore() * 100 / mQuiz.getMaxscore();
		
		// log the activity as complete
		isComplete = true;
		
		// make end time
		endTimestamp = System.currentTimeMillis()/1000;
		
		// save results ready to send back to the mquiz server
		String data = mQuiz.getResultObject().toString();
		DbHelper db = new DbHelper(ctx);
		db.insertMQuizResult(data, module.getModId());
		db.close();
		//Log.d(TAG,data);
		
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
		Log.d(TAG,"restarting");
		mQuiz = new MQuiz();
		mQuiz.load(quizContent);
		startTimestamp = System.currentTimeMillis()/1000;
		endTimestamp = System.currentTimeMillis()/1000;
		this.showQuestion();
	}
	
	public boolean activityHasTracker(){
		return isComplete;
	}
	
	public boolean activityCompleted(){
		if(isComplete){
			mQuiz.mark();
			float percent = mQuiz.getUserscore() * 100 / mQuiz.getMaxscore();
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
			obj.put("quiz_id", mQuiz.getID());
			obj.put("instance_id", mQuiz.getInstanceID());
			mQuiz.mark();
			float percent = mQuiz.getUserscore() * 100 / mQuiz.getMaxscore();
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
	public void mediaStopped() {
		// do nothing
	}

	@Override
	public boolean getMediaPlaying() {
		return false;
	}
	
	@Override
	public long getMediaStartTime() {
		return 0;
	}

	@Override
	public void setMediaPlaying(boolean playing) {
		// do nothing
		
	}

	@Override
	public void setMediaStartTime(long mediaStartTime) {
		// do nothing
		
	}

	@Override
	public String getMediaFileName() {
		return null;
	}

	@Override
	public void setMediaFileName(String mediaFileName) {
		// do nothing
	}

	@Override
	public void setStartTime(long startTime) {
		this.startTimestamp = startTime;
		
	}

	@Override
	public long getStartTime() {
		return this.startTimestamp;
	}
	
	public MQuiz getMquiz(){
		return mQuiz;
	}
	
	@Override
	public void setReadAloud(boolean reading){
		//do nothing
	}
	
	@Override
	public boolean getReadAloud(){
		return false;
	}
}
