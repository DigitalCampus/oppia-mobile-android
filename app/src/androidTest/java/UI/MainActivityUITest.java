package UI;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.android.material.tabs.TabLayout;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.AboutActivity;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.activity.EditProfileActivity;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.SearchActivity;
import org.digitalcampus.oppia.activity.StartUpActivity;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.di.AppModule;
import org.digitalcampus.oppia.model.Badge;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.CompleteCourseProvider;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.Points;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.ParseCourseXMLTask;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;

import Utils.CourseUtils;
import it.cosenonjaviste.daggermock.DaggerMockRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isOpen;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class MainActivityUITest {


    @Rule
    public DaggerMockRule<AppComponent> daggerRule =
            new DaggerMockRule<>(AppComponent.class, new AppModule((App) InstrumentationRegistry.getInstrumentation()
                    .getTargetContext()
                    .getApplicationContext())).set(
                    component -> {
                        App app =
                                (App) InstrumentationRegistry.getInstrumentation()
                                        .getTargetContext()
                                        .getApplicationContext();
                        app.setComponent(component);
                    });

    @Rule
    public ActivityTestRule<MainActivity> mainActivityTestRule =
            new ActivityTestRule<>(MainActivity.class, false, false);


    @Mock
    CoursesRepository coursesRepository;
    @Mock
    CompleteCourseProvider completeCourseProvider;
    @Mock
    SharedPreferences prefs;
    @Mock
    SharedPreferences.Editor editor;
    @Mock
    User user;
    @Mock
    ArrayList<Points> pointList;
    @Mock
    ArrayList<Badge> badgeList;

    @Before
    public void setUp() throws Exception {
        initMockEditor();
        when(prefs.edit()).thenReturn(editor);
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

        when(coursesRepository.getCourses(any())).thenReturn(courses);

    }

    private int getCoursesCount() {
        return coursesRepository.getCourses(any()).size();
    }


    @Test
    public void showsManageCoursesButtonIfThereAreNoCourses() throws Exception {
        givenThereAreSomeCourses(0);

        mainActivityTestRule.launchActivity(null);

        onView(withId(R.id.manage_courses_btn))
                .check(matches(isDisplayed()));
    }

    @Test
    public void doesNotShowManageCoursesButtonIfThereAreCourses() throws Exception {
        givenThereAreSomeCourses(2);

        mainActivityTestRule.launchActivity(null);

        onView(withId(R.id.manage_courses_btn))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void showsTagSelectActivityOnClickManageCourses() throws Exception {

        givenThereAreSomeCourses(0);

        mainActivityTestRule.launchActivity(null);

        onView(withId(R.id.manage_courses_btn))
                .perform(click());

        checkCorrectActivity(TagSelectActivity.class);

    }

    @Test
    public void showsCourseIndexOnCourseClick() throws Exception {

        final CompleteCourse completeCourse = CourseUtils.createMockCompleteCourse(5, 7);
        Mockito.doAnswer((Answer<Void>) invocation -> {
            Context ctx = (Context) invocation.getArguments()[0];
            ((ParseCourseXMLTask.OnParseXmlListener) ctx).onParseComplete(completeCourse);

            return null;

        }).when(completeCourseProvider).getCompleteCourseAsync(any(Context.class), any(Course.class));

        givenThereAreSomeCourses(1);

        mainActivityTestRule.launchActivity(null);

        onView(withId(R.id.recycler_courses))
                .inRoot(RootMatchers.withDecorView(
                        Matchers.is(mainActivityTestRule.getActivity().getWindow().getDecorView())))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));


        checkCorrectActivity(CourseIndexActivity.class);
    }

    @Test
    public void showsContextMenuOnCourseLongClick() throws Exception {
        givenThereAreSomeCourses(1);

        mainActivityTestRule.launchActivity(null);

        Espresso.onView(ViewMatchers.withId(R.id.recycler_courses))
                .inRoot(RootMatchers.withDecorView(
                        Matchers.is(mainActivityTestRule.getActivity().getWindow().getDecorView())))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, longClick()));

        onView(withChild(withId(R.id.course_context_reset)))
                .check(matches(isDisplayed()));

        onView(withChild(withId(R.id.course_context_update_activity)))
                .check(matches(isDisplayed()));

        onView(withChild(withId(R.id.course_context_delete)))
                .check(matches(isDisplayed()));

    }

    @Test
    public void showsCurrentActivityOnLogoutClickNo() throws Exception {


        when(prefs.getBoolean(eq(PrefsActivity.PREF_LOGOUT_ENABLED), anyBoolean())).thenReturn(true);

        mainActivityTestRule.launchActivity(null);

        openDrawer();

        onView(withId(R.id.btn_expand_profile_options)).perform(click());

        onView(withText(R.string.menu_logout)).perform(click());

        onView(withText(R.string.no)).perform(click());

        checkCorrectActivity(MainActivity.class);

    }

    @Test
    public void showsWelcomeActivityOnLogoutClickYes() throws Exception {

        when(prefs.getBoolean(eq(PrefsActivity.PREF_LOGOUT_ENABLED), anyBoolean())).thenReturn(true);

        mainActivityTestRule.launchActivity(null);

        openDrawer();

        onView(withId(R.id.btn_expand_profile_options)).perform(click());

        onView(withText(R.string.menu_logout))
                .perform(click());

        onView(withText(R.string.yes))
                .perform(click());

        checkCorrectActivities(WelcomeActivity.class, StartUpActivity.class);

    }

    @Test
    public void doesNotShowLogoutItemOnPrefsValueFalse() throws Exception {

        when(prefs.getBoolean(eq(PrefsActivity.PREF_LOGOUT_ENABLED), anyBoolean())).thenReturn(false);

        mainActivityTestRule.launchActivity(null);

        openDrawer();

        onView(withId(R.id.btn_expand_profile_options)).perform(click());
        onView(withText(R.string.logout)).check(matches(not(isDisplayed())));
    }

    @Test
    public void showsLogoutItemOnPrefsValueTrue() throws Exception {

        when(prefs.getBoolean(eq(PrefsActivity.PREF_LOGOUT_ENABLED), anyBoolean())).thenReturn(true);

        mainActivityTestRule.launchActivity(null);

        openDrawer();

        onView(withId(R.id.btn_expand_profile_options)).perform(click());

        onView(withText(R.string.logout)).check(matches(isDisplayed()));
    }

    @Test
    public void doesNotShowPointsListWhenThereAreNoPoints() throws Exception {

        when(user.getPoints()).thenReturn(0);

        doReturn(true).when(pointList).add((Points) any());

        mainActivityTestRule.launchActivity(null);

        onView(withId(R.id.nav_bottom_points)).perform(click());

        assertEquals(0, pointList.size());

    }

    @Test
    public void doesNotShowBadgesListWhenThereAreNoBadges() throws Exception {

        when(user.getBadges()).thenReturn(0);

        doReturn(true).when(badgeList).add((Badge) any());

        mainActivityTestRule.launchActivity(null);

        onView(withId(R.id.nav_bottom_points)).perform(click());
        onView(withId(R.id.tabs)).perform(selectTabAtPosition(2));

        assertEquals(0, badgeList.size());

    }

    @NonNull
    private static ViewAction selectTabAtPosition(final int position) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return allOf(isDisplayed(), isAssignableFrom(TabLayout.class));
            }

            @Override
            public String getDescription() {
                return "with tab at index" + position;
            }

            @Override
            public void perform(UiController uiController, View view) {
                if (view instanceof TabLayout) {
                    TabLayout tabLayout = (TabLayout) view;
                    TabLayout.Tab tab = tabLayout.getTabAt(position);

                    if (tab != null) {
                        tab.select();
                    }
                }
            }
        };
    }

    @Test
    public void deleteCourseOnContextMenuDeleteClickNo() throws Exception {
        givenThereAreSomeCourses(1);

        when(prefs.getBoolean(eq(PrefsActivity.PREF_DELETE_COURSE_ENABLED), anyBoolean())).thenReturn(true);

        mainActivityTestRule.launchActivity(null);

        Espresso.onView(ViewMatchers.withId(R.id.recycler_courses))
                .inRoot(RootMatchers.withDecorView(
                        Matchers.is(mainActivityTestRule.getActivity().getWindow().getDecorView())))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, longClick()));

        onView(withId(R.id.course_context_delete))
                .perform(click());

        onView(withText(R.string.no))
                .check(matches(isDisplayed()))
                .perform(click());

        //TODO: Check

    }

    @Test
    public void deleteCourseOnContextMenuDeleteClickYes() throws Exception {
        givenThereAreSomeCourses(3);

        when(prefs.getBoolean(eq(PrefsActivity.PREF_DELETE_COURSE_ENABLED), anyBoolean())).thenReturn(true);

        mainActivityTestRule.launchActivity(null);


        Espresso.onView(ViewMatchers.withId(R.id.recycler_courses))
                .inRoot(RootMatchers.withDecorView(
                        Matchers.is(mainActivityTestRule.getActivity().getWindow().getDecorView())))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, longClick()));

        onView(withId(R.id.course_context_delete))
                .perform(click());

        onView(withText(R.string.yes))
                .check(matches(isDisplayed()))
                .perform(click());

        //TODO: Check
    }

    @Test
    public void doesNotDeleteCourseOnContextMenuDelete() throws Exception {
        givenThereAreSomeCourses(3);

        when(prefs.getBoolean(eq(PrefsActivity.PREF_DELETE_COURSE_ENABLED), anyBoolean())).thenReturn(false);

        mainActivityTestRule.launchActivity(null);

        Espresso.onView(ViewMatchers.withId(R.id.recycler_courses))
                .inRoot(RootMatchers.withDecorView(
                        Matchers.is(mainActivityTestRule.getActivity().getWindow().getDecorView())))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, longClick()));

        onView(withId(R.id.course_context_delete))
                .perform(click());

        onView(withText(R.string.course_context_delete))
                .check(doesNotExist());

    }

    @Test
    public void resetCourseOnContextMenuResetClickYes() throws Exception {
        givenThereAreSomeCourses(3);

        mainActivityTestRule.launchActivity(null);

        Espresso.onView(ViewMatchers.withId(R.id.recycler_courses))
                .inRoot(RootMatchers.withDecorView(
                        Matchers.is(mainActivityTestRule.getActivity().getWindow().getDecorView())))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, longClick()));

        onView(withId(R.id.course_context_reset))
                .perform(click());

        onView(withText(R.string.yes))
                .check(matches(isDisplayed()))
                .perform(click());

        //TODO: Check

    }

    @Test
    public void resetCourseOnContextMenuResetClickNo() throws Exception {
        givenThereAreSomeCourses(3);

        mainActivityTestRule.launchActivity(null);

        Espresso.onView(ViewMatchers.withId(R.id.recycler_courses))
                .inRoot(RootMatchers.withDecorView(
                        Matchers.is(mainActivityTestRule.getActivity().getWindow().getDecorView())))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, longClick()));

        onView(withId(R.id.course_context_reset))
                .perform(click());

        onView(withText(R.string.no))
                .check(matches(isDisplayed()))
                .perform(click());

        //TODO: Check

    }

    @Test
    public void updateCourseActivityOnContextMenu() throws Exception {
        givenThereAreSomeCourses(1);

        mainActivityTestRule.launchActivity(null);

        Espresso.onView(ViewMatchers.withId(R.id.recycler_courses))
                .inRoot(RootMatchers.withDecorView(
                        Matchers.is(mainActivityTestRule.getActivity().getWindow().getDecorView())))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, longClick()));

        onView(withChild(withId(R.id.course_context_update_activity)))
                .perform(click());

        onView(withText(containsString(InstrumentationRegistry.getInstrumentation().getTargetContext().getString(R.string.course_updating_success))));

    }


    //Drawer Tests

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

    private void checkCorrectActivities(Class activity1, Class activity2) {
        assertThat(Utils.TestUtils.getCurrentActivity().getClass(), Matchers.either(Matchers.equalTo(activity1)).or(Matchers.equalTo(activity2)));
    }


    @Test
    public void showsTagSelectActivityOnDrawerClickDownloadCourses() throws Exception {

        mainActivityTestRule.launchActivity(null);

        openDrawer();
        performClickDrawerItem(R.id.menu_download);
        checkCorrectActivity(TagSelectActivity.class);

    }

    @Test
    public void showsSearchActivityOnMenuClickSearch() throws Exception {

        mainActivityTestRule.launchActivity(null);

        try {
            onView(withId(R.id.menu_search)).perform(click());
        } catch (NoMatchingViewException e) {
            openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());
            onView(withText(R.string.menu_search)).perform(click());
        }

        checkCorrectActivity(SearchActivity.class);

    }

    @Test
    public void showsChangeLanguageMenuOptionIfACoursesHasMoreThanOneLanguage() throws Exception {

        when(prefs.getBoolean(eq(PrefsActivity.PREF_CHANGE_LANGUAGE_ENABLED), anyBoolean())).thenReturn(true);

        givenThereAreSomeCourses(1);

        coursesRepository.getCourses((Context) any()).get(0).setLangs(new ArrayList<Lang>() {{
            add(new Lang("en", "English"));
            add(new Lang("es", "Spanish"));
        }});

        mainActivityTestRule.launchActivity(null);

        openDrawer();
        onView(withText(R.string.menu_language)).check(matches(isDisplayed()));
    }

    @Test
    public void hidesChangeLanguageMenuOptionIfACoursesHasOnlyOneLanguage() throws Exception {

        when(prefs.getBoolean(eq(PrefsActivity.PREF_CHANGE_LANGUAGE_ENABLED), anyBoolean())).thenReturn(true);

        givenThereAreSomeCourses(1);
        coursesRepository.getCourses((Context) any()).get(0).setLangs(new ArrayList<Lang>() {{
            add(new Lang("en", "English"));
        }});

        mainActivityTestRule.launchActivity(null);

        openDrawer();
        onView(withText(R.string.change_language)).check(doesNotExist());
    }

    @Test
    public void hidesChangeLanguageMenuOptionIfThereAreNoCoursesWithLangs() throws Exception {

        when(prefs.getBoolean(eq(PrefsActivity.PREF_CHANGE_LANGUAGE_ENABLED), anyBoolean())).thenReturn(true);

        givenThereAreSomeCourses(1);

        mainActivityTestRule.launchActivity(null);

        openDrawer();
        performClickDrawerItem(R.id.menu_language);

        onView(withText(R.string.change_language)).check(doesNotExist());
    }


    @Test
    public void hidesChangeLanguageMenuOptionWhenPrefIsDisabled() throws Exception {

        when(prefs.getBoolean(eq(PrefsActivity.PREF_CHANGE_LANGUAGE_ENABLED), anyBoolean())).thenReturn(false);

        givenThereAreSomeCourses(1);

        coursesRepository.getCourses((Context) any()).get(0).setLangs(new ArrayList<Lang>() {{
            add(new Lang("en", "English"));
            add(new Lang("es", "Spanish"));
        }});

        mainActivityTestRule.launchActivity(null);

        openDrawer();
        onView(withText(R.string.change_language)).check(doesNotExist());
    }

    @Test
    public void showsScorecardsOnBottomNavClickScorecard() throws Exception {

        mainActivityTestRule.launchActivity(null);

        onView(withId(R.id.nav_bottom_scorecard)).perform(click());

        onView(allOf(
                withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                withId(R.id.tabs)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void showsPrefsActivityOnDrawerClickSettings() throws Exception {

        mainActivityTestRule.launchActivity(null);

        openDrawer();
        performClickDrawerItem(R.id.menu_settings);
        checkCorrectActivity(PrefsActivity.class);

    }

    @Test
    public void showsAboutActivityOnDrawerClickAbout() throws Exception {

        mainActivityTestRule.launchActivity(null);

        openDrawer();
        performClickDrawerItem(R.id.menu_about);
        checkCorrectActivity(AboutActivity.class);

        onView(allOf(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                withId(R.id.about_versionno))).check(matches(isDisplayed()));
    }

    @Test
    public void showsSearchCoursesActionButtonCoursesListScreen() throws Exception {

        mainActivityTestRule.launchActivity(null);

        onView(withId(R.id.menu_search)).check(matches(isDisplayed()));
        openDrawer();
        performClickDrawerItem(R.id.menu_settings);
        Utils.TestUtils.getCurrentActivity().finish();
        Utils.TestUtils.getCurrentActivity().invalidateOptionsMenu();
        onView(withId(R.id.menu_search)).check(matches(isDisplayed()));

    }

    @Test
    public void hidesSearchCoursesActionButtonOutsideCoursesListScreen() throws Exception {

        mainActivityTestRule.launchActivity(null);

        onView(withId(R.id.nav_bottom_scorecard)).perform(click());
        onView(withId(R.id.menu_search)).check(doesNotExist());
        openDrawer();
        performClickDrawerItem(R.id.menu_settings);
        Utils.TestUtils.getCurrentActivity().finish();
        onView(withId(R.id.menu_search)).check(doesNotExist());

    }

    @Test
    public void returnsToMainScreenWhenBackArrowButtonInCourseIndexScreenIsClicked() throws Exception {


        final CompleteCourse completeCourse = CourseUtils.createMockCompleteCourse(5, 7);
        Mockito.doAnswer((Answer<Void>) invocation -> {
            Context ctx = (Context) invocation.getArguments()[0];
            ((ParseCourseXMLTask.OnParseXmlListener) ctx).onParseComplete(completeCourse);

            return null;

        }).when(completeCourseProvider).getCompleteCourseAsync(any(Context.class), any(Course.class));

        givenThereAreSomeCourses(1);

        mainActivityTestRule.launchActivity(null);

        onView(withId(R.id.recycler_courses))
                .inRoot(RootMatchers.withDecorView(
                        Matchers.is(mainActivityTestRule.getActivity().getWindow().getDecorView())))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));


        checkCorrectActivity(CourseIndexActivity.class);

        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());

        checkCorrectActivity(MainActivity.class);

    }

    @Test
    public void showsEditProfileActivityOnMenuItemClick() throws Exception {

        mainActivityTestRule.launchActivity(null);

        openDrawer();
        onView(withId(R.id.btn_expand_profile_options)).perform(click());
        onView(withText(R.string.edit_profile)).perform(click());
        checkCorrectActivity(EditProfileActivity.class);

    }
}
