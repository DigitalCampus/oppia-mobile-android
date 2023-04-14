package androidTestFiles.widgets;


import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static androidTestFiles.utils.UITestActionsUtils.waitForView;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.QuizAttemptRepository;
import org.digitalcampus.oppia.model.QuizStats;
import org.digitalcampus.oppia.widgets.QuizWidget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;

import androidTestFiles.utils.FileUtils;
import androidTestFiles.utils.parent.DaggerInjectMockUITest;


@RunWith(AndroidJUnit4.class)
public class QuizWidgetTest extends DaggerInjectMockUITest {

    private static final String SIMPLE_QUIZ_JSON = "quizzes/simple_quiz.json";
    private static final String WITH_MAX_ATTEMPTS_JSON = "quizzes/with_max_attempts_quiz.json";

    public static final String PASSWORD_PROTECT_NO_PASSWORD_FIELD = "quizzes/password_protect/no_password_field.json";
    public static final String PASSWORD_PROTECT_NULL_PASSWORD = "quizzes/password_protect/null_password.json";
    public static final String PASSWORD_PROTECT_EMPTY_PASSWORD = "quizzes/password_protect/empty_password.json";
    public static final String PASSWORD_PROTECT_NON_EMPTY_PASSWORD = "quizzes/password_protect/non_empty_password.json";

    private Activity act;
    private Bundle args;
    private QuizStats stats;

    @Mock
    QuizAttemptRepository attemptsRepository;

    @Before
    public void setup() throws Exception {
        // Setting up before every test
        loadQuizAndSetArgs(SIMPLE_QUIZ_JSON);
        stats = new QuizStats();
        Mockito.doAnswer((Answer<QuizStats>) invocation -> stats).when(attemptsRepository).getQuizAttemptStats(any(Context.class), anyInt(), any());
    }

    public void initMocks(){

    }

    private void loadQuizAndSetArgs(String quizJsonFile) throws Exception {
        act = new Activity();
        String quizContent = FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), quizJsonFile);

        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang("en", quizContent));
        act.setContents(contents);

        args = new Bundle();
        args.putSerializable(Activity.TAG, act);
        args.putSerializable(Course.TAG, new Course(""));
        args.putBoolean(CourseActivity.BASELINE_TAG, false);
    }

    @Test
    public void showContinueIfQuizPassed() {
        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme);
        waitForView(withId(R.id.take_quiz_btn)).perform(click());
        waitForView(withText("correctanswer")).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());
        waitForView(withText("correctanswer")).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        //If the quiz is passed, we only have to show the "Continue" button
        waitForView(withId(R.id.quiz_exit_button))
                .check(matches(withText(R.string.widget_quiz_continue)));
        waitForView(withId(R.id.quiz_results_button))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void showRetakeIfQuizNotPassed() {
        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme);
        waitForView(withId(R.id.take_quiz_btn)).perform(click());
        waitForView(withText("correctanswer")).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());
        waitForView(withText("wronganswer")).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        waitForView(withId(R.id.quiz_exit_button))
                .check(matches(withText(R.string.widget_quiz_continue)));
        waitForView(withId(R.id.quiz_results_button))
                .check(matches(isDisplayed()));
        waitForView(withId(R.id.quiz_results_button))
                .check(matches(withText(R.string.widget_quiz_results_restart)));
    }

    @Test
    public void showMessageIfAlreadyMaxAttempts() throws Exception {
        loadQuizAndSetArgs(WITH_MAX_ATTEMPTS_JSON);

        stats.setNumAttempts(5);
        Mockito.doAnswer((Answer<QuizStats>) invocation -> stats).when(attemptsRepository).getQuizAttemptStats(any(Context.class), anyInt(), any());

        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme);

        waitForView(withText(R.string.widget_quiz_unavailable_attempts))
            .check(matches(isDisplayed()));
    }

    @Test
    public void hideRetakeButtonAfterLastAttempt() throws Exception {
        loadQuizAndSetArgs(WITH_MAX_ATTEMPTS_JSON);

        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme);
        waitForView(withId(R.id.take_quiz_btn)).perform(click());
        waitForView(withText("correctanswer")).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        stats.setNumAttempts(1);
        Mockito.doAnswer((Answer<QuizStats>) invocation -> stats).when(attemptsRepository).getQuizAttemptStats(any(Context.class), anyInt(), any());

        waitForView(withText("wronganswer")).perform(click());
        waitForView(withId(R.id.mquiz_next_btn)).perform(click());

        waitForView(withText(R.string.widget_quiz_unavailable_attempts))
            .check(matches(isDisplayed()));

        //If we cannot retake the quiz, we only have to show the "Continue" button
        waitForView(withId(R.id.quiz_exit_button))
                .check(matches(withText(R.string.widget_quiz_continue)));
        waitForView(withId(R.id.quiz_results_button))
                .check(matches(not(isDisplayed())));
    }


    // Password protect

    @Test
    public void dontShowPasswordDialogIfPasswordPropDoesNotExist() throws Exception {
        checkPasswordViewDisplayed(PASSWORD_PROTECT_NO_PASSWORD_FIELD, false);
    }

    @Test
    public void dontShowPasswordDialogIfPasswordPropIsNull() throws Exception {
        checkPasswordViewDisplayed(PASSWORD_PROTECT_NULL_PASSWORD, false);
    }

    @Test
    public void dontShowPasswordDialogIfPasswordPropIsEmpty() throws Exception {
        checkPasswordViewDisplayed(PASSWORD_PROTECT_EMPTY_PASSWORD, false);
    }

    @Test
    public void showPasswordDialogIfPasswordPropIsNotEmpty() throws Exception {
        checkPasswordViewDisplayed(PASSWORD_PROTECT_NON_EMPTY_PASSWORD, true);
    }

    private void checkPasswordViewDisplayed(String quizJson, boolean mustBeDisplayed) throws Exception {

        loadQuizAndSetArgs(quizJson);
        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme);

        waitForView(withId(R.id.take_quiz_btn)).perform(click());

        if (mustBeDisplayed) {
            waitForView(withText(R.string.password_activity_description))
                    .check(matches(isDisplayed()));
        } else {
            waitForView(withText(R.string.quiz_protected))
                    .check(doesNotExist());
            waitForView(withText("Is it snowing today?")).check(matches(isDisplayed()));
        }
    }


    @Test
    public void enterQuizWhenValidPassword() throws Exception {
        checkPasswordViewDisplayed(PASSWORD_PROTECT_NON_EMPTY_PASSWORD, true);
        waitForView(withId(R.id.activity_password_field))
                .perform(typeText("letmein"), closeSoftKeyboard());
        waitForView(withText(R.string.password_activity_submit)).perform(click());
        waitForView(withText("Is it snowing today?")).check(matches(isDisplayed()));
    }

    @Test
    @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.Q)
    // Skipping test for API >= 30 until a fix for asserting Toast messages is found.
    // https://oppia.atlassian.net/browse/OPPIA-1130
    public void dontEnterQuizWhenInvalidPassword() throws Exception {
        checkPasswordViewDisplayed(PASSWORD_PROTECT_NON_EMPTY_PASSWORD, true);

        waitForView(withId(R.id.activity_password_field)).perform(typeText("wrong_pass"), closeSoftKeyboard());

        waitForView(withId(R.id.submit_activity_password)).perform(click());

        waitForView(withText(R.string.password_activity_incorrect)).check(matches(isDisplayed()));

        waitForView(withText("Is it snowing today?")).check(doesNotExist());

    }
}
