/* 
 * This file is part of OppiaMobile - https://digital-campus.org/
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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.InvalidQuizException;
import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.questiontypes.DragAndDrop;
import org.digitalcampus.mobile.quiz.model.questiontypes.Essay;
import org.digitalcampus.mobile.quiz.model.questiontypes.MultiChoice;
import org.digitalcampus.mobile.quiz.model.questiontypes.MultiSelect;
import org.digitalcampus.mobile.quiz.model.questiontypes.Numerical;
import org.digitalcampus.mobile.quiz.model.questiontypes.ShortAnswer;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.gamification.Gamification;
import org.digitalcampus.oppia.gamification.GamificationServiceDelegate;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.widgets.quiz.DragAndDropWidget;
import org.digitalcampus.oppia.widgets.quiz.EssayWidget;
import org.digitalcampus.oppia.widgets.quiz.MultiChoiceWidget;
import org.digitalcampus.oppia.widgets.quiz.MultiSelectWidget;
import org.digitalcampus.oppia.widgets.quiz.NumericalWidget;
import org.digitalcampus.oppia.widgets.quiz.QuestionWidget;
import org.digitalcampus.oppia.widgets.quiz.ShortAnswerWidget;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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
        // Required empty public constructor
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
		View vv = inflater.inflate(R.layout.widget_quiz, container, false);
		this.container = container;
		course = (Course) getArguments().getSerializable(Course.TAG);
		activity = ((Activity) getArguments().getSerializable(Activity.TAG));
		this.setIsBaseline(getArguments().getBoolean(CourseActivity.BASELINE_TAG));
		feedbackContent = ((Activity) getArguments().getSerializable(Activity.TAG)).getContents(prefs.getString(
				PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));

		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		vv.setLayoutParams(lp);
		vv.setId(activity.getActId());
		if ((savedInstanceState != null) && (savedInstanceState.getSerializable(WidgetFactory.WIDGET_CONFIG) != null)){
			setWidgetConfig((HashMap<String, Object>) savedInstanceState.getSerializable(WidgetFactory.WIDGET_CONFIG));
		}
		return vv;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(WidgetFactory.WIDGET_CONFIG, getWidgetConfig());
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		prevBtn = getView().findViewById(R.id.mquiz_prev_btn);
		nextBtn = getView().findViewById(R.id.mquiz_next_btn);
		qText = getView().findViewById(R.id.question_text);
		questionImage = getView().findViewById(R.id.question_image);

        loadFeedback();
	}
	
	@Override
	public void onResume(){
		super.onResume();
        loadFeedback();
	}

    private void loadFeedback(){
        if (this.feedback == null) {
            this.feedback = new Quiz();
            this.feedback.load(feedbackContent,prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
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
			Mint.logException(e);
			Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e);
			return;
		}
		qText.setVisibility(View.VISIBLE);
		// convert in case has any html special chars
		qText.setText(Html.fromHtml(q.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()))));

		if (q.getProp("image") == null) {
			questionImage.setVisibility(View.GONE);
		} else {
			String fileUrl = course.getLocation() + q.getProp("image");
			Bitmap myBitmap = BitmapFactory.decodeFile(fileUrl);
			File file = new File(fileUrl);
			ImageView iv = getView().findViewById(R.id.question_image_image);
			iv.setImageBitmap(myBitmap);
			iv.setTag(file);
		}

		if (q instanceof MultiChoice) {
			qw = new MultiChoiceWidget(super.getActivity(), getView(), container, q);
		} else if (q instanceof Essay) {
			qw = new EssayWidget(super.getActivity(), getView(), container);
		} else if (q instanceof ShortAnswer) {
			qw = new ShortAnswerWidget(super.getActivity(), getView(), container);
		} else if (q instanceof Numerical) {
			qw = new NumericalWidget(super.getActivity(), getView(), container);
		} else if (q instanceof MultiSelect) {
			qw = new MultiSelectWidget(super.getActivity(), getView(), container, q);
		} else if (q instanceof DragAndDrop) {
			qw = new DragAndDropWidget(super.getActivity(), getView(), container, q, course.getLocation());
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

        if (!isOnResultsPage){
            // log the activity as complete
            isOnResultsPage = true;
            this.saveTracker();

            // save results ready to send back to the quiz server
            String data = feedback.getResultObject(Gamification.GAMIFICATION_UNDEFINED).toString();
            DbHelper db = DbHelper.getInstance(super.getActivity());
            long userId = db.getUserId(SessionManager.getUsername(getActivity()));
    		
    		QuizAttempt qa = new QuizAttempt();
    		qa.setCourseId(course.getCourseId());
    		qa.setUserId(userId);
    		qa.setData(data);
    		qa.setActivityDigest(activity.getDigest());
    		qa.setScore(feedback.getUserscore());
    		qa.setMaxscore(feedback.getMaxscore());
    		qa.setPassed(this.getActivityCompleted());

            db.insertQuizAttempt(qa);
        }

        //Check if feedback results layout is already loaded
        View feedbackResultsLayout = getView().findViewById(R.id.widget_feedback_results);
        if (feedbackResultsLayout == null){
            View view = getView().findViewById(R.id.quiz_progress);
            ViewGroup parent = (ViewGroup) view.getParent();
            int index = parent.indexOfChild(view);
            parent.removeView(view);
            view = super.getActivity().getLayoutInflater().inflate(R.layout.widget_feedback_results, parent, false);
            parent.addView(view, index);
        }
	}
	
	private void setProgress() {
		TextView progress = getView().findViewById(R.id.quiz_progress);
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
            Mint.logException(e);
            Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e);
		}
		return false;
	}
	
	@Override
	public boolean getActivityCompleted() {
		return isOnResultsPage;
	}

	@Override
	public void saveTracker() {
		long timetaken = this.getSpentTime();
		if(activity == null || !isOnResultsPage){
			return;
		}

		new GamificationServiceDelegate(getActivity())
				.createActivityIntent(course, activity, getActivityCompleted(), isBaseline)
				.registerFeedbackEvent(timetaken, feedback.getID(), feedback.getInstanceID());
	}

	@Override
	public String getContentToRead() {
		// Get the current question text
		String toRead = "";
		try {
			toRead = feedback.getCurrentQuestion().getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
		} catch (InvalidQuizException e) {
            Mint.logException(e);
            Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e);
		}
		return toRead;
	}

	@Override
	public HashMap<String, Object> getWidgetConfig() {
		HashMap<String, Object> config = new HashMap<>();
		config.put(WidgetFactory.PROPERTY_FEEDBACK, this.feedback);
		config.put(WidgetFactory.PROPERTY_ACTIVITY_STARTTIME, this.getStartTime());
		config.put(WidgetFactory.PROPERTY_ON_RESULTS_PAGE, this.isOnResultsPage);
		return config;
	}

	@Override
	public void setWidgetConfig(HashMap<String, Object> config) {
		if (config.containsKey(WidgetFactory.PROPERTY_FEEDBACK)) {
			this.feedback = (Quiz) config.get(WidgetFactory.PROPERTY_FEEDBACK);
		}
		if (config.containsKey(WidgetFactory.PROPERTY_ACTIVITY_STARTTIME)) {
			this.setStartTime((Long) config.get(WidgetFactory.PROPERTY_ACTIVITY_STARTTIME));
		}
		if (config.containsKey(WidgetFactory.PROPERTY_ON_RESULTS_PAGE)) {
			this.isOnResultsPage = (Boolean) config.get(WidgetFactory.PROPERTY_ON_RESULTS_PAGE);
		}		
	}

}
