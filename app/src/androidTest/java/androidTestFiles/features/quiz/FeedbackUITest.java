package androidTestFiles.features.quiz;


import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static androidTestFiles.utils.UITestActionsUtils.waitForView;

import android.Manifest;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.QuizAttemptRepository;
import org.digitalcampus.oppia.model.QuizStats;
import org.digitalcampus.oppia.widgets.FeedbackWidget;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.io.IOException;
import java.util.ArrayList;

import androidTestFiles.utils.FileUtils;
import androidTestFiles.utils.parent.DaggerInjectMockUITest;


@RunWith(AndroidJUnit4.class)
public class FeedbackUITest extends DaggerInjectMockUITest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private static final String SIMPLE_FEEDBACK_JSON = "quizzes/simple_feedback.json";
    private static final String SINGLE_ATTEMPT_FEEDBACK_JSON = "quizzes/single_attempt_feedback.json";

    @Mock
    QuizAttemptRepository attemptsRepository;

    private Activity act;
    private Bundle args;

    @Before
    public void setup() throws Exception {
        // Setting up before every test
        args = new Bundle();
        args.putSerializable(Course.TAG, new Course(""));
        args.putBoolean(CourseActivity.BASELINE_TAG, false);
    }

    private void setFeedback(String feedbackJSON) throws IOException {
        act = new Activity();
        String quizContent = FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), feedbackJSON);

        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang("en", quizContent));
        act.setContents(contents);
        args.putSerializable(Activity.TAG, act);

    }

    @Test
    public void showGreetingsAtFinishInFeedbackWithSingleAttempt() throws Exception {
        setFeedback(SINGLE_ATTEMPT_FEEDBACK_JSON);
        QuizStats stats = new QuizStats();
        stats.setNumAttempts(0);
        when(attemptsRepository.getQuizAttemptStats(anyObject(), anyInt(), anyString())).thenReturn(stats);

        launchInContainer(FeedbackWidget.class, args, R.style.Oppia_ToolbarTheme);

        waitForView(withText("firstanswer")).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());
        waitForView(withText("secondanswer")).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        waitForView(withId(R.id.quiz_exit_button))
                .check(matches(withText(R.string.widget_quiz_continue)));
        waitForView(withId(R.id.quiz_results_button))
                .check(matches(not(isDisplayed())));

        waitForView(withId(R.id.quiz_results_score))
                .check(matches(withText(R.string.widget_feedback_submit_title)));
    }

    @Test
    public void showWarningMessageInFeedbackWithSingleAttemptAlreadySubmitted() throws Exception {
        setFeedback(SINGLE_ATTEMPT_FEEDBACK_JSON);

        QuizStats stats = new QuizStats();
        stats.setNumAttempts(1);
        when(attemptsRepository.getQuizAttemptStats(anyObject(), anyInt(), anyString())).thenReturn(stats);

        launchInContainer(FeedbackWidget.class, args, R.style.Oppia_ToolbarTheme);

        waitForView(withText(R.string.widget_feedback_unavailable_attempts)).check(matches(isDisplayed()));
    }

    @Test
    public void showGreetingsAtFinish() throws Exception {
        setFeedback(SIMPLE_FEEDBACK_JSON);
        launchInContainer(FeedbackWidget.class, args, R.style.Oppia_ToolbarTheme);

        waitForView(withText("firstanswer")).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());
        waitForView(withText("secondanswer")).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        waitForView(withId(R.id.quiz_exit_button))
                .check(matches(withText(R.string.widget_quiz_continue)));
        waitForView(withId(R.id.quiz_results_button))
                .check(matches(not(isDisplayed())));

        waitForView(withId(R.id.quiz_results_score))
                .check(matches(withText(R.string.widget_feedback_submit_title)));

    }

    @Test
    public void showProgressTextInNotRequiredQuestions() throws Exception{
        setFeedback(SIMPLE_FEEDBACK_JSON);
        launchInContainer(FeedbackWidget.class, args, R.style.Oppia_ToolbarTheme);

        waitForView(withId(R.id.tv_quiz_progress)).check(matches(withText("1/2")));
        waitForView(withText("firstanswer")).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());
        waitForView(withId(R.id.tv_quiz_progress)).check(matches(withText("2/2")));
        waitForView(withText("secondanswer")).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());


    }
}
