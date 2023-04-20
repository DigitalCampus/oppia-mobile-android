package androidTestFiles.features.passwordProtected;

import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static androidTestFiles.utils.CourseUtils.runInstallCourseTask;
import static androidTestFiles.utils.UITestActionsUtils.waitForView;
import static androidTestFiles.utils.matchers.EspressoTestsMatchers.withDrawable;
import static androidTestFiles.utils.matchers.RecyclerViewMatcher.withRecyclerView;
import static androidTestFiles.utils.parent.BaseTest.COURSE_PASSWORD_PROTECT;
import static androidTestFiles.utils.parent.BaseTest.COURSE_PASSWORD_PROTECT_UPDATE;
import static androidTestFiles.utils.parent.BaseTest.PATH_COURSES_TOPICS_PASSWORD_PROTECT_TESTS;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.activity.CourseQuizAttemptsActivity;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.QuizAttemptActivity;
import org.digitalcampus.oppia.activity.SearchActivity;
import org.digitalcampus.oppia.activity.ViewDigestActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.analytics.AnalyticsProvider;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.model.Section;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import androidTestFiles.database.TestDBHelper;
import androidTestFiles.utils.CourseUtils;
import androidTestFiles.utils.FileUtils;
import androidTestFiles.utils.UITestActionsUtils;
import androidTestFiles.utils.assertions.RecyclerViewItemCountAssertion;
import androidTestFiles.utils.parent.MockedApiEndpointTest;

@RunWith(AndroidJUnit4.class)
public class TopicsPasswordProtectedTest extends MockedApiEndpointTest {


    private static final String PASSWORD_INITIAL = "password";
    private static final String PASSWORD_UPDATE = "newpassword";

    private static final long USER_ID_NONE = -1;
    private static final int COURSE_ID = 1;

    private static final String VALID_LOGIN_REGISTER_RESPONSE = "responses/response_200_register.json";

    private Context context;
    private TestDBHelper testDBHelper;

    @Mock
    AnalyticsProvider analyticsProvider;
    @Mock
    SharedPreferences prefs;
    @Mock
    SharedPreferences.Editor editor;
    @Mock
    User user;

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

