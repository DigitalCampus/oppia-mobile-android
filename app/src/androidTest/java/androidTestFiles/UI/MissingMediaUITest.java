package androidTestFiles.UI;

import android.Manifest;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidTestFiles.Utils.Assertions.RecyclerViewItemCountAssertion;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import static androidTestFiles.Matchers.RecyclerViewMatcher.withRecyclerView;
import static androidTestFiles.Utils.CourseUtils.runInstallCourseTask;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MissingMediaUITest extends CourseMediaBaseTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Test
    public void main_mediaViewHiddenWhenThereAreNoCourses(){

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.view_media_missing))
                    .check(matches(not(isDisplayed())));
        }

    }

    @Test
    public void main_mediaViewHiddenWhenThereAreCourseWithNoMedia(){

        copyCourseFromAssets(COURSE_WITH_NO_MEDIA);

        BasicResult response = runInstallCourseTask(context);
        assertTrue(response.isSuccess());

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            onView(withId(R.id.view_media_missing))
                    .check(matches(not(isDisplayed())));

        }
    }

    @Test
    public void main_mediaViewVisibleWhenCourseHasNoDownloadedMedia() throws InterruptedException {

        copyCourseFromAssets(COURSE_WITH_MEDIA_1);

        BasicResult response = runInstallCourseTask(context);
        assertTrue(response.isSuccess());

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            onView(withId(R.id.view_media_missing))
                    .check(matches(isDisplayed()));
        }

    }

    @Test
    public void main_mediaViewHiddenWhenCourseHasAllMediaDownloaded() throws InterruptedException {

        copyCourseFromAssets(COURSE_WITH_MEDIA_1);

        BasicResult response = runInstallCourseTask(context);
        assertTrue(response.isSuccess());

        copyMediaFromAssets(MEDIA_FILE_VIDEO_TEST_1);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            onView(withId(R.id.view_media_missing))
                    .check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void main_mediaViewVisibleWhenAnyCourseHasNoDownloadedMedia() throws InterruptedException {

        copyCourseFromAssets(COURSE_WITH_MEDIA_1);
        copyCourseFromAssets(COURSE_WITH_MEDIA_2);

        BasicResult response = runInstallCourseTask(context);
        assertTrue(response.isSuccess());

        copyMediaFromAssets(MEDIA_FILE_VIDEO_TEST_1);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.view_media_missing))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void main_mediaViewHiddenWhenAllCoursesHaveAllMediaDownloaded() throws InterruptedException {

        copyCourseFromAssets(COURSE_WITH_MEDIA_1);
        copyCourseFromAssets(COURSE_WITH_MEDIA_2);

        BasicResult response = runInstallCourseTask(context);
        assertTrue(response.isSuccess());

        copyMediaFromAssets(MEDIA_FILE_VIDEO_TEST_1);
        copyMediaFromAssets(MEDIA_FILE_VIDEO_TEST_2);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.view_media_missing))
                    .check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void courseIndex_mediaViewHiddenWhenTheCourseHasNoMedia() throws InterruptedException {

        copyCourseFromAssets(COURSE_WITH_NO_MEDIA);

        BasicResult response = runInstallCourseTask(context);
        assertTrue(response.isSuccess());

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withRecyclerView(R.id.recycler_courses)
                    .atPosition(0))
                    .perform(click());

            onView(withId(R.id.view_media_missing))
                    .check(matches(not(isDisplayed())));
        }
    }


    @Test
    public void courseIndex_mediaViewVisibleWhenCourseHasNotAllDownloadedMedia() throws InterruptedException {

        copyCourseFromAssets(COURSE_WITH_MEDIA_2);

        BasicResult response = runInstallCourseTask(context);
        assertTrue(response.isSuccess());

        copyMediaFromAssets(MEDIA_FILE_VIDEO_TEST_2);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withRecyclerView(R.id.recycler_courses)
                    .atPosition(0))
                    .perform(click());

            onView(withId(R.id.view_media_missing))
                    .check(matches(isDisplayed()));
        }

    }

    @Test
    public void courseIndex_mediaViewHiddenWhenCourseHasAllMediaDownloaded() throws InterruptedException {

        copyCourseFromAssets(COURSE_WITH_MEDIA_1);

        BasicResult response = runInstallCourseTask(context);
        assertTrue(response.isSuccess());

        copyMediaFromAssets(MEDIA_FILE_VIDEO_TEST_1);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.view_media_missing))
                    .check(matches(not(isDisplayed())));
        }

    }

    @Test
    public void downloadMedia_showAllCoursesNoDownloadedFiles() throws InterruptedException {

        copyCourseFromAssets(COURSE_WITH_MEDIA_1);
        copyCourseFromAssets(COURSE_WITH_MEDIA_2);

        BasicResult response = runInstallCourseTask(context);
        assertTrue(response.isSuccess());


        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.btn_media_download)).perform(click());

            onView(withId(R.id.missing_media_list)).check(new RecyclerViewItemCountAssertion(2));
        }
    }

    @Test
    public void downloadMedia_showOneCourseNoDownloadedFiles() throws InterruptedException {

        copyCourseFromAssets(COURSE_WITH_MEDIA_1);
        copyCourseFromAssets(COURSE_WITH_MEDIA_2);

        BasicResult response = runInstallCourseTask(context);
        assertTrue(response.isSuccess());

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            onView(withRecyclerView(R.id.recycler_courses)
                    .atPosition(0))
                    .perform(click());

            onView(withId(R.id.btn_media_download)).perform(click());

            onView(withId(R.id.missing_media_list)).check(new RecyclerViewItemCountAssertion(1));
        }
    }


}
