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

import android.content.Context
import android.content.DialogInterface
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.appcompat.app.AlertDialog
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.mobile.learning.databinding.WidgetQuizBinding
import org.digitalcampus.mobile.quiz.InvalidQuizException
import org.digitalcampus.mobile.quiz.Quiz
import org.digitalcampus.mobile.quiz.model.QuizQuestion
import org.digitalcampus.mobile.quiz.model.questiontypes.Description
import org.digitalcampus.mobile.quiz.model.questiontypes.Essay
import org.digitalcampus.mobile.quiz.model.questiontypes.Matching
import org.digitalcampus.mobile.quiz.model.questiontypes.MultiChoice
import org.digitalcampus.mobile.quiz.model.questiontypes.MultiSelect
import org.digitalcampus.mobile.quiz.model.questiontypes.Numerical
import org.digitalcampus.mobile.quiz.model.questiontypes.ShortAnswer
import org.digitalcampus.oppia.activity.CourseActivity
import org.digitalcampus.oppia.analytics.Analytics.logException
import org.digitalcampus.oppia.model.Activity
import org.digitalcampus.oppia.model.Course
import org.digitalcampus.oppia.model.QuizAttemptRepository
import org.digitalcampus.oppia.utils.TextUtilsJava
import org.digitalcampus.oppia.utils.UIUtils.getFromHtmlAndTrim
import org.digitalcampus.oppia.utils.UIUtils.hideSoftKeyboard
import org.digitalcampus.oppia.utils.resources.ExternalResourceOpener.Companion.getIntentToOpenResource
import org.digitalcampus.oppia.utils.ui.ProgressBarAnimator
import org.digitalcampus.oppia.utils.ui.SimpleAnimator
import org.digitalcampus.oppia.widgets.quiz.DescriptionWidget
import org.digitalcampus.oppia.widgets.quiz.EssayWidget
import org.digitalcampus.oppia.widgets.quiz.MatchingWidget
import org.digitalcampus.oppia.widgets.quiz.MultiChoiceWidget
import org.digitalcampus.oppia.widgets.quiz.MultiSelectWidget
import org.digitalcampus.oppia.widgets.quiz.NumericalWidget
import org.digitalcampus.oppia.widgets.quiz.QuestionWidget
import org.digitalcampus.oppia.widgets.quiz.ShortAnswerWidget
import java.io.File
import javax.inject.Inject

abstract class AnswerWidget : BaseWidget() {

    companion object {
        val TAG = AnswerWidget::class.simpleName
        protected const val PROPERTY_QUIZ = "quiz"
        protected const val PROPERTY_ON_RESULTS_PAGE = "OnResultsPage"
        protected const val PROPERTY_ATTEMPT_SAVED = "attemptSaved"
        protected const val PROPERTY_INITIAL_INFO_SHOWN = "initialInfoShown"
        protected const val PROPERTY_LANG = "quiz_lang"
        const val QUIZ_AVAILABLE = -1
        private const val PROGRESS_ANIM_DURATION = 600
    }

    @JvmField protected var quiz: Quiz? = null
    private lateinit var currentQuestion: QuestionWidget
    private var previousLang: String? = null
    private lateinit var contents: String
    @JvmField var isOnResultsPage = false
    private var initialInfoShown = false
    private var quizAttemptSaved = false
    private var loadingQuizErrorDisplayed = false
    private var mp: MediaPlayer? = null
    private var barAnim: ProgressBarAnimator? = null

    @JvmField
    @Inject
    var attemptsRepository: QuizAttemptRepository? = null
    private lateinit var binding: WidgetQuizBinding
    private lateinit var container: ViewGroup

