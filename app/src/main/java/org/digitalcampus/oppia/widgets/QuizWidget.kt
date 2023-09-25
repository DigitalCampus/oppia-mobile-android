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

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.quiz.Quiz
import org.digitalcampus.mobile.quiz.model.questiontypes.Description
import org.digitalcampus.oppia.activity.CourseActivity
import org.digitalcampus.oppia.activity.CourseQuizAttemptsActivity
import org.digitalcampus.oppia.adapter.QuizAnswersFeedbackAdapter
import org.digitalcampus.oppia.application.SessionManager.getUsername
import org.digitalcampus.oppia.database.DbHelper
import org.digitalcampus.oppia.gamification.GamificationServiceDelegate
import org.digitalcampus.oppia.model.Activity
import org.digitalcampus.oppia.model.Course
import org.digitalcampus.oppia.model.QuizAnswerFeedback
import org.digitalcampus.oppia.model.QuizStats

class QuizWidget : AnswerWidget() {

    companion object {
        val TAG = QuizWidget::class.simpleName
        @JvmStatic
        fun newInstance(activity: Activity, course: Course, isBaseline: Boolean): QuizWidget {
            val myFragment = QuizWidget()
            val args = Bundle()
            args.putSerializable(Activity.TAG, activity)
            args.putSerializable(Course.TAG, course)
            args.putBoolean(CourseActivity.BASELINE_TAG, isBaseline)
            myFragment.arguments = args
            return myFragment
        }
    }

    override fun getContentAvailability(afterAttempt: Boolean): Int {
        if (isUserOverLimitedAttempts(afterAttempt)) {
            return R.string.widget_quiz_unavailable_attempts
        }
        // determine availability
        if (quiz!!.availability == Quiz.AVAILABILITY_ALWAYS) {
            return QUIZ_AVAILABLE
        } else if (quiz!!.availability == Quiz.AVAILABILITY_SECTION) {
            // check to see if all previous section activities have been completed
            val db = DbHelper.getInstance(requireActivity())
            val userId = db.getUserId(getUsername(requireActivity()))
            return if (db.isPreviousSectionActivitiesCompleted(activity, userId)) {
                QUIZ_AVAILABLE
            } else {
                R.string.widget_quiz_unavailable_section
            }
        } else if (quiz!!.availability == Quiz.AVAILABILITY_COURSE) {
            // check to see if all previous course activities have been completed
            val db = DbHelper.getInstance(requireActivity())
            val userId = db.getUserId(getUsername(requireActivity()))
            return if (db.isPreviousCourseActivitiesCompleted(activity, userId)) {
                QUIZ_AVAILABLE
            } else {
                R.string.widget_quiz_unavailable_course
            }
        }
        //If none of the conditions apply, set it as available
        return QUIZ_AVAILABLE
    }

    override fun getAnswerWidgetType(): String {
        return getString(R.string.quiz)
    }

    override fun showContentUnavailableRationale(unavailabilityReasonString: String?) {
        super.showContentUnavailableRationale(unavailabilityReasonString)
        val quizStats = attemptsRepository!!.getQuizAttemptStats(
            requireActivity(),
            course!!.courseId,
            activity?.digest
        )
        quizStats.quizTitle = activity?.getTitle(prefLang)
        if (quizStats.isAttempted()) {
            val button = view?.findViewById<Button>(R.id.btn_quiz_unavailable)
            button?.visibility = View.VISIBLE
            button?.setText(R.string.view_your_previous_attempts)
            button?.setOnClickListener {
                val i = Intent(requireActivity(), CourseQuizAttemptsActivity::class.java)
                i.putExtra(QuizStats.TAG, quizStats)
                i.putExtra(CourseQuizAttemptsActivity.SHOW_ATTEMPT_BUTTON, false)
                startActivity(i)
            }
        }
    }

    override fun getFinishButtonLabel(): String {
        return getString(R.string.widget_quiz_getresults)
    }

    override fun showResultsInfo() {
        val title = view?.findViewById<TextView>(R.id.quiz_results_score)
        title?.text = getString(R.string.widget_quiz_results_score, getPercentScore())
        if (!isBaseline) {
            val info = view?.findViewById<ViewGroup>(R.id.quiz_stats)
            info?.visibility = View.VISIBLE
            val stats = attemptsRepository!!.getQuizAttemptStats(
                context,
                course!!.courseId,
                activity?.digest
            )
            // We take into account the current quiz (not saved yet)
            val numAttempts = stats.numAttempts
            val average = (stats.averageScore * numAttempts + quiz!!.userscore) / (numAttempts + 1)
            stats.maxScore = Math.max(quiz!!.maxscore, stats.maxScore)
            stats.numAttempts = numAttempts + 1
            stats.userScore = Math.max(quiz!!.userscore, stats.userScore)
            stats.averageScore = average
            showStats(info, stats)
        }
        if (!quiz!!.mustShowQuizResultsAtEnd()) {
            view?.findViewById<View>(R.id.recycler_quiz_results_feedback)?.visibility = View.GONE
        }
    }

