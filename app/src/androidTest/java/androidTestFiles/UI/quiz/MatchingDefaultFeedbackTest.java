package androidTestFiles.UI.quiz;


import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.widgets.QuizWidget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import androidTestFiles.Utils.FileUtils;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MatchingDefaultFeedbackTest {

    private static final String MATCHING_DEFAULT_FEEDBACK_JSON =
            "quizzes/matching_default_feedback.json";
    private static final String FIRST_QUESTION_TITLE =
            "What beats what?";
    private static final String CORRECT_ANSWER_1 = "Red";
    private static final String CORRECT_ANSWER_2 = "Yellow";
    private static final String INCORRECT_ANSWER_1 = "Black";

    private Activity act;
    private Bundle args;

    @Before
    public void setup() throws Exception {
        // Setting up before every test
        act = new Activity();
        String quizContent = FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(),
                MATCHING_DEFAULT_FEEDBACK_JSON);

        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang("en", quizContent));
        act.setContents(contents);

        args = new Bundle();
        args.putSerializable(Activity.TAG, act);
        args.putSerializable(Course.TAG, new Course(""));
        args.putBoolean(CourseActivity.BASELINE_TAG, false);
    }

    @Test
    public void correctAnswer() {
        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme, null);
        onView(withId(R.id.take_quiz_btn)).perform(click());

        onView(withId(R.id.question_text))
                .check(matches(withText(FIRST_QUESTION_TITLE)));
        /* TODO
        onView(withText(CORRECT_ANSWER_1)).perform(click());
        onView(withText(CORRECT_ANSWER_2)).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        String actual = Utils.TestUtils.getCurrentActivity().getString(R.string.widget_quiz_results_score, (float) 100);
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
        */
        // TODO - check the image matches for question response
        // onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
        // .atPositionOnView(0, R.id.quiz_question_feedback_image))
        // .check(matches(R.drawable.quiz_tick));

    }
}
