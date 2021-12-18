package androidTestFiles.UI;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isOpen;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidTestFiles.TestRules.DaggerInjectMockUITest;
import androidTestFiles.Utils.CourseUtils;
import androidTestFiles.Utils.TestUtils;

@RunWith(Parameterized.class)
public class AdminProtectedUITest extends DaggerInjectMockUITest {

    public static final int PROTECTION_OPTION_ADMIN_AND_ACTION = 0;
    public static final int PROTECTION_OPTION_ONLY_ACTION = 1;
    public static final int PROTECTION_OPTION_ONLY_ADMIN = 2;

    private final int adminProtectionOption;

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

        when(prefs.getString(eq(PrefsActivity.PREF_ADMIN_PASSWORD), anyString())).thenReturn("testpass");

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

    private void fillPasswordDialog() {
        onView(withId(R.id.admin_password_field)).perform(clearText(), typeText("testpass"));
        onView(withText(R.string.ok)).perform(click());
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
        assertEquals(activity, TestUtils.getCurrentActivity().getClass());
    }

    // --- TESTS ---


    @Test
    public void checkAdminProtectionToEnterSettingsScreen() throws Exception {

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

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

    }

    @Test
    public void checkAdminProtectionOnPrefsCheckboxAdminProtectionClick() throws Exception {

        try (ActivityScenario<PrefsActivity> scenario = ActivityScenario.launch(PrefsActivity.class)) {

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefSecurity_title)),
                            click()));

            if (adminProtectionOption == PROTECTION_OPTION_ADMIN_AND_ACTION) {
                fillPasswordDialog();
            }

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
    }

    @Test
    public void checkAdminProtectionOnPrefsEditTextChangeAdminPassClick() throws Exception {

        try (ActivityScenario<PrefsActivity> scenario = ActivityScenario.launch(PrefsActivity.class)) {

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefSecurity_title)),
                            click()));

            if (adminProtectionOption == PROTECTION_OPTION_ADMIN_AND_ACTION) {
                fillPasswordDialog();
            }

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefAdminPassword)),
                            click()));

            switch (adminProtectionOption) {

                case PROTECTION_OPTION_ADMIN_AND_ACTION:
                    checkAdminPasswordDialogIsDisplayed();
                    break;
                case PROTECTION_OPTION_ONLY_ACTION:
                    onView(withId(android.R.id.edit)).check(doesNotExist());
                    break;
                case PROTECTION_OPTION_ONLY_ADMIN:
                    checkAdminPasswordDialogIsDisplayed();
                    break;
            }
        }
    }

    @Test
    public void checkAdminProtectionOnPrefsEditTextChangeServerEndpointClick() throws Exception {

        try (ActivityScenario<PrefsActivity> scenario = ActivityScenario.launch(PrefsActivity.class)) {

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefAdvanced_title)),
                            click()));

            if (adminProtectionOption == PROTECTION_OPTION_ADMIN_AND_ACTION) {
                checkAdminPasswordDialogIsDisplayed();
                // Advances settings admin pass
                return;
            }

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefServer)),
                            click()));

            switch (adminProtectionOption) {

                case PROTECTION_OPTION_ONLY_ACTION:
                    checkAdminPasswordDialogIsNOTDisplayed();
                    break;
                case PROTECTION_OPTION_ONLY_ADMIN:
                    checkAdminPasswordDialogIsNOTDisplayed();
                    break;
            }
        }
    }


    @Test
    public void checkAdminProtectionOnAdvancedSettingsClick() throws Exception {

        try (ActivityScenario<PrefsActivity> scenario = ActivityScenario.launch(PrefsActivity.class)) {

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefAdvanced_title)),
                            click()));

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

    @Test
    public void checkAdminProtectionOnManageCoursesClick() throws Exception {
        givenThereAreSomeCourses(0);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

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
    }



    @Test
    public void checkAdminProtectionOnCourseOptionsClick() throws Exception {

        givenThereAreSomeCourses(3);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

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
    }

    private void clickOptionInCourseContextMenu(int optionId) {

        Espresso.onView(ViewMatchers.withId(R.id.recycler_courses))
//                .inRoot(RootMatchers.withDecorView(
//                        Matchers.is(mainActivityTestRule.getActivity().getWindow().getDecorView())))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, longClick()));

        onView(withId(optionId))
                .perform(click());
    }



    @Test
    public void checkAdminProtectionOnGlobalScorecardManageCoursesClick() throws Exception {
        givenThereAreSomeCourses(0);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

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


    private void setCheckBoxPreferenceInitialValue(String key, boolean value) {
        PreferenceManager.getDefaultSharedPreferences(
                InstrumentationRegistry.getInstrumentation().getTargetContext())
                .edit().putBoolean(key, value).commit();
    }

    @Test
    public void checkAdminProtectionOnDisableNotifications() throws Exception {

        try (ActivityScenario<PrefsActivity> scenario = ActivityScenario.launch(PrefsActivity.class)) {

            setCheckBoxPreferenceInitialValue(PrefsActivity.PREF_DISABLE_NOTIFICATIONS, false);

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefNotifications_title)),
                            click()));

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefDisableNotifications)),
                            click()));

            checkStandardAdminPrompt();
        }
    }

    @Test
    public void checkAdminProtectionOnEnableNotifications() throws Exception {

        try (ActivityScenario<PrefsActivity> scenario = ActivityScenario.launch(PrefsActivity.class)) {

            setCheckBoxPreferenceInitialValue(PrefsActivity.PREF_DISABLE_NOTIFICATIONS, true);

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefNotifications_title)),
                            click()));

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefDisableNotifications)),
                            click()));

            switch (adminProtectionOption) {

                case PROTECTION_OPTION_ADMIN_AND_ACTION:
                    checkAdminPasswordDialogIsNOTDisplayed();
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


    @Test
    public void checkAdminProtectionOnDisableReminderNotifications() throws Exception {

        try (ActivityScenario<PrefsActivity> scenario = ActivityScenario.launch(PrefsActivity.class)) {

            setCheckBoxPreferenceInitialValue(PrefsActivity.PREF_COURSES_REMINDER_ENABLED, true);

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefNotifications_title)),
                            click()));

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefCoursesReminderEnabled)),
                            click()));

            checkStandardAdminPrompt();
        }
    }

    @Test
    public void checkAdminProtectionOnEnableReminderNotifications() throws Exception {

        try (ActivityScenario<PrefsActivity> scenario = ActivityScenario.launch(PrefsActivity.class)) {

            setCheckBoxPreferenceInitialValue(PrefsActivity.PREF_COURSES_REMINDER_ENABLED, false);

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefNotifications_title)),
                            click()));

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefCoursesReminderEnabled)),
                            click()));

            switch (adminProtectionOption) {

                case PROTECTION_OPTION_ADMIN_AND_ACTION:
                    checkAdminPasswordDialogIsNOTDisplayed();
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



    @Test
    public void checkAdminProtectionReminderInterval() throws Exception {
        checkAdminProtectionReminderItem(R.string.prefCoursesReminderIntervalTitle);
    }

    @Test
    public void checkAdminProtectionReminderDays() throws Exception {
        checkAdminProtectionReminderItem(R.string.prefCoursesReminderDaysTitle);
    }

    @Test
    public void checkAdminProtectionReminderTime() throws Exception {
        checkAdminProtectionReminderItem(R.string.prefCoursesReminderTimeTitle);
    }

    public void checkAdminProtectionReminderItem(int titleStringId) throws Exception {

        try (ActivityScenario<PrefsActivity> scenario = ActivityScenario.launch(PrefsActivity.class)) {

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefNotifications_title)),
                            click()));

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(titleStringId)),
                            click()));

            checkStandardAdminPrompt();
        }

    }

    private void checkStandardAdminPrompt() {

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
