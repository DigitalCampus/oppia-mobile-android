package androidTestFiles.UI.quiz;


import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.widgets.QuizWidget;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidTestFiles.Utils.TestUtils;
import androidTestFiles.quiz.models.MatchingQuestionTest;

import static androidTestFiles.Matchers.EspressoTestsMatchers.withDrawable;
import static androidTestFiles.Matchers.RecyclerViewMatcher.withRecyclerView;
import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withParentIndex;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class MatchingDefaultFeedbackTest extends BaseQuizTest {

    private static final String MATCHING_DEFAULT_FEEDBACK_JSON =
            "quizzes/matching_default_feedback.json";
    private static final String FIRST_QUESTION_TITLE =
            "What beats what?";

    private static final String QUESTION_TEXT_1 = "Rock beats";
    private static final String CORRECT_ANSWER_1 = "Scissors";

    private static final String QUESTION_TEXT_2 = "Paper beats";
    private static final String CORRECT_ANSWER_2 = "Rock";

    private static final String QUESTION_TEXT_3 = "Scissors beat";
    private static final String CORRECT_ANSWER_3 = "Paper";


    @Override
    protected String getQuizContentFile() {
        return MATCHING_DEFAULT_FEEDBACK_JSON;
    }


    // To click on a child index. For dinamicaly added sppiner views
    private Matcher<View> withPositionInParent(int parentViewId, int position) {
        return allOf(withParent(withId(parentViewId)), withParentIndex(position));
    }

    private Matcher<View> withQuestionText(String text) {
        return allOf(
                withParent(allOf(withParent(withId(R.id.questionresponses)),
                        hasDescendant(allOf(is(instanceOf(TextView.class)), withText(text))))),
                instanceOf(Spinner.class));
    }

    @Test
    public void correctAnswer() throws InterruptedException {
        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme, null);
        onView(withId(R.id.take_quiz_btn)).perform(click());

        onView(withId(R.id.question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));


        onView(withQuestionText(QUESTION_TEXT_1)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(CORRECT_ANSWER_1))).perform(click());

        onView(withQuestionText(QUESTION_TEXT_2)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(CORRECT_ANSWER_2))).perform(click());

        onView(withQuestionText(QUESTION_TEXT_3)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(CORRECT_ANSWER_3))).perform(click());

        onView(withId(R.id.mquiz_next_btn)).perform(click());

        String actual = TestUtils.getCurrentActivity().getString(R.string.widget_quiz_results_score, (float) 100);
        onView(withId(R.id.quiz_results_score))
                .check(matches(withText(actual)));

        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));

        checkResultAnswersContains(CORRECT_ANSWER_1, CORRECT_ANSWER_2, CORRECT_ANSWER_3);

        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_user_feedback_text))
                .check(matches(withText(MatchingQuestionTest.FEEDBACK_TEXT_CORRECT)));

        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_feedback_image))
                .check(matches(withDrawable(R.drawable.quiz_tick)));

    }

    @Test
    public void partiallyIncorrectAnswer() throws InterruptedException {
        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme, null);
        onView(withId(R.id.take_quiz_btn)).perform(click());

        onView(withId(R.id.question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));

        onView(withQuestionText(QUESTION_TEXT_1)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(CORRECT_ANSWER_2))).perform(click());

        onView(withQuestionText(QUESTION_TEXT_2)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(CORRECT_ANSWER_2))).perform(click());

        onView(withQuestionText(QUESTION_TEXT_3)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(CORRECT_ANSWER_3))).perform(click());

        onView(withId(R.id.mquiz_next_btn)).perform(click());

        String actual = TestUtils.getCurrentActivity().getString(R.string.widget_quiz_results_score, (float) 67);
        onView(withId(R.id.quiz_results_score))
                .check(matches(withText(actual)));

        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));

        checkResultAnswersContains(CORRECT_ANSWER_1, CORRECT_ANSWER_2, CORRECT_ANSWER_3);

        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_user_feedback_text))
                .check(matches(withText(MatchingQuestionTest.FEEDBACK_TEXT_PARTIALLY_CORRECT)));

        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_feedback_image))
                .check(matches(withDrawable(R.drawable.quiz_cross)));


    }

    @Test
    public void incorrectAnswer() throws InterruptedException {
        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme, null);
        onView(withId(R.id.take_quiz_btn)).perform(click());

        onView(withId(R.id.question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));

        onView(withQuestionText(QUESTION_TEXT_1)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(CORRECT_ANSWER_2))).perform(click());

        onView(withQuestionText(QUESTION_TEXT_2)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(CORRECT_ANSWER_3))).perform(click());

        onView(withQuestionText(QUESTION_TEXT_3)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(CORRECT_ANSWER_1))).perform(click());

        onView(withId(R.id.mquiz_next_btn)).perform(click());

        String actual = TestUtils.getCurrentActivity().getString(R.string.widget_quiz_results_score, (float) 0);
        onView(withId(R.id.quiz_results_score))
                .check(matches(withText(actual)));

        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));

        checkResultAnswersContains(CORRECT_ANSWER_1, CORRECT_ANSWER_2, CORRECT_ANSWER_3);

        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_user_feedback_text))
                .check(matches(withText(MatchingQuestionTest.FEEDBACK_TEXT_INCORRECT)));

        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(0, R.id.quiz_question_feedback_image))
                .check(matches(withDrawable(R.drawable.quiz_cross)));


    }

    private void checkResultAnswersContains(String... texts) {
        for (String text : texts) {
            onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                    .atPositionOnView(0, R.id.quiz_question_user_response_text))
                    .check(matches(withText(containsString(text))));
        }
    }
}
