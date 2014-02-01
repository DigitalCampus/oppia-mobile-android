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
import java.util.ArrayList;
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
import org.digitalcampus.oppia.adapter.QuizFeedbackAdapter;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.Tracker;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.QuizFeedback;
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
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class QuizWidget extends WidgetFactory {

	private static final String TAG = QuizWidget.class.getSimpleName();
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

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
		View vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.widget_quiz, null);
		this.container = container;
		course = (Course) getArguments().getSerializable(Course.TAG);
		activity = ((Activity) getArguments().getSerializable(Activity.TAG));
		this.setIsBaseline(getArguments().getBoolean(CourseActivity.BASELINE_TAG));
		quizContent = ((Activity) getArguments().getSerializable(Activity.TAG)).getContents(prefs.getString(
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
			OnImageClickListener oicl = new OnImageClickListener(super.getActivity(), "image/*");
			iv.setOnClickListener(oicl);
			questionImage.setVisibility(View.VISIBLE);
		}

		if (q instanceof MultiChoice) {
			qw = new MultiChoiceWidget(super.getActivity(), getView(), container);
		} else if (q instanceof MultiSelect) {
			qw = new MultiSelectWidget(super.getActivity(), getView(),container);
		} else if (q instanceof ShortAnswer) {
			qw = new ShortAnswerWidget(super.getActivity(), getView(),container);
		} else if (q instanceof Matching) {
			qw = new MatchingWidget(super.getActivity(), getView(),container);
		} else if (q instanceof Numerical) {
			qw = new NumericalWidget(super.getActivity(), getView(),container);
		} else if (q instanceof Description) {
			qw = new DescriptionWidget(super.getActivity(), getView(),container);
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
		TextView progress = (TextView) getView().findViewById(R.id.quiz_progress);
		try {
			if (quiz.getCurrentQuestion().responseExpected()) {
				progress.setText(super.getActivity().getString(R.string.widget_quiz_progress, quiz.getCurrentQuestionNo(),
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
		AlertDialog.Builder builder = new AlertDialog.Builder(super.getActivity());
		builder.setTitle(super.getActivity().getString(R.string.feedback));
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
		DbHelper db = new DbHelper(super.getActivity());
		db.insertQuizResult(data, course.getModId());
		db.close();
		Log.d(TAG, data);
		
		// load new layout
		View C = getView().findViewById(R.id.quiz_progress);
	    ViewGroup parent = (ViewGroup) C.getParent();
	    int index = parent.indexOfChild(C);
	    parent.removeView(C);
	    C = super.getActivity().getLayoutInflater().inflate(R.layout.widget_quiz_results, parent, false);
	    parent.addView(C, index);
		
		TextView title = (TextView) getView().findViewById(R.id.quiz_results_score);
		title.setText(super.getActivity().getString(R.string.widget_quiz_results_score, this.getPercent()));

		if (this.isBaseline) {
			TextView baselineExtro = (TextView) getView().findViewById(R.id.quiz_results_baseline);
			baselineExtro.setVisibility(View.VISIBLE);
			baselineExtro.setText(super.getActivity().getString(R.string.widget_quiz_baseline_completed));
		} 
		
		// TODO add TextView here to give overall feedback if it's in the quiz
		
		// Show the detail of which questions were right/wrong
		ListView questionFeedbackLV = (ListView) getView().findViewById(R.id.quiz_results_feedback);
		ArrayList<QuizFeedback> quizFeedback = new ArrayList<QuizFeedback>();
		List<QuizQuestion> questions = this.quiz.getQuestions();
		for(QuizQuestion q: questions){
			if(!(q instanceof Description)){
				QuizFeedback qf = new QuizFeedback();
				qf.setScore(q.getScoreAsPercent());
				qf.setQuestionText(q.getTitle());
				qf.setUserResponse(q.getUserResponses());
				qf.setFeedbackText(q.getFeedback());
				quizFeedback.add(qf);
			}
		}
		QuizFeedbackAdapter qfa = new QuizFeedbackAdapter(super.getActivity(), quizFeedback);
		questionFeedbackLV.setAdapter(qfa);
		
		// Show restart or continue button
		Button restartBtn = (Button) getView().findViewById(R.id.quiz_results_button);
		
		if (this.isBaseline) {
			restartBtn.setText(super.getActivity().getString(R.string.widget_quiz_baseline_goto_course));
			restartBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					QuizWidget.this.getActivity().finish();
				}
			});
		} else {
			restartBtn.setText(super.getActivity().getString(R.string.widget_quiz_results_restart));
			restartBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					QuizWidget.this.restart();
				}
			});
		}
	}

	private void restart() {
		this.setStartTime(System.currentTimeMillis() / 1000);
		
		quiz = new Quiz();
		quiz.load(quizContent);
		isOnResultsPage = false;
		
		// reload quiz layout
		View C = getView().findViewById(R.id.widget_quiz_results);
	    ViewGroup parent = (ViewGroup) C.getParent();
	    int index = parent.indexOfChild(C);
	    parent.removeView(C);
	    C = super.getActivity().getLayoutInflater().inflate(R.layout.widget_quiz, parent, false);
	    parent.addView(C, index);
	    
	    prevBtn = (Button) getView().findViewById(R.id.mquiz_prev_btn);
		nextBtn = (Button) getView().findViewById(R.id.mquiz_next_btn);
		qText = (TextView) getView().findViewById(R.id.question_text);
		questionImage = (LinearLayout) getView().findViewById(R.id.question_image);
		questionImage.setVisibility(View.GONE);
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
			obj.put("quiz_id", quiz.getID());
			obj.put("instance_id", quiz.getInstanceID());
			obj.put("score", this.getPercent());
			// if it's a baseline activity then assume completed
			if (this.isBaseline) {
				t.saveTracker(course.getModId(), activity.getDigest(), obj, true);
			} else {
				t.saveTracker(course.getModId(), activity.getDigest(), obj, this.getActivityCompleted());
			}
		} catch (JSONException e) {
			// Do nothing
		} catch (NullPointerException npe){
			//do nothing
		}
		
	}

	@Override
	public HashMap<String, Object> getWidgetConfig() {
		HashMap<String, Object> config = new HashMap<String, Object>();
		// this.saveAnswer();
		config.put("quiz", this.quiz);
		config.put("Activity_StartTime", this.getStartTime());
		config.put("OnResultsPage", this.isOnResultsPage);
		return config;
	}

	@Override
	public void setWidgetConfig(HashMap<String, Object> config) {
		if (config.containsKey("quiz")) {
			this.quiz = (Quiz) config.get("quiz");
		}
		if (config.containsKey("Activity_StartTime")) {
			this.setStartTime((Long) config.get("Activity_StartTime"));
		}
		if (config.containsKey("OnResultsPage")) {
			this.isOnResultsPage = (Boolean) config.get("OnResultsPage");
		}
		Log.d(TAG,"Set quiz widget config");
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
	
	// TODO
	/*private String getOverallFeedback(){
		String feedback = "";
		
		return feedback;
	}*/
	
	private class OnImageClickListener implements OnClickListener{

		private Context ctx;
		private String type;
		
		public OnImageClickListener(Context ctx, String type){
			this.ctx = ctx;
			this.type = type;
		}

		public void onClick(View v) {
			File file = (File) v.getTag();
			// check the file is on the file system (should be but just in case)
			if(!file.exists()){
				Toast.makeText(this.ctx,this.ctx.getString(R.string.error_resource_not_found,file.getName()), Toast.LENGTH_LONG).show();
				return;
			} 
			Uri targetUri = Uri.fromFile(file);
			
			// check there is actually an app installed to open this filetype
			
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(targetUri, type);
			
			PackageManager pm = this.ctx.getPackageManager();

			List<ResolveInfo> infos = pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
			boolean appFound = false;
			for (ResolveInfo info : infos) {
				IntentFilter filter = info.filter;
				if (filter != null && filter.hasAction(Intent.ACTION_VIEW)) {
					// Found an app with the right intent/filter
					appFound = true;
				}
			}

			if(appFound){
				this.ctx.startActivity(intent);
			} else {
				Toast.makeText(this.ctx,this.ctx.getString(R.string.error_resource_app_not_found,file.getName()), Toast.LENGTH_LONG).show();
			}
			return;
		}
		
	}
}
