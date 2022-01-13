package androidTestFiles.UI.prefs;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBackUnconditionally;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static androidTestFiles.Matchers.EspressoTestsMatchers.withDrawable;
import static androidTestFiles.Matchers.RecyclerViewMatcher.withRecyclerView;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;

import androidTestFiles.TestRules.DaggerInjectMockUITest;
import androidTestFiles.Utils.CourseUtils;
import androidTestFiles.Utils.FileUtils;
import androidTestFiles.Utils.MockedApiEndpointTest;

@RunWith(AndroidJUnit4.class)
public class FlushCacheUITest extends MockedApiEndpointTest {

    @Mock
    CoursesRepository coursesRepository;

    private void givenThereAreSomeCourses(int numberOfCourses) {

        ArrayList<Course> courses = new ArrayList<>();

        for (int i = 0; i < numberOfCourses; i++) {
            courses.add(CourseUtils.createMockCourse());
        }

        when(coursesRepository.getCourses(any())).thenReturn(courses);

    }

    @Test
    public void flushCourseListingCache() throws Exception {

        givenThereAreSomeCourses(1);

        String previousCacheAsset = "responses/course/response_200_courses_list.json";
        String previousCache = FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), previousCacheAsset);

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putString(PrefsActivity.PREF_SERVER_COURSES_CACHE, previousCache).commit();

        startServer(500, null, 0);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            onView(withRecyclerView(R.id.recycler_courses)
                    .atPositionOnView(0, R.id.img_sync_status))
                    .check(matches(withDrawable(R.drawable.ic_action_refresh)));

            openDrawer();
            performClickDrawerItem(R.id.menu_settings);
            clickPrefWithText(R.string.prefAdvanced_title);
            clickPrefWithText(R.string.pref_flush_course_listing_cache);

            pressBackUnconditionally();
            pressBackUnconditionally();

            onView(withRecyclerView(R.id.recycler_courses)
                    .atPositionOnView(0, R.id.img_sync_status))
                    .check(matches(CoreMatchers.not(isDisplayed())));
        }
    }

}