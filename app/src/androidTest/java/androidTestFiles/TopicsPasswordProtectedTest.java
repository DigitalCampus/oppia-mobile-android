package androidTestFiles;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertTrue;
import static androidTestFiles.Matchers.EspressoTestsMatchers.withDrawable;
import static androidTestFiles.Matchers.RecyclerViewMatcher.withRecyclerView;
import static androidTestFiles.Utils.CourseUtils.runInstallCourseTask;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Section;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidTestFiles.TestRules.DaggerInjectMockUITest;
import androidTestFiles.Utils.CourseUtils;
import androidTestFiles.Utils.FileUtils;
import androidTestFiles.Utils.MockedApiEndpointTest;
import androidTestFiles.Utils.UITestActionsUtils;
import androidTestFiles.database.TestDBHelper;

@RunWith(AndroidJUnit4.class)
public class TopicsPasswordProtectedTest extends MockedApiEndpointTest {


    private static final String COURSE_PASSWORD_PROTECT = "password-protect-initial.zip";
    private static final String COURSE_PASSWORD_PROTECT_UPDATE = "password-protect-update.zip";

    private static final String PASSWORD_INITIAL = "password";
    private static final String PASSWORD_UPDATE = "newpassword";

    private static final long USER_ID_NONE = -1;
    private static final int COURSE_ID = 1;

    private static final String VALID_LOGIN_REGISTER_RESPONSE = "responses/response_200_register.json";

    private Context context;
    private TestDBHelper testDBHelper;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // First ensure to use in-memory database
        testDBHelper = new TestDBHelper(InstrumentationRegistry.getInstrumentation().getTargetContext());
        testDBHelper.setUp();

        CourseUtils.cleanUp();
    }


    @After
    public void tearDown() throws Exception {
        testDBHelper.tearDown();
        CourseUtils.cleanUp();
    }

    protected void copyCourseFromAssets(String filename) {
        FileUtils.copyZipFromAssetsPath(context, "courses_topics_password_protect", filename);  //Copy course zip from assets to download path
    }

    private Intent getTestCourseIntent() {

//        int courseId = testDBHelper.getDbHelper().getCourseID("ref-course");
        Course course = testDBHelper.getDbHelper().getCourse(COURSE_ID, USER_ID_NONE);

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

            onView(withText(R.string.password_needed)).check(matches(not(isDisplayed())));
        }
    }


    @Test
    public void showPasswordScreenIfTopicIsProtected() throws Exception {

        installCourse(COURSE_PASSWORD_PROTECT);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIntent())) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 4);

            onView(withText(R.string.password_needed)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void dontShowPasswordScreenIfTopicIsUnlocked() throws Exception {

        installCourse(COURSE_PASSWORD_PROTECT);

        setCourseSectionUnlockPassword(2, PASSWORD_INITIAL);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIntent())) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 4);

            onView(withText(R.string.password_needed)).check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void checkPasswordLockedIcons() throws Exception {

        installCourse(COURSE_PASSWORD_PROTECT);

        setCourseSectionUnlockPassword(2, PASSWORD_INITIAL);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIntent())) {

            onView(withRecyclerView(R.id.recycler_course_sections)
                    .atPositionOnView(1, R.id.lock_indicator))
                    .check(matches(not(isDisplayed())));

            onView(withRecyclerView(R.id.recycler_course_sections)
                    .atPositionOnView(3, R.id.lock_indicator))
                    .check(matches(withDrawable(R.drawable.ic_unlock)));
//
            onView(withRecyclerView(R.id.recycler_course_sections)
                    .atPositionOnView(6, R.id.lock_indicator))
                    .check(matches(withDrawable(R.drawable.ic_lock)));
        }
    }

    private void performLogout() {

        onView(withId(R.id.drawer)).perform(DrawerActions.open());
        onView(withId(R.id.btn_expand_profile_options)).perform(click());
        onView(withId(R.id.btn_logout)).perform(click());
        onView(withText(R.string.yes)).perform(click());

    }

    private void performValidLogin(String username) {

        startServer(200, VALID_LOGIN_REGISTER_RESPONSE, 0);
        onView(withId(R.id.welcome_login)).perform(scrollTo(), click());
        onView(withId(R.id.login_username_field)).perform(closeSoftKeyboard(), scrollTo(), typeText(username));
        onView(withId(R.id.login_password_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("valid_password"));
        onView(withId(R.id.login_btn)).perform(scrollTo(), click());
    }

    @Test
    public void checkTopicLockedWhenLogoutLoginSameUser() throws Exception {

        installCourse(COURSE_PASSWORD_PROTECT);

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {

            performValidLogin("username-1");
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_courses, 0);
        }
    }


}
