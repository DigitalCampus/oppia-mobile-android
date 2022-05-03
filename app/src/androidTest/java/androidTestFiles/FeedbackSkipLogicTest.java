package androidTestFiles;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static androidTestFiles.Matchers.RecyclerViewMatcher.withRecyclerView;
import static androidTestFiles.Utils.CourseUtils.runInstallCourseTask;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import androidTestFiles.TestRules.DaggerInjectMockUITest;
import androidTestFiles.Utils.Assertions.RecyclerViewItemCountAssertion;
import androidTestFiles.Utils.CourseUtils;
import androidTestFiles.Utils.FileUtils;
import androidTestFiles.Utils.UITestActionsUtils;
import androidTestFiles.database.TestDBHelper;

@RunWith(AndroidJUnit4.class)
public class FeedbackSkipLogicTest extends DaggerInjectMockUITest {


    private static final String COURSE_SKIP_LOGIC = "course-skip-logic.zip";
    private Context context;

    private static final long USER_ID_NONE = -1;
    private static final int COURSE_ID = 1;
    private TestDBHelper testDBHelper;



    private static final String QUESTION_RATE_APP = "How do you rate the app?";
    private static final String QUESTION_RATE_CONTENT = "How do you rate the course content?";
    private static final String QUESTION_SCALE_CONTENT = "on scale of 1-5 how likely are you to recommend the app and content";

    private static final String RESPONSE_OK = "ok";
    private static final String RESPONSE_GOOD = "good";
    private static final String RESPONSE_BAD = "bad";


    private static final String QUESTION_APP_GOOD_REASON = "Why is it good? visible only if app rating=good";

    private static final String QUESTION_APP_BAD_ISSUES = "What issues did you have? visible only if app-rating=bad";
    private static final String QUESTION_APP_OTHER_ISSUE_REASON = "What other issues did you have? only visible if app-rating-issues=other";
    private static final String RESPONSE_BAD_ISSUE_CRASHING = "app crashing";
    private static final String RESPONSE_BAD_ISSUE_OTHER = "other";


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

        // First ensure to use in-memory database
        testDBHelper = new TestDBHelper(InstrumentationRegistry.getInstrumentation().getTargetContext());
        testDBHelper.setUp();

        initMockEditor();
        when(prefs.getBoolean(eq(PrefsActivity.PREF_SHOW_GAMIFICATION_EVENTS), anyBoolean())).thenReturn(false);

