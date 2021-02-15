package androidTestFiles.UI.quiz;


import android.content.Context;
import android.os.Bundle;

import androidTestFiles.TestRules.DaggerInjectMockUITest;
import androidx.fragment.app.Fragment;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.QuizAttemptRepository;
import org.digitalcampus.oppia.model.QuizStats;
import org.digitalcampus.oppia.widgets.FeedbackWidget;
import org.digitalcampus.oppia.widgets.QuizWidget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;

import androidTestFiles.Utils.FileUtils;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.mockito.Matchers.any;


@RunWith(Parameterized.class)
public class AnswerWidgetTest extends DaggerInjectMockUITest {

    private static final String SIMPLE_QUIZ_JSON = "quizzes/simple_quiz.json";
    private static final String FIRST_QUESTION_TITLE = "First question";

    private Activity act;
    private Bundle args;
    private Class widgetClass;

    @Mock
    QuizAttemptRepository attemptsRepository;

    @Parameterized.Parameters
    public static Class<? extends Fragment>[] widgetClasses() {
        return new Class[]{ QuizWidget.class, FeedbackWidget.class };
    }

    public AnswerWidgetTest(Class widgetClass){
        this.widgetClass = widgetClass;
    }

    @Before
    public void setup() throws Exception {
        // Setting up before every test
        act = new Activity();
        String quizContent = FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), SIMPLE_QUIZ_JSON);

        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang("en", quizContent));
        act.setContents(contents);

        args = new Bundle();
        args.putSerializable(Activity.TAG, act);
        args.putSerializable(Course.TAG, new Course(""));
        args.putBoolean(CourseActivity.BASELINE_TAG, false);

        QuizStats stats = new QuizStats();
        Mockito.doAnswer((Answer<QuizStats>) invocation -> stats).when(attemptsRepository).getQuizAttemptStats(any(Context.class), any());
    }


    @Test
    public void WrongQuizFormat() {
        act = new Activity();
        String quizContent = "not_a_json_string";
        ArrayList<Lang> contents = new ArrayList<>();
        contents.add(new Lang("en", quizContent));
        act.setContents(contents);
        args.putSerializable(Activity.TAG, act);
        args.putSerializable(Course.TAG, new Course(""));

        launchInContainer(this.widgetClass, args, R.style.Oppia_ToolbarTheme, null);

        onView(withId(R.id.quiz_unavailable))
                .check(matches(withText((R.string.quiz_loading_error))));

    }

    @Test
    public void dontContinueIfQuestionUnaswered() {
        launchInContainer(this.widgetClass, args, R.style.Oppia_ToolbarTheme, null);
        if (widgetClass == QuizWidget.class){
            onView(withId(R.id.take_quiz_btn)).perform(click());
        }

        onView(withId(R.id.question_text))
                .check(matches(withText(startsWith(FIRST_QUESTION_TITLE))));
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        //If we didn't select any option, it should have stayed in the same question
        onView(withId(R.id.question_text))
                .check(matches(withText(startsWith(FIRST_QUESTION_TITLE))));

    }

    @Test
    public void continueIfQuestionAnswered() {
        launchInContainer(this.widgetClass, args, R.style.Oppia_ToolbarTheme, null);
        if (widgetClass == QuizWidget.class){
            onView(withId(R.id.take_quiz_btn)).perform(click());
        }
        onView(withId(R.id.question_text))
                .check(matches(withText(startsWith(FIRST_QUESTION_TITLE))));

        onView(withText("correctanswer")).perform(click());
        onView(withId(R.id.mquiz_next_btn)).perform(click());

        onView(withId(R.id.question_text))
                .check(matches(not(withText(startsWith(FIRST_QUESTION_TITLE)))));

    }
}
