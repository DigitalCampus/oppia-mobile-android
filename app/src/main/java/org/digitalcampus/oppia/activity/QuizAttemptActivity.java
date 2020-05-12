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
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.questiontypes.Description;
import org.digitalcampus.oppia.adapter.QuizAnswersFeedbackAdapter;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.model.QuizAnswerFeedback;
import org.digitalcampus.oppia.utils.DateUtils;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

public class QuizAttemptActivity extends AppActivity {

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
		attemptDate.setText(DateUtils.DISPLAY_DATETIME_FORMAT.print(quizAttempt.getDatetime()));
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
			Log.d(TAG,"Invalid json for quiz attempt", e);
			return;
		}
		List<QuizAnswerFeedback> quizAnswerFeedback = new ArrayList<>();
		List<QuizQuestion> questions = quiz.getQuestions();
		for(QuizQuestion q: questions){
			if(!(q instanceof Description)){
				updateQuizResponse(q, jsonData);
				q.mark(prefLang);
				QuizAnswerFeedback qf = new QuizAnswerFeedback();
				qf.setScore(q.getScoreAsPercent());
				qf.setQuestionText(q.getTitle(prefLang));
				qf.setUserResponse(q.getUserResponses());
				qf.setFeedbackText(q.getFeedback(prefLang));
				quizAnswerFeedback.add(qf);
			}
		}

		QuizAnswersFeedbackAdapter adapterQuizFeedback = new QuizAnswersFeedbackAdapter(this, quizAnswerFeedback);
		recyclerQuestionFeedbackLV.setAdapter(adapterQuizFeedback);
	    
	}

	private void updateQuizResponse(QuizQuestion q, JSONObject data){
		try {
			JSONArray responses =  data.getJSONArray(Quiz.JSON_PROPERTY_RESPONSES);
			for (int i = 0; i < responses.length(); i++) {
				JSONObject response = responses.getJSONObject(i);
				if (response.getInt(Quiz.JSON_PROPERTY_QUESTION_ID) == q.getID()){
					String r = response.getString(Quiz.JSON_PROPERTY_TEXT);
					List<String> userResponses = new ArrayList<>();
					if (r.contains(Quiz.RESPONSE_SEPARATOR)){
						userResponses = Arrays.asList(r.split(Quiz.RESPONSE_SEPARATOR));
					}
					else{
						userResponses.add(r);
					}
					q.setUserResponses(userResponses);
				}
			}

		} catch (JSONException e) {
			Log.d(TAG,"Invalid json for quiz attempt response", e);
		}
	}
	
}
