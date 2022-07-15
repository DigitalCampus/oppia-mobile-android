package androidTestFiles.org.digitalcampus.oppia.fragments;

import static androidx.fragment.app.testing.FragmentScenario.launchInContainer;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static androidTestFiles.Utils.Matchers.RecyclerViewMatcher.withRecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.fragments.CoursesListFragment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidTestFiles.Utils.Assertions.RecyclerViewItemCountAssertion;
import androidTestFiles.database.sampledata.CourseData;
import androidTestFiles.database.sampledata.UserData;

@RunWith(AndroidJUnit4.class)
public class CoursesListFragmentTest {

    private Bundle args;
    private Context context;

    @Before
    public void setup() throws Exception {
        args = new Bundle();

        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        UserData.loadData(context);
        CourseData.loadData(context);
    }

    @Test
    public void showCourse3ForUser3(){

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(PrefsActivity.PREF_USER_NAME, UserData.TEST_USER_3).commit();


        launchInContainer(CoursesListFragment.class, args, R.style.Oppia_ToolbarTheme);

        onView(withId(R.id.recycler_courses)).check(new RecyclerViewItemCountAssertion(1));

        onView(withRecyclerView(R.id.recycler_courses)
                .atPositionOnView(0, R.id.course_title))
                .check(matches(withText(CourseData.TEST_COURSE_3)));
    }

    @Test
    public void showCourse2AndCourse3ForUser2() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(PrefsActivity.PREF_USER_NAME, UserData.TEST_USER_2).commit();

        launchInContainer(CoursesListFragment.class, args, R.style.Oppia_ToolbarTheme);

        onView(withId(R.id.recycler_courses)).check(new RecyclerViewItemCountAssertion(2));

        onView(withRecyclerView(R.id.recycler_courses)
                .atPositionOnView(0, R.id.course_title))
                .check(matches(withText(CourseData.TEST_COURSE_2)));

        onView(withRecyclerView(R.id.recycler_courses)
                .atPositionOnView(1, R.id.course_title))
                .check(matches(withText(CourseData.TEST_COURSE_3)));
    }

    @Test
    public void showCourse1Course2AndCourse3ForUser1() throws Exception {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(PrefsActivity.PREF_USER_NAME, UserData.TEST_USER_1).commit();

        launchInContainer(CoursesListFragment.class, args, R.style.Oppia_ToolbarTheme);

        onView(withId(R.id.recycler_courses)).check(new RecyclerViewItemCountAssertion(3));

        onView(withRecyclerView(R.id.recycler_courses)
                .atPositionOnView(0, R.id.course_title))
                .check(matches(withText(CourseData.TEST_COURSE_1)));

        onView(withRecyclerView(R.id.recycler_courses)
                .atPositionOnView(1, R.id.course_title))
                .check(matches(withText(CourseData.TEST_COURSE_2)));

        onView(withRecyclerView(R.id.recycler_courses)
                .atPositionOnView(2, R.id.course_title))
                .check(matches(withText(CourseData.TEST_COURSE_3)));
    }

    @Test
    public void testCohortVisibilityWithUserChange() throws Exception {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(PrefsActivity.PREF_USER_NAME, UserData.TEST_USER_1).commit();

        launchInContainer(CoursesListFragment.class, args, R.style.Oppia_ToolbarTheme);

        onView(withId(R.id.recycler_courses)).check(new RecyclerViewItemCountAssertion(3));

        onView(withRecyclerView(R.id.recycler_courses)
                .atPositionOnView(0, R.id.course_title))
                .check(matches(withText(CourseData.TEST_COURSE_1)));

        onView(withRecyclerView(R.id.recycler_courses)
                .atPositionOnView(1, R.id.course_title))
                .check(matches(withText(CourseData.TEST_COURSE_2)));

        onView(withRecyclerView(R.id.recycler_courses)
                .atPositionOnView(2, R.id.course_title))
                .check(matches(withText(CourseData.TEST_COURSE_3)));

        prefs.edit().putString(PrefsActivity.PREF_USER_NAME, UserData.TEST_USER_3).commit();

        launchInContainer(CoursesListFragment.class, args, R.style.Oppia_ToolbarTheme);

        onView(withId(R.id.recycler_courses)).check(new RecyclerViewItemCountAssertion(1));

        onView(withRecyclerView(R.id.recycler_courses)
                .atPositionOnView(0, R.id.course_title))
                .check(matches(withText(CourseData.TEST_COURSE_3)));
    }
}
