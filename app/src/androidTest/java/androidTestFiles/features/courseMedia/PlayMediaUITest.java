package androidTestFiles.features.courseMedia;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.web.sugar.Web.onWebView;
import static androidx.test.espresso.web.webdriver.DriverAtoms.findElement;
import static androidx.test.espresso.web.webdriver.DriverAtoms.webClick;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static androidTestFiles.utils.UITestActionsUtils.waitForView;
import static androidTestFiles.utils.ViewsUtils.isToast;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.web.webdriver.Locator;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.Section;
import org.digitalcampus.oppia.activity.VideoPlayerActivity;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.io.File;
import java.util.ArrayList;

import androidTestFiles.database.sampledata.UserData;
import androidTestFiles.utils.FileUtils;
import androidTestFiles.utils.TestUtils;
import androidTestFiles.utils.parent.BaseTest;

@RunWith(AndroidJUnit4.class)
public class PlayMediaUITest extends CourseMediaBaseTest {

    private static final String COURSE_SHORTNAME = "courseactivity_test";
    private static final String MEDIA_TEST_FILENAME = BaseTest.MEDIA_FILE_VIDEO_TEST_1;
    private static final String MEDIA_TEST_FILENAME_WITH_SPACES = "video with spaces.mp4";
    private static final String PAGE_FILENAME = "page.html";

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Mock
    SharedPreferences prefs;
    @Mock
    SharedPreferences.Editor editor;

    private void initMockEditor() {
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        initMockEditor();
        when(prefs.edit()).thenReturn(editor);

        UserData.loadData(context);

        Intents.init();
    }

    @After
    public void tearDown() throws Exception {
        Intents.release();
    }

    private Course getMockCourse() {
        Course course = new Course(Storage.getStorageLocationRoot(context));
        course.setShortname(COURSE_SHORTNAME);
        ArrayList<Lang> langs = new ArrayList<>();
        langs.add(new Lang("en", "Course"));
        course.setTitles(langs);
        course.setCourseId(0);
        return course;
    }

    private String getPageContent(String filename) {
        return "<!DOCTYPE html><html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n" +
                "<a href=\"/video/" + Uri.encode(filename) + "\"> My video</a>\n" +
                "</body></html>";
    }

    private Section getMockSectionActivityWithMediaFilename(String mediaFilename) {
        Section s = new Section();
        Activity act = new Activity();
        act.setActType("page");
        act.setDigest("aaaaaa");
        act.setTitlesFromJSONString("[{\"en\":\"Media page\"}]");

        File courseDir = new File(Storage.getCoursesPath(context), COURSE_SHORTNAME);
        FileUtils.createFileWithContents(context, getPageContent(mediaFilename), courseDir, PAGE_FILENAME);

        ArrayList<Lang> location = new ArrayList<>();
        location.add(new Lang("en", PAGE_FILENAME));
        act.setLocations(location);

        s.addActivity(act);

        return s;
    }

    private Intent getIntentParams(Course course, Section s) {
        Intent i = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), CourseActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Section.TAG, s);
        bundle.putSerializable(Course.TAG, course);
        bundle.putSerializable(CourseActivity.NUM_ACTIVITY_TAG, 0);
        i.putExtras(bundle);
        return i;
    }

    @Test
    public void openExistingMediaFile() throws Exception {

        copyMediaFromAssets(MEDIA_TEST_FILENAME);

        Intent i = getIntentParams(getMockCourse(), getMockSectionActivityWithMediaFilename(MEDIA_TEST_FILENAME));
        try (ActivityScenario<CourseActivity> scenario = ActivityScenario.launch(i)) {
            onWebView()
                    .withElement(findElement(Locator.TAG_NAME, "a"))
                    .perform(webClick());

            assertEquals(VideoPlayerActivity.class, TestUtils.getCurrentActivity().getClass());
        }
    }

    @Test
    @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.Q)
    public void openMissingMediaFile() throws Exception {

        // Don't copy the video initially
        int trackers = DbHelper.getInstance(context).getUnsentTrackersCount();
        Intent i = getIntentParams(getMockCourse(), getMockSectionActivityWithMediaFilename(MEDIA_TEST_FILENAME));
        try (ActivityScenario<CourseActivity> scenario = ActivityScenario.launch(i)) {

            onWebView()
                    .withElement(findElement(Locator.TAG_NAME, "a"))
                    .perform(webClick());

            waitForView(withText(context.getString(R.string.error_media_not_found, MEDIA_TEST_FILENAME))).
                    inRoot(isToast()).
                    check(matches(isDisplayed()));

            assertEquals(CourseActivity.class, TestUtils.getCurrentActivity().getClass());

            // There should be a new tracker for the missing media event
            int newTrackers = DbHelper.getInstance(context).getUnsentTrackersCount();
            assertEquals(newTrackers, trackers + 1);
        }


    }

    @Test
    public void openExistingMediaFileWithSpacesInFilename() throws Exception {
        copyMediaFromAssets(MEDIA_TEST_FILENAME, MEDIA_TEST_FILENAME_WITH_SPACES);

        Intent i = getIntentParams(getMockCourse(), getMockSectionActivityWithMediaFilename(MEDIA_TEST_FILENAME_WITH_SPACES));
        try (ActivityScenario<CourseActivity> scenario = ActivityScenario.launch(i)) {
            onWebView()
                    .withElement(findElement(Locator.TAG_NAME, "a"))
                    .perform(webClick());

            intended(hasComponent(new ComponentName(InstrumentationRegistry.getInstrumentation().getTargetContext(), VideoPlayerActivity.class)));
//            assertEquals(VideoPlayerActivity.class, TestUtils.getCurrentActivity().getClass());
        }
    }
}
