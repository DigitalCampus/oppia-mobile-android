package androidTestFiles.features.updateActivityOnLogin;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.pressBackUnconditionally;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static androidTestFiles.features.authentication.login.LoginUITest.VALID_LOGIN_RESPONSE;
import static androidTestFiles.utils.UITestActionsUtils.waitForView;
import static androidTestFiles.utils.parent.BaseTest.COURSE_SINGLE_PAGE;
import static androidTestFiles.utils.parent.BaseTest.PATH_COMMON_TESTS;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.fragments.CoursesListFragment;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.utils.ConnectionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import java.util.concurrent.TimeUnit;

import androidTestFiles.database.TestDBHelper;
import androidTestFiles.utils.CourseUtils;
import androidTestFiles.utils.MockUtils;
import androidTestFiles.utils.TestUtils;
import androidTestFiles.utils.assertions.CircularProgressbarAssertion;
import androidTestFiles.utils.parent.BaseTest;
import androidTestFiles.utils.parent.MockedApiEndpointTest;

public class UpdateActivityOnLoginUiTests extends MockedApiEndpointTest {


    private static final String VALID_COURSE_ACTIVITY_RESPONSE = BaseTest.PATH_RESPONSES + "/response_200_course_activity.xml";


    @Mock
    SharedPreferences prefs;
    @Mock
    SharedPreferences.Editor editor;
    @Mock(answer = Answers.CALLS_REAL_METHODS)
    CoursesRepository coursesRepository;
    @Mock
    ConnectionUtils connectionUtils;

    private Context context;
    private TestDBHelper testDBHelper;

