package androidTestFiles.features.quiz;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
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

import androidTestFiles.utils.FileUtils;

@RunWith(AndroidJUnit4.class)
public class FeedbackGradeBoundariesUITest {

    private static final String MULTICHOICE_NOFEEDBACK_JSON = "quizzes/multichoice_no_feedback.json";
    private static final String FEEDBACK_GRADE_BOUNDARIES_JSON = "quizzes/feedback_grade_boundaries.json";
    private static final String FEEDBACK_INCORRECT_GRADE_BOUNDARIES_JSON = "quizzes/feedback_incorrect_grade_boundaries.json";
    private static final String DEFAULT_LANG = "en";
    private static String feedbackWithGradeBoundariesContent;
    private static String feedbackWithDefaultFeedbackMessageContent;
    private static String feedbackWithIncorrectGradeBoundariesContent;
    private static final String FEEDBACK_MESSAGE_25 = "Quiz score is between 0% and 49%. MaxQuizScore: 8; UserScore: 2; QuizScore: 25%";
    private static final String FEEDBACK_MESSAGE_75 = "Quiz score is between 50% and 99%. MaxQuizScore: 8; UserScore: 6; QuizScore: 75%";
    private static final String FEEDBACK_MESSAGE_100 = "Quiz score is 100%. MaxQuizScore: 8; UserScore: 8; QuizScore: 100%";
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

        act = new Activity();

        args = new Bundle();
        args.putSerializable(Activity.TAG, act);
        args.putSerializable(Course.TAG, new Course(""));
        args.putBoolean(CourseActivity.BASELINE_TAG, true);
    }

    @Test
    public void test_gradeBoundary_feedback_message_100_is_shown() {
        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang(DEFAULT_LANG, feedbackWithGradeBoundariesContent));
        act.setContents(contents);

        launchInContainer(FeedbackWidget.class, args, R.style.Oppia_ToolbarTheme);

        // Question 1
        onView(withId(R.id.question_text))
                .check(matches(withText("Question 1")));

        onView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 2
        onView(withId(R.id.question_text))
                .check(matches(withText("Question 2")));

        onView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 3
        onView(withId(R.id.question_text))
                .check(matches(withText("Question 3")));

        onView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 4
        onView(withId(R.id.question_text))
                .check(matches(withText("Question 4")));

        onView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        String expected = FEEDBACK_MESSAGE_100;
        onView(withId(R.id.quiz_results_score))
                .check(matches(withText(expected)));
    }

    @Test
    public void test_gradeBoundary_feedback_message_75_is_shown() {
        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang(DEFAULT_LANG, feedbackWithGradeBoundariesContent));
        act.setContents(contents);

        launchInContainer(FeedbackWidget.class, args, R.style.Oppia_ToolbarTheme);

        // Question 1
        onView(withId(R.id.question_text))
                .check(matches(withText("Question 1")));

        onView(withText(FEEDBACK_RESPONSE_SCORE_0)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 2
        onView(withId(R.id.question_text))
                .check(matches(withText("Question 2")));

        onView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 3
        onView(withId(R.id.question_text))
                .check(matches(withText("Question 3")));

        onView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 4
        onView(withId(R.id.question_text))
                .check(matches(withText("Question 4")));

        onView(withText(FEEDBACK_RESPONSE_SCORE_2)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        String expected = FEEDBACK_MESSAGE_75;
        onView(withId(R.id.quiz_results_score))
                .check(matches(withText(expected)));
    }

    @Test
    public void test_gradeBoundary_feedback_message_25_is_shown() {

        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang(DEFAULT_LANG, feedbackWithGradeBoundariesContent));
        act.setContents(contents);

        launchInContainer(FeedbackWidget.class, args, R.style.Oppia_ToolbarTheme);

        // Question 1
        onView(withId(R.id.question_text))
                .check(matches(withText("Question 1")));

        onView(withText(FEEDBACK_RESPONSE_SCORE_0)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 2
        onView(withId(R.id.question_text))
                .check(matches(withText("Question 2")));

        onView(withText(FEEDBACK_RESPONSE_SCORE_0)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 3
        onView(withId(R.id.question_text))
                .check(matches(withText("Question 3")));

        onView(withText(FEEDBACK_RESPONSE_SCORE_1)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 4
        onView(withId(R.id.question_text))
                .check(matches(withText("Question 4")));

        onView(withText(FEEDBACK_RESPONSE_SCORE_1)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        String expected = FEEDBACK_MESSAGE_25;
        onView(withId(R.id.quiz_results_score))
                .check(matches(withText(expected)));
    }

    @Test
    public void test_no_gradeBoundary_default_feedback_message_is_shown() {
        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang(DEFAULT_LANG, feedbackWithDefaultFeedbackMessageContent));
        act.setContents(contents);

        launchInContainer(FeedbackWidget.class, args, R.style.Oppia_ToolbarTheme);

        // Question 1
        onView(withId(R.id.question_text))
                .check(matches(withText("What is the capital of Germany?")));

        onView(withText("Berlin")).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());


        int expected = R.string.widget_feedback_submit_title;
        onView(withId(R.id.quiz_results_score))
                .check(matches(withText(expected)));
    }

    @Test
    public void test_gradeBoundary_incorrect_feedback_message_is_shown() {

        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang(DEFAULT_LANG, feedbackWithIncorrectGradeBoundariesContent));
        act.setContents(contents);

        launchInContainer(FeedbackWidget.class, args, R.style.Oppia_ToolbarTheme);

        // Question 1
        onView(withId(R.id.question_text))
                .check(matches(withText("Question 1")));

        onView(withText(FEEDBACK_RESPONSE_SCORE_0)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 2
        onView(withId(R.id.question_text))
                .check(matches(withText("Question 2")));

        onView(withText(FEEDBACK_RESPONSE_SCORE_0)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 3
        onView(withId(R.id.question_text))
                .check(matches(withText("Question 3")));

        onView(withText(FEEDBACK_RESPONSE_SCORE_0)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        // Question 4
        onView(withId(R.id.question_text))
                .check(matches(withText("Question 4")));

        onView(withText(FEEDBACK_RESPONSE_SCORE_0)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        String expected = FEEDBACK_INCORRECT_MESSAGE;
        onView(withId(R.id.quiz_results_score))
                .check(matches(withText(expected)));
    }

}
