package UI;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.di.AppModule;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.User;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Utils.CourseUtils;
import it.cosenonjaviste.daggermock.DaggerMockRule;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isOpen;
import static androidx.test.espresso.matcher.PreferenceMatchers.withKey;
import static androidx.test.espresso.matcher.PreferenceMatchers.withTitle;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class AdminProtectedUITest {

    public static final int PROTECTION_OPTION_ADMIN_AND_ACTION = 0;
    public static final int PROTECTION_OPTION_ONLY_ACTION = 1;
    public static final int PROTECTION_OPTION_ONLY_ADMIN = 2;

    private final int adminProtectionOption;


    @Rule
    public ActivityTestRule<MainActivity> mainActivityTestRule =
            new ActivityTestRule<>(MainActivity.class, false, false);
    @Rule
    public ActivityTestRule<PrefsActivity> prefsActivityTestRule = new ActivityTestRule<>(PrefsActivity.class, false, false);

    @Rule
    public DaggerMockRule<AppComponent> daggerRule =
            new DaggerMockRule<>(AppComponent.class, new AppModule((App) InstrumentationRegistry.getInstrumentation()
                    .getTargetContext()
                    .getApplicationContext())).set(
                    new DaggerMockRule.ComponentSetter<AppComponent>() {
                        @Override
                        public void setComponent(AppComponent component) {
                            App app =
                                    (App) InstrumentationRegistry.getInstrumentation()
                                            .getTargetContext()
                                            .getApplicationContext();
                            app.setComponent(component);
                        }
                    });

    @Mock
    CoursesRepository coursesRepository;
    @Mock
    SharedPreferences prefs;
    @Mock
    SharedPreferences.Editor editor;
    @Mock
    User user;


    // --- INITIALIZATIONS ---

    public AdminProtectedUITest(int adminProtectionOption) {
        this.adminProtectionOption = adminProtectionOption;
    }

    @Parameterized.Parameters
    public static List<Integer[]> adminProtectionOptions() {
        return Arrays.asList(new Integer[][]{
                new Integer[]{PROTECTION_OPTION_ADMIN_AND_ACTION},
                new Integer[]{PROTECTION_OPTION_ONLY_ACTION},
                new Integer[]{PROTECTION_OPTION_ONLY_ADMIN}});
    }

    @Before
    public void setUp() throws Exception {
        initMockEditor();
        when(prefs.edit()).thenReturn(editor);
        when(user.getPoints()).thenReturn(0);

        switch (adminProtectionOption) {

            case PROTECTION_OPTION_ADMIN_AND_ACTION:
                when(prefs.getBoolean(eq(PrefsActivity.PREF_ADMIN_PROTECTION), anyBoolean())).thenReturn(true);
                when(prefs.getString(eq(PrefsActivity.PREF_TEST_ACTION_PROTECTED), anyString())).thenReturn("true");
                setPrefAdminProtectionValue(true);
                break;

            case PROTECTION_OPTION_ONLY_ACTION:
                when(prefs.getBoolean(eq(PrefsActivity.PREF_ADMIN_PROTECTION), anyBoolean())).thenReturn(false);
                when(prefs.getString(eq(PrefsActivity.PREF_TEST_ACTION_PROTECTED), anyString())).thenReturn("true");
                setPrefAdminProtectionValue(false);
                break;

            case PROTECTION_OPTION_ONLY_ADMIN:
                when(prefs.getBoolean(eq(PrefsActivity.PREF_ADMIN_PROTECTION), anyBoolean())).thenReturn(true);
                when(prefs.getString(eq(PrefsActivity.PREF_TEST_ACTION_PROTECTED), anyString())).thenReturn("false");
                setPrefAdminProtectionValue(true);
                break;
        }
    }

    /**
     * Mock SharedPreferences don't work on PreferenceActivity (or Fragment) so we need to set a real known value to test
     * @param value
     */
    private void setPrefAdminProtectionValue(boolean value) {
        PreferenceManager.getDefaultSharedPreferences(
                InstrumentationRegistry.getInstrumentation().getTargetContext())
                .edit().putBoolean(PrefsActivity.PREF_ADMIN_PROTECTION, value).commit();
    }

    private void initMockEditor() {
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);
    }

    private void givenThereAreSomeCourses(int numberOfCourses) {
        ArrayList<Course> courses = new ArrayList<>();
        for (int i = 0; i < numberOfCourses; i++) {
            courses.add(CourseUtils.createMockCourse());
        }
        when(coursesRepository.getCourses((Context) any())).thenReturn(courses);
    }


    // --- CHECKS ---
    private void checkAdminPasswordDialogIsDisplayed() {
        onView(withText(R.string.admin_password_required))
                .check(matches(isDisplayed()));
    }

    private void checkAdminPasswordDialogIsNOTDisplayed() {
        onView(withText(R.string.admin_password_required))
                .check(doesNotExist());
    }


    private void openDrawer() {
        onView(withId(R.id.drawer))
                .perform(DrawerActions.open());

        onView(withId(R.id.drawer)).check(matches(isOpen()));
    }

    private void performClickDrawerItem(int itemId) {
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(itemId));
    }

    private void checkCorrectActivity(Class activity) {
        assertEquals(activity, Utils.TestUtils.getCurrentActivity().getClass());
    }

    // --- TESTS ---


    @Test
    public void checkAdminProtectionToEnterSettingsScreen() throws Exception {

        mainActivityTestRule.launchActivity(null);

        openDrawer();

        performClickDrawerItem(R.id.menu_settings);

        switch (adminProtectionOption) {

            case PROTECTION_OPTION_ADMIN_AND_ACTION:
                checkAdminPasswordDialogIsDisplayed();
                break;
            case PROTECTION_OPTION_ONLY_ACTION:
                checkCorrectActivity(PrefsActivity.class);
                break;
            case PROTECTION_OPTION_ONLY_ADMIN:
                checkCorrectActivity(PrefsActivity.class);
                break;
        }

    }

    @Test
    public void checkAdminProtectionOnPrefsCheckboxAdminProtectionClick() throws Exception {

        prefsActivityTestRule.launchActivity(null);

        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefSecurity_title)),
                        click()));

        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefAdminProtection)),
                        click()));

        switch (adminProtectionOption) {

            case PROTECTION_OPTION_ADMIN_AND_ACTION:
                checkAdminPasswordDialogIsDisplayed();
                break;
            case PROTECTION_OPTION_ONLY_ACTION:
                checkAdminPasswordDialogIsNOTDisplayed();
                pressBack();
                break;
            case PROTECTION_OPTION_ONLY_ADMIN:
                checkAdminPasswordDialogIsDisplayed();
                break;
        }
    }

    @Test
    public void checkAdminProtectionOnPrefsEditTextChangeAdminPassClick() throws Exception {

        prefsActivityTestRule.launchActivity(null);

        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefSecurity_title)),
                        click()));

        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefAdminPassword)),
                        click()));

        switch (adminProtectionOption) {

            case PROTECTION_OPTION_ADMIN_AND_ACTION:
                onView(withId(android.R.id.edit)).perform(clearText(), typeText("any_pass"), closeSoftKeyboard());
                onView(withId(android.R.id.button1)).perform(click());
                checkAdminPasswordDialogIsDisplayed();
                break;
            case PROTECTION_OPTION_ONLY_ACTION:
                onView(withId(android.R.id.edit)).check(doesNotExist());
                break;
            case PROTECTION_OPTION_ONLY_ADMIN:
                onView(withId(android.R.id.edit)).perform(clearText(), typeText("any_pass"), closeSoftKeyboard());
                onView(withId(android.R.id.button1)).perform(click());
                checkAdminPasswordDialogIsDisplayed();
                break;
        }
    }

    @Test
    public void checkAdminProtectionOnPrefsEditTextChangeServerEndpointClick() throws Exception {

        prefsActivityTestRule.launchActivity(null);

        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefAdvanced_title)),
                        click()));

        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefServer)),
                        click()));

        onView(withId(android.R.id.edit)).perform(clearText(), typeText("http://any.server"), closeSoftKeyboard());
        onView(withId(android.R.id.button1)).perform(click());

        switch (adminProtectionOption) {

            case PROTECTION_OPTION_ADMIN_AND_ACTION:
                checkAdminPasswordDialogIsDisplayed();
                break;
            case PROTECTION_OPTION_ONLY_ACTION:
                checkAdminPasswordDialogIsNOTDisplayed();
                break;
            case PROTECTION_OPTION_ONLY_ADMIN:
                checkAdminPasswordDialogIsDisplayed();
                break;
        }

    }



    @Test
    public void checkAdminProtectionOnManageCoursesClick() throws Exception {
        givenThereAreSomeCourses(0);

        mainActivityTestRule.launchActivity(null);

        onView(withId(R.id.manage_courses_btn))
                .perform(click());

        switch (adminProtectionOption) {

            case PROTECTION_OPTION_ADMIN_AND_ACTION:
                checkAdminPasswordDialogIsDisplayed();
                break;
            case PROTECTION_OPTION_ONLY_ACTION:
                checkAdminPasswordDialogIsNOTDisplayed();
                break;
            case PROTECTION_OPTION_ONLY_ADMIN:
                checkAdminPasswordDialogIsNOTDisplayed();
                break;
        }
    }



    @Test
    public void checkAdminProtectionOnCourseOptionsClick() throws Exception {

        givenThereAreSomeCourses(3);

        mainActivityTestRule.launchActivity(null);


        switch (adminProtectionOption) {

            case PROTECTION_OPTION_ADMIN_AND_ACTION:
                clickOptionInCourseContextMenu(R.id.course_context_reset);
                checkAdminPasswordDialogIsDisplayed();
                pressBack();
                clickOptionInCourseContextMenu(R.id.course_context_delete);
                checkAdminPasswordDialogIsDisplayed();
                pressBack();
                clickOptionInCourseContextMenu(R.id.course_context_update_activity);
                checkAdminPasswordDialogIsDisplayed();
                break;
            case PROTECTION_OPTION_ONLY_ACTION:
                clickOptionInCourseContextMenu(R.id.course_context_reset);
                checkAdminPasswordDialogIsNOTDisplayed();
                pressBack();
                clickOptionInCourseContextMenu(R.id.course_context_delete);
                checkAdminPasswordDialogIsNOTDisplayed();
//                pressBack();
                clickOptionInCourseContextMenu(R.id.course_context_update_activity);
                checkAdminPasswordDialogIsNOTDisplayed();
                break;
            case PROTECTION_OPTION_ONLY_ADMIN:
                clickOptionInCourseContextMenu(R.id.course_context_reset);
                checkAdminPasswordDialogIsNOTDisplayed();
                pressBack();
                clickOptionInCourseContextMenu(R.id.course_context_delete);
                checkAdminPasswordDialogIsNOTDisplayed();
//                pressBack();
                clickOptionInCourseContextMenu(R.id.course_context_update_activity);
                checkAdminPasswordDialogIsNOTDisplayed();
                break;
        }


    }

    private void clickOptionInCourseContextMenu(int optionId) {

        Espresso.onView(ViewMatchers.withId(R.id.recycler_courses))
                .inRoot(RootMatchers.withDecorView(
                        Matchers.is(mainActivityTestRule.getActivity().getWindow().getDecorView())))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, longClick()));

        onView(withId(optionId))
                .perform(click());
    }



    @Test
    public void checkAdminProtectionOnGlobalScorecardManageCoursesClick() throws Exception {
        givenThereAreSomeCourses(0);

        mainActivityTestRule.launchActivity(null);

        onView(withId(R.id.nav_bottom_scorecard)).perform(click());

        onView(withId(R.id.btn_download_courses))
                .perform(click());

        switch (adminProtectionOption) {

            case PROTECTION_OPTION_ADMIN_AND_ACTION:
                checkAdminPasswordDialogIsDisplayed();
                break;
            case PROTECTION_OPTION_ONLY_ACTION:
                checkAdminPasswordDialogIsNOTDisplayed();
                break;
            case PROTECTION_OPTION_ONLY_ADMIN:
                checkAdminPasswordDialogIsNOTDisplayed();
                break;
        }
    }


}
