package androidTestFiles.features.quiz;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidTestFiles.utils.UITestActionsUtils.waitForView;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.widgets.FeedbackWidget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Locale;

import androidTestFiles.utils.FileUtils;
import androidTestFiles.utils.parent.BaseTest;

@RunWith(AndroidJUnit4.class)
public class FeedbackGradeBoundariesUITest {

    private static final String MULTICHOICE_NOFEEDBACK_JSON = BaseTest.PATH_QUIZZES + "/multichoice_no_feedback.json";
    private static final String FEEDBACK_GRADE_BOUNDARIES_JSON = BaseTest.PATH_QUIZZES + "/feedback_grade_boundaries.json";
    private static final String FEEDBACK_INCORRECT_GRADE_BOUNDARIES_JSON = BaseTest.PATH_QUIZZES + "/feedback_incorrect_grade_boundaries.json";
    private static final String FEEDBACK_MULTILANG_GRADE_BOUNDARIES_JSON = BaseTest.PATH_QUIZZES + "/feedback_multilang_grade_boundaries.json";
    private static final String EN_LANG = "en";
    private static final String ES_LANG = "es";
    private static final String FI_LANG = "fi";
    private static String feedbackWithGradeBoundariesContent;
    private static String feedbackWithDefaultFeedbackMessageContent;
    private static String feedbackWithIncorrectGradeBoundariesContent;
    private static String feedbackWithMultilangGradeBoundariesContent;
    private static final String DEFAULT_FEEDBACK_MESSAGE = "Thank you for submitting your feedback.";
    private static final String FEEDBACK_MESSAGE_25_EN = "Quiz score is between 20% and 49%. MaxQuizScore: 8; UserScore: 2; QuizScore: 25%";
    private static final String FEEDBACK_MESSAGE_75_EN = "Quiz score is between 50% and 99%. MaxQuizScore: 8; UserScore: 6; QuizScore: 75%";
    private static final String FEEDBACK_MESSAGE_100_EN = "Quiz score is 100%. MaxQuizScore: 8; UserScore: 8; QuizScore: 100%";
    private static final String FEEDBACK_MESSAGE_75_ES = "La puntuación del quiz está entre 50% y 99%. PuntuaciónMáxima: 8; PuntuaciónDelUsuario: 6; PuntuaciónDelQuiz: 75%";
    private static final String FEEDBACK_MESSAGE_100_ES = "La puntuación del quiz es de 100%. PuntuaciónMáxima: 8; PuntuaciónDelUsuario: 8; PuntuaciónDelQuiz: 100%";
    private static final String FEEDBACK_INCORRECT_MESSAGE = "Property missing double curly braces: {max_score}; Property does not exist: {{not_exist}}";
    private static final String FEEDBACK_RESPONSE_SCORE_2 = "Response with score 2.0";
    private static final String FEEDBACK_RESPONSE_SCORE_1 = "Response with score 1.0";
    private static final String FEEDBACK_RESPONSE_SCORE_0 = "Response with score 0.0";

    private Activity act;
    private Bundle args;

    @Before
    public void setup() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();

        feedbackWithGradeBoundariesContent = FileUtils.getStringFromFile(context, FEEDBACK_GRADE_BOUNDARIES_JSON);
        feedbackWithDefaultFeedbackMessageContent = FileUtils.getStringFromFile(context, MULTICHOICE_NOFEEDBACK_JSON);
        feedbackWithIncorrectGradeBoundariesContent = FileUtils.getStringFromFile(context, FEEDBACK_INCORRECT_GRADE_BOUNDARIES_JSON);
        feedbackWithMultilangGradeBoundariesContent = FileUtils.getStringFromFile(context, FEEDBACK_MULTILANG_GRADE_BOUNDARIES_JSON);

