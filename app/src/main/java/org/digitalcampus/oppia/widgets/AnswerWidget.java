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

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.WidgetQuizBinding;
import org.digitalcampus.mobile.quiz.InvalidQuizException;
import org.digitalcampus.mobile.quiz.Quiz;
import org.digitalcampus.mobile.quiz.model.QuizQuestion;
import org.digitalcampus.mobile.quiz.model.questiontypes.Description;
import org.digitalcampus.mobile.quiz.model.questiontypes.Essay;
import org.digitalcampus.mobile.quiz.model.questiontypes.Matching;
import org.digitalcampus.mobile.quiz.model.questiontypes.MultiChoice;
import org.digitalcampus.mobile.quiz.model.questiontypes.MultiSelect;
import org.digitalcampus.mobile.quiz.model.questiontypes.Numerical;
import org.digitalcampus.mobile.quiz.model.questiontypes.ShortAnswer;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.QuizAttemptRepository;
import org.digitalcampus.oppia.model.QuizStats;
import org.digitalcampus.oppia.utils.TextUtilsJava;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.resources.ExternalResourceOpener;
import org.digitalcampus.oppia.utils.ui.ProgressBarAnimator;
import org.digitalcampus.oppia.utils.ui.SimpleAnimator;
import org.digitalcampus.oppia.widgets.quiz.DescriptionWidget;
import org.digitalcampus.oppia.widgets.quiz.EssayWidget;
import org.digitalcampus.oppia.widgets.quiz.MatchingWidget;
import org.digitalcampus.oppia.widgets.quiz.MultiChoiceWidget;
import org.digitalcampus.oppia.widgets.quiz.MultiSelectWidget;
import org.digitalcampus.oppia.widgets.quiz.NumericalWidget;
import org.digitalcampus.oppia.widgets.quiz.QuestionWidget;
import org.digitalcampus.oppia.widgets.quiz.ShortAnswerWidget;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

public abstract class AnswerWidget extends BaseWidget {

    public static final String TAG = AnswerWidget.class.getSimpleName();
    protected static final String PROPERTY_QUIZ = "quiz";
    protected static final String PROPERTY_ON_RESULTS_PAGE = "OnResultsPage";
    protected static final String PROPERTY_ATTEMPT_SAVED = "attemptSaved";
    protected static final String PROPERTY_INITIAL_INFO_SHOWN = "initialInfoShown";
    protected static final String PROPERTY_LANG = "quiz_lang";

    static final int QUIZ_AVAILABLE = -1;
    private static final int PROGRESS_ANIM_DURATION = 600;
    protected Quiz quiz;
    private QuestionWidget currentQuestion;
    private String previousLang;
    private String contents;

    boolean isOnResultsPage = false;
    private boolean initialInfoShown = false;
    private boolean quizAttemptSaved = false;
    private boolean loadingQuizErrorDisplayed = false;

    private MediaPlayer mp;

    private ProgressBarAnimator barAnim;

    @Inject
    QuizAttemptRepository attemptsRepository;
    protected WidgetQuizBinding binding;
    private ViewGroup container;

    public AnswerWidget() {
        // Required empty public constructor
    }

    abstract int getContentAvailability(boolean afterAttempt);

    abstract String getAnswerWidgetType();

    abstract String getFinishButtonLabel();

    abstract void showBaselineResultMessage();

    abstract void saveAttemptTracker();

    abstract void showAnswersFeedback();

    abstract boolean shouldShowInitialInfo();

    abstract void loadInitialInfo(ViewGroup infoContainer);

    abstract void showResultsInfo();

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = WidgetQuizBinding.inflate(inflater, container, false);

        this.container = container;
        course = (Course) getArguments().getSerializable(Course.TAG);
        activity = ((Activity) getArguments().getSerializable(Activity.TAG));
        contents = activity.getContents(prefLang);

        getAppComponent().inject(this);

        setIsBaseline(getArguments().getBoolean(CourseActivity.BASELINE_TAG));
        binding.getRoot().setId(activity.getActId());
        if ((savedInstanceState != null) && (savedInstanceState.getSerializable(BaseWidget.WIDGET_CONFIG) != null)) {
            setWidgetConfig((HashMap<String, Object>) savedInstanceState.getSerializable(BaseWidget.WIDGET_CONFIG));
        }

