package UI;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.storage.ExternalStorageStrategy;
import org.digitalcampus.oppia.utils.storage.InternalStorageStrategy;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.io.File;

import Utils.Assertions.RecyclerViewItemCountAssertion;
import Utils.CourseUtils;
import Utils.FileUtils;
import database.TestDBHelper;

import static Utils.CourseUtils.runInstallCourseTask;
import static Utils.RecyclerViewMatcher.withRecyclerView;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;

@RunWith(AndroidJUnit4.class)
public class DownloadMediaUITest extends CourseMediaBaseTest {


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

        Payload response = runInstallCourseTask(context);
        assertTrue(response.isResult());


        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.view_media_missing))
                    .check(matches(not(isDisplayed())));
        }

    }

    @Test
    public void main_mediaViewVisibleWhenCourseHasNoDownloadedMedia() throws InterruptedException {

        copyCourseFromAssets(COURSE_WITH_MEDIA_1);

        Payload response = runInstallCourseTask(context);
        assertTrue(response.isResult());

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.view_media_missing))
                    .check(matches(isDisplayed()));
        }

    }

    @Test
    public void main_mediaViewHiddenWhenCourseHasAllMediaDownloaded() throws InterruptedException {

        copyCourseFromAssets(COURSE_WITH_MEDIA_1);

        Payload response = runInstallCourseTask(context);
        assertTrue(response.isResult());

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

        Payload response = runInstallCourseTask(context);
        assertTrue(response.isResult());

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

        Payload response = runInstallCourseTask(context);
        assertTrue(response.isResult());

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

        Payload response = runInstallCourseTask(context);
        assertTrue(response.isResult());

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

        Payload response = runInstallCourseTask(context);
        assertTrue(response.isResult());

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

        Payload response = runInstallCourseTask(context);
        assertTrue(response.isResult());

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

        Payload response = runInstallCourseTask(context);
        assertTrue(response.isResult());


        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            onView(withId(R.id.btn_media_download)).perform(click());

            onView(withId(R.id.missing_media_list)).check(new RecyclerViewItemCountAssertion(2));

        }

    }

    @Test
    public void downloadMedia_showOneCourseNoDownloadedFiles() throws InterruptedException {

        copyCourseFromAssets(COURSE_WITH_MEDIA_1);
        copyCourseFromAssets(COURSE_WITH_MEDIA_2);

        Payload response = runInstallCourseTask(context);
        assertTrue(response.isResult());

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            onView(withRecyclerView(R.id.recycler_courses)
                    .atPosition(0))
                    .perform(click());

            onView(withId(R.id.btn_media_download)).perform(click());

            onView(withId(R.id.missing_media_list)).check(new RecyclerViewItemCountAssertion(1));

        }

    }


}