        act = new Activity();
        args = new Bundle();
        args.putSerializable(Activity.TAG, act);
        args.putSerializable(Course.TAG, new Course(""));
        args.putBoolean(CourseActivity.BASELINE_TAG, true);
    }

    private void changeDefaultLocale(String lang) {
        Resources resources = InstrumentationRegistry.getInstrumentation().getTargetContext().getResources();
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = resources.getConfiguration();
        config.locale = locale;
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    @Test
    public void test_gradeBoundary_feedback_0_default_message_is_shown() {
        changeDefaultLocale(EN_LANG);
        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang(EN_LANG, feedbackWithGradeBoundariesContent));
        act.setContents(contents);

        launchInContainer(FeedbackWidget.class, args, R.style.Oppia_ToolbarTheme);

        // Question 1
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 1")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_0)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 2
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 2")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_0)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 3
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 3")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_0)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 4
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 4")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_0)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        String expected = DEFAULT_FEEDBACK_MESSAGE;
        waitForView(withId(R.id.quiz_results_score))
                .check(matches(withText(expected)));
    }

    @Test
    public void test_gradeBoundary_feedback_message_100_is_shown() {
        changeDefaultLocale(EN_LANG);
        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang(EN_LANG, feedbackWithGradeBoundariesContent));
        act.setContents(contents);

        launchInContainer(FeedbackWidget.class, args, R.style.Oppia_ToolbarTheme);

        // Question 1
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 1")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 2
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 2")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 3
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 3")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 4
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 4")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        String expected = FEEDBACK_MESSAGE_100_EN;
        waitForView(withId(R.id.quiz_results_score))
                .check(matches(withText(expected)));
    }

    @Test
    public void test_gradeBoundary_feedback_message_75_is_shown() {
        changeDefaultLocale(EN_LANG);
        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang(EN_LANG, feedbackWithGradeBoundariesContent));
        act.setContents(contents);

        launchInContainer(FeedbackWidget.class, args, R.style.Oppia_ToolbarTheme);

        // Question 1
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 1")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_0)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 2
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 2")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 3
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 3")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 4
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 4")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        String expected = FEEDBACK_MESSAGE_75_EN;
        waitForView(withId(R.id.quiz_results_score))
                .check(matches(withText(expected)));
    }

    @Test
    public void test_gradeBoundary_feedback_message_25_is_shown() {
        changeDefaultLocale(EN_LANG);
        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang(EN_LANG, feedbackWithGradeBoundariesContent));
        act.setContents(contents);

        launchInContainer(FeedbackWidget.class, args, R.style.Oppia_ToolbarTheme);

        // Question 1
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 1")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_0)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 2
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 2")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_0)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 3
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 3")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_1)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 4
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 4")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_1)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        String expected = FEEDBACK_MESSAGE_25_EN;
        waitForView(withId(R.id.quiz_results_score))
                .check(matches(withText(expected)));
    }

    @Test
    public void test_no_gradeBoundary_default_feedback_message_is_shown() {
        changeDefaultLocale(EN_LANG);
        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang(EN_LANG, feedbackWithDefaultFeedbackMessageContent));
        act.setContents(contents);

        launchInContainer(FeedbackWidget.class, args, R.style.Oppia_ToolbarTheme);

        // Question 1
        waitForView(withId(R.id.question_text))
                .check(matches(withText("What is the capital of Germany?")));

        waitForView(withText("Berlin")).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());


        int expected = R.string.widget_feedback_submit_title;
        waitForView(withId(R.id.quiz_results_score))
                .check(matches(withText(expected)));
    }

    @Test
    public void test_gradeBoundary_incorrect_feedback_message_is_shown() {
        changeDefaultLocale(EN_LANG);
        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang(EN_LANG, feedbackWithIncorrectGradeBoundariesContent));
        act.setContents(contents);

        launchInContainer(FeedbackWidget.class, args, R.style.Oppia_ToolbarTheme);

        // Question 1
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 1")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_0)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 2
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 2")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_0)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 3
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 3")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_0)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 4
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 4")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_0)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        String expected = FEEDBACK_INCORRECT_MESSAGE;
        waitForView(withId(R.id.quiz_results_score))
                .check(matches(withText(expected)));
    }

    @Test
    public void test_gradeBoundary_feedback_message_100_is_shown_ES() {
        changeDefaultLocale(ES_LANG);
        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang(ES_LANG, feedbackWithMultilangGradeBoundariesContent));
        act.setContents(contents);

        launchInContainer(FeedbackWidget.class, args, R.style.Oppia_ToolbarTheme);

        // Question 1
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 1")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 2
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 2")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 3
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 3")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 4
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 4")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        String expected = FEEDBACK_MESSAGE_100_ES;
        waitForView(withId(R.id.quiz_results_score))
                .check(matches(withText(expected)));
    }

    @Test
    public void test_gradeBoundary_feedback_message_75_is_shown_ES() {
        changeDefaultLocale(ES_LANG);
        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang(ES_LANG, feedbackWithMultilangGradeBoundariesContent));
        act.setContents(contents);

        launchInContainer(FeedbackWidget.class, args, R.style.Oppia_ToolbarTheme);

        // Question 1
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 1")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_0)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 2
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 2")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 3
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 3")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 4
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 4")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        String expected = FEEDBACK_MESSAGE_75_ES;
        waitForView(withId(R.id.quiz_results_score))
                .check(matches(withText(expected)));
    }

    @Test
    public void test_gradeBoundary_feedback_message_25_is_shown_ES() {
        changeDefaultLocale(ES_LANG);
        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang(ES_LANG, feedbackWithMultilangGradeBoundariesContent));
        act.setContents(contents);

        launchInContainer(FeedbackWidget.class, args, R.style.Oppia_ToolbarTheme);

        // Question 1
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 1")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_0)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 2
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 2")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_0)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 3
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 3")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_1)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 4
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 4")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_1)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        String expected = FEEDBACK_MESSAGE_25_EN;   // This grade boundary is only in english,
                                                    // so even if the language is set to Spanish,
                                                    // the English message will be displayed
        waitForView(withId(R.id.quiz_results_score))
                .check(matches(withText(expected)));
    }

    @Test
    public void test_gradeBoundary_feedback_message_100_is_shown_nonexisting_lang() {
        changeDefaultLocale(FI_LANG);
        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang(FI_LANG, feedbackWithMultilangGradeBoundariesContent));
        act.setContents(contents);

        launchInContainer(FeedbackWidget.class, args, R.style.Oppia_ToolbarTheme);

        // Question 1
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 1")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 2
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 2")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 3
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 3")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 4
        waitForView(withId(R.id.question_text))
                .check(matches(withText("Question 4")));

        waitForView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        String expected = FEEDBACK_MESSAGE_100_EN;  // English message will be displayed because
                                                    // is the first defined language in the grade boundary message
        waitForView(withId(R.id.quiz_results_score))
                .check(matches(withText(expected)));

    }

}
