package UI;


import android.os.Bundle;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.widgets.FeedbackWidget;
import org.digitalcampus.oppia.widgets.QuizWidget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.StringStartsWith.startsWith;


@RunWith(AndroidJUnit4.class)
public class FeedbackUITest {

    private static final String SIMPLE_FEEDBACK_JSON = "quizzes/simple_feedback.json";

    private Activity act;
    private Bundle args;

    @Before
    public void setup() throws Exception {
        // Setting up before every test
        act = new Activity();
        String quizContent = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), SIMPLE_FEEDBACK_JSON);

        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang("en", quizContent));
        act.setContents(contents);

        args = new Bundle();
        args.putSerializable(Activity.TAG, act);
        args.putSerializable(Course.TAG, new Course(""));
        args.putBoolean(CourseActivity.BASELINE_TAG, false);
    }

    @Test
    public void showGreetingsAtFinish() {
        launchInContainer(FeedbackWidget.class, args, R.style.Oppia_ToolbarTheme, null);

        onView(withText("firstanswer")).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());
        onView(withText("secondanswer")).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        //If the quiz is passed, we only have to show the "Continue" button
        onView(withId(R.id.quiz_exit_button))
                .check(matches(withText(R.string.widget_quiz_continue)));
        onView(withId(R.id.quiz_results_button))
                .check(matches(not(isDisplayed())));

        onView(withId(R.id.quiz_results_score))
                .check(matches(withText(R.string.widget_feedback_submit_title)));

    }

}
