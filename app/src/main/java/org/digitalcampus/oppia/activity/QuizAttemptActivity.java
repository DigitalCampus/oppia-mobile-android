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

package org.digitalcampus.oppia.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.questiontypes.Description;
import org.digitalcampus.oppia.adapter.QuizFeedbackAdapter;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CourseMetaPage;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.model.QuizFeedback;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

public class QuizAttemptActivity extends AppActivity {

	private Course course;
	private CourseMetaPage cmp;

	public final static String COURSE_TITLE = "course_title";
	public final static String QUIZ_TITLE = "quiz_title";

	@Override
	public void onStart() {
		super.onStart();
		initialize(false);
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_attempt);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String prefLang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
        Bundle bundle = this.getIntent().getExtras();

		QuizAttempt quizAttempt;

        if (bundle == null) {
        	return;
        }

		quizAttempt = (QuizAttempt) bundle.getSerializable(QuizAttempt.TAG);
		Course course = DbHelper.getInstance(this).getCourse(quizAttempt.getCourseId(), quizAttempt.getUserId());

		TextView courseTitle = findViewById(R.id.course_title);
		TextView quizTitle = findViewById(R.id.quiz_title);
		TextView score = findViewById(R.id.score);
		TextView attemptDate = findViewById(R.id.attempt_date);
		TextView timetaken = findViewById(R.id.attempt_timetaken);

		courseTitle.setText(course.getTitle(prefLang));
		quizTitle.setText(quizAttempt.getDisplayTitle(this));
		attemptDate.setText(MobileLearning.DISPLAY_DATETIME_FORMAT.print(quizAttempt.getDatetime()));
		score.setText(quizAttempt.getScorePercentLabel());
		timetaken.setText(quizAttempt.getHumanTimetaken());

		RecyclerView recyclerQuestionFeedbackLV = findViewById(R.id.recycler_quiz_results_feedback);
		recyclerQuestionFeedbackLV.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
		Quiz quiz = new Quiz();
		Activity act = DbHelper.getInstance(this).getActivityByDigest(quizAttempt.getActivityDigest());

		CompleteCourse parsed;
		try {
			CourseXMLReader cxr = new CourseXMLReader(course.getCourseXMLLocation(), course.getCourseId(), this);
			cxr.parse(CourseXMLReader.ParseMode.COMPLETE);
			parsed = cxr.getParsedCourse();
			act = parsed.getActivityByDigest(quizAttempt.getActivityDigest());

		} catch (InvalidXMLException ixmle) {
			Log.d(TAG,"Invalid course xml file", ixmle);
			Mint.logException(ixmle);
		}

		if (quizAttempt.getData() == null){
			return;
		}

		quiz.load(act.getContents(prefLang), prefLang);

		JSONObject jsonData;
		try {
			jsonData = new JSONObject(quizAttempt.getData());
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}
		List<QuizFeedback> quizFeedback = new ArrayList<>();
		List<QuizQuestion> questions = quiz.getQuestions();
		for(QuizQuestion q: questions){
			if(!(q instanceof Description)){
				updateQuizResponse(q, jsonData);
				q.mark(prefLang);
				QuizFeedback qf = new QuizFeedback();
				qf.setScore(q.getScoreAsPercent());
				qf.setQuestionText(q.getTitle(prefLang));
				qf.setUserResponse(q.getUserResponses());
				qf.setFeedbackText(q.getFeedback(prefLang));
				quizFeedback.add(qf);
			}
		}

		QuizFeedbackAdapter adapterQuizFeedback = new QuizFeedbackAdapter(this, quizFeedback);
		recyclerQuestionFeedbackLV.setAdapter(adapterQuizFeedback);
	    
	}

	private void updateQuizResponse(QuizQuestion q, JSONObject data){
		try {
			for (int i = 0; i < data.getJSONArray("responses").length(); i++) {
				JSONObject response = data.getJSONArray("responses").getJSONObject(i);
				if (response.getInt("question_id") == q.getID()){
					String r = response.getString("text");
					ArrayList<String> responses = new ArrayList<>();
					responses.add(r);
					q.setUserResponses(responses);
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
}
