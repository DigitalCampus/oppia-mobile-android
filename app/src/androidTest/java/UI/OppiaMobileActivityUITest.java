package UI;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

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
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import Utils.CourseUtils;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
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

@RunWith(AndroidJUnit4.class)
public class OppiaMobileActivityUITest {

    private Context context;

    @Rule
    public ActivityTestRule<OppiaMobileActivity> oppiaMobileActivityTestRule =
            new ActivityTestRule<OppiaMobileActivity>(OppiaMobileActivity.class);

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getTargetContext();
    }


    private int getCoursesCount(){
        DbHelper db = DbHelper.getInstance(context);
        long userId = db.getUserId(SessionManager.getUsername(context));
        return db.getCourses(userId).size();
    }

    @Test
    public void drawer_clickDownloadCourses() throws Exception{

        onView(withId(R.id.drawer))
                .perform(DrawerActions.open());

        onView(withText(R.string.menu_download))
                .perform(click());

        assertEquals(TagSelectActivity.class, Utils.TestUtils.getCurrentActivity().getClass());
    }

    @Test
    public void drawer_clickSearch() throws Exception{

        onView(withId(R.id.drawer))
                .perform(DrawerActions.open());

        onView(withText(R.string.menu_search))
                .perform(click());

        assertEquals(SearchActivity.class, Utils.TestUtils.getCurrentActivity().getClass());
    }

    @Test
    public void drawer_clickChangeLanguage() throws Exception{

        onView(withId(R.id.drawer))
                .perform(DrawerActions.open());

        onView(withText(R.string.menu_language))
                .perform(click());

        ViewInteraction dialog = onView(withText(R.string.change_language));

        int coursesCount = getCoursesCount();

        if(coursesCount > 0) {
            dialog.check(matches(isDisplayed()));
        }
    }

    @Test
    public void drawer_clickScorecard() throws Exception{

        onView(withId(R.id.drawer))
                .perform(DrawerActions.open());

        onView(withText(R.string.menu_scorecard))
                .perform(click());

        onView(allOf(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                withId(R.id.activity_scorecard_pager))).check(matches(isDisplayed()));
    }

    @Test
    public void drawer_clickMonitor() throws Exception{

        onView(withId(R.id.drawer))
                .perform(DrawerActions.open());

        onView(withText(R.string.menu_monitor))
                .perform(click());

        assertEquals(MonitorActivity.class, Utils.TestUtils.getCurrentActivity().getClass());
    }

    @Test
    public void drawer_clickSettings() throws Exception{

        onView(withId(R.id.drawer))
                .perform(DrawerActions.open());

        onView(withText(R.string.menu_settings))
                .perform(click());

        assertEquals(PrefsActivity.class, Utils.TestUtils.getCurrentActivity().getClass());
    }

    @Test
    public void drawer_clickAbout() throws Exception{

        onView(withId(R.id.drawer))
                .perform(DrawerActions.open());

        onView(withText(R.string.menu_about))
                .perform(click());

        assertEquals(AboutActivity.class, Utils.TestUtils.getCurrentActivity().getClass());

        onView(allOf(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                withId(R.id.about_versionno))).check(matches(isDisplayed()));
    }

    @Test
    public void drawer_clickLogout() throws Exception{

        onView(withId(R.id.drawer))
                .perform(DrawerActions.open());

        onView(withText(R.string.menu_logout))
                .perform(click());

        onView(withText(R.string.logout))
                .check(matches(isDisplayed()));
    }

    @Test
    public void drawer_clickLogout_dialogNo() throws Exception{

        onView(withId(R.id.drawer))
                .perform(DrawerActions.open());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean logoutEnabled = prefs.getBoolean(PrefsActivity.PREF_LOGOUT_ENABLED, false);

        if(logoutEnabled) {

            onView(withText(R.string.menu_logout))
                    .perform(click());

            onView(withText(R.string.no))
                    .perform(click());

            assertEquals(OppiaMobileActivity.class, Utils.TestUtils.getCurrentActivity().getClass());
        }

    }

    @Test
    public void drawer_clickLogout_dialogYes() throws Exception{

        onView(withId(R.id.drawer))
                .perform(DrawerActions.open());

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean logoutEnabled = prefs.getBoolean(PrefsActivity.PREF_LOGOUT_ENABLED, false);

        if(logoutEnabled){
            onView(withText(R.string.menu_logout))
                    .perform(click());

            onView(withText(R.string.yes))
                    .perform(click());

            assertEquals(WelcomeActivity.class, Utils.TestUtils.getCurrentActivity().getClass());
        }



    }

    @Test
    public void actionBar_clickPoints() throws Exception{

        onView(withId(R.id.userpoints))
                .perform(click());

        Context context = InstrumentationRegistry.getTargetContext();
        DbHelper db = DbHelper.getInstance(context);
        long userId = db.getUserId(SessionManager.getUsername(context));
        try{
            int points = db.getUser(userId).getPoints();


            if(points > 0) {
                onView(allOf(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                        withId(R.id.points_list))).check(matches(isDisplayed()));
            }else{
                onView(allOf(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                        withId(R.id.fragment_points_title))).check(matches(isDisplayed()));
            }
        }catch(UserNotFoundException e){
            e.printStackTrace();
        }

    }

    @Test
    public void actionBar_clickBadges() throws Exception{

        onView(withId(R.id.userbadges))
                .perform(click());

        Context context = InstrumentationRegistry.getTargetContext();
        DbHelper db = DbHelper.getInstance(context);
        long userId = db.getUserId(SessionManager.getUsername(context));
        try {
            int badgesCount = db.getUser(userId).getBadges();


            if (badgesCount > 0) {
                onView(allOf(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                        withId(R.id.badges_list))).check(matches(isDisplayed()));
            } else {
                onView(allOf(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                        withId(R.id.fragment_badges_title))).check(matches(isDisplayed()));
            }
        }catch(UserNotFoundException e){
            e.printStackTrace();
        }

    }

    @Test
    public void clickManageCourses_emptyCoursesList() throws Exception{
        int coursesCount = getCoursesCount();

        if(coursesCount == 0) {
            onView(withId(R.id.manage_courses_btn))
                    .perform(click());

            assertEquals(TagSelectActivity.class, Utils.TestUtils.getCurrentActivity().getClass());
        }

    }

    @Test
    public void clickCourseItem_ShowCourse() throws Exception{

        int coursesCount = getCoursesCount();

        if(coursesCount > 0) {
            onData(anything())
                    .inAdapterView(withId(R.id.course_list))
                    .atPosition(0)
                    .perform(click());

            assertEquals(CourseIndexActivity.class, Utils.TestUtils.getCurrentActivity().getClass());


        }else{
            clickManageCourses_emptyCoursesList();
        }

    }

    @Test
    public void longClickCourseItem_DisplayContextMenu() throws Exception{
        int coursesCount = getCoursesCount();

        if(coursesCount > 0) {
            onData(anything())
                    .inAdapterView(withId(R.id.course_list))
                    .atPosition(0)
                    .perform(longClick());

            onView(withChild(withId(R.id.course_context_reset)))
                    .check(matches(isDisplayed()));


            //assertEquals(CourseIndexActivity.class, Utils.TestUtils.getCurrentActivity().getClass());


        }else{
            clickManageCourses_emptyCoursesList();
        }
    }

    @Test
    public void contextMenu_UpdateCourseActivity() throws Exception{
        int coursesCount = getCoursesCount();

        if(coursesCount > 0) {
            longClickCourseItem_DisplayContextMenu();

            onView(withChild(withId(R.id.course_context_update_activity)))
                    .perform(click());

           onView(withText(containsString(InstrumentationRegistry.getTargetContext().getString(R.string.course_updating_success))));


        }

    }

    /*@Test
    public void contextMenu_DeleteCourse_clickNo() throws Exception{
        int coursesCount = getCoursesCount();

        if(coursesCount > 0) {
            longClickCourseItem_DisplayContextMenu();

            onView(withChild(withId(R.id.course_context_delete)))
                    .perform(click());

            onView(withText(R.string.no))
                    .perform(click());


            assertEquals(OppiaMobileActivity.class, Utils.TestUtils.getCurrentActivity().getClass());

        }

    }*/



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