    protected abstract fun getContentAvailability(afterAttempt: Boolean): Int
    abstract fun getAnswerWidgetType(): String
    abstract fun getFinishButtonLabel(): String
    abstract fun showBaselineResultMessage()
    abstract fun saveAttemptTracker()
    abstract fun showAnswersFeedback()
    abstract fun shouldShowInitialInfo(): Boolean
    abstract fun loadInitialInfo(infoContainer: ViewGroup)
    abstract fun showResultsInfo()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = WidgetQuizBinding.inflate(inflater, container, false)
        this.container = container!!
        course = requireArguments().getSerializable(Course.TAG) as Course
        activity = requireArguments().getSerializable(Activity.TAG) as Activity
        contents = activity?.getContents(prefLang).toString()
        getAppComponent().inject(this)
        setIsBaseline(requireArguments().getBoolean(CourseActivity.BASELINE_TAG))
        binding.root.id = activity?.actId ?: 0
        if (savedInstanceState?.getSerializable(WIDGET_CONFIG) != null) {
            setWidgetConfig(savedInstanceState.getSerializable(WIDGET_CONFIG) as HashMap<String, Any>)
        }
        return binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(WIDGET_CONFIG, getWidgetConfig())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fetchViews()
        loadContent()
    }

    private fun fetchViews() {
        barAnim = ProgressBarAnimator(binding.progressQuiz)
        barAnim?.animDuration = PROGRESS_ANIM_DURATION
        binding.questionImage.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        loadContent()
    }

    private fun loadContent() {
        if (quiz == null) {
            val loadedQuiz = Quiz()
            val loadSuccess = loadedQuiz.load(contents, prefLang)
            if (!loadSuccess) {
                if (!loadingQuizErrorDisplayed) {
                    showLoadingError()
                }
                return
            }
            quiz = loadedQuiz
        }
        if (previousLang != null && !previousLang.equals(prefLang, ignoreCase = true)) {
            Log.d(TAG, "Quiz lang changed, updating responses!")
            quiz?.updateResponsesAfterLanguageChange(previousLang, prefLang)
            previousLang = prefLang
        }
        if (isOnResultsPage) {
            showResults()
            return
        }
        if (course!!.hasStatus(Course.STATUS_READ_ONLY)) {
            showContentUnavailableRationale(getString(R.string.read_only_answer_unavailable_message, getAnswerWidgetType().lowercase()))
            return
        }
        val contentAvailability = getContentAvailability(false)
        if (contentAvailability != QUIZ_AVAILABLE) {
            showContentUnavailableRationale(getString(contentAvailability))
            return
        }
        if (quiz!!.currentQuestionNo <= 1 && !initialInfoShown && shouldShowInitialInfo()) {
            loadInitialInfo(binding.initialInfoContainer)
            binding.initialInfoContainer.visibility = View.VISIBLE
            return
        }
        binding.initialInfoContainer.visibility = View.GONE
        showQuestion()
    }

    @CallSuper
    protected open fun showContentUnavailableRationale(unavailabilityReasonString: String?) {
        val localContainer = view
        if (localContainer != null) {
            val vg = localContainer.findViewById<ViewGroup>(activity?.actId ?: 0)
            if (vg != null) {
                vg.removeAllViews()
                vg.addView(View.inflate(view?.context, R.layout.widget_quiz_unavailable, null))
                val tv = view?.findViewById<TextView>(R.id.quiz_unavailable)
                tv?.text = unavailabilityReasonString
            }
        }
    }

    protected fun isUserOverLimitedAttempts(afterAttempt: Boolean): Boolean {
        if (quiz!!.limitAttempts()) {
            //Check if the user has attempted the quiz the max allowed
            val qs = attemptsRepository?.getQuizAttemptStats(
                requireActivity(),
                course!!.courseId,
                activity?.digest
            )
            if (afterAttempt) {
                //If the quiz was just attempted, it is not saved yet, so we added
                qs!!.numAttempts++
            }
            return qs!!.numAttempts >= quiz!!.maxAttempts
        }
        return false
    }

    private fun showLoadingError() {
        val localContainer = view
        if (localContainer != null) {
            val vg = localContainer.findViewById<ViewGroup>(activity?.actId ?: 0)
            if (vg != null) {
                vg.removeAllViews()
                vg.addView(View.inflate(view?.context, R.layout.widget_quiz_unavailable, null))
                val tv = view?.findViewById<TextView>(R.id.quiz_unavailable)
                tv?.setText(R.string.quiz_loading_error)
                loadingQuizErrorDisplayed = true
            }
        }
    }

    private fun showQuestion() {
        binding.initialInfoContainer.visibility = View.GONE
        initialInfoShown = true
        clearMediaPlayer()
        val q: QuizQuestion = try {
            quiz!!.currentQuestion
        } catch (e: InvalidQuizException) {
            Toast.makeText(
                super.requireActivity(),
                super.requireActivity().getString(R.string.error_quiz_no_questions),
                Toast.LENGTH_LONG
            ).show()
            logException(e)
            Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e)
            return
        }
        binding.questionText.visibility = View.VISIBLE
        // convert in case has any html special chars
        val questionText = q.getTitle(prefLang)
        binding.questionText.text = getFromHtmlAndTrim(questionText)
        if (q.getProp("image") == null) {
            binding.questionImage.visibility = View.GONE
        } else {
            val fileUrl = course!!.getLocation() + q.getProp("image")
            val myBitmap = BitmapFactory.decodeFile(fileUrl)
            val file = File(fileUrl)
            binding.questionImageImage.setImageBitmap(myBitmap)
            binding.questionImageImage.tag = file
            if (q.getProp("media") == null) {
                val oicl = OnImageClickListener(super.requireActivity())
                binding.questionImageImage.setOnClickListener(oicl)
                binding.questionImageCaption.setText(R.string.widget_quiz_image_caption)
                binding.questionImage.visibility = View.VISIBLE
            } else {
                binding.questionImageCaption.setText(R.string.widget_quiz_media_caption)
                val omcl = OnMediaClickListener(q.getProp("media"))
                binding.questionImageImage.setOnClickListener(omcl)
                binding.questionImage.visibility = View.VISIBLE
            }
        }

        currentQuestion = when (q) {
            is MultiChoice -> MultiChoiceWidget(super.requireActivity(), requireView(), container, q)
            is Essay -> EssayWidget(super.requireActivity(), requireView(), container)
            is MultiSelect -> MultiSelectWidget(super.requireActivity(), requireView(), container, q)
            is ShortAnswer -> ShortAnswerWidget(super.requireActivity(), requireView(), container)
            is Matching -> MatchingWidget(super.requireActivity(), requireView(), container)
            is Numerical -> NumericalWidget(super.requireActivity(), requireView(), container)
            is Description -> DescriptionWidget(requireView())
            else -> return
        }

        currentQuestion.setQuestionResponses(q.responseOptions, q.userResponses)
        setProgress()
        setNav()
    }

    protected fun checkPasswordProtectionAndShowQuestion() {
        if (!quiz!!.isPasswordProtected) {
            showQuestion()
        } else {
            val infoContainer: ViewGroup = binding.initialInfoContainer
            infoContainer.removeAllViews()
            val passwordView = View.inflate(infoContainer.context, R.layout.view_activity_password, infoContainer) as ViewGroup
            val passwordET = passwordView.findViewById<EditText>(R.id.activity_password_field)
            passwordView.findViewById<View>(R.id.submit_activity_password)
                .setOnClickListener {
                    val password = passwordET.text.toString()
                    if (TextUtilsJava.equals(password, quiz!!.password)) {
                        showQuestion()
                    } else {
                        val passwordError =
                            passwordView.findViewById<View>(R.id.activity_password_error)
                        passwordError.visibility = View.VISIBLE
                        SimpleAnimator.fade(passwordError, SimpleAnimator.FADE_IN)
                        passwordET.setText("")
                    }
                }
        }
    }

    private fun setNav() {
        binding.mquizNextBtn.visibility = View.VISIBLE
        binding.mquizPrevBtn.visibility = View.VISIBLE
        if (quiz!!.hasPrevious()) {
            binding.mquizPrevBtn.setOnClickListener {
                // save answer
                saveAnswer()
                if (quiz!!.hasPrevious()) {
                    quiz!!.movePrevious()
                    showQuestion()
                }
            }
            binding.mquizPrevBtn.isEnabled = true
        } else {
            binding.mquizPrevBtn.isEnabled = false
        }
        binding.mquizNextBtn.setOnClickListener(nextBtnClickListener())
        // set label on next button
        if (quiz!!.currentQuestionNo == quiz!!.totalNoQuestions) {
            binding.mquizNextBtn.text = getFinishButtonLabel()
        } else {
            binding.mquizNextBtn.text = getString(R.string.widget_quiz_next)
        }
    }

    private fun nextBtnClickListener(): View.OnClickListener {
        return View.OnClickListener { v: View? ->
            // save answer
            if (saveAnswer()) {
                val feedback: String
                try {
                    feedback = quiz!!.currentQuestion.getFeedback(prefLang)
                    if (feedback != "" && quiz!!.showFeedback == Quiz.SHOW_FEEDBACK_ALWAYS && !quiz!!.currentQuestion.feedbackDisplayed) {
                        hideSoftKeyboard(v)
                        showFeedback(feedback)
                    } else {
                        nextStep()
                    }
                } catch (e: InvalidQuizException) {
                    logException(e)
                    Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e)
                }
            } else {
                val text: CharSequence = getString(R.string.widget_quiz_noanswergiven)
                val duration = Toast.LENGTH_SHORT
                val toast = Toast.makeText(context, text, duration)
                toast.show()
            }
        }
    }

    private fun getCloseBtnListener(): View.OnClickListener {
        return View.OnClickListener { (requireActivity() as CourseActivity).onQuizFinished() }
    }

    private fun nextStep() {
        if (quiz!!.hasNext()) {
            quiz!!.moveNext()
            showQuestion()
        } else {
            showResults()
        }
    }

    private fun setProgress() {
        val current = binding.progressQuiz.progress
        binding.progressQuiz.max = quiz!!.totalNoQuestions
        barAnim!!.animate(current, quiz!!.currentQuestionNo)
        binding.tvQuizProgress.text = quiz!!.currentQuestionNo.toString() + "/" + quiz!!.totalNoQuestions
    }

    private fun saveAnswer(): Boolean {
        try {
            val answers = currentQuestion.getQuestionResponses(
                quiz!!.currentQuestion.responseOptions
            )
            if (quiz!!.currentQuestion.responseExpected() && (answers == null || answers.isEmpty())) {
                return false
            }
            quiz!!.currentQuestion.userResponses = answers
            return true
        } catch (e: InvalidQuizException) {
            logException(e)
            Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e)
        }
        return false
    }

    private fun showFeedback(msg: String) {
        val builder = AlertDialog.Builder(requireContext(), R.style.Oppia_AlertDialogStyle)
        builder.setTitle(requireContext().getString(R.string.feedback))
        builder.setMessage(getFromHtmlAndTrim(msg))
        try {
            if (quiz!!.currentQuestion.scoreAsPercent >= Quiz.QUIZ_QUESTION_PASS_THRESHOLD) {
                builder.setIcon(R.drawable.quiz_tick)
            } else if (quiz!!.currentQuestion.scoreAsPercent > 0) {
                builder.setIcon(R.drawable.quiz_partially_correct)
            } else {
                builder.setIcon(R.drawable.quiz_cross)
            }
        } catch (e: InvalidQuizException) {
            logException(e)
            Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e)
        }
        builder.setPositiveButton(R.string.ok) { arg0: DialogInterface?, arg1: Int -> nextStep() }
        builder.show()
        try {
            quiz!!.currentQuestion.feedbackDisplayed = true
        } catch (e: InvalidQuizException) {
            logException(e)
            Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e)
        }
    }

    private fun showResults() {
        clearMediaPlayer()
        // log the activity as complete
        isOnResultsPage = true
        quiz!!.mark(prefLang)
        saveTracker()
        val quizResultsLayout = view?.findViewById<View>(R.id.widget_quiz_results)
        if (quizResultsLayout == null) {
            // load new layout
            var progressContainer = view?.findViewById<View>(R.id.progress_container)
            val parent = progressContainer?.parent as ViewGroup
            val index = parent.indexOfChild(progressContainer)
            parent.removeView(progressContainer)
            progressContainer = super.requireActivity().layoutInflater.inflate(R.layout.widget_quiz_results, parent, false)
            parent.addView(progressContainer, index)
        }
        val actionBtn = view?.findViewById<Button>(R.id.quiz_results_button)
        val exitBtn = view?.findViewById<Button>(R.id.quiz_exit_button)
        showResultsInfo()
        if (isBaseline) {
            showBaselineResultMessage()
        }
        // Show the detail of which questions were right/wrong
        if (quiz!!.showFeedback == Quiz.SHOW_FEEDBACK_ALWAYS || quiz!!.showFeedback == Quiz.SHOW_FEEDBACK_AT_END) {
            showAnswersFeedback()
        }
        val quizAvailabilityMessage = getContentAvailability(true)
        val contentAvailable = quizAvailabilityMessage == QUIZ_AVAILABLE
        if (!contentAvailable) {
            val availabilityMsg = view?.findViewById<TextView>(R.id.quiz_availability_message)
            availabilityMsg?.setText(quizAvailabilityMessage)
            availabilityMsg?.visibility = View.VISIBLE
        }
        exitBtn?.setOnClickListener(getCloseBtnListener())
        if (isBaseline) {
            exitBtn?.text = getString(R.string.widget_quiz_baseline_goto_course)
            actionBtn?.visibility = View.GONE
        } else if (!contentAvailable) {
            actionBtn?.visibility = View.GONE
        } else {
            actionBtn?.text = getString(R.string.widget_quiz_results_restart)
            actionBtn?.setOnClickListener { restart() }
        }
    }

    private fun restart() {
        setStartTime(System.currentTimeMillis() / 1000)
        quiz = Quiz()
        quiz!!.load(contents, prefLang)
        isOnResultsPage = false
        quizAttemptSaved = false

        // reload quiz layout
        val quizResultsView = view?.findViewById<View>(R.id.widget_quiz_results)
        val parent = quizResultsView?.parent as ViewGroup
        val index = parent.indexOfChild(quizResultsView)
        parent.removeView(quizResultsView)
        binding = WidgetQuizBinding.inflate(
            layoutInflater, parent, false
        )
        parent.addView(binding.root, index)
        fetchViews()
        showQuestion()
    }

    override fun saveTracker() {
        if (activity == null || !isOnResultsPage || quizAttemptSaved) {
            return
        }
        Log.d(TAG, "Saving tracker")
        saveAttemptTracker()
        quizAttemptSaved = true
    }

    override fun getWidgetConfig(): HashMap<String, Any> {
        saveAnswer() // Before setting the quiz, we save the current answer
        val config = HashMap<String, Any>()
        config[PROPERTY_QUIZ] = quiz!!
        config[PROPERTY_ACTIVITY_STARTTIME] = getStartTime()
        config[PROPERTY_ON_RESULTS_PAGE] = isOnResultsPage
        config[PROPERTY_ATTEMPT_SAVED] = quizAttemptSaved
        config[PROPERTY_INITIAL_INFO_SHOWN] = initialInfoShown
        config[PROPERTY_LANG] = prefLang!!
        return config
    }

    override fun setWidgetConfig(config: HashMap<String, Any>) {
        if (config.containsKey(PROPERTY_QUIZ)) {
            quiz = config[PROPERTY_QUIZ] as Quiz
        }
        if (config.containsKey(PROPERTY_ACTIVITY_STARTTIME)) {
            setStartTime(config[PROPERTY_ACTIVITY_STARTTIME] as Long)
        }
        if (config.containsKey(PROPERTY_ON_RESULTS_PAGE)) {
            isOnResultsPage = config[PROPERTY_ON_RESULTS_PAGE] as Boolean
        }
        if (config.containsKey(PROPERTY_ATTEMPT_SAVED)) {
            quizAttemptSaved = config[PROPERTY_ATTEMPT_SAVED] as Boolean
        }
        if (config.containsKey(PROPERTY_INITIAL_INFO_SHOWN)) {
            initialInfoShown = config[PROPERTY_INITIAL_INFO_SHOWN] as Boolean
        }
        if (config.containsKey(PROPERTY_LANG)) {
            previousLang = config[PROPERTY_LANG] as String
        }
    }

    override fun getContentToRead(): String {
        // Get the current question text
        var toRead = ""
        try {
            toRead = quiz!!.currentQuestion.getTitle(prefLang)
        } catch (e: InvalidQuizException) {
            logException(e)
            Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e)
        }
        return toRead
    }

    private fun clearMediaPlayer() {
        if (mp != null) {
            if (mp!!.isPlaying) {
                mp!!.stop()
            }
            mp!!.release()
            mp = null
        }
    }

    private inner class OnImageClickListener(private val ctx: Context) : View.OnClickListener {
        override fun onClick(v: View) {
            val file = v.tag as File
            // check the file is on the file system (should be but just in case)
            if (!file.exists()) {
                Toast.makeText(ctx, ctx.getString(R.string.error_resource_not_found, file.name), Toast.LENGTH_LONG).show()
                return
            }
            // check there is actually an app installed to open this filetype
            val intent = getIntentToOpenResource(ctx, file)
            if (intent != null) {
                ctx.startActivity(intent)
            } else {
                Toast.makeText(ctx, ctx.getString(R.string.error_resource_app_not_found, file.name), Toast.LENGTH_LONG).show()
            }
        }
    }

    private inner class OnMediaClickListener(private val mediaFileName: String) : View.OnClickListener {
        override fun onClick(v: View) {
            startMediaPlayerWithFile(mediaFileName)
        }
    }

    fun getPercentScore(): Float {
        quiz!!.mark(prefLang)
        return quiz!!.quizPercentageScore
    }
}