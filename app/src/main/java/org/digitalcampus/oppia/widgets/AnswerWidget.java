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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
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
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.resources.ExternalResourceOpener;
import org.digitalcampus.oppia.utils.ui.ProgressBarAnimator;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.FragmentActivity;

public abstract class AnswerWidget extends BaseWidget {

    public static final String TAG = QuizWidget.class.getSimpleName();
    static final int QUIZ_AVAILABLE = -1;
    private static final int PROGRESS_ANIM_DURATION = 600;
    protected Quiz quiz;
    private QuestionWidget currentQuestion;
    private String contents;

    private Button prevBtn;
    private Button nextBtn;
    private ImageView playAudioBtn;
    private TextView qText;
    private LinearLayout questionImage;
    private ViewGroup container;
    private MediaPlayer mp;

    boolean isOnResultsPage = false;
    private boolean quizAttemptSaved = false;
    private boolean loadingQuizErrorDisplayed = false;

    private ProgressBar progressBar;
    private ProgressBarAnimator barAnim;

    public AnswerWidget() {
        // Required empty public constructor
    }

    abstract int getContentAvailability();
    abstract void showContentUnavailableRationale(int unavailabilityReasonStringResId);
    abstract String getFinishButtonLabel();
    abstract String getResultsTitle();
    abstract void showBaselineResultMessage();
    abstract void saveAttemptTracker();
    abstract void showAnswersFeedback();

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vv = inflater.inflate(R.layout.widget_quiz, container, false);
        this.container = container;
        course = (Course) getArguments().getSerializable(Course.TAG);
        activity = ((Activity) getArguments().getSerializable(Activity.TAG));
        contents = activity.getContents(prefLang);

        setIsBaseline(getArguments().getBoolean(CourseActivity.BASELINE_TAG));
        vv.setId(activity.getActId());
        if ((savedInstanceState != null) && (savedInstanceState.getSerializable(BaseWidget.WIDGET_CONFIG) != null)){
            setWidgetConfig((HashMap<String, Object>) savedInstanceState.getSerializable(BaseWidget.WIDGET_CONFIG));
        }

