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

import android.os.Bundle;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.questiontypes.Description;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.adapter.QuizAnswersFeedbackAdapter;
import org.digitalcampus.oppia.gamification.GamificationServiceDelegate;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.QuizAnswerFeedback;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

public class FeedbackWidget extends AnswerWidget {

	public static final String TAG = FeedbackWidget.class.getSimpleName();

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

	@Override
	int getContentAvailability() {
		// Feedback widget always available?
		return QUIZ_AVAILABLE;
	}

	@Override
	void showContentUnavailableRationale(int unavailabilityReasonStringResId) {
		// Always available, so no need to show any kind of message
	}

	@Override
	String getFinishButtonLabel() {
		return getString(R.string.widget_feedback_submit);
	}

	@Override
	String getResultsTitle() {
		return getString(R.string.widget_feedback_submit_title);
	}

	@Override
	void showBaselineResultMessage() {
		// We don't show any baseline message for feedback
	}

	@Override
	void saveAttemptTracker() {
		long timetaken = this.getSpentTime();
		new GamificationServiceDelegate(getActivity())
				.createActivityIntent(course, activity, getActivityCompleted(), isBaseline)
				.registerFeedbackEvent(timetaken, quiz, quiz.getID(), quiz.getInstanceID());
	}

	@Override
	void showAnswersFeedback() {
		RecyclerView recyclerQuestionFeedbackLV = getView().findViewById(R.id.recycler_quiz_results_feedback);
		recyclerQuestionFeedbackLV.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
		ArrayList<QuizAnswerFeedback> quizAnswersFeedback = new ArrayList<>();
		List<QuizQuestion> questions = this.quiz.getQuestions();
		for(QuizQuestion q: questions){
			if(!(q instanceof Description)){
				QuizAnswerFeedback qf = new QuizAnswerFeedback();
				qf.setIsSurvey(true);
				qf.setScore(100);
				qf.setQuestionText(q.getTitle(prefLang));
				qf.setUserResponse(q.getUserResponses());
				quizAnswersFeedback.add(qf);
			}
		}
		QuizAnswersFeedbackAdapter adapterQuizFeedback = new QuizAnswersFeedbackAdapter(getActivity(), quizAnswersFeedback);
		recyclerQuestionFeedbackLV.setAdapter(adapterQuizFeedback);
	}


	@Override
	public boolean getActivityCompleted() {
		return isOnResultsPage;
	}

}
