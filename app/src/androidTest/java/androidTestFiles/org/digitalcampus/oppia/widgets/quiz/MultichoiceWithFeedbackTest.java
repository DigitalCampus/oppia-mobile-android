package androidTestFiles.org.digitalcampus.oppia.widgets.quiz;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.widgets.QuizWidget;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidTestFiles.Utils.TestUtils;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static androidTestFiles.Utils.Matchers.EspressoTestsMatchers.withDrawable;
import static androidTestFiles.Utils.Matchers.RecyclerViewMatcher.withRecyclerView;
import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MultichoiceWithFeedbackTest extends BaseQuizTest {

    private static final String MULTICHOICE_WITHFEEDBACK_JSON =
            "quizzes/multichoice_with_feedback.json";
    private static final String FIRST_QUESTION_TITLE =
            "Which of the following is a colour of the rainbow?";
    private static final String CORRECT_ANSWER = "Red";
    private static final String INCORRECT_ANSWER = "Black";
    private static final String CORRECT_ANSWER_FEEDBACK = "correct";
    private static final String INCORRECT_ANSWER_FEEDBACK = "try again";

    @Override
    protected String getQuizContentFile() {
        return MULTICHOICE_WITHFEEDBACK_JSON;
    }

    @Test
    public void correctAnswer() {
        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme, null);
        onView(withId(R.id.take_quiz_btn)).perform(click());

        onView(withId(R.id.question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));

        onView(withText(CORRECT_ANSWER)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        String actual = TestUtils.getCurrentActivity().getString(R.string.widget_quiz_results_score, (float) 100);
        onView(withId(R.id.quiz_results_score))
                .check(matches(withText(actual)));

        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));
        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_user_response_text))
                .check(matches(withText(CORRECT_ANSWER)));
        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_user_feedback_text))
                .check(matches(withText(CORRECT_ANSWER_FEEDBACK)));

        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_feedback_image))
                .check(matches(withDrawable(R.drawable.quiz_tick)));

    }

    @Test
    public void incorrectAnswer() {
        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme, null);
        onView(withId(R.id.take_quiz_btn)).perform(click());

        onView(withId(R.id.question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));

        onView(withText(INCORRECT_ANSWER)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        String actual = TestUtils.getCurrentActivity().getString(R.string.widget_quiz_results_score, (float) 0);
        onView(withId(R.id.quiz_results_score))
                .check(matches(withText(actual)));
        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));
        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_user_response_text))
                .check(matches(withText(INCORRECT_ANSWER)));
        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_user_feedback_text))
                .check(matches(withText(INCORRECT_ANSWER_FEEDBACK)));

        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_feedback_image))
                .check(matches(withDrawable(R.drawable.quiz_cross)));
    }
}
