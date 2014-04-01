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
import org.digitalcampus.mobile.quiz.model.questiontypes.Essay;
import org.digitalcampus.mobile.quiz.model.questiontypes.MultiChoice;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.Tracker;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.digitalcampus.oppia.widgets.quiz.EssayWidget;
import org.digitalcampus.oppia.widgets.quiz.MultiChoiceWidget;
import org.digitalcampus.oppia.widgets.quiz.QuestionWidget;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class FeedbackWidget extends WidgetFactory {

	public static final String TAG = FeedbackWidget.class.getSimpleName();
	private ViewGroup container;
	private Quiz feedback;
	private String feedbackContent;
	public Button prevBtn;
	public Button nextBtn;
	private TextView qText;
	private LinearLayout questionImage;
	private boolean isOnResultsPage = false;
	private QuestionWidget qw;
	
	public static FeedbackWidget newInstance(Activity activity, Course course, boolean isBaseline) {
		FeedbackWidget myFragment = new FeedbackWidget();

		Bundle args = new Bundle();
		args.putSerializable(Activity.TAG, activity);
		args.putSerializable(Course.TAG, course);
		args.putBoolean(CourseActivity.BASELINE_TAG, isBaseline);
		myFragment.setArguments(args);

		return myFragment;
	}
	
	public FeedbackWidget() {

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
		View vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.widget_quiz, null);
		this.container = container;
		course = (Course) getArguments().getSerializable(Course.TAG);
		activity = ((Activity) getArguments().getSerializable(Activity.TAG));
		this.setIsBaseline(getArguments().getBoolean(CourseActivity.BASELINE_TAG));
		feedbackContent = ((Activity) getArguments().getSerializable(Activity.TAG)).getContents(prefs.getString(
				super.getActivity().getString(R.string.prefs_language), Locale.getDefault().getLanguage()));

		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		vv.setLayoutParams(lp);
		vv.setId(activity.getActId());
		if ((savedInstanceState != null) && (savedInstanceState.getSerializable("widget_config") != null)){
			setWidgetConfig((HashMap<String, Object>) savedInstanceState.getSerializable("widget_config"));
		}
		return vv;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("widget_config", getWidgetConfig());
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		prevBtn = (Button) getView().findViewById(R.id.mquiz_prev_btn);
		nextBtn = (Button) getView().findViewById(R.id.mquiz_next_btn);
		qText = (TextView) getView().findViewById(R.id.question_text);
		questionImage = (LinearLayout) getView().findViewById(R.id.question_image);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if (this.feedback == null) {
			this.feedback = new Quiz();
			this.feedback.load(feedbackContent);
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
			q = this.feedback.getCurrentQuestion();
		} catch (InvalidQuizException e) {
			Toast.makeText(super.getActivity(), super.getActivity().getString(R.string.error_quiz_no_questions), Toast.LENGTH_LONG).show();
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
			ImageView iv = (ImageView) getView().findViewById(R.id.question_image_image);
			iv.setImageBitmap(myBitmap);
			iv.setTag(file);
		}

		if (q instanceof MultiChoice) {
			qw = new MultiChoiceWidget(super.getActivity(), getView(), container);
		} else if (q instanceof Essay) {
			qw = new EssayWidget(super.getActivity(), getView(), container);
		} else {
			return;
		}
		qw.setQuestionResponses(q.getResponseOptions(), q.getUserResponses());
		this.setProgress();
		this.setNav();
	}
	
	private void setNav() {
		nextBtn.setVisibility(View.VISIBLE);
		prevBtn.setVisibility(View.VISIBLE);

		if (this.feedback.hasPrevious()) {
			prevBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					// save answer
					saveAnswer();

					if (FeedbackWidget.this.feedback.hasPrevious()) {
						FeedbackWidget.this.feedback.movePrevious();
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
					if (FeedbackWidget.this.feedback.hasNext()) {
						FeedbackWidget.this.feedback.moveNext();
						showQuestion();
					} else {
						showResults();
					}
				} else {
					CharSequence text = FeedbackWidget.super.getActivity().getString(R.string.widget_quiz_noanswergiven);
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(FeedbackWidget.super.getActivity(), text, duration);
					toast.show();
				}
			}
		});

		// set label on next button
		if (feedback.hasNext()) {
			nextBtn.setText(super.getActivity().getString(R.string.widget_quiz_next));
		} else {
			nextBtn.setText(super.getActivity().getString(R.string.widget_feedback_submit));
		}
	}
	
	public void showResults() {

		// log the activity as complete
		isOnResultsPage = true;
		this.saveTracker();

		// save results ready to send back to the quiz server
		String data = feedback.getResultObject().toString();
		DbHelper db = new DbHelper(super.getActivity());
		db.insertQuizResult(data, course.getCourseId());
		db.close();
		
		// load new layout
		View C = getView().findViewById(R.id.quiz_progress);
	    ViewGroup parent = (ViewGroup) C.getParent();
	    int index = parent.indexOfChild(C);
	    parent.removeView(C);
	    C = super.getActivity().getLayoutInflater().inflate(R.layout.widget_feedback_results, parent, false);
	    parent.addView(C, index);

	}
	
	private void setProgress() {
		TextView progress = (TextView) getView().findViewById(R.id.quiz_progress);
		progress.setText(super.getActivity().getString(R.string.widget_quiz_progress, feedback.getCurrentQuestionNo(),
				this.feedback.getTotalNoQuestions()));
	}
	
	
	private boolean saveAnswer() {
		try {
			List<String> answers = qw.getQuestionResponses(feedback.getCurrentQuestion().getResponseOptions());
			if (answers != null) {
				feedback.getCurrentQuestion().setUserResponses(answers);
				return true;
			}
			if (!feedback.getCurrentQuestion().responseExpected()) {
				return true;
			}
		} catch (InvalidQuizException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	@Override
	protected boolean getActivityCompleted() {
		if (isOnResultsPage) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void saveTracker() {
		long timetaken = System.currentTimeMillis() / 1000 - this.getStartTime();
		Tracker t = new Tracker(super.getActivity());
		JSONObject obj = new JSONObject();
		if(!isOnResultsPage){
			return;
		}
		// add in extra meta-data
		try {
			MetaDataUtils mdu = new MetaDataUtils(super.getActivity());
			obj.put("timetaken", timetaken);
			obj = mdu.getMetaData(obj);
			String lang = prefs.getString(super.getActivity().getString(R.string.prefs_language), Locale.getDefault().getLanguage());
			obj.put("lang", lang);
			obj.put("quiz_id", feedback.getID());
			obj.put("instance_id", feedback.getInstanceID());
			t.saveTracker(course.getCourseId(), activity.getDigest(), obj, this.getActivityCompleted());
		} catch (JSONException e) {
			// Do nothing
		} catch (NullPointerException npe){
			//do nothing
		}
		
		
	}

	@Override
	public String getContentToRead() {
		// Get the current question text
		String toRead = "";
		try {
			toRead = feedback.getCurrentQuestion().getTitle();
		} catch (InvalidQuizException e) {
			e.printStackTrace();
		}
		return toRead;
	}

	@Override
	public HashMap<String, Object> getWidgetConfig() {
		HashMap<String, Object> config = new HashMap<String, Object>();
		config.put("feedback", this.feedback);
		config.put("Activity_StartTime", this.getStartTime());
		config.put("OnResultsPage", this.isOnResultsPage);
		return config;
	}

	@Override
	public void setWidgetConfig(HashMap<String, Object> config) {
		if (config.containsKey("feedback")) {
			this.feedback = (Quiz) config.get("feedback");
		}
		if (config.containsKey("Activity_StartTime")) {
			this.setStartTime((Long) config.get("Activity_StartTime"));
		}
		if (config.containsKey("OnResultsPage")) {
			this.isOnResultsPage = (Boolean) config.get("OnResultsPage");
		}		
	}

}
