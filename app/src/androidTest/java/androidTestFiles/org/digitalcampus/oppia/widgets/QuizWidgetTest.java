package androidTestFiles.org.digitalcampus.oppia.widgets;


import android.content.Context;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
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


import androidTestFiles.TestRules.DaggerInjectMockUITest;
import androidTestFiles.Utils.FileUtils;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;


@RunWith(AndroidJUnit4.class)
public class QuizWidgetTest extends DaggerInjectMockUITest {

    private static final String SIMPLE_QUIZ_JSON = "quizzes/simple_quiz.json";
    private static final String WITH_MAX_ATTEMPTS_JSON = "quizzes/with_max_attempts_quiz.json";
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
        Mockito.doAnswer((Answer<QuizStats>) invocation -> stats).when(attemptsRepository).getQuizAttemptStats(any(Context.class), any());
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
        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme, null);
        onView(withId(R.id.take_quiz_btn)).perform(click());
        onView(withText("correctanswer")).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());
        onView(withText("correctanswer")).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        //If the quiz is passed, we only have to show the "Continue" button
        onView(withId(R.id.quiz_exit_button))
                .check(matches(withText(R.string.widget_quiz_continue)));
        onView(withId(R.id.quiz_results_button))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void showRetakeIfQuizNotPassed() {
        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme, null);
        onView(withId(R.id.take_quiz_btn)).perform(click());
        onView(withText("correctanswer")).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());
        onView(withText("wronganswer")).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        onView(withId(R.id.quiz_exit_button))
                .check(matches(withText(R.string.widget_quiz_continue)));
        onView(withId(R.id.quiz_results_button))
                .check(matches(isDisplayed()));
        onView(withId(R.id.quiz_results_button))
                .check(matches(withText(R.string.quiz_attempts_retake_quiz)));
    }

    @Test
    public void showMessageIfAlreadyMaxAttempts() throws Exception {
        loadQuizAndSetArgs(WITH_MAX_ATTEMPTS_JSON);

        stats.setNumAttempts(5);
        Mockito.doAnswer((Answer<QuizStats>) invocation -> stats).when(attemptsRepository).getQuizAttemptStats(any(Context.class), any());

        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme, null);

        onView(withText(R.string.widget_quiz_unavailable_attempts))
            .check(matches(isDisplayed()));
    }

    @Test
    public void hideRetakeButtonAfterLastAttempt() throws Exception {
        loadQuizAndSetArgs(WITH_MAX_ATTEMPTS_JSON);

        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme, null);
        onView(withId(R.id.take_quiz_btn)).perform(click());
        onView(withText("correctanswer")).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        stats.setNumAttempts(1);
        Mockito.doAnswer((Answer<QuizStats>) invocation -> stats).when(attemptsRepository).getQuizAttemptStats(any(Context.class), any());

        onView(withText("wronganswer")).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        onView(withText(R.string.widget_quiz_unavailable_attempts))
            .check(matches(isDisplayed()));

        //If we cannot retake the quiz, we only have to show the "Continue" button
        onView(withId(R.id.quiz_exit_button))
                .check(matches(withText(R.string.widget_quiz_continue)));
        onView(withId(R.id.quiz_results_button))
                .check(matches(not(isDisplayed())));
    }

}
