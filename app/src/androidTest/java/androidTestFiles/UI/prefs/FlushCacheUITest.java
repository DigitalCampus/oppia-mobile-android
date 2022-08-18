package androidTestFiles.UI.prefs;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBackUnconditionally;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static androidTestFiles.Utils.Matchers.EspressoTestsMatchers.withDrawable;
import static androidTestFiles.Utils.Matchers.RecyclerViewMatcher.withRecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.model.TagRepository;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.UpdateUserCohortsTask;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import androidTestFiles.Utils.CourseUtils;
import androidTestFiles.Utils.FileUtils;
import androidTestFiles.Utils.MockedApiEndpointTest;
import androidTestFiles.database.sampledata.UserData;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class FlushCacheUITest extends MockedApiEndpointTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Mock
    CoursesRepository coursesRepository;

    @Mock
    TagRepository tagRepository;

    private static final String COHORTS_5_AND_6 = "responses/cohorts/response_200_cohorts_5_6.json";

    private void givenThereAreSomeCourses(int numberOfCourses) {

        ArrayList<Course> courses = new ArrayList<>();

        for (int i = 0; i < numberOfCourses; i++) {
            courses.add(CourseUtils.createMockCourse());
        }

        when(coursesRepository.getCourses(any())).thenReturn(courses);

    }

    @Test
    public void flushAppCache() throws Exception {

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

            if (BuildConfig.ADMIN_PROTECT_ADVANCED_SETTINGS) {
                String adminPass = context.getString(R.string.prefAdminPasswordDefault);
                onView(withId(R.id.admin_password_field)).perform(clearText(), typeText(adminPass));
                onView(withText(R.string.ok)).perform(click());
            }

            clickPrefWithText(R.string.pref_flush_app_cache);

            pressBackUnconditionally();
            pressBackUnconditionally();

            onView(withRecyclerView(R.id.recycler_courses)
                    .atPositionOnView(0, R.id.img_sync_status))
                    .check(matches(CoreMatchers.not(isDisplayed())));
        }
    }

    @Test
    public void refreshCachedCoursesInTagSelectScreen() throws Exception {

        doAnswer(invocationOnMock -> {
            Context ctx = (Context) invocationOnMock.getArguments()[0];
            BasicResult result = new BasicResult();
            result.setSuccess(true);
            result.setResultMessage("{}");
            ((TagSelectActivity) ctx).apiRequestComplete(result);
            return null;
        }).when(tagRepository).getTagList(any(), any());


        doAnswer(invocationOnMock -> {
            ArrayList<Tag> tags = (ArrayList<Tag>) invocationOnMock.getArguments()[0];
            tags.add(new Tag() {{
                setName("Mocked Course Name");
                setDescription("Mocked Course Description");
            }});
            return null;
        }).when(tagRepository).refreshTagList(any(), any(), any());

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().remove(PrefsActivity.PREF_SERVER_COURSES_CACHE).commit();

        String responseAsset = "responses/course/response_200_courses_list.json";
        startServer(200, responseAsset, 0);

        try (ActivityScenario<TagSelectActivity> scenario = ActivityScenario.launch(TagSelectActivity.class)) {

            String responseBody = FileUtils.getStringFromFile(
                    InstrumentationRegistry.getInstrumentation().getContext(), responseAsset);

            Thread.sleep(3000); // Manual waiting for Asynctask. Espresso only waits if there is a view interaction at the end.

            String cachedCourses = prefs.getString(PrefsActivity.PREF_SERVER_COURSES_CACHE, "");

            assertEquals(responseBody, cachedCourses);
        }
    }

    @Test
    public void testFlushUserCohorts() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        DbHelper dbHelper = DbHelper.getInstance(context);
        UserData.loadData(context);

        User user;
        try {
            // 1. Initially, User1 belongs to cohorts 1 and 2
            user = dbHelper.getUser(UserData.TEST_USER_1);
            assertEquals(Arrays.asList(1, 2), user.getCohorts());

            // 2. Flush cache and retrieve mocked response with cohorts 5 and 6
            startServer(200, COHORTS_5_AND_6, 0);
            UpdateUserCohortsTask task = new UpdateUserCohortsTask();
            task.setListener(() -> signal.countDown());
            task.updateLoggedUserCohorts(context, apiEndpoint, user);
            signal.await();

            // 3. Assert User1 now belongs to cohorts 5 and 6
            user = dbHelper.getUser(UserData.TEST_USER_1);
            assertEquals(Arrays.asList(5, 6), user.getCohorts());

        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }
    }
}
