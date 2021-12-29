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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.questiontypes.Description;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.adapter.QuizAnswersFeedbackAdapter;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.gamification.GamificationServiceDelegate;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.QuizAnswerFeedback;
import org.digitalcampus.oppia.model.QuizStats;

import java.util.ArrayList;
import java.util.List;

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
    int getContentAvailability(boolean afterAttempt) {

        if (isUserOverLimitedAttempts(afterAttempt)){
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

    private boolean isUserOverLimitedAttempts(boolean afterAttempt){
        if (this.quiz.limitAttempts()){
            //Check if the user has attempted the quiz the max allowed
            QuizStats qs = attemptsRepository.getQuizAttemptStats(this.getActivity(), activity.getDigest());
            if (afterAttempt){
                //If the quiz was just attempted, it is not saved yet, so we added
                qs.setNumAttempts(qs.getNumAttempts() + 1);
            }
            return qs.getNumAttempts() >= quiz.getMaxAttempts();
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
    void showResultsInfo() {
        TextView title = getView().findViewById(R.id.quiz_results_score);
        title.setText(getString(R.string.widget_quiz_results_score, this.getPercentScore()));

        if (!isBaseline){
            ViewGroup info = getView().findViewById(R.id.quiz_stats);
            info.setVisibility(View.VISIBLE);

            QuizStats stats = attemptsRepository.getQuizAttemptStats(getContext(), activity.getDigest());
            // We take into account the current quiz (not saved yet)
            int numAttempts = stats.getNumAttempts();
            float average = ((stats.getAverageScore() * numAttempts) + quiz.getUserscore()) / (numAttempts + 1);
            stats.setMaxScore(Math.max(quiz.getMaxscore(), stats.getMaxScore()));
            stats.setNumAttempts(numAttempts + 1);
            stats.setUserScore(Math.max(quiz.getUserscore(), stats.getUserScore()));
            stats.setAverageScore(average);
            showStats(info, stats);
        }
    }

    @Override
    boolean shouldShowInitialInfo() {
        return !this.isBaseline;
    }

    private void showStats(ViewGroup infoContainer, QuizStats stats){
        TextView average = infoContainer.findViewById(R.id.highlight_average);
        TextView best = infoContainer.findViewById(R.id.highlight_best);
        TextView numAttempts = infoContainer.findViewById(R.id.highlight_attempted);
        TextView infoAttempts = infoContainer.findViewById(R.id.info_num_attempts);
        TextView threshold = infoContainer.findViewById(R.id.info_threshold);

        numAttempts.setText(String.valueOf(stats.getNumAttempts()));
        threshold.setText(getString(R.string.widget_quiz_pass_threshold, quiz.getPassThreshold()));

        if (quiz.limitAttempts()){
            int attemptsLeft = Math.max(quiz.getMaxAttempts() - stats.getNumAttempts(), 0);
            infoAttempts.setText(getString(R.string.quiz_attempts_left, quiz.getMaxAttempts(), attemptsLeft));
        }
        else{
            infoAttempts.setText(R.string.quiz_attempts_unlimited);
        }

        if (stats.getNumAttempts() == 0){
            average.setText("-");
            best.setText("-");
        }
        else{
            average.setText(stats.getAveragePercent() + "%");
            best.setText(stats.getPercent() + "%");
        }
    }

    @Override
    void loadInitialInfo(ViewGroup infoContainer) {
        infoContainer.removeAllViews();
        ViewGroup info = (ViewGroup) View.inflate(infoContainer.getContext(), R.layout.view_quiz_info, infoContainer);
        ProgressBar thresholdBar = info.findViewById(R.id.threshold_bar);
        TextView numQuestions = info.findViewById(R.id.info_num_questions);

        numQuestions.setText(getString(R.string.widget_quiz_num_questions, quiz.getTotalNoQuestions()));
        thresholdBar.setProgress(quiz.getPassThreshold());

        info.findViewById(R.id.take_quiz_btn).setOnClickListener(view -> checkPasswordProtected());
        QuizStats stats = attemptsRepository.getQuizAttemptStats(getContext(), activity.getDigest());
        showStats(info, stats);
    }

    private void checkPasswordProtected() {
        if (TextUtils.isEmpty(quiz.getPassword())) {
            showQuestion();
        } else {
            final EditText editPassword = new EditText(getActivity());
            new AlertDialog.Builder(getActivity(), R.style.Oppia_AlertDialogStyle)
                    .setTitle(getString(R.string.quiz_protected))
                    .setMessage(getString(R.string.please_enter_quiz_password))
                    .setView(editPassword)
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        String passwordInput = editPassword.getText().toString();
                        if (TextUtils.equals(passwordInput, quiz.getPassword())) {
                            showQuestion();
                        } else {
                            Toast.makeText(getActivity(), R.string.invalid_password, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        }
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
        if (quiz.getPassThreshold() >= 0){
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
                qf.setFeedbackText(feedbackText.replaceAll("&amp;gt;","<"));
                quizAnswersFeedback.add(qf);
            }
        }
        QuizAnswersFeedbackAdapter adapterQuizFeedback = new QuizAnswersFeedbackAdapter(getActivity(), quizAnswersFeedback);
        recyclerQuestionFeedbackLV.setAdapter(adapterQuizFeedback);
    }


}
