package UI;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.AboutActivity;
import org.digitalcampus.oppia.activity.MonitorActivity;
import org.digitalcampus.oppia.activity.OppiaMobileActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.ScorecardActivity;
import org.digitalcampus.oppia.activity.SearchActivity;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.core.IsNot.not;

@RunWith(AndroidJUnit4.class)
public class OppiaMobileActivityUITest {

    @Rule
    public ActivityTestRule<OppiaMobileActivity> welcomeActivityTestRule =
            new ActivityTestRule<OppiaMobileActivity>(OppiaMobileActivity.class);


    @Test
    public void drawer_clickDownloadCourses() throws Exception{

        onView(withId(R.id.drawer))
                .perform(DrawerActions.open());

        onView(withText(R.string.menu_download))
                .perform(click());

        assertEquals(Utils.TestUtils.getCurrentActivity().getClass(), TagSelectActivity.class);
    }

    @Test
    public void drawer_clickSearch() throws Exception{

        onView(withId(R.id.drawer))
                .perform(DrawerActions.open());

        onView(withText(R.string.menu_search))
                .perform(click());

        assertEquals(Utils.TestUtils.getCurrentActivity().getClass(), SearchActivity.class);
    }

    @Test
    public void drawer_clickChangeLanguage() throws Exception{

        onView(withId(R.id.drawer))
                .perform(DrawerActions.open());

        onView(withText(R.string.menu_language))
                .perform(click());

        Context context = InstrumentationRegistry.getTargetContext();
        DbHelper db = DbHelper.getInstance(context);
        long userId = db.getUserId(SessionManager.getUsername(context));
        int coursesCount = db.getCourses(userId).size();

        ViewInteraction dialog = onView(withText(R.string.change_language));

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

        assertEquals(Utils.TestUtils.getCurrentActivity().getClass(), MonitorActivity.class);
    }

    @Test
    public void drawer_clickSettings() throws Exception{

        onView(withId(R.id.drawer))
                .perform(DrawerActions.open());

        onView(withText(R.string.menu_settings))
                .perform(click());

        assertEquals(Utils.TestUtils.getCurrentActivity().getClass(), PrefsActivity.class);
    }

    @Test
    public void drawer_clickAbout() throws Exception{

        onView(withId(R.id.drawer))
                .perform(DrawerActions.open());

        onView(withText(R.string.menu_about))
                .perform(click());

        assertEquals(Utils.TestUtils.getCurrentActivity().getClass(), AboutActivity.class);
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

        onView(withText(R.string.menu_logout))
                .perform(click());

        onView(withText(R.string.no))
                .perform(click());

        assertEquals(Utils.TestUtils.getCurrentActivity().getClass(), OppiaMobileActivity.class);

    }

    @Test
    public void drawer_clickLogout_dialogYes() throws Exception{

        onView(withId(R.id.drawer))
                .perform(DrawerActions.open());

        onView(withText(R.string.menu_logout))
                .perform(click());

        onView(withText(R.string.yes))
                .perform(click());

        assertEquals(Utils.TestUtils.getCurrentActivity().getClass(), WelcomeActivity.class);

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

}
