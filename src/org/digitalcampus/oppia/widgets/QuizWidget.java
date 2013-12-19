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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.InvalidQuizException;
import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.questiontypes.Description;
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
import org.digitalcampus.oppia.widgets.quiz.MatchingWidget;
import org.digitalcampus.oppia.widgets.quiz.MultiChoiceWidget;
import org.digitalcampus.oppia.widgets.quiz.MultiSelectWidget;
import org.digitalcampus.oppia.widgets.quiz.NumericalWidget;
import org.digitalcampus.oppia.widgets.quiz.QuestionWidget;
import org.digitalcampus.oppia.widgets.quiz.ShortAnswerWidget;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.ContextThemeWrapper;
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
	private Context ctx;
	private Quiz quiz;
	private QuestionWidget qw;
	public Button prevBtn;
	public Button nextBtn;
	private TextView qText;
	private String quizContent;
	private LinearLayout questionImage;
	private boolean isOnResultsPage = false;
	private ViewGroup container;

	public static QuizWidget newInstance(Activity activity, Course course, boolean isBaseline) {
		QuizWidget myFragment = new QuizWidget();

		Bundle args = new Bundle();
		args.putSerializable(Activity.TAG, activity);
		args.putSerializable(Course.TAG, course);
		args.putBoolean(CourseActivity.BASELINE_TAG, isBaseline);
		myFragment.setArguments(args);

		return myFragment;
	}

	public QuizWidget() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
		ctx = new ContextThemeWrapper(getActivity(), R.style.Oppia_Theme_Light);
		LayoutInflater localInflater = inflater.cloneInContext(ctx);
		View vv = localInflater.inflate(R.layout.widget_quiz, container, false);
		
		this.container = container;
		course = (Course) getArguments().getSerializable(Course.TAG);
		activity = ((Activity) getArguments().getSerializable(Activity.TAG));
		this.setIsBaseline(getArguments().getBoolean(CourseActivity.BASELINE_TAG));
		quizContent = ((Activity) getArguments().getSerializable(Activity.TAG)).getContents(prefs.getString(
				ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage()));

		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		vv.setLayoutParams(lp);
		vv.setId(activity.getActId());
		return vv;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		prevBtn = (Button) super.getActivity().findViewById(R.id.mquiz_prev_btn);
		nextBtn = (Button) super.getActivity().findViewById(R.id.mquiz_next_btn);
		qText = (TextView) super.getActivity().findViewById(R.id.question_text);
		questionImage = (LinearLayout) super.getActivity().findViewById(R.id.question_image);

		if (this.quiz == null) {
			this.quiz = new Quiz();
			this.quiz.load(quizContent);
		}
		if (this.isOnResultsPage) {
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
			Toast.makeText(ctx, ctx.getString(R.string.error_quiz_no_questions), Toast.LENGTH_LONG).show();
			e.printStackTrace();
			return;
		}
		qText.setVisibility(View.VISIBLE);
		// convert in case has any html special chars
		qText.setText(Html.fromHtml(q.getTitle()).toString());

		if (q.getProp("image") == null) {
			questionImage.setVisibility(View.GONE);
		} else {
			String fileUrl = course.getLocation() + q.getProp("image");
			// File file = new File(fileUrl);
			Bitmap myBitmap = BitmapFactory.decodeFile(fileUrl);
			File file = new File(fileUrl);
			ImageView iv = (ImageView) super.getActivity().findViewById(R.id.question_image_image);
			iv.setImageBitmap(myBitmap);
			iv.setTag(file);
			OnResourceClickListener orcl = new OnResourceClickListener(this.ctx, "image/*");
			iv.setOnClickListener(orcl);
			questionImage.setVisibility(View.VISIBLE);
		}

		if (q instanceof MultiChoice) {
			qw = new MultiChoiceWidget(super.getActivity(), container);
		} else if (q instanceof MultiSelect) {
			qw = new MultiSelectWidget(super.getActivity(), container);
		} else if (q instanceof ShortAnswer) {
			qw = new ShortAnswerWidget(super.getActivity(), container);
		} else if (q instanceof Matching) {
			qw = new MatchingWidget(super.getActivity(), container);
		} else if (q instanceof Numerical) {
			qw = new NumericalWidget(super.getActivity(), container);
		} else if (q instanceof Description) {
			qw = new DescriptionWidget(super.getActivity(), container);
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
					CharSequence text = ctx.getString(R.string.widget_quiz_noanswergiven);
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(ctx, text, duration);
					toast.show();
				}
			}
		});

		// set label on next button
		if (quiz.hasNext()) {
			nextBtn.setText(ctx.getString(R.string.widget_quiz_next));
		} else {
			nextBtn.setText(ctx.getString(R.string.widget_quiz_getresults));
		}
	}

	private void setProgress() {
		TextView progress = (TextView) super.getActivity().findViewById(R.id.mquiz_progress);
		try {
			if (quiz.getCurrentQuestion().responseExpected()) {
				progress.setText(ctx.getString(R.string.widget_quiz_progress, quiz.getCurrentQuestionNo(),
						quiz.getTotalNoQuestions()));
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
			if (!quiz.getCurrentQuestion().responseExpected()) {
				return true;
			}
		} catch (InvalidQuizException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void showFeedback(String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this.ctx);
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

		// log the activity as complete
		isOnResultsPage = true;
		this.saveTracker();

		// save results ready to send back to the quiz server
		String data = quiz.getResultObject().toString();
		DbHelper db = new DbHelper(ctx);
		db.insertQuizResult(data, course.getModId());
		db.close();
		Log.d(TAG, data);

		LinearLayout responsesLL = (LinearLayout) super.getActivity().findViewById(R.id.quizResponseWidget);
		responsesLL.removeAllViews();
		nextBtn.setVisibility(View.GONE);
		prevBtn.setVisibility(View.GONE);
		qText.setVisibility(View.GONE);
		questionImage.setVisibility(View.GONE);

		if (this.isBaseline) {
			TextView progress = (TextView) super.getActivity().findViewById(R.id.mquiz_progress);
			progress.setText("");

			TextView intro = new TextView(this.ctx);
			intro.setText(ctx.getString(R.string.widget_quiz_baseline_completed));
			intro.setGravity(Gravity.CENTER);
			intro.setTextSize(20);
			intro.setPadding(0, 20, 0, 50);
			responsesLL.addView(intro);

			Button restartBtn = new Button(this.ctx);
			restartBtn.setText(ctx.getString(R.string.widget_quiz_baseline_goto_course));
			restartBtn.setTextSize(20);
			restartBtn.setTypeface(Typeface.DEFAULT_BOLD);
			restartBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					QuizWidget.this.getActivity().finish();
				}
			});

			responsesLL.addView(restartBtn);
		} else {
			// set page heading
			TextView progress = (TextView) super.getActivity().findViewById(R.id.mquiz_progress);
			progress.setText(ctx.getString(R.string.widget_quiz_results));

			// show final score
			TextView intro = new TextView(this.ctx);
			intro.setText(ctx.getString(R.string.widget_quiz_results_intro));
			intro.setGravity(Gravity.CENTER);
			intro.setTextSize(20);
			responsesLL.addView(intro);

			TextView score = new TextView(this.ctx);
			score.setText(ctx.getString(R.string.widget_quiz_results_score, this.getPercent()));
			score.setTextSize(60);
			score.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			score.setGravity(Gravity.CENTER);
			score.setPadding(0, 20, 0, 20);
			responsesLL.addView(score);

			Button restartBtn = new Button(this.ctx);
			restartBtn.setText(ctx.getString(R.string.widget_quiz_results_restart));
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
		this.startTime = System.currentTimeMillis() / 1000;
		quiz = new Quiz();
		quiz.load(quizContent);
		isOnResultsPage = false;
		this.showQuestion();
	}

	@Override
	protected boolean getActivityCompleted() {
		if (isOnResultsPage && this.getPercent() > 99) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void saveTracker() {
		long timetaken = System.currentTimeMillis() / 1000 - startTime;
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
			obj.put("score", this.getPercent());
		} catch (JSONException e) {
			// Do nothing
		}
		// if it's a baseline activity then assume completed
		if (this.isBaseline) {
			t.saveTracker(course.getModId(), activity.getDigest(), obj, true);
		} else {
			t.saveTracker(course.getModId(), activity.getDigest(), obj, this.getActivityCompleted());
		}
	}

	@Override
	public HashMap<String, Object> getWidgetConfig() {
		HashMap<String, Object> config = new HashMap<String, Object>();
		// this.saveAnswer();
		config.put("quiz", this.getQuiz());
		config.put("Activity_StartTime", this.getStartTime());
		config.put("OnResultsPage", this.isOnResultsPage);
		return config;
	}

	@Override
	public void setWidgetConfig(HashMap<String, Object> config) {
		if (config.containsKey("quiz")) {
			this.setQuiz((Quiz) config.get("quiz"));
		}
		if (config.containsKey("Activity_StartTime")) {
			this.setStartTime((Long) config.get("Activity_StartTime"));
		}
		if (config.containsKey("OnResultsPage")) {
			this.isOnResultsPage = (Boolean) config.get("OnResultsPage");
		}
	}

	private void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	private long getStartTime() {
		return this.startTime;
	}

	public Quiz getQuiz() {
		return this.quiz;
	}

	private void setQuiz(Quiz quiz) {
		this.quiz = quiz;
	}

	@Override
	public String getContentToRead() {
		// Get the current question text
		String toRead = "";
		try {
			toRead = quiz.getCurrentQuestion().getTitle();
		} catch (InvalidQuizException e) {
			e.printStackTrace();
		}
		return toRead;
	}

	private float getPercent() {
		quiz.mark();
		float percent = quiz.getUserscore() * 100 / quiz.getMaxscore();
		return percent;
	}
}
