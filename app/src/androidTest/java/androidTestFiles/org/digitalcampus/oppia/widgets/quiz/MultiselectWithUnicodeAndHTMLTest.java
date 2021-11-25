package androidTestFiles.org.digitalcampus.oppia.widgets.quiz;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;
import static androidTestFiles.Matchers.EspressoTestsMatchers.withDrawable;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.widgets.QuizWidget;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MultiselectWithUnicodeAndHTMLTest extends BaseQuizTest {

    private static final String MULTISELECT_WITH_UNICODE_AND_HTML_JSON =
            "quizzes/multiselect_with_unicode_and_html.json";
    private static final String FIRST_QUESTION_TITLE =
            "Test with Unicode characters in response options";
    private static final String CORRECT_ANSWER_1 = "response option without unicode - correct";
    private static final String CORRECT_ANSWER_2_CONTAINS = "With unicode - correct"; // Espresso does not find views with &nbsp; character
    private static final String INCORRECT_ANSWER_1 = "response option without unicode - incorrect";
    private static final String INCORRECT_ANSWER_2_CONTAINS = "With unicode - incorrect"; // Espresso does not find views with &nbsp; character
    private static final String CORRECT_ANSWER_01_FEEDBACK = "Correct!";
    private static final String CORRECT_ANSWER_02_FEEDBACK = "correct";
    private static final String INCORRECT_ANSWER_1_FEEDBACK = "incorrect";
    private static final String INCORRECT_ANSWER_2_FEEDBACK = "that's wrong!";

    @Override
    protected String getQuizContentFile() {
        return MULTISELECT_WITH_UNICODE_AND_HTML_JSON;
    }

    @Test
    public void correctAnswer() {
        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme, null);
        onView(withId(R.id.take_quiz_btn)).perform(click());

        onView(withId(R.id.question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));

        onView(withText(CORRECT_ANSWER_1)).perform(click());
        onView(withText(containsString(CORRECT_ANSWER_2_CONTAINS))).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        onView(withText(containsString(CORRECT_ANSWER_01_FEEDBACK)))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withText(containsString(CORRECT_ANSWER_02_FEEDBACK)))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withDrawable(R.drawable.quiz_tick))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

    }

    @Test
    public void incorrectAnswer() {
        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme, null);
        onView(withId(R.id.take_quiz_btn)).perform(click());

        onView(withId(R.id.question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));

        onView(withText(INCORRECT_ANSWER_1)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        onView(withText(containsString(INCORRECT_ANSWER_1_FEEDBACK)))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withDrawable(R.drawable.quiz_cross))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void previousAnswersMarkedWhenGoBack() {
        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme, null);
        onView(withId(R.id.take_quiz_btn)).perform(click());

        onView(withId(R.id.question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));

        onView(withText(CORRECT_ANSWER_1)).perform(click());
        onView(withText(containsString(INCORRECT_ANSWER_2_CONTAINS))).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        onView(withText(R.string.ok))
                .inRoot(isDialog())
                .perform(click());

        onView(withId(R.id.mquiz_prev_btn))
                .perform(click());


        onView(withText(CORRECT_ANSWER_1)).check(matches(isChecked()));
        onView(withText(containsString(INCORRECT_ANSWER_2_CONTAINS))).check(matches(isChecked()));

    }


}
