package androidTestFiles.widgets.quiz;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidTestFiles.utils.UITestActionsUtils.waitForView;
import static androidTestFiles.utils.matchers.EspressoTestsMatchers.withDrawable;
import static androidTestFiles.utils.matchers.RecyclerViewMatcher.withRecyclerView;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.widgets.QuizWidget;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidTestFiles.utils.TestUtils;

@RunWith(AndroidJUnit4.class)
public class NumericInexactNoFeedbackTest extends BaseQuizTest {

    private static final String NUMERIC_CLOSE_NO_FEEDBACK_JSON =
            "quizzes/numeric_close_no_feedback.json";
    private static final String FIRST_QUESTION_TITLE = "How high in metres is Everest?";
    private static final String CORRECT_ANSWER = "8848";
    private static final String CLOSE_ANSWER = "8799";
    private static final String INCORRECT_ANSWER = "8797";

    @Override
    protected String getQuizContentFile() {
        return NUMERIC_CLOSE_NO_FEEDBACK_JSON;
    }

    @Test
    public void correctAnswer() {
        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme);
        waitForView(withId(R.id.take_quiz_btn)).perform(click());

        waitForView(withId(R.id.question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));

        waitForView(withId(R.id.responsetext))
                .perform(closeSoftKeyboard(), scrollTo(), typeText(CORRECT_ANSWER));
        waitForView(withId(R.id.mquiz_next_btn)).perform(closeSoftKeyboard(), click());

        String actual = TestUtils.getCurrentActivity().getString(R.string.widget_quiz_results_score, (float) 100);
        waitForView(withId(R.id.quiz_results_score))
                .check(matches(withText(actual)));

        waitForView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));
        waitForView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_user_response_text))
                .check(matches(withText(CORRECT_ANSWER)));

        waitForView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_feedback_image))
                .check(matches(withDrawable(R.drawable.quiz_tick)));

    }

    @Test
    public void closeAnswer() {
        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme);
        waitForView(withId(R.id.take_quiz_btn)).perform(click());

        waitForView(withId(R.id.question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));

        waitForView(withId(R.id.responsetext))
                .perform(closeSoftKeyboard(), scrollTo(), typeText(CLOSE_ANSWER));
        waitForView(withId(R.id.mquiz_next_btn)).perform(closeSoftKeyboard(), click());

        String actual = TestUtils.getCurrentActivity()
                .getString(R.string.widget_quiz_results_score, (float) 60);
        waitForView(withId(R.id.quiz_results_score))
                .check(matches(withText(actual)));
        waitForView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));
        waitForView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_user_response_text))
                .check(matches(withText(CLOSE_ANSWER)));

        waitForView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_feedback_image))
                .check(matches(withDrawable(R.drawable.quiz_partially_correct)));
    }

    @Test
    public void incorrectAnswer() {
        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme);
        waitForView(withId(R.id.take_quiz_btn)).perform(click());

        waitForView(withId(R.id.question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));

        waitForView(withId(R.id.responsetext))
                .perform(closeSoftKeyboard(), scrollTo(), typeText(INCORRECT_ANSWER));
        waitForView(withId(R.id.mquiz_next_btn)).perform(closeSoftKeyboard(), click());

        String actual = TestUtils.getCurrentActivity().getString(R.string.widget_quiz_results_score, (float) 0);
        waitForView(withId(R.id.quiz_results_score))
                .check(matches(withText(actual)));
        waitForView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));
        waitForView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_user_response_text))
                .check(matches(withText(INCORRECT_ANSWER)));

        waitForView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_feedback_image))
                .check(matches(withDrawable(R.drawable.quiz_cross)));
    }

}
