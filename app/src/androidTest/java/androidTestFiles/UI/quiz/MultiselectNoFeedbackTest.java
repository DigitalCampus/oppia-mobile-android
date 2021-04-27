package androidTestFiles.UI.quiz;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.widgets.QuizWidget;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidTestFiles.Utils.TestUtils;

import static androidTestFiles.Matchers.EspressoTestsMatchers.withDrawable;
import static androidTestFiles.Matchers.RecyclerViewMatcher.withRecyclerView;
import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;

@RunWith(AndroidJUnit4.class)
public class MultiselectNoFeedbackTest extends BaseQuizTest {

    private static final String MULTISELECT_NOFEEDBACK_JSON =
            "quizzes/multiselect_no_feedback.json";
    private static final String FIRST_QUESTION_TITLE =
            "What of the following are colours of the rainbow?";
    private static final String CORRECT_ANSWER_1 = "Red";
    private static final String CORRECT_ANSWER_2 = "Yellow";
    private static final String INCORRECT_ANSWER_1 = "Black";

    @Override
    protected String getQuizContentFile() {
        return MULTISELECT_NOFEEDBACK_JSON;
    }

    @Test
    public void correctAnswer() {
        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme, null);
        onView(withId(R.id.take_quiz_btn)).perform(click());

        onView(withId(R.id.question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));

        onView(withText(CORRECT_ANSWER_1)).perform(click());
        onView(withText(CORRECT_ANSWER_2)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        String actual = TestUtils.getCurrentActivity().getString(R.string.widget_quiz_results_score, (float) 100);
        onView(withId(R.id.quiz_results_score))
                .check(matches(withText(actual)));

        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));
        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_user_response_text))
                .check(matches(withText(containsString(CORRECT_ANSWER_1))));
        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_user_response_text))
                .check(matches(withText(containsString(CORRECT_ANSWER_2))));

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

        onView(withText(INCORRECT_ANSWER_1)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        String actual = TestUtils.getCurrentActivity().getString(R.string.widget_quiz_results_score, (float) 0);
        onView(withId(R.id.quiz_results_score))
                .check(matches(withText(actual)));
        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));
        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_user_response_text))
                .check(matches(withText(INCORRECT_ANSWER_1)));

        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_feedback_image))
                .check(matches(withDrawable(R.drawable.quiz_cross)));
    }

    @Test
    public void oneCorrectAnswer() {
        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme, null);
        onView(withId(R.id.take_quiz_btn)).perform(click());

        onView(withId(R.id.question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));

        onView(withText(CORRECT_ANSWER_1)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        String actual = TestUtils.getCurrentActivity().getString(R.string.widget_quiz_results_score, (float) 50);
        onView(withId(R.id.quiz_results_score))
                .check(matches(withText(actual)));
        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));
        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_user_response_text))
                .check(matches(withText(CORRECT_ANSWER_1)));

        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_feedback_image))
                .check(matches(withDrawable(R.drawable.quiz_cross)));
    }

    @Test
    public void oneCorrectOneIncorrectAnswer() {
        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme, null);
        onView(withId(R.id.take_quiz_btn)).perform(click());

        onView(withId(R.id.question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));

        onView(withText(CORRECT_ANSWER_1)).perform(click());
        onView(withText(INCORRECT_ANSWER_1)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        String actual = TestUtils.getCurrentActivity().getString(R.string.widget_quiz_results_score, (float) 0);
        onView(withId(R.id.quiz_results_score))
                .check(matches(withText(actual)));
        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));
        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_user_response_text))
                .check(matches(withText(containsString(CORRECT_ANSWER_1))));
        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_user_response_text))
                .check(matches(withText(containsString(INCORRECT_ANSWER_1))));

        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_feedback_image))
                .check(matches(withDrawable(R.drawable.quiz_cross)));
    }

}