        return vv;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(BaseWidget.WIDGET_CONFIG, getWidgetConfig());

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetchViews();
        loadContent();
    }

    private void fetchViews(){
        this.prevBtn = getView().findViewById(R.id.mquiz_prev_btn);
        this.nextBtn = getView().findViewById(R.id.mquiz_next_btn);
        this.qText = getView().findViewById(R.id.question_text);
        this.questionImage = getView().findViewById(R.id.question_image);
        this.playAudioBtn = getView().findViewById(R.id.playAudioBtn);
        this.progressBar =  getView().findViewById(R.id.progress_quiz);
        this.barAnim = new ProgressBarAnimator(progressBar);
        this.barAnim.setAnimDuration(PROGRESS_ANIM_DURATION);
        this.questionImage.setVisibility(View.GONE);
        this.playAudioBtn.setVisibility(View.GONE);
    }

    @Override
    public void onResume(){
        super.onResume();
        loadContent();
    }

    private void loadContent(){
        if (this.quiz == null) {
            Quiz loadedQuiz = new Quiz();
            boolean loadSuccess = loadedQuiz.load(contents, prefLang);
            if (!loadSuccess){
                if (!loadingQuizErrorDisplayed){
                    showLoadingError();
                }
                return;
            }
            quiz = loadedQuiz;
        }

        if (this.isOnResultsPage) {
            showResults();
        }
        else{
            int contentAvailability = getContentAvailability();
            if (contentAvailability  == QUIZ_AVAILABLE){
                this.showQuestion();
            }
            else{
                showContentUnavailableRationale(contentAvailability);
            }
        }

    }

    private void showLoadingError() {
        View localContainer = getView();
        if (localContainer != null){
            ViewGroup vg = localContainer.findViewById(activity.getActId());
            if (vg!=null){
                vg.removeAllViews();
                vg.addView(View.inflate(getView().getContext(), R.layout.widget_quiz_unavailable, null));

                TextView tv = getView().findViewById(R.id.quiz_unavailable);
                tv.setText(R.string.quiz_loading_error);
                loadingQuizErrorDisplayed = true;
            }
        }
    }

    private void showQuestion() {
        clearMediaPlayer();
        QuizQuestion q;
        try {
            q = this.quiz.getCurrentQuestion();
        } catch (InvalidQuizException e) {
            Toast.makeText(super.getActivity(), super.getActivity().getString(R.string.error_quiz_no_questions), Toast.LENGTH_LONG).show();
            Mint.logException(e);
            Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e);
            return;
        }

        qText.setVisibility(View.VISIBLE);
        // convert in case has any html special chars
        String questionText = stripAudioFromText(q);
        qText.setText(HtmlCompat.fromHtml(questionText, HtmlCompat.FROM_HTML_MODE_LEGACY));

        if (q.getProp("image") == null) {
            questionImage.setVisibility(View.GONE);
        } else {
            String fileUrl = course.getLocation() + q.getProp("image");
            Bitmap myBitmap = BitmapFactory.decodeFile(fileUrl);
            File file = new File(fileUrl);
            ImageView iv = getView().findViewById(R.id.question_image_image);
            iv.setImageBitmap(myBitmap);
            iv.setTag(file);
            if (q.getProp("media") == null){
                OnImageClickListener oicl = new OnImageClickListener(super.getActivity());
                iv.setOnClickListener(oicl);
                TextView tv = getView().findViewById(R.id.question_image_caption);
                tv.setText(R.string.widget_quiz_image_caption);
                questionImage.setVisibility(View.VISIBLE);
            } else {
                TextView tv = getView().findViewById(R.id.question_image_caption);
                tv.setText(R.string.widget_quiz_media_caption);
                OnMediaClickListener omcl = new OnMediaClickListener(q.getProp("media"));
                iv.setOnClickListener(omcl);
                questionImage.setVisibility(View.VISIBLE);
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

    private String stripAudioFromText(QuizQuestion q) {
        String questionText = q.getTitle(prefLang);
        Pattern p = Pattern.compile("[a-zA-Z0-9\\-_]+\\.mp3");
        Matcher m = p.matcher(questionText);
        if (m.find()){
            final String mp3filename = m.group();
            questionText = questionText.replace(mp3filename, "");
            File file = new File(course.getLocation() + "resources/" + mp3filename);
            if (!file.exists()){
                playAudioBtn.setVisibility(View.GONE);
                return questionText;
            }
            final Uri mp3Uri = Uri.fromFile(file);
            Log.d(TAG, mp3Uri.getPath());

            playAudioBtn.setVisibility(View.VISIBLE);
            playAudioBtn.setOnClickListener(v -> {
                if ((mp != null) && mp.isPlaying() ) {
                    mp.stop();
                    mp.release();
                    mp = null;
                }
                mp = MediaPlayer.create(getContext(), mp3Uri);
                mp.start();
            });
        }
        else{
            playAudioBtn.setVisibility(View.GONE);
        }
        return questionText;
    }

    private void setNav() {
        nextBtn.setVisibility(View.VISIBLE);
        prevBtn.setVisibility(View.VISIBLE);

        if (quiz.hasPrevious()) {
            prevBtn.setOnClickListener(v -> {
                // save answer
                saveAnswer();
                if (quiz.hasPrevious()) {
                    quiz.movePrevious();
                    showQuestion();
                }
            });
            prevBtn.setEnabled(true);
        } else {
            prevBtn.setEnabled(false);
        }

        nextBtn.setOnClickListener(nextBtnClickListener());
        // set label on next button
        if (quiz.hasNext()) {
            nextBtn.setText(getString(R.string.widget_quiz_next));
        } else {
            nextBtn.setText(getFinishButtonLabel());
        }
    }

    private View.OnClickListener nextBtnClickListener(){
        return v -> {
            // save answer
            if (saveAnswer()) {
                String feedback;
                try {
                    feedback = quiz.getCurrentQuestion().getFeedback(prefLang);
                    if (!feedback.equals("") &&
                            quiz.getShowFeedback() == Quiz.SHOW_FEEDBACK_ALWAYS
                            && !quiz.getCurrentQuestion().getFeedbackDisplayed()) {
                        //We hide the keyboard before showing the dialog
                        InputMethodManager imm =  (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                        showFeedback(feedback);
                    } else {
                        nextStep();
                    }
                } catch (InvalidQuizException e) {
                    Mint.logException(e);
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

    private View.OnClickListener getCloseBtnListener(){
        return v -> {
            FragmentActivity act = getActivity();
            if (act != null){
                getActivity().finish();
            }
        };
    }

    private void nextStep(){
        if (quiz.hasNext()) {
            quiz.moveNext();
            showQuestion();
        } else {
            showResults();
        }
    }

    private void setProgress() {
        TextView progress = getView().findViewById(R.id.quiz_progress);

        int current = progressBar.getProgress();
        progressBar.setMax(quiz.getTotalNoQuestions());
        barAnim.animate(current, quiz.getCurrentQuestionNo());
        try {
            if (quiz.getCurrentQuestion().responseExpected()) {
                progress.setText(quiz.getCurrentQuestionNo() + "/" + quiz.getTotalNoQuestions());
            } else {
                progress.setText("");
            }
        } catch (InvalidQuizException e) {
            Mint.logException(e);
            Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e);
        }
    }

    private boolean saveAnswer() {
        try {
            List<String> answers = currentQuestion.getQuestionResponses(quiz.getCurrentQuestion().getResponseOptions());
            if ( (answers != null) && (!answers.isEmpty())) {
                quiz.getCurrentQuestion().setUserResponses(answers);
                return true;
            }
            if (!quiz.getCurrentQuestion().responseExpected()) {
                return true;
            }
        } catch (InvalidQuizException e) {
            Mint.logException(e);
            Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e);
        }
        return false;
    }

    private void showFeedback(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Oppia_AlertDialogStyle);
        builder.setTitle(getContext().getString(R.string.feedback));
        builder.setMessage(msg);
        try {
            if(quiz.getCurrentQuestion().getScoreAsPercent() >= Quiz.QUIZ_QUESTION_PASS_THRESHOLD){
                builder.setIcon(R.drawable.quiz_tick);
            } else {
                builder.setIcon(R.drawable.quiz_cross);
            }
        } catch (InvalidQuizException e) {
            Mint.logException(e);
            Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e);
        }
        builder.setPositiveButton(R.string.ok, (arg0, arg1) -> nextStep());
        builder.show();
        try {
            quiz.getCurrentQuestion().setFeedbackDisplayed(true);
        } catch (InvalidQuizException e) {
            Mint.logException(e);
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
        if (quizResultsLayout == null){
            // load new layout
            View progressContainer = getView().findViewById(R.id.progress_container);
            ViewGroup parent = (ViewGroup) progressContainer.getParent();
            int index = parent.indexOfChild(progressContainer);
            parent.removeView(progressContainer);
            progressContainer = super.getActivity().getLayoutInflater().inflate(R.layout.widget_quiz_results, parent, false);
            parent.addView(progressContainer, index);
        }

        TextView title = getView().findViewById(R.id.quiz_results_score);
        Button actionBtn = getView().findViewById(R.id.quiz_results_button);
        Button exitBtn = getView().findViewById(R.id.quiz_exit_button);
        title.setText(getResultsTitle());

        if (this.isBaseline) {
            showBaselineResultMessage();
        }
        // Show the detail of which questions were right/wrong
        if (quiz.getShowFeedback() == Quiz.SHOW_FEEDBACK_ALWAYS || quiz.getShowFeedback() == Quiz.SHOW_FEEDBACK_AT_END){
            showAnswersFeedback();
        }

        int quizAvailabilityMessage = getContentAvailability();
        boolean contentAvailable = getContentAvailability() == QUIZ_AVAILABLE;
        if (!contentAvailable){
            TextView availabilityMsg = getView().findViewById(R.id.quiz_availability_message);
            availabilityMsg.setText(quizAvailabilityMessage);
            availabilityMsg.setVisibility(View.VISIBLE);
        }

        exitBtn.setOnClickListener(getCloseBtnListener());
        if (this.isBaseline) {
            exitBtn.setText(getString(R.string.widget_quiz_baseline_goto_course));
            actionBtn.setVisibility(View.GONE);
        } else if (this.getActivityCompleted() || !contentAvailable){
            actionBtn.setVisibility(View.GONE);
        } else{
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
        quizResultsView = super.getActivity().getLayoutInflater().inflate(R.layout.widget_quiz, parent, false);
        parent.addView(quizResultsView, index);

        fetchViews();
        showQuestion();
    }


    @Override
    public void saveTracker() {
        if(activity == null || !isOnResultsPage || quizAttemptSaved){
            return;
        }

        Log.d(TAG,"Saving tracker");
        saveAttemptTracker();
        quizAttemptSaved = true;
    }

    @Override
    public HashMap<String, Object> getWidgetConfig() {
        HashMap<String, Object> config = new HashMap<>();
        config.put(BaseWidget.PROPERTY_QUIZ, this.quiz);
        config.put(BaseWidget.PROPERTY_ACTIVITY_STARTTIME, this.getStartTime());
        config.put(BaseWidget.PROPERTY_ON_RESULTS_PAGE, this.isOnResultsPage);
        config.put(BaseWidget.PROPERTY_ATTEMPT_SAVED, this.quizAttemptSaved);
        return config;
    }

    @Override
    public void setWidgetConfig(HashMap<String, Object> config) {
        if (config.containsKey(BaseWidget.PROPERTY_QUIZ)) {
            this.quiz = (Quiz) config.get(BaseWidget.PROPERTY_QUIZ);
        }
        if (config.containsKey(BaseWidget.PROPERTY_ACTIVITY_STARTTIME)) {
            this.setStartTime((Long) config.get(BaseWidget.PROPERTY_ACTIVITY_STARTTIME));
        }
        if (config.containsKey(BaseWidget.PROPERTY_ON_RESULTS_PAGE)) {
            this.isOnResultsPage = (Boolean) config.get(BaseWidget.PROPERTY_ON_RESULTS_PAGE);
        }
        if (config.containsKey(BaseWidget.PROPERTY_ATTEMPT_SAVED)) {
            this.quizAttemptSaved = (Boolean) config.get(BaseWidget.PROPERTY_ATTEMPT_SAVED);
        }
    }

    @Override
    public String getContentToRead() {
        // Get the current question text
        String toRead = "";
        try {
            toRead = quiz.getCurrentQuestion().getTitle(prefLang);
        } catch (InvalidQuizException e) {
            Mint.logException(e);
            Log.d(TAG, QUIZ_EXCEPTION_MESSAGE, e);
        }
        return toRead;
    }

    private void clearMediaPlayer(){
        if ((mp != null) ) {
            if (mp.isPlaying()){
                mp.stop();
            }
            mp.release();
            mp = null;
        }
    }

    private class OnImageClickListener implements OnClickListener{

        private Context ctx;

        public OnImageClickListener(Context ctx){
            this.ctx = ctx;
        }

        public void onClick(View v) {
            File file = (File) v.getTag();
            // check the file is on the file system (should be but just in case)
            if(!file.exists()){
                Toast.makeText(this.ctx,this.ctx.getString(R.string.error_resource_not_found,file.getName()), Toast.LENGTH_LONG).show();
                return;
            }
            // check there is actually an app installed to open this filetype
            Intent intent = ExternalResourceOpener.getIntentToOpenResource(ctx, file);
            if(intent != null){
                this.ctx.startActivity(intent);
            } else {
                Toast.makeText(this.ctx,this.ctx.getString(R.string.error_resource_app_not_found,file.getName()), Toast.LENGTH_LONG).show();
            }
        }

    }

    private class OnMediaClickListener implements OnClickListener{
        private String mediaFileName;

        public OnMediaClickListener(String mediaFileName){
            this.mediaFileName = mediaFileName;
        }

        public void onClick(View v) {
            startMediaPlayerWithFile(mediaFileName);
        }
    }
}