    private void initMockEditor() {
        when(editor.remove(anyString())).thenReturn(editor);
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);
    }

    @Before
    public void setUp() throws Exception {
        CourseUtils.cleanUp();
        initMockEditor();
        when(prefs.edit()).thenReturn(editor);

        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        when(connectionUtils.isConnected(any())).thenReturn(true);

        testDBHelper = new TestDBHelper(InstrumentationRegistry.getInstrumentation().getTargetContext());
        testDBHelper.setUp();

    }

    @After
    public void tearDown() throws Exception {
        CourseUtils.cleanUp();
        testDBHelper.tearDown();
    }


    private void setOption(int optionStringId) {
        String option = context.getString(optionStringId);
        when(prefs.getString(eq(PrefsActivity.PREF_UPDATE_ACTIVITY_ON_LOGIN), anyString())).thenReturn(option);
    }


    private void launchCoursesListFragment(boolean firstLogin) {
        Bundle args = new Bundle();
        args.putBoolean(MainActivity.EXTRA_FIRST_LOGIN, firstLogin);
        launchInContainer(CoursesListFragment.class, args, R.style.Oppia_ToolbarTheme);
    }

    private void setMockCourse() {

        installCourse(PATH_COMMON_TESTS, COURSE_SINGLE_PAGE);

    }




    @Test
    public void checkExtraParameterIfFirstLogin() {
        Intents.init();
        startServer(200, VALID_LOGIN_RESPONSE, 0);
        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {

            waitForView(withId(R.id.welcome_login))
                    .perform(scrollTo(), click());

            waitForView(withId(R.id.login_username_field))
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("valid_username"));

            waitForView(withId(R.id.login_password_field))
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("valid_password"));

            waitForView(withId(R.id.login_btn))
                    .perform(closeSoftKeyboard(), scrollTo(), click());

            intended(allOf(
                    hasComponent(MainActivity.class.getName()),
                    hasExtra(MainActivity.EXTRA_FIRST_LOGIN, true)));
        }
        Intents.release();
    }

    @Test
    public void checkExtraParameterIfNotFirstLogin() {

        Intents.init();
        startServer(200, VALID_LOGIN_RESPONSE, 0);
        testDBHelper.getTestDataManager().addUsers();
        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {

            waitForView(withId(R.id.welcome_login))
                    .perform(scrollTo(), click());

            waitForView(withId(R.id.login_username_field))
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("user1"));

            waitForView(withId(R.id.login_password_field))
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("password"));

            waitForView(withId(R.id.login_btn))
                    .perform(closeSoftKeyboard(), scrollTo(), click());

            intended(allOf(
                    hasComponent(MainActivity.class.getName()),
                    hasExtra(MainActivity.EXTRA_FIRST_LOGIN, false)));
        }
        Intents.release();
    }


    private void prepareCircularProgressbarTests() {

        testDBHelper.getTestDataManager().addUsers();
        App.getPrefs(InstrumentationRegistry.getInstrumentation().getTargetContext()).edit().putString(PrefsActivity.PREF_USER_NAME, "user1").apply();
        setMockCourse();
        startServer(200, VALID_COURSE_ACTIVITY_RESPONSE);
    }

    @Test
    public void updateActivityIfFirstLoginAndUpdateValueOptional() {

        prepareCircularProgressbarTests();
        setOption(R.string.update_activity_on_login_value_optional);
        launchCoursesListFragment(true);

            await().atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(
                            () ->
                                    waitForView(ViewMatchers.withId(R.id.circularProgressBar))
                                            .check(CircularProgressbarAssertion.withProgress(100))
                    );
        waitForView(withId(R.id.circularProgressBar)).check(CircularProgressbarAssertion.withProgress(100));

    }


    @Test
    public void updateActivityIfFirstLoginAndUpdateValueForce() {

        prepareCircularProgressbarTests();
        setOption(R.string.update_activity_on_login_value_force);
        launchCoursesListFragment(true);

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(
                        () ->
                                waitForView(ViewMatchers.withId(R.id.circularProgressBar))
                                        .check(CircularProgressbarAssertion.withProgress(100))
                );

        waitForView(withId(R.id.circularProgressBar)).check(CircularProgressbarAssertion.withProgress(100));
    }

    @Test
    public void dontUpdateActivityIfFirstLoginAndUpdateValueNone() {

        prepareCircularProgressbarTests();
        setOption(R.string.update_activity_on_login_value_none);
        launchCoursesListFragment(true);

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(
                        () ->
                                waitForView(ViewMatchers.withId(R.id.circularProgressBar))
                                        .check(CircularProgressbarAssertion.withProgress(0))
                );

        waitForView(withId(R.id.circularProgressBar)).check(CircularProgressbarAssertion.withProgress(0));
    }

    @Test
    public void dontUpdateActivityIfNotFirstLoginAndUpdateValueOptional() {

        prepareCircularProgressbarTests();
        setOption(R.string.update_activity_on_login_value_optional);
        launchCoursesListFragment(false);

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(
                        () ->
                                waitForView(ViewMatchers.withId(R.id.circularProgressBar))
                                        .check(CircularProgressbarAssertion.withProgress(0))
                );

        waitForView(withId(R.id.circularProgressBar)).check(CircularProgressbarAssertion.withProgress(0));

    }

    @Test
    public void dontUpdateActivityIfNotFirstLoginAndUpdateValueForce() {

        prepareCircularProgressbarTests();
        setOption(R.string.update_activity_on_login_value_force);
        launchCoursesListFragment(false);

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(
                        () ->
                                waitForView(ViewMatchers.withId(R.id.circularProgressBar))
                                        .check(CircularProgressbarAssertion.withProgress(0))
                );

        waitForView(withId(R.id.circularProgressBar)).check(CircularProgressbarAssertion.withProgress(0));

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void checkErrorMessageWhenNoConnectionAndUpdateValueOptional() {

        MockUtils.givenThereAreSomeCourses(coursesRepository, 2);

        when(connectionUtils.isConnected(any())).thenReturn(false);

        setOption(R.string.update_activity_on_login_value_optional);

        launchCoursesListFragment(true);

        waitForView(withText(R.string.connection_unavailable_couse_activity))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        waitForView(withText(R.string.continue_anyway))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void checkErrorMessageWhenNoConnectionAndUpdateValueForce() {

        MockUtils.givenThereAreSomeCourses(coursesRepository, 2);

        when(connectionUtils.isConnected(any())).thenReturn(false);

        setOption(R.string.update_activity_on_login_value_force);

        launchCoursesListFragment(true);

        waitForView(withText(R.string.connection_unavailable_couse_activity))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        waitForView(withText(R.string.continue_anyway))
                .inRoot(isDialog())
                .check(doesNotExist());
    }

    @Test
    public void checkErrorMessageWhenRequestFailsAndUpdateValueOptional() {

        MockUtils.givenThereAreSomeCourses(coursesRepository, 2);

        setOption(R.string.update_activity_on_login_value_optional);

        startServer(400, null);

        launchCoursesListFragment(true);

        waitForView(withText(R.string.error_unable_retrieve_course_activity))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        waitForView(withText(R.string.continue_anyway))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }


    @Test
    public void checkErrorMessageWhenRequestFailsAndUpdateValueForce() {

        MockUtils.givenThereAreSomeCourses(coursesRepository, 2);

        setOption(R.string.update_activity_on_login_value_force);

        startServer(400, null);

        launchCoursesListFragment(true);

        waitForView(withText(R.string.error_unable_retrieve_course_activity))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        waitForView(withText(R.string.continue_anyway))
                .inRoot(isDialog())
                .check(doesNotExist());
    }


    @Test
    public void checkAutoLogoutWhenChangeUpdateActivityOnLoginOption() throws InterruptedException {

        launchCheckAutologoutProcess(context.getString(R.string.update_activity_on_login_value_none),
                context.getString(R.string.force), WelcomeActivity.class, true);

        launchCheckAutologoutProcess(context.getString(R.string.update_activity_on_login_value_none),
                context.getString(R.string.optional), WelcomeActivity.class, true);

        launchCheckAutologoutProcess(context.getString(R.string.update_activity_on_login_value_optional),
                context.getString(R.string.force), MainActivity.class, false);

        launchCheckAutologoutProcess(context.getString(R.string.update_activity_on_login_value_optional),
                context.getString(R.string.none), MainActivity.class, false);

    }

    public void launchCheckAutologoutProcess(String initialValue, String finalOptionText,
                                             Class expectedActivityClass, boolean closeWarningDialog) {

        if (!BuildConfig.MENU_ALLOW_SETTINGS) {
            return;
        }

        when(prefs.getString(eq(PrefsActivity.PREF_USER_NAME), anyString())).thenReturn("test_user");
        when(prefs.getBoolean(eq(PrefsActivity.PREF_ADMIN_PROTECTION), anyBoolean())).thenReturn(false);
        when(prefs.getString(eq(PrefsActivity.PREF_TEST_ACTION_PROTECTED), anyString())).thenReturn("false");
        when(prefs.getString(eq(PrefsActivity.PREF_UPDATE_ACTIVITY_ON_LOGIN), anyString())).thenReturn(initialValue);

        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(
                PrefsActivity.PREF_UPDATE_ACTIVITY_ON_LOGIN, initialValue).apply();

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            openDrawer();

            waitForView(withText(R.string.menu_settings)).perform(click());

            waitForView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(
                            hasDescendant(withText(R.string.prefAdvanced_title)), click()));

            waitForView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(
                            hasDescendant(withText(R.string.pref_update_activity_on_login)), click()));

            waitForView(withText(finalOptionText)).perform(click());

            if (closeWarningDialog) {
                waitForView(withText(R.string.accept))
                        .inRoot(isDialog())
                        .check(matches(isDisplayed()))
                        .perform(click());
            }

            pressBackUnconditionally();
            pressBackUnconditionally();

            assertEquals(expectedActivityClass, TestUtils.getCurrentActivity().getClass());

        }

    }

}
