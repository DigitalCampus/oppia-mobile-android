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

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.InvalidQuizException;
import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.questiontypes.Description;
import org.digitalcampus.mobile.quiz.model.questiontypes.Essay;
import org.digitalcampus.mobile.quiz.model.questiontypes.Matching;
import org.digitalcampus.mobile.quiz.model.questiontypes.MultiChoice;
import org.digitalcampus.mobile.quiz.model.questiontypes.MultiSelect;
import org.digitalcampus.mobile.quiz.model.questiontypes.Numerical;
import org.digitalcampus.mobile.quiz.model.questiontypes.ShortAnswer;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.Tracker;
import org.digitalcampus.oppia.listener.OnResourceClickListener;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.digitalcampus.oppia.widgets.quiz.DescriptionWidget;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

public class QuizWidget extends WidgetFactory {

	private static final String TAG = QuizWidget.class.getSimpleName();
	private android.app.Activity ctx;
	private Quiz quiz;
	private QuestionWidget qw;
	public Button prevBtn;
	public Button nextBtn;
	private TextView qText;
	private String quizContent;
	private LinearLayout questionImage;
	private boolean isOnResultsPage = false; 
	
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
		ctx = super.getActivity();
		course = (Course) getArguments().getSerializable(Course.TAG);
		activity = ((Activity) getArguments().getSerializable(Activity.TAG));
		this.setIsBaseline(getArguments().getBoolean(CourseActivity.BASELINE_TAG));
		quizContent = ((Activity) getArguments().getSerializable(Activity.TAG)).getContents(prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage()));
		View vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.widget_quiz, null);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		vv.setLayoutParams(lp);
		return vv;
	}
	
	 @Override
	 public void onActivityCreated(Bundle savedInstanceState) { 
		super.onActivityCreated(savedInstanceState);
		prevBtn = (Button) ((android.app.Activity) this.ctx).findViewById(R.id.mquiz_prev_btn);
		nextBtn = (Button) ((android.app.Activity) this.ctx).findViewById(R.id.mquiz_next_btn);
		qText = (TextView) ((android.app.Activity) this.ctx).findViewById(R.id.question_text);
		questionImage = (LinearLayout) ((android.app.Activity) this.ctx).findViewById(R.id.question_image);
		
		if(this.quiz == null){
			this.quiz = new Quiz();
			this.quiz.load(quizContent);
		}
		if (this.isOnResultsPage){
			this.showResults();
		} else {
			this.showQuestion();
		}
	}
	
	public void showQuestion() {
		QuizQuestion q = null;
		try {
			q = this.quiz.getCurrentQuestion();
		} catch (InvalidQuizException e) {
			Toast.makeText(this.ctx, this.ctx.getString(R.string.error_quiz_no_questions), Toast.LENGTH_LONG).show();
			e.printStackTrace();
			return;
		}
		qText.setVisibility(View.VISIBLE);
		// convert in case has any html special chars
		qText.setText(Html.fromHtml(q.getTitle()).toString());
		
		if (q.getProp("image") == null){
			questionImage.setVisibility(View.GONE);
		} else {
			String fileUrl = course.getLocation() + q.getProp("image");
			//File file = new File(fileUrl);
			Bitmap myBitmap = BitmapFactory.decodeFile(fileUrl);
			File file = new File(fileUrl);
			ImageView iv = (ImageView) ((android.app.Activity) this.ctx).findViewById(R.id.question_image_image);
			iv.setImageBitmap(myBitmap);
			iv.setTag(file);
			OnResourceClickListener orcl = new OnResourceClickListener(this.ctx, "image/*");
			iv.setOnClickListener(orcl);
			questionImage.setVisibility(View.VISIBLE);
		}

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
		} else if (q instanceof Description) {
			qw = new DescriptionWidget(this.ctx);
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
					String feedback = "";
					try {
						feedback = QuizWidget.this.quiz.getCurrentQuestion().getFeedback();
					} catch (InvalidQuizException e) {
						e.printStackTrace();
					}
					if (!feedback.equals("") && !isBaseline) {
						showFeedback(feedback);
					} else if (QuizWidget.this.quiz.hasNext()) {
						QuizWidget.this.quiz.moveNext();
						showQuestion();
					} else {
						showResults();
					}
				} else {
					CharSequence text = ((android.app.Activity) ctx).getString(R.string.widget_quiz_noanswergiven);
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(ctx, text, duration);
					toast.show();
				}
			}
		});

		// set label on next button
		if (quiz.hasNext()) {
			nextBtn.setText(((android.app.Activity) ctx).getString(R.string.widget_quiz_next));
		} else {
			nextBtn.setText(((android.app.Activity) ctx).getString(R.string.widget_quiz_getresults));
		}
	}

	private void setProgress() {
		TextView progress = (TextView) ((android.app.Activity) this.ctx).findViewById(R.id.mquiz_progress);
		try {
			if (quiz.getCurrentQuestion().responseExpected()){
				progress.setText(((android.app.Activity) this.ctx).getString(R.string.widget_quiz_progress,
					quiz.getCurrentQuestionNo(), quiz.getTotalNoQuestions()));
			} else {
				progress.setText("");
			}
		} catch (InvalidQuizException e) {
			e.printStackTrace();
		}

	}

	private boolean saveAnswer() {
		try {
			List<String> answers = qw.getQuestionResponses(quiz.getCurrentQuestion().getResponseOptions());
			if (answers != null) {
				quiz.getCurrentQuestion().setUserResponses(answers);
				return true;
			}
			if(!quiz.getCurrentQuestion().responseExpected()){
				return true;
			}
		} catch (InvalidQuizException e){
			e.printStackTrace();
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

	public void showResults() {
		quiz.mark();
		float percent = quiz.getUserscore() * 100 / quiz.getMaxscore();
		this.saveTracker();
		// log the activity as complete
		isOnResultsPage = true;
		
		// save results ready to send back to the quiz server
		String data = quiz.getResultObject().toString();
		DbHelper db = new DbHelper(ctx);
		db.insertQuizResult(data, course.getModId());
		db.close();
		Log.d(TAG,data);
		
		LinearLayout responsesLL = (LinearLayout) ((android.app.Activity) ctx).findViewById(R.id.quizResponseWidget);
    	responsesLL.removeAllViews();
		nextBtn.setVisibility(View.GONE);
		prevBtn.setVisibility(View.GONE);
		qText.setVisibility(View.GONE);
		questionImage.setVisibility(View.GONE);
		
		if (this.isBaseline){
			TextView progress = (TextView) ((android.app.Activity) this.ctx).findViewById(R.id.mquiz_progress);
			progress.setText("");
			
			TextView intro = new TextView(this.ctx);
			intro.setText(((android.app.Activity) this.ctx).getString(R.string.widget_quiz_baseline_completed));
			intro.setGravity(Gravity.CENTER);
			intro.setTextSize(20);
			intro.setPadding(0, 20, 0, 50);
			responsesLL.addView(intro);
			
			Button restartBtn = new Button(this.ctx);
			restartBtn.setText(((android.app.Activity) this.ctx).getString(R.string.widget_quiz_baseline_goto_course));
			restartBtn.setTextSize(20);
			restartBtn.setTypeface(Typeface.DEFAULT_BOLD);
			restartBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					QuizWidget.this.ctx.finish();
				}
			});
			
			responsesLL.addView(restartBtn);
		} else {
			// set page heading
			TextView progress = (TextView) ((android.app.Activity) this.ctx).findViewById(R.id.mquiz_progress);
			progress.setText(((android.app.Activity) this.ctx).getString(R.string.widget_quiz_results));
			
			// show final score
			TextView intro = new TextView(this.ctx);
			intro.setText(((android.app.Activity) this.ctx).getString(R.string.widget_quiz_results_intro));
			intro.setGravity(Gravity.CENTER);
			intro.setTextSize(20);
			responsesLL.addView(intro);
			
			TextView score = new TextView(this.ctx);
			score.setText(((android.app.Activity) this.ctx).getString(R.string.widget_quiz_results_score,percent));
			score.setTextSize(60);
			score.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			score.setGravity(Gravity.CENTER);
			score.setPadding(0, 20, 0, 20);
			responsesLL.addView(score);
			
			
			Button restartBtn = new Button(this.ctx);
			restartBtn.setText(((android.app.Activity) this.ctx).getString(R.string.widget_quiz_results_restart));
			restartBtn.setTextSize(20);
			restartBtn.setTypeface(Typeface.DEFAULT_BOLD);
			restartBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					restart();
				}
			});
			
			responsesLL.addView(restartBtn);
		}
	}

	private void restart() {
		this.startTime = System.currentTimeMillis()/1000;
		quiz = new Quiz();
		quiz.load(quizContent);
		isOnResultsPage = false;
		this.showQuestion();
	}

	@Override
	protected boolean getActivityCompleted() {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	protected void saveTracker(){
		long timetaken = System.currentTimeMillis()/1000 - startTime;
		Tracker t = new Tracker(ctx);
		JSONObject obj = new JSONObject();
		MetaDataUtils mdu = new MetaDataUtils(ctx);
		// add in extra meta-data
		try {
			obj.put("timetaken", timetaken);
			obj = mdu.getMetaData(obj);
			String lang = prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage());
			obj.put("lang", lang);
			obj.put("quiz_id", quiz.getID());
            obj.put("instance_id", quiz.getInstanceID());
            quiz.mark();
            float percent = quiz.getUserscore() * 100 / quiz.getMaxscore();
            obj.put("score", percent);
		} catch (JSONException e) {
			// Do nothing
		} 
		// if it's a baseline activity then assume completed
		if(this.isBaseline){
			t.saveTracker(course.getModId(), activity.getDigest(), obj, true);
		} else {
			t.saveTracker(course.getModId(), activity.getDigest(), obj, this.getActivityCompleted());
		}
	}
}
