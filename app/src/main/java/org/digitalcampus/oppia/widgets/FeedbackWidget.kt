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
package org.digitalcampus.oppia.widgets

import android.os.Bundle
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.quiz.model.questiontypes.Description
import org.digitalcampus.oppia.activity.CourseActivity
import org.digitalcampus.oppia.adapter.QuizAnswersFeedbackAdapter
import org.digitalcampus.oppia.gamification.GamificationServiceDelegate
import org.digitalcampus.oppia.model.Activity
import org.digitalcampus.oppia.model.Course
import org.digitalcampus.oppia.model.QuizAnswerFeedback

class FeedbackWidget : AnswerWidget() {

    companion object {
        val TAG = FeedbackWidget::class.simpleName

        @JvmStatic
        fun newInstance(activity: Activity, course: Course, isBaseline: Boolean): FeedbackWidget {
            val myFragment = FeedbackWidget()
            val args = Bundle()
            args.putSerializable(Activity.TAG, activity)
            args.putSerializable(Course.TAG, course)
            args.putBoolean(CourseActivity.BASELINE_TAG, isBaseline)
            myFragment.arguments = args
            return myFragment
        }
    }

    override fun getContentAvailability(afterAttempt: Boolean): Int {
        return if (afterAttempt) {
            // In the case we just submitted the feedback, we set the value as available
            // to avoid showing the retake message
            QUIZ_AVAILABLE
        } else if (isUserOverLimitedAttempts(afterAttempt)) {
            R.string.widget_feedback_unavailable_attempts
        } else {
            QUIZ_AVAILABLE
        }
    }

    override fun getAnswerWidgetType(): String {
        return getString(R.string.feedback)
    }

    override fun shouldShowInitialInfo(): Boolean {
        return quiz!!.isPasswordProtected
    }

    override fun loadInitialInfo(infoContainer: ViewGroup) {
        // Only in the case of password protection
        checkPasswordProtectionAndShowQuestion()
    }

    override fun showResultsInfo() {
        val feedbackMessage = quiz?.getFeedbackMessageBasedOnQuizGrade(getPercentScore())
        val title = view?.findViewById<TextView>(R.id.quiz_results_score)
        if (!feedbackMessage.isNullOrEmpty()) {
            title?.text = feedbackMessage
        } else {
            title?.setText(R.string.widget_feedback_submit_title)
        }
    }

    override fun getFinishButtonLabel(): String {
        return getString(R.string.widget_feedback_submit)
    }

    override fun showBaselineResultMessage() {
        // We don't show any baseline message for feedback
    }

    override fun saveAttemptTracker() {
        val timetaken = getSpentTime()
        GamificationServiceDelegate(requireActivity())
            .createActivityIntent(course!!, activity, getActivityCompleted(), isBaseline)
            .registerFeedbackEvent(timetaken, quiz, quiz!!.id, quiz!!.instanceID)
    }

    override fun showAnswersFeedback() {
        val recyclerQuestionFeedbackLV = view?.findViewById<RecyclerView>(R.id.recycler_quiz_results_feedback)
        recyclerQuestionFeedbackLV?.addItemDecoration(
            DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL)
        )
        val quizAnswersFeedback = ArrayList<QuizAnswerFeedback>()
        val questions = quiz!!.questions
        for (q in questions) {
            if (q !is Description && !q.isSkipped) {
                val qf = QuizAnswerFeedback()
                qf.isSurvey = true
                qf.score = 100F
                qf.questionText = q.getTitle(prefLang)
                qf.userResponse = q.userResponses
                quizAnswersFeedback.add(qf)
            }
        }
        val adapterQuizFeedback = QuizAnswersFeedbackAdapter(requireActivity(), quizAnswersFeedback)
        recyclerQuestionFeedbackLV?.adapter = adapterQuizFeedback
    }

    override fun getActivityCompleted(): Boolean {
        return isOnResultsPage
    }

}