    override fun shouldShowInitialInfo(): Boolean {
        return !isBaseline
    }

    private fun showStats(infoContainer: ViewGroup?, stats: QuizStats) {
        val average = infoContainer?.findViewById<TextView>(R.id.highlight_average)
        val best = infoContainer?.findViewById<TextView>(R.id.highlight_best)
        val numAttempts = infoContainer?.findViewById<TextView>(R.id.highlight_attempted)
        val infoAttempts = infoContainer?.findViewById<TextView>(R.id.info_num_attempts)
        val threshold = infoContainer?.findViewById<TextView>(R.id.info_threshold)
        numAttempts?.text = stats.numAttempts.toString()
        threshold?.text = getString(R.string.widget_quiz_pass_threshold, quiz!!.passThreshold)
        if (quiz!!.limitAttempts()) {
            val attemptsLeft = (quiz!!.maxAttempts - stats.numAttempts).coerceAtLeast(0)
            infoAttempts?.text = getString(R.string.quiz_attempts_left, quiz!!.maxAttempts, attemptsLeft)
        } else {
            infoAttempts?.setText(R.string.quiz_attempts_unlimited)
        }
        if (stats.numAttempts == 0) {
            average?.text = "-"
            best?.text = "-"
        } else {
            average?.text = stats.getAveragePercent().toString() + "%"
            best?.text = stats.getPercent().toString() + "%"
        }
    }

    override fun loadInitialInfo(infoContainer: ViewGroup) {
        infoContainer.removeAllViews()
        val info = View.inflate(infoContainer.context, R.layout.view_quiz_info, infoContainer) as ViewGroup
        val thresholdBar = info.findViewById<ProgressBar>(R.id.threshold_bar)
        val numQuestions = info.findViewById<TextView>(R.id.info_num_questions)
        numQuestions.text = getString(R.string.widget_quiz_num_questions, quiz!!.totalNoQuestions)
        thresholdBar.progress = quiz!!.passThreshold
        info.findViewById<View>(R.id.take_quiz_btn).setOnClickListener { checkPasswordProtectionAndShowQuestion() }
        val stats = attemptsRepository!!.getQuizAttemptStats(context, course!!.courseId, activity?.digest)
        showStats(info, stats)
    }

    override fun showBaselineResultMessage() {
        val baselineText = view?.findViewById<TextView>(R.id.quiz_results_baseline)
        baselineText?.text = getString(R.string.widget_quiz_baseline_completed)
        baselineText?.visibility = View.VISIBLE
    }

    override fun getActivityCompleted(): Boolean {
        Log.d(TAG, "Threshold:" + quiz!!.passThreshold)
        val passThreshold: Int = if (quiz!!.passThreshold >= 0) {
            quiz!!.passThreshold
        } else {
            Quiz.QUIZ_DEFAULT_PASS_THRESHOLD
        }
        Log.d(TAG, "Percent:" + getPercentScore())
        return isOnResultsPage && getPercentScore() >= passThreshold
    }

    override fun saveAttemptTracker() {
        val timetaken = getSpentTime()
        GamificationServiceDelegate(requireActivity())
            .createActivityIntent(course!!, activity, getActivityCompleted(), isBaseline)
            .registerQuizAttemptEvent(timetaken, quiz, getPercentScore())
    }

    override fun showAnswersFeedback() {
        val recyclerQuestionFeedbackLV = view?.findViewById<RecyclerView>(R.id.recycler_quiz_results_feedback)
        recyclerQuestionFeedbackLV?.addItemDecoration(
            DividerItemDecoration(requireActivity(), DividerItemDecoration.VERTICAL)
        )
        val quizAnswersFeedback = ArrayList<QuizAnswerFeedback>()
        val questions = quiz!!.questions
        for (q in questions) {
            if (q !is Description) {
                val qf = QuizAnswerFeedback()
                qf.score = q.scoreAsPercent.toFloat()
                qf.questionText = q.getTitle(prefLang)
                qf.userResponse = q.userResponses
                val feedbackText = q.getFeedback(prefLang)
                qf.feedbackText = feedbackText.replace("&amp;gt;", "<")
                quizAnswersFeedback.add(qf)
            }
        }
        val adapterQuizFeedback = QuizAnswersFeedbackAdapter(requireActivity(), quizAnswersFeedback)
        recyclerQuestionFeedbackLV?.adapter = adapterQuizFeedback
    }
}