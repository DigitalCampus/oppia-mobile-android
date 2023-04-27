package androidTestFiles.features.quiz;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static androidTestFiles.utils.CourseUtils.runInstallCourseTask;
import static androidTestFiles.utils.UITestActionsUtils.waitForView;
import static androidTestFiles.utils.matchers.RecyclerViewMatcher.withRecyclerView;
import static androidTestFiles.utils.parent.BaseTest.COURSE_SKIP_LOGIC;
import static androidTestFiles.utils.parent.BaseTest.PATH_COURSES_SKIP_LOGIC_TESTS;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.widgets.QuizWidget;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import androidTestFiles.database.TestDBHelper;
import androidTestFiles.utils.CourseUtils;
import androidTestFiles.utils.FileUtils;
import androidTestFiles.utils.UITestActionsUtils;
import androidTestFiles.utils.parent.BaseTest;
import androidTestFiles.utils.parent.DaggerInjectMockUITest;
import androidTestFiles.widgets.quiz.BaseQuizTest;

@RunWith(AndroidJUnit4.class)
public class QuizChangeResponsesUiTest extends BaseQuizTest {

    private Context context;

    private static final long USER_ID_NONE = -1;
    private static final int COURSE_ID = 1;
    private TestDBHelper testDBHelper;


    private static final String QUESTION_RAINBOW = "What of the following are colours of the rainbow?";
    private static final String RESPONSE_RED = "Red";
    private static final String RESPONSE_YELLOW = "Yellow";
    private static final String RESPONSE_BLACK = "Black";

    private static final String QUESTION_CAPITAL_GERMANY = "What is the capital of Germany?";
    private static final String RESPONSE_BERLIN = "Berlin";
    private static final String RESPONSE_MUNICH = "Munich";

    private static final String QUESTION_CAPITAL_SPAIN = "What is the capital of Spain?";


    @Override
    protected String getQuizContentFile() {
        return BaseTest.PATH_QUIZZES + "/mix_question_types.json";
    }

    @Mock
    SharedPreferences prefs;
    @Mock
    SharedPreferences.Editor editor;

    private void initMockEditor() {
        when(prefs.edit()).thenReturn(editor);
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);
    }

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        initMockEditor();
        when(prefs.getBoolean(eq(PrefsActivity.PREF_SHOW_GAMIFICATION_EVENTS), anyBoolean())).thenReturn(false);

    }


    private void typeResponse(String questionText, String responseText, boolean performNext) {
        waitForView(allOf(withId(R.id.question_text))).check(matches(withText(questionText)));
        waitForView(withId(R.id.responsetext)).perform(scrollTo(), clearText(), typeText(responseText), closeSoftKeyboard());
        if (performNext) {
            waitForView(allOf(withId(R.id.mquiz_next_btn))).perform(click());
        }
    }

    private void selectResponse(String questionText, String responseText) {
        selectResponse(questionText, responseText, true);
    }

    private void selectResponse(String questionText, String responseText, boolean performNext) {
        waitForView(allOf(withId(R.id.question_text)))
                .check(matches(withText(questionText)));
        waitForView(allOf(withText(responseText))).perform(click());
        if (performNext) {
            waitForView(allOf(withId(R.id.mquiz_next_btn))).perform(click());
        }
    }

    private void checkResultAtPosition(int position, String questionText, String responseText) {

        waitForView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(position, R.id.quiz_question_text))
                .check(matches(withText(questionText)));
        waitForView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(position, R.id.quiz_question_user_response_text))
                .check(matches(withText(responseText)));
    }


    @Test
    public void checkQuizResponsesWithChanges() throws Exception {


        launchInContainer(QuizWidget.class, args, R.style.Oppia_ToolbarTheme);
        waitForView(withId(R.id.take_quiz_btn)).perform(click());

        selectResponse(QUESTION_RAINBOW, RESPONSE_BLACK);
        selectResponse(QUESTION_CAPITAL_GERMANY, RESPONSE_MUNICH);
        typeResponse(QUESTION_CAPITAL_SPAIN, "Cadiz", false);
        waitForView(allOf(withId(R.id.mquiz_prev_btn))).perform(click());
        waitForView(allOf(withId(R.id.mquiz_prev_btn))).perform(click());
        selectResponse(QUESTION_RAINBOW, RESPONSE_BLACK, false);
        selectResponse(QUESTION_RAINBOW, RESPONSE_RED);
        selectResponse(QUESTION_CAPITAL_GERMANY, RESPONSE_BERLIN);
        typeResponse(QUESTION_CAPITAL_SPAIN, "Madrid", true);

        checkResultAtPosition(0, QUESTION_RAINBOW, RESPONSE_RED);
        checkResultAtPosition(1, QUESTION_CAPITAL_GERMANY, RESPONSE_BERLIN);
        checkResultAtPosition(2, QUESTION_CAPITAL_SPAIN, "Madrid");

    }

}