        CourseUtils.cleanUp();
    }


    @After
    public void tearDown() throws Exception {
        testDBHelper.tearDown();
        CourseUtils.cleanUp();
    }

    protected void copyCourseFromAssets(String filename) {
        FileUtils.copyZipFromAssetsPath(context, "courses_skip_logic", filename);  //Copy course zip from assets to download path
    }


    private void installCourse(String filename) {

        copyCourseFromAssets(filename);

        BasicResult response = runInstallCourseTask(context);
        assertTrue(response.isSuccess());
    }

    private Intent getTestCourseIntent() {

//        int courseId = testDBHelper.getDbHelper().getCourseID("ref-course");
        Course course = testDBHelper.getDbHelper().getCourse(COURSE_ID, USER_ID_NONE);

        Intent i = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), CourseIndexActivity.class);
        i.putExtra(Course.TAG, course);
        return i;
    }

    private void typeResponse(String questionText, String responseText) {
        onView(allOf(withId(R.id.question_text), isCompletelyDisplayed())).check(matches(withText(questionText)));
        onView(withId(R.id.responsetext)).perform(scrollTo(), typeText(responseText), closeSoftKeyboard());
        onView(allOf(withId(R.id.mquiz_next_btn), isCompletelyDisplayed())).perform(click());
    }

    private void selectResponse(String questionText, String responseText) {
        selectResponse(questionText, responseText, true);
    }

    private void selectResponse(String questionText, String responseText, boolean performNext) {
        onView(allOf(withId(R.id.question_text), isCompletelyDisplayed()))
                .check(matches(withText(questionText)));
        onView(allOf(withText(responseText), isCompletelyDisplayed())).perform(click());
        if (performNext) {
            onView(allOf(withId(R.id.mquiz_next_btn), isCompletelyDisplayed())).perform(click());
        }
    }

    private void checkResultAtPosition(int position, String questionText, String responseText) {

        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(position, R.id.quiz_question_text))
                .check(matches(withText(questionText)));
        onView(withRecyclerView(R.id.recycler_quiz_results_feedback)
                .atPositionOnView(position, R.id.quiz_question_user_response_text))
                .check(matches(withText(responseText)));
    }

    @Test
    public void dontSkipAnyQuestionIfLabelIsMissing() throws Exception { // To ensure retrocompatibility

        installCourse(COURSE_SKIP_LOGIC);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIntent())) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 2);

            selectResponse(QUESTION_RATE_APP, RESPONSE_BAD);
            selectResponse(QUESTION_RATE_CONTENT, RESPONSE_GOOD);
            typeResponse(QUESTION_SCALE_CONTENT, "4");

            checkResultAtPosition(0, QUESTION_RATE_APP, RESPONSE_BAD);
            checkResultAtPosition(1, QUESTION_RATE_CONTENT, RESPONSE_GOOD);
            checkResultAtPosition(2, QUESTION_SCALE_CONTENT, "4");
        }
    }

    @Test
    public void dontSkipAnyQuestionIfLabelExistsButNoSkipLogic() throws Exception {

        installCourse(COURSE_SKIP_LOGIC);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIntent())) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 3);

            selectResponse(QUESTION_RATE_APP, RESPONSE_BAD);
            selectResponse(QUESTION_RATE_CONTENT, RESPONSE_GOOD);
            typeResponse(QUESTION_SCALE_CONTENT, "4");

            checkResultAtPosition(0, QUESTION_RATE_APP, RESPONSE_BAD);
            checkResultAtPosition(1, QUESTION_RATE_CONTENT, RESPONSE_GOOD);
            checkResultAtPosition(2, QUESTION_SCALE_CONTENT, "4");
        }
    }

    @Test
    public void checkOneLevelSkipQuestion() throws Exception {

        installCourse(COURSE_SKIP_LOGIC);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIntent())) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 4);

            selectResponse(QUESTION_RATE_APP, RESPONSE_GOOD);
            typeResponse(QUESTION_APP_GOOD_REASON, "it's beautiful and useful");
            selectResponse(QUESTION_RATE_CONTENT, RESPONSE_GOOD);
            typeResponse(QUESTION_SCALE_CONTENT, "4");

            checkResultAtPosition(0, QUESTION_RATE_APP, RESPONSE_GOOD);
            checkResultAtPosition(1, QUESTION_APP_GOOD_REASON, "it's beautiful and useful");
            checkResultAtPosition(2, QUESTION_RATE_CONTENT, RESPONSE_GOOD);
            checkResultAtPosition(3, QUESTION_SCALE_CONTENT, "4");
        }
    }

    @Test
    public void checkOneLevelReverseSkipLogic() throws Exception {

        installCourse(COURSE_SKIP_LOGIC);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIntent())) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 4);

            selectResponse(QUESTION_RATE_APP, RESPONSE_GOOD);
            typeResponse(QUESTION_APP_GOOD_REASON, "it's beautiful and useful");
            onView(allOf(withId(R.id.mquiz_prev_btn), isCompletelyDisplayed())).perform(click());
            onView(allOf(withId(R.id.mquiz_prev_btn), isCompletelyDisplayed())).perform(click());
            selectResponse(QUESTION_RATE_APP, RESPONSE_OK);
            selectResponse(QUESTION_RATE_CONTENT, RESPONSE_GOOD);
            typeResponse(QUESTION_SCALE_CONTENT, "4");

            checkResultAtPosition(0, QUESTION_RATE_APP, RESPONSE_OK);
            checkResultAtPosition(1, QUESTION_RATE_CONTENT, RESPONSE_GOOD);
            checkResultAtPosition(2, QUESTION_SCALE_CONTENT, "4");
        }
    }

    @Test
    public void checkTwoLevelsSkipNoQuestion() throws Exception {

        installCourse(COURSE_SKIP_LOGIC);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIntent())) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 5);

            selectResponse(QUESTION_RATE_APP, RESPONSE_BAD);
            selectResponse(QUESTION_APP_BAD_ISSUES, RESPONSE_BAD_ISSUE_OTHER);
            typeResponse(QUESTION_APP_OTHER_ISSUE_REASON, "it's ugly");
            selectResponse(QUESTION_RATE_CONTENT, RESPONSE_GOOD);
            typeResponse(QUESTION_SCALE_CONTENT, "4");

            checkResultAtPosition(0, QUESTION_RATE_APP, RESPONSE_BAD);
            checkResultAtPosition(1, QUESTION_APP_BAD_ISSUES, RESPONSE_BAD_ISSUE_OTHER);
            checkResultAtPosition(2, QUESTION_APP_OTHER_ISSUE_REASON, "it's ugly");
            checkResultAtPosition(3, QUESTION_RATE_CONTENT, RESPONSE_GOOD);
            checkResultAtPosition(4, QUESTION_SCALE_CONTENT, "4");
        }
    }

    @Test
    public void checkTwoLevelsSkipNoQuestionManyMultiselect() throws Exception {

        installCourse(COURSE_SKIP_LOGIC);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIntent())) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 5);

            selectResponse(QUESTION_RATE_APP, RESPONSE_BAD);
            selectResponse(QUESTION_APP_BAD_ISSUES, RESPONSE_BAD_ISSUE_CRASHING, false);
            selectResponse(QUESTION_APP_BAD_ISSUES, RESPONSE_BAD_ISSUE_OTHER);
            typeResponse(QUESTION_APP_OTHER_ISSUE_REASON, "it's ugly");
            selectResponse(QUESTION_RATE_CONTENT, RESPONSE_GOOD);

            // No need to check results, just check dependent question is shown selecting multiple responses in multiselect
        }
    }

    @Test
    public void checkTwoLevelsSkipOneQuestion() throws Exception {

        installCourse(COURSE_SKIP_LOGIC);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIntent())) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 5);

            selectResponse(QUESTION_RATE_APP, RESPONSE_BAD);
            selectResponse(QUESTION_APP_BAD_ISSUES, RESPONSE_BAD_ISSUE_CRASHING);
            selectResponse(QUESTION_RATE_CONTENT, RESPONSE_GOOD);
            typeResponse(QUESTION_SCALE_CONTENT, "4");

            checkResultAtPosition(0, QUESTION_RATE_APP, RESPONSE_BAD);
            checkResultAtPosition(1, QUESTION_APP_BAD_ISSUES, RESPONSE_BAD_ISSUE_CRASHING);
            checkResultAtPosition(2, QUESTION_RATE_CONTENT, RESPONSE_GOOD);
            checkResultAtPosition(3, QUESTION_SCALE_CONTENT, "4");
        }
    }

    @Test
    public void checkTwoLevelsSkipTwoQuestions() throws Exception {

        installCourse(COURSE_SKIP_LOGIC);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIntent())) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 5);

            selectResponse(QUESTION_RATE_APP, RESPONSE_OK);
            selectResponse(QUESTION_RATE_CONTENT, RESPONSE_GOOD);
            typeResponse(QUESTION_SCALE_CONTENT, "4");

            checkResultAtPosition(0, QUESTION_RATE_APP, RESPONSE_OK);
            checkResultAtPosition(1, QUESTION_RATE_CONTENT, RESPONSE_GOOD);
            checkResultAtPosition(2, QUESTION_SCALE_CONTENT, "4");
        }
    }

    @Test
    public void checkTwoLevelsReverseSkipLogic() throws Exception {

        installCourse(COURSE_SKIP_LOGIC);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIntent())) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 5);

            selectResponse(QUESTION_RATE_APP, RESPONSE_BAD);
            selectResponse(QUESTION_APP_BAD_ISSUES, RESPONSE_BAD_ISSUE_OTHER);
            typeResponse(QUESTION_APP_OTHER_ISSUE_REASON, "it's ugly");
            onView(allOf(withId(R.id.mquiz_prev_btn), isCompletelyDisplayed())).perform(click());
            onView(allOf(withId(R.id.mquiz_prev_btn), isCompletelyDisplayed())).perform(click());
            onView(allOf(withId(R.id.mquiz_prev_btn), isCompletelyDisplayed())).perform(click());
            selectResponse(QUESTION_RATE_APP, RESPONSE_OK);
            selectResponse(QUESTION_RATE_CONTENT, RESPONSE_GOOD);
            typeResponse(QUESTION_SCALE_CONTENT, "4");

            checkResultAtPosition(0, QUESTION_RATE_APP, RESPONSE_OK);
            checkResultAtPosition(1, QUESTION_RATE_CONTENT, RESPONSE_GOOD);
            checkResultAtPosition(2, QUESTION_SCALE_CONTENT, "4");
        }
    }
}