        return binding.getRoot();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BaseWidget.WIDGET_CONFIG, getWidgetConfig());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetchViews();
        loadContent();
    }

    private void fetchViews() {
        this.barAnim = new ProgressBarAnimator(binding.progressQuiz);
        this.barAnim.setAnimDuration(PROGRESS_ANIM_DURATION);
        this.binding.questionImage.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadContent();
    }

    private void loadContent() {
        if (this.quiz == null) {
            Quiz loadedQuiz = new Quiz();
            boolean loadSuccess = loadedQuiz.load(contents, prefLang);
            if (!loadSuccess) {
                if (!loadingQuizErrorDisplayed) {
                    showLoadingError();
                }
                return;
            }
            quiz = loadedQuiz;
        }

        if (previousLang != null && !previousLang.equalsIgnoreCase(prefLang)){
            Log.d(TAG, "Quiz lang changed, updating responses!");
            quiz.updateResponsesAfterLanguageChange(previousLang, prefLang);
            previousLang = prefLang;
        }

        if (this.isOnResultsPage) {
            showResults();
            return;
        }

        if (course.hasStatus(Course.STATUS_READ_ONLY)) {
            showContentUnavailableRationale(getString(R.string.read_only_answer_unavailable_message,
                    getAnswerWidgetType().toLowerCase(Locale.ROOT)));
            return;
        }

        int contentAvailability = getContentAvailability(false);
        if (contentAvailability != QUIZ_AVAILABLE) {
            showContentUnavailableRationale(getString(contentAvailability));
            return;
        }

        if (quiz.getCurrentQuestionNo() <= 1 && !initialInfoShown && shouldShowInitialInfo()) {
            loadInitialInfo(binding.initialInfoContainer);
            binding.initialInfoContainer.setVisibility(View.VISIBLE);
            return;
        }

        binding.initialInfoContainer.setVisibility(View.GONE);
        this.showQuestion();
    }

    @CallSuper
    protected void showContentUnavailableRationale(String unavailabilityReasonString) {
        View localContainer = getView();
        if (localContainer != null){
            ViewGroup vg = localContainer.findViewById(activity.getActId());
            if (vg!=null){
                vg.removeAllViews();
                vg.addView(View.inflate(getView().getContext(), R.layout.widget_quiz_unavailable, null));

                TextView tv = getView().findViewById(R.id.quiz_unavailable);
                tv.setText(unavailabilityReasonString);
            }
        }
    }

    protected boolean isUserOverLimitedAttempts(boolean afterAttempt){
        if (this.quiz.limitAttempts()){
            //Check if the user has attempted the quiz the max allowed
            QuizStats qs = attemptsRepository.getQuizAttemptStats(this.getActivity(), course.getCourseId(), activity.getDigest());
            if (afterAttempt){
                //If the quiz was just attempted, it is not saved yet, so we added
                qs.setNumAttempts(qs.getNumAttempts() + 1);
            }
            return qs.getNumAttempts() >= quiz.getMaxAttempts();
        }
        return false;
    }

    private void showLoadingError() {
        View localContainer = getView();
        if (localContainer != null) {
            ViewGroup vg = localContainer.findViewById(activity.getActId());
            if (vg != null) {
                vg.removeAllViews();
                vg.addView(View.inflate(getView().getContext(), R.layout.widget_quiz_unavailable, null));

                TextView tv = getView().findViewById(R.id.quiz_unavailable);
                tv.setText(R.string.quiz_loading_error);
                loadingQuizErrorDisplayed = true;
            }
        }
    }

    protected void showQuestion() {

        binding.initialInfoContainer.setVisibility(View.GONE);
        initialInfoShown = true;

        clearMediaPlayer();
        QuizQuestion q;
        try {
            q = this.quiz.getCurrentQuestion();
        } catch (InvalidQuizException e) {
            Toast.makeText(super.getActivity(), super.getActivity().getString(R.string.error_quiz_no_questions), Toast.LENGTH_LONG).show();
            Analytics.logException(e);
            Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e);
            return;
        }

        binding.questionText.setVisibility(View.VISIBLE);
        // convert in case has any html special chars
        String questionText = q.getTitle(prefLang);
        binding.questionText.setText(UIUtils.getFromHtmlAndTrim(questionText));

        if (q.getProp("image") == null) {
            binding.questionImage.setVisibility(View.GONE);
        } else {
            String fileUrl = course.getLocation() + q.getProp("image");
            Bitmap myBitmap = BitmapFactory.decodeFile(fileUrl);
            File file = new File(fileUrl);
            binding.questionImageImage.setImageBitmap(myBitmap);
            binding.questionImageImage.setTag(file);
            if (q.getProp("media") == null) {
                OnImageClickListener oicl = new OnImageClickListener(super.getActivity());
                binding.questionImageImage.setOnClickListener(oicl);
                binding.questionImageCaption.setText(R.string.widget_quiz_image_caption);
                binding.questionImage.setVisibility(View.VISIBLE);
            } else {
                binding.questionImageCaption.setText(R.string.widget_quiz_media_caption);
                OnMediaClickListener omcl = new OnMediaClickListener(q.getProp("media"));
                binding.questionImageImage.setOnClickListener(omcl);
                binding.questionImage.setVisibility(View.VISIBLE);
            }

        }

        if (q instanceof MultiChoice) {
            currentQuestion = new MultiChoiceWidget(super.getActivity(), getView(), container, q);
        } else if (q instanceof Essay) {
            currentQuestion = new EssayWidget(super.getActivity(), getView(), container);
        } else if (q instanceof MultiSelect) {
            currentQuestion = new MultiSelectWidget(super.getActivity(), getView(), container, q);
        } else if (q instanceof ShortAnswer) {
            currentQuestion = new ShortAnswerWidget(super.getActivity(), getView(), container);
        } else if (q instanceof Matching) {
            currentQuestion = new MatchingWidget(super.getActivity(), getView(), container);
        } else if (q instanceof Numerical) {
            currentQuestion = new NumericalWidget(super.getActivity(), getView(), container);
        } else if (q instanceof Description) {
            currentQuestion = new DescriptionWidget(getView());
        } else {
            return;
        }
        currentQuestion.setQuestionResponses(q.getResponseOptions(), q.getUserResponses());
        this.setProgress();
        this.setNav();
    }

    protected void checkPasswordProtectionAndShowQuestion() {
        if (!quiz.isPasswordProtected()) {
            showQuestion();
        } else {
            ViewGroup infoContainer = binding.initialInfoContainer;
            infoContainer.removeAllViews();
            ViewGroup password_view = (ViewGroup) View.inflate(infoContainer.getContext(), R.layout.view_activity_password, infoContainer);
            EditText passwordET = password_view.findViewById(R.id.activity_password_field);

            password_view.findViewById(R.id.submit_activity_password).setOnClickListener(view -> {
                String password = passwordET.getText().toString();
                if (TextUtilsJava.equals(password, quiz.getPassword())) {
                    showQuestion();
                }
                else{
                    View passwordError = password_view.findViewById(R.id.activity_password_error);
                    passwordError.setVisibility(View.VISIBLE);
                    SimpleAnimator.fade(passwordError, SimpleAnimator.FADE_IN);
                    passwordET.setText("");
                }
            });
        }
    }

    private void setNav() {
        binding.mquizNextBtn.setVisibility(View.VISIBLE);
        binding.mquizPrevBtn.setVisibility(View.VISIBLE);

        if (quiz.hasPrevious()) {
            binding.mquizPrevBtn.setOnClickListener(v -> {
                // save answer
                saveAnswer();
                if (quiz.hasPrevious()) {
                    quiz.movePrevious();
                    showQuestion();
                }
            });
            binding.mquizPrevBtn.setEnabled(true);
        } else {
            binding.mquizPrevBtn.setEnabled(false);
        }

        binding.mquizNextBtn.setOnClickListener(nextBtnClickListener());
        // set label on next button
        if (quiz.getCurrentQuestionNo() == quiz.getTotalNoQuestions()) {
            binding.mquizNextBtn.setText(getFinishButtonLabel());
        } else {
            binding.mquizNextBtn.setText(getString(R.string.widget_quiz_next));
        }
    }

    private View.OnClickListener nextBtnClickListener() {
        return v -> {
            // save answer
            if (saveAnswer()) {
                String feedback;
                try {
                    feedback = quiz.getCurrentQuestion().getFeedback(prefLang);
                    if (!feedback.equals("") &&
                            quiz.getShowFeedback() == Quiz.SHOW_FEEDBACK_ALWAYS
                            && !quiz.getCurrentQuestion().getFeedbackDisplayed()) {
                        UIUtils.hideSoftKeyboard(v);
                        showFeedback(feedback);
                    } else {
                        nextStep();
                    }
                } catch (InvalidQuizException e) {
                    Analytics.logException(e);
                    Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e);
                }
            } else {
                CharSequence text = getString(R.string.widget_quiz_noanswergiven);
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(getContext(), text, duration);
                toast.show();
            }
        };
    }

    private View.OnClickListener getCloseBtnListener() {
        return v -> ((CourseActivity) getActivity()).onQuizFinished();
    }

    private void nextStep() {
        if (quiz.hasNext()) {
            quiz.moveNext();
            showQuestion();
        } else {
            showResults();
        }
    }

    private void setProgress() {

        int current = binding.progressQuiz.getProgress();
        binding.progressQuiz.setMax(quiz.getTotalNoQuestions());
        barAnim.animate(current, quiz.getCurrentQuestionNo());

        binding.tvQuizProgress.setText(quiz.getCurrentQuestionNo() + "/" + quiz.getTotalNoQuestions());

    }

    private boolean saveAnswer() {
        if (currentQuestion == null){
            return false;
        }
        try {
            List<String> answers = currentQuestion.getQuestionResponses(quiz.getCurrentQuestion().getResponseOptions());
            if (quiz.getCurrentQuestion().responseExpected() && (answers == null || answers.isEmpty())) {
                return false;
            }

            quiz.getCurrentQuestion().setUserResponses(answers);
            return true;

        } catch (InvalidQuizException e) {
            Analytics.logException(e);
            Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e);
        }
        return false;
    }

    private void showFeedback(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Oppia_AlertDialogStyle);
        builder.setTitle(getContext().getString(R.string.feedback));
        builder.setMessage(UIUtils.getFromHtmlAndTrim(msg));
        try {
            if (quiz.getCurrentQuestion().getScoreAsPercent() >= Quiz.QUIZ_QUESTION_PASS_THRESHOLD) {
                builder.setIcon(R.drawable.quiz_tick);
            } else if (quiz.getCurrentQuestion().getScoreAsPercent() > 0) {
                builder.setIcon(R.drawable.quiz_partially_correct);
            } else {
                builder.setIcon(R.drawable.quiz_cross);
            }
        } catch (InvalidQuizException e) {
            Analytics.logException(e);
            Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e);
        }
        builder.setPositiveButton(R.string.ok, (arg0, arg1) -> nextStep());
        builder.show();
        try {
            quiz.getCurrentQuestion().setFeedbackDisplayed(true);
        } catch (InvalidQuizException e) {
            Analytics.logException(e);
            Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e);
        }
    }

    public void showResults() {
        clearMediaPlayer();
        // log the activity as complete
        isOnResultsPage = true;
        quiz.mark(prefLang);
        this.saveTracker();

        View quizResultsLayout = getView() == null ? null : getView().findViewById(R.id.widget_quiz_results);
        if (quizResultsLayout == null) {
            // load new layout
            View progressContainer = getView().findViewById(R.id.progress_container);
            ViewGroup parent = (ViewGroup) progressContainer.getParent();
            int index = parent.indexOfChild(progressContainer);
            parent.removeView(progressContainer);
            progressContainer = super.getActivity().getLayoutInflater().inflate(R.layout.widget_quiz_results, parent, false);
            parent.addView(progressContainer, index);
        }

        Button actionBtn = getView().findViewById(R.id.quiz_results_button);
        Button exitBtn = getView().findViewById(R.id.quiz_exit_button);
        showResultsInfo();

        if (this.isBaseline) {
            showBaselineResultMessage();
        }
        // Show the detail of which questions were right/wrong
        if (quiz.getShowFeedback() == Quiz.SHOW_FEEDBACK_ALWAYS || quiz.getShowFeedback() == Quiz.SHOW_FEEDBACK_AT_END) {
            showAnswersFeedback();
        }

        int quizAvailabilityMessage = getContentAvailability(true);
        boolean contentAvailable = quizAvailabilityMessage == QUIZ_AVAILABLE;
        if (!contentAvailable) {
            TextView availabilityMsg = getView().findViewById(R.id.quiz_availability_message);
            availabilityMsg.setText(quizAvailabilityMessage);
            availabilityMsg.setVisibility(View.VISIBLE);
        }

        exitBtn.setOnClickListener(getCloseBtnListener());
        if (this.isBaseline) {
            exitBtn.setText(getString(R.string.widget_quiz_baseline_goto_course));
            actionBtn.setVisibility(View.GONE);
        } else if (this.getActivityCompleted() || !contentAvailable) {
            actionBtn.setVisibility(View.GONE);
        } else {
            actionBtn.setText(getString(R.string.widget_quiz_results_restart));
            actionBtn.setOnClickListener(v -> restart());
        }
    }

    private void restart() {
        this.setStartTime(System.currentTimeMillis() / 1000);

        this.quiz = new Quiz();
        this.quiz.load(contents, prefLang);
        this.isOnResultsPage = false;
        this.quizAttemptSaved = false;

        // reload quiz layout
        View quizResultsView = getView().findViewById(R.id.widget_quiz_results);
        ViewGroup parent = (ViewGroup) quizResultsView.getParent();
        int index = parent.indexOfChild(quizResultsView);
        parent.removeView(quizResultsView);
        binding = WidgetQuizBinding.inflate(getLayoutInflater(), parent, false);
        parent.addView(binding.getRoot(), index);

        fetchViews();
        showQuestion();
    }


    @Override
    public void saveTracker() {
        if (activity == null || !isOnResultsPage || quizAttemptSaved) {
            return;
        }

        Log.d(TAG, "Saving tracker");
        saveAttemptTracker();
        quizAttemptSaved = true;
    }

    @Override
    public HashMap<String, Object> getWidgetConfig() {
        saveAnswer(); // Before setting the quiz, we save the current answer
        HashMap<String, Object> config = new HashMap<>();
        config.put(PROPERTY_QUIZ, this.quiz);
        config.put(PROPERTY_ACTIVITY_STARTTIME, this.getStartTime());
        config.put(PROPERTY_ON_RESULTS_PAGE, this.isOnResultsPage);
        config.put(PROPERTY_ATTEMPT_SAVED, this.quizAttemptSaved);
        config.put(PROPERTY_INITIAL_INFO_SHOWN, initialInfoShown);
        config.put(PROPERTY_LANG, prefLang);
        return config;
    }

    @Override
    public void setWidgetConfig(HashMap<String, Object> config) {
        if (config.containsKey(PROPERTY_QUIZ)) {
            this.quiz = (Quiz) config.get(PROPERTY_QUIZ);
        }
        if (config.containsKey(BaseWidget.PROPERTY_ACTIVITY_STARTTIME)) {
            this.setStartTime((Long) config.get(BaseWidget.PROPERTY_ACTIVITY_STARTTIME));
        }
        if (config.containsKey(PROPERTY_ON_RESULTS_PAGE)) {
            this.isOnResultsPage = (Boolean) config.get(PROPERTY_ON_RESULTS_PAGE);
        }
        if (config.containsKey(PROPERTY_ATTEMPT_SAVED)) {
            this.quizAttemptSaved = (Boolean) config.get(PROPERTY_ATTEMPT_SAVED);
        }
        if (config.containsKey(PROPERTY_INITIAL_INFO_SHOWN)){
            this.initialInfoShown = (Boolean) config.get(PROPERTY_INITIAL_INFO_SHOWN);
        }
        if (config.containsKey(PROPERTY_LANG)) {
            this.previousLang = (String) config.get(PROPERTY_LANG);
        }
    }

    @Override
    public String getContentToRead() {
        // Get the current question text
        String toRead = "";
        try {
            toRead = quiz.getCurrentQuestion().getTitle(prefLang);
        } catch (InvalidQuizException e) {
            Analytics.logException(e);
            Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e);
        }
        return toRead;
    }

    private void clearMediaPlayer() {
        if ((mp != null)) {
            if (mp.isPlaying()) {
                mp.stop();
            }
            mp.release();
            mp = null;
        }
    }

    private class OnImageClickListener implements OnClickListener {

        private final Context ctx;

        public OnImageClickListener(Context ctx) {
            this.ctx = ctx;
        }

        public void onClick(View v) {
            File file = (File) v.getTag();
            // check the file is on the file system (should be but just in case)
            if (!file.exists()) {
                Toast.makeText(this.ctx, this.ctx.getString(R.string.error_resource_not_found, file.getName()), Toast.LENGTH_LONG).show();
                return;
            }
            // check there is actually an app installed to open this filetype
            Intent intent = ExternalResourceOpener.getIntentToOpenResource(ctx, file);
            if (intent != null) {
                this.ctx.startActivity(intent);
            } else {
                Toast.makeText(this.ctx, this.ctx.getString(R.string.error_resource_app_not_found, file.getName()), Toast.LENGTH_LONG).show();
            }
        }

    }

    private class OnMediaClickListener implements OnClickListener {
        private final String mediaFileName;

        public OnMediaClickListener(String mediaFileName) {
            this.mediaFileName = mediaFileName;
        }

        public void onClick(View v) {
            startMediaPlayerWithFile(mediaFileName);
        }
    }

    public float getPercentScore() {
        quiz.mark(prefLang);
        return quiz.getQuizPercentageScore();
    }
}
