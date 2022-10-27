package androidTestFiles.features;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBackUnconditionally;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static androidTestFiles.features.authentication.login.LoginUITest.VALID_LOGIN_RESPONSE;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Gravity;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.fragments.CoursesListFragment;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.Section;
import org.digitalcampus.oppia.utils.ConnectionUtils;
import org.digitalcampus.oppia.widgets.QuizWidget;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;

import androidTestFiles.database.TestDBHelper;
import androidTestFiles.utils.CourseUtils;
import androidTestFiles.utils.MockUtils;
import androidTestFiles.utils.TestUtils;
import androidTestFiles.utils.parent.MockedApiEndpointTest;

public class UpdateActivityOnLoginTests extends MockedApiEndpointTest {


    private static final String VALID_COURSE_ACTIVITY_RESPONSE = "responses/response_200_course_activity.xml";


    @Mock
    SharedPreferences prefs;
    @Mock
    SharedPreferences.Editor editor;
    @Mock
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

        MockUtils.givenThereAreSomeCourses(coursesRepository, 2);
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

        Activity activity = new Activity();
        activity.setTitles(new ArrayList<Lang>(){{ add(new Lang("en", "Activity 1")); }});
        activity.setDigest("32ed06745d261f70895950c519dcabbb");
        activity.setActType("page");

        Section section = new Section();
        section.setTitles(new ArrayList<Lang>(){{ add(new Lang("en", "Section 1")); }});
        section.setActivities(Arrays.asList(activity));

        CompleteCourse course = new CompleteCourse();
        course.setShortname("mock-course");
        course.setTitles(new ArrayList<Lang>(){{ add(new Lang("en", "Mock Course")); }});
        course.setSections(Arrays.asList(section));

        when(coursesRepository.getCourses(any())).thenReturn(Arrays.asList(course));
    }




    @Test
    public void checkExtraParameterIfFirstLogin() {
        Intents.init();
        startServer(200, VALID_LOGIN_RESPONSE, 0);
        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {

            onView(withId(R.id.welcome_login))
                    .perform(scrollTo(), click());

            onView(withId(R.id.login_username_field))
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("valid_username"));

            onView(withId(R.id.login_password_field))
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("valid_password"));

            onView(withId(R.id.login_btn))
                    .perform(scrollTo(), click());

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

            onView(withId(R.id.welcome_login))
                    .perform(scrollTo(), click());

            onView(withId(R.id.login_username_field))
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("user1"));

            onView(withId(R.id.login_password_field))
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("password"));

            onView(withId(R.id.login_btn))
                    .perform(scrollTo(), click());

            intended(allOf(
                    hasComponent(MainActivity.class.getName()),
                    hasExtra(MainActivity.EXTRA_FIRST_LOGIN, false)));
        }
        Intents.release();
    }



    @Test
    public void updateActivityIfFirstLoginAndUpdateValueOptional() {

        testDBHelper.getTestDataManager().addUsers();
        App.getPrefs(InstrumentationRegistry.getInstrumentation().getTargetContext()).edit().putString(PrefsActivity.PREF_USER_NAME, "user1").apply();
        setMockCourse();
        setOption(R.string.update_activity_on_login_value_optional);
        startServer(200, VALID_COURSE_ACTIVITY_RESPONSE);
        launchCoursesListFragment(true);
    }


    @Test
    public void updateActivityIfFirstLoginAndUpdateValueForze() {

    }

    @Test
    public void dontUpdateActivityIfFirstLoginAndUpdateValueNone() {

    }

    @Test
    public void dontUpdateActivityIfNotFirstLoginAndUpdateValueOptional() {

        setOption(R.string.update_activity_on_login_value_optional);

        startServer(200, null);

        launchCoursesListFragment(false);

    }


    @Test
    public void avoidUpdateActivityAgainWhenBackToScreen() {

    }


    @Test
    public void checkErrorMessageWhenNoConnectionAndUpdateValueOptional() {

        when(connectionUtils.isConnected(any())).thenReturn(false);

        setOption(R.string.update_activity_on_login_value_optional);

        launchCoursesListFragment(true);

        onView(withText(R.string.connection_unavailable_couse_activity))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withText(R.string.continue_anyway))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void checkErrorMessageWhenNoConnectionAndUpdateValueForce() {

        when(connectionUtils.isConnected(any())).thenReturn(false);

        setOption(R.string.update_activity_on_login_value_force);

        launchCoursesListFragment(true);

        onView(withText(R.string.connection_unavailable_couse_activity))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withText(R.string.continue_anyway))
                .inRoot(isDialog())
                .check(doesNotExist());
    }

    @Test
    public void checkErrorMessageWhenRequestFailsAndUpdateValueOptional() {

        setOption(R.string.update_activity_on_login_value_optional);

        startServer(400, null);

        launchCoursesListFragment(true);

        onView(withText(R.string.error_unable_retrieve_course_activity))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withText(R.string.continue_anyway))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }


    @Test
    public void checkErrorMessageWhenRequestFailsAndUpdateValueForce() {

        setOption(R.string.update_activity_on_login_value_force);

        startServer(400, null);

        launchCoursesListFragment(true);

        onView(withText(R.string.error_unable_retrieve_course_activity))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));

        onView(withText(R.string.continue_anyway))
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
            onView(withId(R.id.drawer))
                    .check(matches(isClosed(Gravity.START)))
                    .perform(DrawerActions.open());

            onView(withText(R.string.menu_settings)).perform(click());

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(
                            hasDescendant(withText(R.string.prefAdvanced_title)), click()));

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(
                            hasDescendant(withText(R.string.pref_update_activity_on_login)), click()));

            onView(withText(finalOptionText)).perform(click());

            if (closeWarningDialog) {
                onView(withText(R.string.accept))
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
