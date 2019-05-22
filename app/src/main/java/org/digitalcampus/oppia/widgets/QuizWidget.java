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

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.InvalidQuizException;
import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.questiontypes.Description;
import org.digitalcampus.mobile.quiz.model.questiontypes.DragAndDrop;
import org.digitalcampus.mobile.quiz.model.questiontypes.Matching;
import org.digitalcampus.mobile.quiz.model.questiontypes.MultiChoice;
import org.digitalcampus.mobile.quiz.model.questiontypes.MultiSelect;
import org.digitalcampus.mobile.quiz.model.questiontypes.Numerical;
import org.digitalcampus.mobile.quiz.model.questiontypes.ShortAnswer;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.adapter.QuizFeedbackAdapter;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.gamification.GamificationServiceDelegate;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.QuizFeedback;
import org.digitalcampus.oppia.model.QuizStats;
import org.digitalcampus.oppia.utils.resources.ExternalResourceOpener;
import org.digitalcampus.oppia.utils.ui.ProgressBarAnimator;
import org.digitalcampus.oppia.widgets.quiz.DescriptionWidget;
import org.digitalcampus.oppia.widgets.quiz.DragAndDropWidget;
import org.digitalcampus.oppia.widgets.quiz.MatchingWidget;
import org.digitalcampus.oppia.widgets.quiz.MultiChoiceWidget;
import org.digitalcampus.oppia.widgets.quiz.MultiSelectWidget;
import org.digitalcampus.oppia.widgets.quiz.NumericalWidget;
import org.digitalcampus.oppia.widgets.quiz.QuestionWidget;
import org.digitalcampus.oppia.widgets.quiz.ShortAnswerWidget;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuizWidget extends WidgetFactory {

	public static final String TAG = QuizWidget.class.getSimpleName();
    private static final int QUIZ_AVAILABLE = -1;
	private static final int PROGRESS_ANIM_DURATION = 600;
	private Quiz quiz;
	private QuestionWidget qw;
	public Button prevBtn;
	public Button nextBtn;
	public ImageView playAudioBtn;
	private TextView qText;
	private String quizContent;
	private LinearLayout questionImage;
	private boolean isOnResultsPage = false;
	private ViewGroup container;
	private MediaPlayer mp;

	private ProgressBar progressBar;
	private ProgressBarAnimator barAnim;

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
		quizContent = ((Activity) getArguments().getSerializable(Activity.TAG)).getContents(prefs.getString(
				PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));

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
		fetchViews();
        loadQuiz();
	}

	private void fetchViews(){
		this.prevBtn = getView().findViewById(R.id.mquiz_prev_btn);
		this.nextBtn = getView().findViewById(R.id.mquiz_next_btn);
		this.qText = getView().findViewById(R.id.question_text);
		this.questionImage = getView().findViewById(R.id.question_image);
		this.playAudioBtn = getView().findViewById(R.id.playAudioBtn);
		this.progressBar =  getView().findViewById(R.id.progress_quiz);
		this.barAnim = new ProgressBarAnimator(progressBar);
		this.barAnim.setAnimDuration(PROGRESS_ANIM_DURATION);
		this.questionImage.setVisibility(View.GONE);
		this.playAudioBtn.setVisibility(View.GONE);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		loadQuiz();
	}

    public void loadQuiz(){
        if (this.quiz == null) {
            this.quiz = new Quiz();
            this.quiz.load(quizContent,prefs.getString(
                    PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
        }

        if (this.isOnResultsPage) {
            showResults();
            return;
        }

        int result = checkQuizAvailability();
        if (result == QUIZ_AVAILABLE){
            this.showQuestion();
        }
        else{
            View localContainer = getView();
            if (localContainer != null){
                ViewGroup vg = localContainer.findViewById(activity.getActId());
                if (vg!=null){
                    vg.removeAllViews();
                    vg.addView(View.inflate(getView().getContext(), R.layout.widget_quiz_unavailable, null));

                    TextView tv = getView().findViewById(R.id.quiz_unavailable);
                    tv.setText(result);
                }
            }
        }
    }

    private int checkQuizAvailability(){

        DbHelper db = null;
        if (this.quiz.limitAttempts()){
            //Check if the user has attempted the quiz the max allowed
            db = DbHelper.getInstance(getActivity());
            long userId = db.getUserId(SessionManager.getUsername(getActivity()));
            QuizStats qs = db.getQuizAttempt(this.activity.getDigest(), userId);
            if (qs.getNumAttempts() > quiz.getMaxAttempts()){
                return R.string.widget_quiz_unavailable_attempts;
            }
        }

        // determine availability
        if (this.quiz.getAvailability() == Quiz.AVAILABILITY_ALWAYS){
            return QUIZ_AVAILABLE;

        } else if (this.quiz.getAvailability() == Quiz.AVAILABILITY_SECTION){
            // check to see if all previous section activities have been completed
            if (db == null) db = DbHelper.getInstance(getActivity());
            long userId = db.getUserId(SessionManager.getUsername(getActivity()));

            if( db.isPreviousSectionActivitiesCompleted(activity, userId) )
                return QUIZ_AVAILABLE;
            else
                return R.string.widget_quiz_unavailable_section;

        } else if (this.quiz.getAvailability() == Quiz.AVAILABILITY_COURSE){
            // check to see if all previous course activities have been completed
            if (db == null) db = DbHelper.getInstance(getActivity());
            long userId = db.getUserId(SessionManager.getUsername(getActivity()));
            if (db.isPreviousCourseActivitiesCompleted(activity, userId))
                return QUIZ_AVAILABLE;
            else
                return R.string.widget_quiz_unavailable_course;
        }
        //If none of the conditions apply, set it as available
        return QUIZ_AVAILABLE;
    }

	public void showQuestion() {
		clearMediaPlayer();
		QuizQuestion q;
		try {
			q = this.quiz.getCurrentQuestion();
		} catch (InvalidQuizException e) {
			Toast.makeText(super.getActivity(), super.getActivity().getString(R.string.error_quiz_no_questions), Toast.LENGTH_LONG).show();
			Mint.logException(e);
			Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e);
			return;
		}

		qText.setVisibility(View.VISIBLE);
		// convert in case has any html special chars
		String questionText = stripAudioFromText(q);
		qText.setText(Html.fromHtml(questionText));

		if (q.getProp("image") == null) {
			questionImage.setVisibility(View.GONE);
		} else {
			String fileUrl = course.getLocation() + q.getProp("image");
			Bitmap myBitmap = BitmapFactory.decodeFile(fileUrl);
			File file = new File(fileUrl);
			ImageView iv = getView().findViewById(R.id.question_image_image);
			iv.setImageBitmap(myBitmap);
			iv.setTag(file);
			if (q.getProp("media") == null){
				OnImageClickListener oicl = new OnImageClickListener(super.getActivity());
				iv.setOnClickListener(oicl);
				TextView tv = getView().findViewById(R.id.question_image_caption);
				tv.setText(R.string.widget_quiz_image_caption);
				questionImage.setVisibility(View.VISIBLE);
			} else {
				TextView tv = getView().findViewById(R.id.question_image_caption);
				tv.setText(R.string.widget_quiz_media_caption);
				OnMediaClickListener omcl = new OnMediaClickListener(q.getProp("media"));
				iv.setOnClickListener(omcl);
				questionImage.setVisibility(View.VISIBLE);
			}
			
		}

		if (q instanceof MultiChoice) {
			qw = new MultiChoiceWidget(super.getActivity(), getView(), container, q);
		} else if (q instanceof MultiSelect) {
			qw = new MultiSelectWidget(super.getActivity(), getView(), container, q);
		} else if (q instanceof ShortAnswer) {
			qw = new ShortAnswerWidget(super.getActivity(), getView(), container);
		} else if (q instanceof Matching) {
			qw = new MatchingWidget(super.getActivity(), getView(), container);
		} else if (q instanceof Numerical) {
			qw = new NumericalWidget(super.getActivity(), getView(), container);
		} else if (q instanceof Description) {
			qw = new DescriptionWidget(getView());
		} else if (q instanceof DragAndDrop) {
			qw = new DragAndDropWidget(super.getActivity(), getView(), container, q, course.getLocation());
		}	else {
			return;
		}
		qw.setQuestionResponses(q.getResponseOptions(), q.getUserResponses());
		this.setProgress();
		this.setNav();
	}

	private String stripAudioFromText(QuizQuestion q) {
		String questionText = q.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
		Pattern p = Pattern.compile("[a-zA-Z0-9\\-_]+\\.mp3");
		Matcher m = p.matcher(questionText);
		if (m.find()){
			final String mp3filename = m.group();
			questionText = questionText.replace(mp3filename, "");
			File file = new File(course.getLocation() + "resources/" + mp3filename);
			if (!file.exists()){
				playAudioBtn.setVisibility(View.GONE);
				return questionText;
			}
			final Uri mp3Uri = Uri.fromFile(file);
			Log.d(TAG, mp3Uri.getPath());

			playAudioBtn.setVisibility(View.VISIBLE);
			playAudioBtn.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if ((mp != null) && mp.isPlaying() ) {
						mp.stop();
						mp.release();
						mp = null;
					}
					mp = MediaPlayer.create(getContext(), mp3Uri);
					mp.start();
				}
			});
		}
		else{
			playAudioBtn.setVisibility(View.GONE);
		}
		return questionText;
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
					String feedback;
					try {
						feedback = QuizWidget.this.quiz.getCurrentQuestion().getFeedback(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
					
						if (!feedback.equals("") && 
								quiz.getShowFeedback() == Quiz.SHOW_FEEDBACK_ALWAYS 
								&& !QuizWidget.this.quiz.getCurrentQuestion().getFeedbackDisplayed()) {
                            //We hide the keyboard before showing the dialog
                            InputMethodManager imm =  (InputMethodManager) QuizWidget.super.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
							showFeedback(feedback);
						} else if (QuizWidget.this.quiz.hasNext()) {
							QuizWidget.this.quiz.moveNext();
							showQuestion();
						} else {
							showResults();
						}
					} catch (InvalidQuizException e) {
						Mint.logException(e);
						Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e);
					}
				} else {
					CharSequence text = QuizWidget.super.getActivity().getString(R.string.widget_quiz_noanswergiven);
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(QuizWidget.super.getActivity(), text, duration);
					toast.show();
				}
			}
		});

		// set label on next button
		if (quiz.hasNext()) {
			nextBtn.setText(super.getActivity().getString(R.string.widget_quiz_next));
		} else {
			nextBtn.setText(super.getActivity().getString(R.string.widget_quiz_getresults));
		}
	}

	private void setProgress() {
		TextView progress = getView().findViewById(R.id.quiz_progress);

		int current = progressBar.getProgress();
		progressBar.setMax(quiz.getTotalNoQuestions());
		barAnim.animate(current, quiz.getCurrentQuestionNo());
		try {
			if (quiz.getCurrentQuestion().responseExpected()) {
				progress.setText(quiz.getCurrentQuestionNo() + "/" + quiz.getTotalNoQuestions());
			} else {
				progress.setText("");
			}
		} catch (InvalidQuizException e) {
			Mint.logException(e);
			Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e);
		}
	}

	private boolean saveAnswer() {
		try {
			List<String> answers = qw.getQuestionResponses(quiz.getCurrentQuestion().getResponseOptions());
			if ( (answers != null) && (answers.size() > 0)) {
				quiz.getCurrentQuestion().setUserResponses(answers);
				return true;
			}
			if (!quiz.getCurrentQuestion().responseExpected()) {
				return true;
			}
		} catch (InvalidQuizException e) {
			Mint.logException(e);
			Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e);
		}
		return false;
	}

	private void showFeedback(String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(super.getActivity(), R.style.Oppia_AlertDialogStyle);
		builder.setTitle(super.getActivity().getString(R.string.feedback));
		builder.setMessage(msg);
		try {
			if(this.quiz.getCurrentQuestion().getScoreAsPercent() >= Quiz.QUIZ_QUESTION_PASS_THRESHOLD){
				builder.setIcon(R.drawable.quiz_tick);
			} else {
				builder.setIcon(R.drawable.quiz_cross);
			}
		} catch (InvalidQuizException e) {
			Mint.logException(e);
			Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e);
		}
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface arg0, int arg1) {
				if (QuizWidget.this.quiz.hasNext()) {
					QuizWidget.this.quiz.moveNext();
					QuizWidget.this.showQuestion();
				} else {
					QuizWidget.this.showResults();
				}
			}
		});
		builder.show();
		try {
			this.quiz.getCurrentQuestion().setFeedbackDisplayed(true);
		} catch (InvalidQuizException e) {
			Mint.logException(e);
			Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e);
		}
	}

	public void showResults() {
		clearMediaPlayer();
		// log the activity as complete
		isOnResultsPage = true;
		quiz.mark(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));

		//Check if quiz results layout is already loaded
        View quizResultsLayout = getView()==null ? null : getView().findViewById(R.id.widget_quiz_results);
        if (quizResultsLayout == null){
            // load new layout
            View C = getView().findViewById(R.id.progress_container);
            ViewGroup parent = (ViewGroup) C.getParent();
            int index = parent.indexOfChild(C);
            parent.removeView(C);
            C = super.getActivity().getLayoutInflater().inflate(R.layout.widget_quiz_results, parent, false);
            parent.addView(C, index);
        }

		TextView title = getView().findViewById(R.id.quiz_results_score);
		title.setText(super.getActivity().getString(R.string.widget_quiz_results_score, this.getPercent()));

		if (this.isBaseline) {
			TextView baselineExtro = getView().findViewById(R.id.quiz_results_baseline);
			baselineExtro.setVisibility(View.VISIBLE);
			baselineExtro.setText(super.getActivity().getString(R.string.widget_quiz_baseline_completed));
		}
		
		// Show the detail of which questions were right/wrong
		if (quiz.getShowFeedback() == Quiz.SHOW_FEEDBACK_ALWAYS || quiz.getShowFeedback() == Quiz.SHOW_FEEDBACK_ATEND){
			ListView questionFeedbackLV = getView().findViewById(R.id.quiz_results_feedback);
			ArrayList<QuizFeedback> quizFeedback = new ArrayList<>();
			List<QuizQuestion> questions = this.quiz.getQuestions();
			for(QuizQuestion q: questions){
				if(!(q instanceof Description)){
					QuizFeedback qf = new QuizFeedback();
					qf.setScore(q.getScoreAsPercent());
					qf.setQuestionText(q.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage())));
					qf.setUserResponse(q.getUserResponses());
					String feedbackText = q.getFeedback(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
					qf.setFeedbackText(feedbackText);
					quizFeedback.add(qf);
				}
			}
			QuizFeedbackAdapter qfa = new QuizFeedbackAdapter(super.getActivity(), quizFeedback);
			questionFeedbackLV.setAdapter(qfa);
		}
		
		// Show restart or continue button
		Button restartBtn = getView().findViewById(R.id.quiz_results_button);
		Button exitBtn = (Button) getView().findViewById(R.id.quiz_exit_button);

        int quizAvailability = checkQuizAvailability();
        boolean quizAvailable = quizAvailability == QUIZ_AVAILABLE;

        if (!quizAvailable){
            TextView availabilityMsg = getView().findViewById(R.id.quiz_availability_message);
            availabilityMsg.setText(quizAvailability);
            availabilityMsg.setVisibility(View.VISIBLE);
        }

		exitBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				QuizWidget.this.getActivity().finish();
			}
		});
		if (this.isBaseline) {
			restartBtn.setText(super.getActivity().getString(R.string.widget_quiz_baseline_goto_course));
			restartBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					QuizWidget.this.getActivity().finish();
				}
			});
			exitBtn.setVisibility(View.GONE);
		} else if (this.getActivityCompleted() || !quizAvailable){
			restartBtn.setVisibility(View.GONE);
        } else{
			restartBtn.setText(super.getActivity().getString(R.string.widget_quiz_results_restart));
			restartBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					QuizWidget.this.restart();
				}
			});
		}
	}

	private void restart() {
		this.saveTracker();
		this.setStartTime(System.currentTimeMillis() / 1000);
		
		this.quiz = new Quiz();
		this.quiz.load(quizContent,prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
		this.isOnResultsPage = false;
		
		// reload quiz layout
		View C = getView().findViewById(R.id.widget_quiz_results);
	    ViewGroup parent = (ViewGroup) C.getParent();
	    int index = parent.indexOfChild(C);
	    parent.removeView(C);
	    C = super.getActivity().getLayoutInflater().inflate(R.layout.widget_quiz, parent, false);
	    parent.addView(C, index);

		fetchViews();
		showQuestion();
	}

	@Override
	public boolean getActivityCompleted() {
		int passThreshold;
		Log.d(TAG, "Threshold:" + quiz.getPassThreshold() );
		if (quiz.getPassThreshold() != 0){
			passThreshold = quiz.getPassThreshold();
		} else {
			passThreshold = Quiz.QUIZ_DEFAULT_PASS_THRESHOLD;
		}
		Log.d(TAG, "Percent:" + this.getPercent() );
        return (isOnResultsPage && this.getPercent() >= passThreshold);
	}

	@Override
	public void saveTracker() {
		long timetaken = this.getSpentTime();
		if(activity == null || !isOnResultsPage){
			return;
		}

        Log.d(TAG," saving quiz tracker");
		new GamificationServiceDelegate(getActivity())
			.createActivityIntent(course, activity, getActivityCompleted(), isBaseline)
			.registerQuizAttemptEvent(timetaken, quiz, this.getPercent());
	}

	@Override
	public HashMap<String, Object> getWidgetConfig() {
		HashMap<String, Object> config = new HashMap<>();
		config.put("quiz", this.quiz);
		config.put(WidgetFactory.PROPERTY_ACTIVITY_STARTTIME, this.getStartTime());
		config.put(WidgetFactory.PROPERTY_ON_RESULTS_PAGE, this.isOnResultsPage);
		return config;
	}

	@Override
	public void setWidgetConfig(HashMap<String, Object> config) {
		if (config.containsKey("quiz")) {
			this.quiz = (Quiz) config.get("quiz");
		}
		if (config.containsKey(WidgetFactory.PROPERTY_ACTIVITY_STARTTIME)) {
			this.setStartTime((Long) config.get(WidgetFactory.PROPERTY_ACTIVITY_STARTTIME));
		}
		if (config.containsKey(WidgetFactory.PROPERTY_ON_RESULTS_PAGE)) {
			this.isOnResultsPage = (Boolean) config.get(WidgetFactory.PROPERTY_ON_RESULTS_PAGE);
		}
	}

	@Override
	public String getContentToRead() {
		// Get the current question text
		String toRead = "";
		try {
			toRead = quiz.getCurrentQuestion().getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
		} catch (InvalidQuizException e) {
			Mint.logException(e);
			Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e);
		}
		return toRead;
	}

	private float getPercent() {
		quiz.mark(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
		float percent = quiz.getUserscore() * 100 / quiz.getMaxscore();
		return percent;
	}

	private void clearMediaPlayer(){
		if ((mp != null) ) {
			if (mp.isPlaying()){
				mp.stop();
			}
			mp.release();
			mp = null;
		}
	}
	
	private class OnImageClickListener implements OnClickListener{

		private Context ctx;
		
		public OnImageClickListener(Context ctx){
			this.ctx = ctx;
		}

		public void onClick(View v) {
			File file = (File) v.getTag();
			// check the file is on the file system (should be but just in case)
			if(!file.exists()){
				Toast.makeText(this.ctx,this.ctx.getString(R.string.error_resource_not_found,file.getName()), Toast.LENGTH_LONG).show();
				return;
			}
			// check there is actually an app installed to open this filetype
			Intent intent = ExternalResourceOpener.getIntentToOpenResource(ctx, file);
			if(intent != null){
				this.ctx.startActivity(intent);
			} else {
				Toast.makeText(this.ctx,this.ctx.getString(R.string.error_resource_app_not_found,file.getName()), Toast.LENGTH_LONG).show();
			}
		}
		
	}
	
	private class OnMediaClickListener implements OnClickListener{

		private String mediaFileName;
		
		public OnMediaClickListener(String mediaFileName){
			this.mediaFileName = mediaFileName;
		}

		public void onClick(View v) {
            QuizWidget.super.startMediaPlayerWithFile(mediaFileName);
		}
		
	}
}
