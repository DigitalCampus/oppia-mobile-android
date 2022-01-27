package androidTestFiles.UI;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import androidTestFiles.TestRules.DaggerInjectMockUITest;
import androidTestFiles.Utils.CourseUtils;
import androidTestFiles.Utils.FileUtils;
import androidTestFiles.Utils.UITestActionsUtils;
import androidx.test.rule.GrantPermissionRule;

@RunWith(AndroidJUnit4.class)
public class QuizResultsUITest extends DaggerInjectMockUITest {

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    public static final String COURSE_QUIZ_SHOW_ALL = "show_all.zip";
    public static final String COURSE_QUIZ_HIDE_LATER = "hide_later.zip";
    public static final String COURSE_QUIZ_HIDE_AT_END = "hide_at_end.zip";
    public static final String COURSE_QUIZ_HIDE_AT_END_AND_LATER = "hide_at_end_and_later.zip";

    @Mock
    SharedPreferences prefs;
    @Mock
    SharedPreferences.Editor editor;


    @Before
    public void setUp() {
        initMockEditor();
        when(prefs.getBoolean(eq(PrefsActivity.PREF_SHOW_GAMIFICATION_EVENTS), anyBoolean())).thenReturn(false);
    }

    private void initMockEditor() {
        when(prefs.edit()).thenReturn(editor);
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);
    }

    @After
    public void cleanUp() {
        CourseUtils.cleanUp();
    }

    private void installCourse(String course) {

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        CourseUtils.cleanUp();
        FileUtils.copyZipFromAssetsPath(context, "courses_quiz_results", course);
        BasicResult response = CourseUtils.runInstallCourseTask(context);
        assertTrue(response.isSuccess());
    }

    private void checkShowResults(boolean atEnd, boolean later) throws Exception {

        when(prefs.getBoolean(eq(PrefsActivity.PREF_START_COURSEINDEX_COLLAPSED), anyBoolean())).thenReturn(false);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_courses, 0);

            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 2);
            UITestActionsUtils.clickViewWithText(R.string.quiz_attempts_take_quiz);
            UITestActionsUtils.clickViewWithText("Cardiff");
            UITestActionsUtils.clickViewWithText(R.string.widget_quiz_getresults);

            if (atEnd) {
                onView(withId(R.id.recycler_quiz_results_feedback)).check(matches(isDisplayed()));
            } else {
                onView(withId(R.id.recycler_quiz_results_feedback)).check(matches(not(isDisplayed())));
            }

            UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());

            device.pressBack();
            device.pressBack();

            onView(withId(R.id.nav_bottom_scorecard)).perform(click());
            onView(withId(R.id.tabs)).perform(UITestActionsUtils.selectTabAtPosition(2));

            Thread.sleep(500); // wait for viewpager transition
            UITestActionsUtils.clickRecyclerViewPosition(R.id.attempts_list, 0);

            if (later) {
                onView(withId(R.id.recycler_quiz_results_feedback)).check(matches(isDisplayed()));
            } else {
                onView(withId(R.id.recycler_quiz_results_feedback)).check(matches(not(isDisplayed())));
            }

        }
    }


    @Test
    public void showQuizResultsAtEndAndLater() throws Exception {

        installCourse(COURSE_QUIZ_SHOW_ALL);
        checkShowResults(true, true);

    }

    @Test
    public void hideQuizResultsLater() throws Exception {

        installCourse(COURSE_QUIZ_HIDE_LATER);
        checkShowResults(true, false);

    }

    @Test
    public void hideQuizResultsAtEnd() throws Exception {

        installCourse(COURSE_QUIZ_HIDE_AT_END);
        checkShowResults(false, true);

    }

    @Test
    public void hideQuizResultsAtEndAndLater() throws Exception {

        installCourse(COURSE_QUIZ_HIDE_AT_END_AND_LATER);
        checkShowResults(false, false);

    }

}
