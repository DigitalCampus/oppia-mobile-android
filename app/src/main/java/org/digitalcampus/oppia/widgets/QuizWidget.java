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
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.questiontypes.Description;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.adapter.QuizAnswersFeedbackAdapter;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.gamification.GamificationServiceDelegate;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.QuizAnswerFeedback;
import org.digitalcampus.oppia.model.QuizStats;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

public class QuizWidget extends AnswerWidget {

    public static final String TAG = QuizWidget.class.getSimpleName();

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

    @Override
    int getContentAvailability() {

        if (isUserOverLimitedAttempts()){
            return R.string.widget_quiz_unavailable_attempts;
        }
        // determine availability
        if (this.quiz.getAvailability() == Quiz.AVAILABILITY_ALWAYS){
            return QUIZ_AVAILABLE;
        } else if (this.quiz.getAvailability() == Quiz.AVAILABILITY_SECTION){
            // check to see if all previous section activities have been completed
            DbHelper db = DbHelper.getInstance(getActivity());
            long userId = db.getUserId(SessionManager.getUsername(getActivity()));

            if( db.isPreviousSectionActivitiesCompleted(activity, userId) )
                return QUIZ_AVAILABLE;
            else
                return R.string.widget_quiz_unavailable_section;

        } else if (this.quiz.getAvailability() == Quiz.AVAILABILITY_COURSE){
            // check to see if all previous course activities have been completed
            DbHelper db = DbHelper.getInstance(getActivity());
            long userId = db.getUserId(SessionManager.getUsername(getActivity()));
            if (db.isPreviousCourseActivitiesCompleted(activity, userId))
                return QUIZ_AVAILABLE;
            else
                return R.string.widget_quiz_unavailable_course;
        }
        //If none of the conditions apply, set it as available
        return QUIZ_AVAILABLE;
    }

    private boolean isUserOverLimitedAttempts(){
        if (this.quiz.limitAttempts()){
            //Check if the user has attempted the quiz the max allowed
            DbHelper db = DbHelper.getInstance(getActivity());
            long userId = db.getUserId(SessionManager.getUsername(getActivity()));
            QuizStats qs = db.getQuizAttempt(this.activity.getDigest(), userId);
            return qs.getNumAttempts() > quiz.getMaxAttempts();
        }
        return false;
    }

    @Override
    void showContentUnavailableRationale(int unavailabilityReasonStringResId) {
        View localContainer = getView();
        if (localContainer != null){
            ViewGroup vg = localContainer.findViewById(activity.getActId());
            if (vg!=null){
                vg.removeAllViews();
                vg.addView(View.inflate(getView().getContext(), R.layout.widget_quiz_unavailable, null));

                TextView tv = getView().findViewById(R.id.quiz_unavailable);
                tv.setText(unavailabilityReasonStringResId);
            }
        }
    }

    @Override
    String getFinishButtonLabel() {
        return getString(R.string.widget_quiz_getresults);
    }

    @Override
    String getResultsTitle() {
        return getString(R.string.widget_quiz_results_score, this.getPercentScore());
    }

    @Override
    void showBaselineResultMessage() {
        TextView baselineText = getView().findViewById(R.id.quiz_results_baseline);
        baselineText.setText(getString(R.string.widget_quiz_baseline_completed));
        baselineText.setVisibility(View.VISIBLE);
    }

    private float getPercentScore() {
        quiz.mark(prefLang);
        return quiz.getUserscore() * 100 / quiz.getMaxscore();
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
        Log.d(TAG, "Percent:" + this.getPercentScore() );
        return (isOnResultsPage && this.getPercentScore() >= passThreshold);
    }

    @Override
    void saveAttemptTracker() {
        long timetaken = this.getSpentTime();
        new GamificationServiceDelegate(getActivity())
                .createActivityIntent(course, activity, getActivityCompleted(), isBaseline)
                .registerQuizAttemptEvent(timetaken, quiz, this.getPercentScore());
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
                qf.setScore(q.getScoreAsPercent());
                qf.setQuestionText(q.getTitle(prefLang));
                qf.setUserResponse(q.getUserResponses());
                String feedbackText = q.getFeedback(prefLang);
                qf.setFeedbackText(feedbackText);
                quizAnswersFeedback.add(qf);
            }
        }
        QuizAnswersFeedbackAdapter adapterQuizFeedback = new QuizAnswersFeedbackAdapter(getActivity(), quizAnswersFeedback);
        recyclerQuestionFeedbackLV.setAdapter(adapterQuizFeedback);
    }


}
