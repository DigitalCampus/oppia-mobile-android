package UI;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.animation.AnimationSet;

import junit.framework.AssertionFailedError;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.AboutActivity;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.activity.MonitorActivity;
import org.digitalcampus.oppia.activity.OppiaMobileActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.SearchActivity;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.di.AppModule;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.model.Badges;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.CompleteCourseProvider;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.MultiLangInfo;
import org.digitalcampus.oppia.model.Points;
import org.digitalcampus.oppia.model.Section;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.ParseCourseXMLTask;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;

import TestRules.DisableAnimationsRule;
import Utils.CourseUtils;
import it.cosenonjaviste.daggermock.DaggerMockRule;

import static android.app.PendingIntent.getActivity;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerMatchers.isOpen;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class OppiaMobileActivityUITest {


    @Rule public DaggerMockRule<AppComponent> daggerRule =
            new DaggerMockRule<>(AppComponent.class, new AppModule((MobileLearning) InstrumentationRegistry.getInstrumentation()
                                                                                    .getTargetContext()
                                                                                    .getApplicationContext())).set(
                    new DaggerMockRule.ComponentSetter<AppComponent>() {
                        @Override public void setComponent(AppComponent component) {
                            MobileLearning app =
                                    (MobileLearning) InstrumentationRegistry.getInstrumentation()
                                            .getTargetContext()
                                            .getApplicationContext();
                            app.setComponent(component);
                        }
                    });

    @Rule
    public ActivityTestRule<OppiaMobileActivity> oppiaMobileActivityTestRule =
            new ActivityTestRule<>(OppiaMobileActivity.class, false, false);

    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();

    

    @Mock CoursesRepository coursesRepository;
    @Mock CompleteCourseProvider completeCourseProvider;
    @Mock SharedPreferences prefs;
    @Mock SharedPreferences.Editor editor;
    @Mock User user;
    @Mock ArrayList<Points> pointList;
    @Mock ArrayList<Badges> badgesList;

    @Before
    public void setUp() throws Exception{
        initMockEditor();
        when(prefs.edit()).thenReturn(editor);
    }

    private void initMockEditor(){
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
    }

    private void givenThereAreSomeCourses(int numberOfCourses) {

        ArrayList<Course> courses = new ArrayList<>();

        for(int i = 0; i < numberOfCourses; i++){
            courses.add(CourseUtils.createMockCourse());
        }

        when(coursesRepository.getCourses((Context) any())).thenReturn(courses);

    }
    private int getCoursesCount(){
        return coursesRepository.getCourses((Context) any()).size();
    }


    @Test
    public void showsManageCoursesButtonIfThereAreNoCourses() throws Exception{
        givenThereAreSomeCourses(0);

        oppiaMobileActivityTestRule.launchActivity(null);

        onView(withId(R.id.manage_courses_btn))
                .check(matches(isDisplayed()));
    }

    @Test
    public void doesNotShowManageCoursesButtonIfThereAreCourses() throws Exception{
        givenThereAreSomeCourses(2);

        oppiaMobileActivityTestRule.launchActivity(null);

        onView(withId(R.id.manage_courses_btn))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void showsTagSelectActivityOnClickManageCourses() throws Exception{

        givenThereAreSomeCourses(0);

        oppiaMobileActivityTestRule.launchActivity(null);

        onView(withId(R.id.manage_courses_btn))
                .perform(click());

        checkCorrectActivity(TagSelectActivity.class);

    }

    @Test
    public void showsCourseIndexOnCourseClick() throws Exception{

        givenThereAreSomeCourses(1);

        final CompleteCourse completeCourse = CourseUtils.createMockCompleteCourse(5, 7);

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Context ctx = (Context) invocation.getArguments()[0];
                ((ParseCourseXMLTask.OnParseXmlListener) ctx).onParseComplete(completeCourse);
                return null;
            }
        }).when(completeCourseProvider).getCompleteCourseAsync((Context) any(), (Course) any());


        oppiaMobileActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.course_list))
                .atPosition(0)
                .perform(click());

        checkCorrectActivity(CourseIndexActivity.class);

    }

    @Test
    public void showsContextMenuOnCourseLongClick() throws Exception{
        givenThereAreSomeCourses(1);

        oppiaMobileActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.course_list))
                .atPosition(0)
                .perform(longClick());

        onView(withChild(withId(R.id.course_context_reset)))
                .check(matches(isDisplayed()));

        onView(withChild(withId(R.id.course_context_update_activity)))
                .check(matches(isDisplayed()));

        onView(withChild(withId(R.id.course_context_delete)))
                .check(matches(isDisplayed()));

    }

    @Test
    public void showsCurrentActivityOnLogoutClickNo() throws Exception{


        when(prefs.getBoolean(eq(PrefsActivity.PREF_LOGOUT_ENABLED), anyBoolean())).thenReturn(true);

        oppiaMobileActivityTestRule.launchActivity(null);

        openDrawer();

        onView(isAssignableFrom(NavigationView.class)).perform(swipeUp());

        onView(withText(R.string.menu_logout))
                .perform(click());

        onView(withText(R.string.no))
                .perform(click());

        checkCorrectActivity(OppiaMobileActivity.class);

    }

    @Test
    public void showsWelcomeActivityOnLogoutClickYes() throws Exception{

        when(prefs.getBoolean(eq(PrefsActivity.PREF_LOGOUT_ENABLED), anyBoolean())).thenReturn(true);

        oppiaMobileActivityTestRule.launchActivity(null);

        openDrawer();

        onView(isAssignableFrom(NavigationView.class)).perform(swipeUp());

        onView(withText(R.string.menu_logout))
                .perform(click());

        onView(withText(R.string.yes))
                .perform(click());

        checkCorrectActivity(WelcomeActivity.class);

    }

    @Test
    public void doesNotShowLogoutItemOnPrefsValueFalse() throws Exception{

        when(prefs.getBoolean(eq(PrefsActivity.PREF_LOGOUT_ENABLED), anyBoolean())).thenReturn(false);

        oppiaMobileActivityTestRule.launchActivity(null);

        openDrawer();

        onView(isAssignableFrom(NavigationView.class)).perform(swipeUp());

        onView(withText(R.string.logout))
                .check(doesNotExist());
    }

    @Test
    public void showsLogoutItemOnPrefsValueTrue() throws Exception{

        when(prefs.getBoolean(eq(PrefsActivity.PREF_LOGOUT_ENABLED), anyBoolean())).thenReturn(true);

        oppiaMobileActivityTestRule.launchActivity(null);

        openDrawer();

        onView(isAssignableFrom(NavigationView.class)).perform(swipeUp());

        onView(withText(R.string.logout))
                .check(matches(isDisplayed()));
    }

    @Test
    public void doesNotShowPointsListWhenThereAreNoPoints() throws Exception{

        when(user.getPoints()).thenReturn(0);

        doReturn(true).when(pointList).add((Points) any());

        oppiaMobileActivityTestRule.launchActivity(null);

        onView(withId(R.id.userpoints))
                .perform(click());

        assertEquals(0, pointList.size());

        onView(allOf(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                withId(R.id.points_list))).check(matches(not(isDisplayed())));
    }

    @Test
    public void doesNotShowBadgesListWhenThereAreNoBadges() throws Exception{

        when(user.getBadges()).thenReturn(0);

        doReturn(true).when(badgesList).add((Badges) any());

        oppiaMobileActivityTestRule.launchActivity(null);

        onView(withId(R.id.userbadges))
                .perform(click());

        assertEquals(0, badgesList.size());

        onView(withText(R.string.info_no_badges)).check(matches(isDisplayed()));
    }

    @Test
    public void deleteCourseOnContextMenuDeleteClickNo() throws Exception{
        givenThereAreSomeCourses(1);

        when(prefs.getBoolean(eq(PrefsActivity.PREF_DELETE_COURSE_ENABLED), anyBoolean())).thenReturn(true);

        oppiaMobileActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.course_list))
                .atPosition(0)
                .perform(longClick());

        onView(withId(R.id.course_context_delete))
                .perform(click());

        onView(withText(R.string.no))
                .check(matches(isDisplayed()))
                .perform(click());

        //TODO: Check

    }

    @Test
    public void deleteCourseOnContextMenuDeleteClickYes() throws Exception{
        givenThereAreSomeCourses(3);

        when(prefs.getBoolean(eq(PrefsActivity.PREF_DELETE_COURSE_ENABLED), anyBoolean())).thenReturn(true);

        oppiaMobileActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.course_list))
                .atPosition(0)
                .perform(longClick());

        onView(withId(R.id.course_context_delete))
                .perform(click());

        onView(withText(R.string.yes))
                .check(matches(isDisplayed()))
                .perform(click());

        //TODO: Check

    }

    @Test
    public void doesNotDeleteCourseOnContextMenuDelete() throws Exception{
        givenThereAreSomeCourses(3);

        when(prefs.getBoolean(eq(PrefsActivity.PREF_DELETE_COURSE_ENABLED), anyBoolean())).thenReturn(false);

        oppiaMobileActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.course_list))
                .atPosition(0)
                .perform(longClick());

        onView(withId(R.id.course_context_delete))
                .perform(click());

        onView(withText(R.string.course_context_delete))
                .check(doesNotExist());

    }

    @Test
    public void resetCourseOnContextMenuResetClickYes() throws Exception{
        givenThereAreSomeCourses(3);

        oppiaMobileActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.course_list))
                .atPosition(0)
                .perform(longClick());

        onView(withId(R.id.course_context_reset))
                .perform(click());

        onView(withText(R.string.yes))
                .check(matches(isDisplayed()))
                .perform(click());

        //TODO: Check

    }

    @Test
    public void resetCourseOnContextMenuResetClickNo() throws Exception{
        givenThereAreSomeCourses(3);

        oppiaMobileActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.course_list))
                .atPosition(0)
                .perform(longClick());

        onView(withId(R.id.course_context_reset))
                .perform(click());

        onView(withText(R.string.no))
                .check(matches(isDisplayed()))
                .perform(click());

        //TODO: Check

    }

    @Test
    public void updateCourseActivityOnContextMenu() throws Exception{
        givenThereAreSomeCourses(1);

        oppiaMobileActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.course_list))
                .atPosition(0)
                .perform(longClick());

        onView(withChild(withId(R.id.course_context_update_activity)))
                .perform(click());

        onView(withText(containsString(InstrumentationRegistry.getTargetContext().getString(R.string.course_updating_success))));

    }


    //Drawer Tests

    private void openDrawer(){
        onView(withId(R.id.drawer))
                .perform(DrawerActions.open());

        onView(withId(R.id.drawer)).check(matches(isOpen()));
    }

    private void performClickDrawerItem(int itemId){
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(itemId));
    }

    private void checkCorrectActivity(Class activity){
        assertEquals(activity, Utils.TestUtils.getCurrentActivity().getClass());
    }


    @Test
    public void showsTagSelectActivityOnDrawerClickDownloadCourses() throws Exception{

        oppiaMobileActivityTestRule.launchActivity(null);

        openDrawer();
        performClickDrawerItem(R.id.menu_download);
        checkCorrectActivity(TagSelectActivity.class);

    }

    @Test
    public void showsSearchActivityOnDrawerClickSearch() throws Exception{

        oppiaMobileActivityTestRule.launchActivity(null);

        openDrawer();
        performClickDrawerItem(R.id.menu_search);
        checkCorrectActivity(SearchActivity.class);

    }

    @Test
    public void showsChangeLanguageDialogIfACourseHasAtLeastOneLang() throws Exception{

        givenThereAreSomeCourses(1);
        MultiLangInfo mli = new MultiLangInfo();

        mli.setLangs(new ArrayList<Lang>(){{
            add(new Lang("en", "English"));
        }});

        coursesRepository.getCourses((Context) any()).get(0).setMultiLangInfo(mli);

        oppiaMobileActivityTestRule.launchActivity(null);

        openDrawer();
        performClickDrawerItem(R.id.menu_language);

        onView(withText(R.string.change_language)).check(matches(isDisplayed()));
    }

    @Test
    public void doesNotShowChangeLanguageDialogIfThereAreNoCoursesWithLangs() throws Exception{

        givenThereAreSomeCourses(1);

        oppiaMobileActivityTestRule.launchActivity(null);

        openDrawer();
        performClickDrawerItem(R.id.menu_language);

        ViewInteraction dialog = onView(withText(R.string.change_language));

        dialog.check(doesNotExist());
    }

    @Test
    public void showsScorecardsOnDrawerClickScorecard() throws Exception{

        oppiaMobileActivityTestRule.launchActivity(null);

        openDrawer();
        performClickDrawerItem(R.id.menu_scorecard);

        onView(allOf(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                withId(R.id.activity_scorecard_pager))).check(matches(isDisplayed()));
    }

    @Test
    public void showsMonitorActivityOnDrawerClickMonitor() throws Exception{

        oppiaMobileActivityTestRule.launchActivity(null);

        openDrawer();
        performClickDrawerItem(R.id.menu_monitor);
        checkCorrectActivity(MonitorActivity.class);

    }

    @Test
    public void showsPrefsActivityOnDrawerClickSettings() throws Exception{

        oppiaMobileActivityTestRule.launchActivity(null);

        openDrawer();
        performClickDrawerItem(R.id.menu_settings);
        checkCorrectActivity(PrefsActivity.class);

    }

    @Test
    public void showsAboutActivityOnDrawerClickAbout() throws Exception{

        oppiaMobileActivityTestRule.launchActivity(null);

        openDrawer();
        performClickDrawerItem(R.id.menu_about);
        checkCorrectActivity(AboutActivity.class); 

        onView(allOf(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                withId(R.id.about_versionno))).check(matches(isDisplayed()));
    }


   /* @Test
    public void downloadCourse(){
        onView(withId(R.id.drawer))
                .perform(DrawerActions.open());

        onView(withText(R.string.menu_download))
                .perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.download_course_btn))
                .perform(click(), pressBack());

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .perform(pressBack());

        int coursesCount = getCoursesCount();

        assertTrue(coursesCount > 0);


    }*/



}