        CourseUtils.cleanUp();
    }


    @After
    public void tearDown() throws Exception {
        testDBHelper.tearDown();
        CourseUtils.cleanUp();
    }

    protected void copyCourseFromAssets(String filename) {
        FileUtils.copyZipFromAssetsPath(context, PATH_COURSES_TOPICS_PASSWORD_PROTECT_TESTS, filename);  //Copy course zip from assets to download path
    }

    private Intent getTestCourseIntent() {

//        int courseId = testDBHelper.getDbHelper().getCourseID("ref-course");
        Course course = testDBHelper.getDbHelper().getCourseWithProgress(COURSE_ID, USER_ID_NONE);

        Intent i = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), CourseIndexActivity.class);
        i.putExtra(Course.TAG, course);
        return i;
    }

    private void setCourseSectionUnlockPassword(int sectionOrder, String password) {
        Course course = new Course();
        course.setCourseId(COURSE_ID);
        Section section = new Section();
        section.setOrder(sectionOrder);
        testDBHelper.getDbHelper().saveSectionUnlockedByUser(course, section, USER_ID_NONE, password);
    }

    private void installCourse(String filename) {

        copyCourseFromAssets(filename);

        BasicResult response = runInstallCourseTask(context);
        assertTrue(response.isSuccess());
    }

    @Test
    public void dontShowPasswordScreenIfTopicIsNotProtected() throws Exception {

        installCourse(COURSE_PASSWORD_PROTECT);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIntent())) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 2);

            waitForView(withText(R.string.password_needed)).check(matches(not(isDisplayed())));
        }
    }


    @Test
    public void showPasswordScreenIfTopicIsProtected() throws Exception {

        installCourse(COURSE_PASSWORD_PROTECT);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIntent())) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 4);

            waitForView(withText(R.string.password_needed)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void dontShowPasswordScreenIfTopicIsUnlocked() throws Exception {

        installCourse(COURSE_PASSWORD_PROTECT);

        setCourseSectionUnlockPassword(2, PASSWORD_INITIAL);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIntent())) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 4);

            waitForView(withText(R.string.password_needed)).check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void checkPasswordLockedIcons() throws Exception {

        installCourse(COURSE_PASSWORD_PROTECT);

        setCourseSectionUnlockPassword(2, PASSWORD_INITIAL);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIntent())) {

            waitForView(withRecyclerView(R.id.recycler_course_sections)
                    .atPositionOnView(1, R.id.lock_indicator))
                    .check(matches(not(isDisplayed())));

            waitForView(withRecyclerView(R.id.recycler_course_sections)
                    .atPositionOnView(3, R.id.lock_indicator))
                    .check(matches(withDrawable(R.drawable.ic_unlock)));
//
            waitForView(withRecyclerView(R.id.recycler_course_sections)
                    .atPositionOnView(6, R.id.lock_indicator))
                    .check(matches(withDrawable(R.drawable.ic_lock)));
        }
    }

    private void performLogout() {

        openDrawer();
        waitForView(withId(R.id.btn_expand_profile_options)).perform(click());
        waitForView(withId(R.id.btn_logout)).perform(click());
        waitForView(withText(R.string.yes)).perform(click());

    }

    private void performValidLogin(String username) {

        startServer(200, VALID_LOGIN_REGISTER_RESPONSE, 0);
        waitForView(withId(R.id.welcome_login)).perform(scrollTo(), click());
        waitForView(withId(R.id.login_username_field)).perform(closeSoftKeyboard(), scrollTo(), typeText(username));
        waitForView(withId(R.id.login_password_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("valid_password"));
        waitForView(withId(R.id.login_btn)).perform(closeSoftKeyboard(), scrollTo(), click());
    }


    @Test
    public void showPasswordScreenIfUpdatePasswordHasChanged() throws Exception {

        installCourse(COURSE_PASSWORD_PROTECT);
        when(prefs.getBoolean(eq(PrefsActivity.PREF_SHOW_GAMIFICATION_EVENTS), anyBoolean())).thenReturn(false);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_courses, 0);
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 4);
            waitForView(withText(R.string.password_needed)).check(matches(isDisplayed()));
            waitForView(withId(R.id.section_password_field)).perform(typeText(PASSWORD_INITIAL), closeSoftKeyboard());
            waitForView(withId(R.id.submit_password)).perform(click());
            waitForView(withText(R.string.password_needed)).check(matches(not(isDisplayed())));

            pressBack();
            pressBack();
            installCourse(COURSE_PASSWORD_PROTECT_UPDATE);

            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_courses, 0);
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 4);
            waitForView(withText(R.string.password_needed)).check(matches(isDisplayed()));

        }
    }

    @Ignore
    @Test
    public void dontShowPasswordScreenIfUpdatePasswordHasNotChanged() throws Exception {

        when(prefs.getBoolean(eq(PrefsActivity.PREF_SHOW_GAMIFICATION_EVENTS), anyBoolean())).thenReturn(false);

        installCourse(COURSE_PASSWORD_PROTECT);
        Assert.assertEquals(0, testDBHelper.getDbHelper().unlockedSectionsCount(COURSE_ID, USER_ID_NONE));
        setCourseSectionUnlockPassword(3, PASSWORD_INITIAL);
        Assert.assertEquals(1, testDBHelper.getDbHelper().unlockedSectionsCount(COURSE_ID, USER_ID_NONE));
        installCourse(COURSE_PASSWORD_PROTECT_UPDATE);
        Assert.assertEquals(1, testDBHelper.getDbHelper().unlockedSectionsCount(COURSE_ID, USER_ID_NONE));

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIntent())) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 7);
            waitForView(withText(R.string.password_needed)).check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void showPasswordScreenIfUpdatePasswordIsAdded() throws Exception {

        installCourse(COURSE_PASSWORD_PROTECT);
        when(prefs.getBoolean(eq(PrefsActivity.PREF_SHOW_GAMIFICATION_EVENTS), anyBoolean())).thenReturn(false);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_courses, 0);
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 9);
            waitForView(withText(R.string.password_needed)).check(matches(not(isDisplayed())));

            pressBack();
            pressBack();
            installCourse(COURSE_PASSWORD_PROTECT_UPDATE);

            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_courses, 0);
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 9);
            waitForView(withText(R.string.password_needed)).check(matches(isDisplayed()));

        }
    }

    @Test
    public void showPasswordScreenIfUpdatePasswordIsRemoved() throws Exception {

        installCourse(COURSE_PASSWORD_PROTECT);
        when(prefs.getBoolean(eq(PrefsActivity.PREF_SHOW_GAMIFICATION_EVENTS), anyBoolean())).thenReturn(false);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_courses, 0);
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 11);
            waitForView(withText(R.string.password_needed)).check(matches(isDisplayed()));

            pressBack();
            pressBack();
            installCourse(COURSE_PASSWORD_PROTECT_UPDATE);

            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_courses, 0);
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 11);
            waitForView(withText(R.string.password_needed)).check(matches(not(isDisplayed())));

        }
    }


    @Ignore
    @Test
    public void checkTopicUnlockedWhenLogoutLoginSameUser() throws Exception {

        installCourse(COURSE_PASSWORD_PROTECT);
        when(analyticsProvider.shouldShowOptOutRationale(any())).thenReturn(false);
        when(prefs.getBoolean(eq(PrefsActivity.PREF_SHOW_GAMIFICATION_EVENTS), anyBoolean())).thenReturn(false);
        when(prefs.getBoolean(eq(PrefsActivity.PREF_LOGOUT_ENABLED), anyBoolean())).thenReturn(true);

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {

            performValidLogin("username-1");
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_courses, 0);
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 4);
            waitForView(withText(R.string.password_needed)).check(matches(isDisplayed()));
            waitForView(withId(R.id.section_password_field)).perform(typeText(PASSWORD_INITIAL), closeSoftKeyboard());
            waitForView(withId(R.id.submit_password)).perform(click());
            waitForView(withText(R.string.password_needed)).check(matches(not(isDisplayed())));

            pressBack();
            pressBack();

            performLogout(); // NO FUNCIONA! AL HACER LOGOUT PARECE QUE SE BORRA EL CURSO DE LA MEMORIA
            performValidLogin("username-1");
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_courses, 0);
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 4);
            waitForView(withText(R.string.password_needed)).check(matches(not(isDisplayed())));

        }
    }


    @Ignore
    @Test
    public void checkTopicLockedWhenLogoutLoginDifferentUser() throws Exception {
        installCourse(COURSE_PASSWORD_PROTECT);
        when(analyticsProvider.shouldShowOptOutRationale(any())).thenReturn(false);
        when(prefs.getBoolean(eq(PrefsActivity.PREF_SHOW_GAMIFICATION_EVENTS), anyBoolean())).thenReturn(false);
        when(prefs.getBoolean(eq(PrefsActivity.PREF_LOGOUT_ENABLED), anyBoolean())).thenReturn(true);

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {

            performValidLogin("username-1");
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_courses, 0);
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 4);
            waitForView(withText(R.string.password_needed)).check(matches(isDisplayed()));
            waitForView(withId(R.id.section_password_field)).perform(typeText(PASSWORD_INITIAL), closeSoftKeyboard());
            waitForView(withId(R.id.submit_password)).perform(click());
            waitForView(withText(R.string.password_needed)).check(matches(not(isDisplayed())));

            pressBack();
            pressBack();

            performLogout();
            performValidLogin("username-2");
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_courses, 0);
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 4);
            waitForView(withText(R.string.password_needed)).check(matches(isDisplayed()));

        }
    }

    @Test
    public void dontShowLockedTopicsInSearchResults() throws Exception {
        installCourse(COURSE_PASSWORD_PROTECT);

        try (ActivityScenario<SearchActivity> scenario = ActivityScenario.launch(SearchActivity.class)) {
            waitForView(withId(R.id.search_string)).perform(typeText("omicron"), closeSoftKeyboard());
            waitForView(withId(R.id.searchbutton)).perform(click());

            waitForView(withId(R.id.recycler_results_search)).check(matches(hasChildCount(2)));
        }
    }

    @Test
    public void showUnlockedTopicsInSearchResults() throws Exception {
        installCourse(COURSE_PASSWORD_PROTECT);
        setCourseSectionUnlockPassword(2, PASSWORD_INITIAL);
        setCourseSectionUnlockPassword(3, PASSWORD_INITIAL);

        try (ActivityScenario<SearchActivity> scenario = ActivityScenario.launch(SearchActivity.class)) {
            waitForView(withId(R.id.search_string)).perform(typeText("omicron"), closeSoftKeyboard());
            waitForView(withId(R.id.searchbutton)).perform(click());

            waitForView(withId(R.id.recycler_results_search)).check(new RecyclerViewItemCountAssertion(4));
        }
    }

    @Test
    public void showPasswordScreenWhenRetakingQuizAndPasswordNeeded() {

        QuizAttempt qa = new QuizAttempt();
        qa.setCourseId(COURSE_ID);
        qa.setUserId(USER_ID_NONE);
        qa.setActivityDigest("adc1c509b957cf2398dec407360ce5e8");

        Intent intent = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), QuizAttemptActivity.class);
        intent.putExtra(QuizAttempt.TAG, qa);
        intent.putExtra(CourseQuizAttemptsActivity.SHOW_ATTEMPT_BUTTON, true);

        installCourse(COURSE_PASSWORD_PROTECT);

        try (ActivityScenario<QuizAttemptActivity> scenario = ActivityScenario.launch(intent)) {
            waitForView(withId(R.id.retake_quiz_btn)).perform(click());
            waitForView(withText(R.string.password_needed)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void showPasswordScreenEnteringThroughWeblink() {
        installCourse(COURSE_PASSWORD_PROTECT);
        when(analyticsProvider.shouldShowOptOutRationale(any())).thenReturn(false);
        doAnswer(invocation -> "test").when(user).getUsername();

        String digest = "16da6f6a8a957fa7e04add194e15d8b2";
        Intent startIntent = CourseUtils.getIntentForDigest(digest);
        try (ActivityScenario<ViewDigestActivity> scenario = ActivityScenario.launch(startIntent)) {

            waitForView(withText(R.string.password_needed)).check(matches(isDisplayed()));
        }

    }
}
