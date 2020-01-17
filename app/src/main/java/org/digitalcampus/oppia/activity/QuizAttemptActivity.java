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
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.questiontypes.Description;
import org.digitalcampus.oppia.adapter.QuizFeedbackAdapter;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CourseMetaPage;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.model.QuizFeedback;
import org.digitalcampus.oppia.utils.storage.FileUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
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

		QuizFeedbackAdapter adapterQuizFeedback = new QuizFeedbackAdapter(getActivity(), quizFeedback);
		recyclerQuestionFeedbackLV.setAdapter(adapterQuizFeedback);
	    
	}
	
}
