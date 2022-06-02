package androidTestFiles.org.digitalcampus.oppia.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.DownloadActivity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CourseInstallRepository;
import org.digitalcampus.oppia.model.CourseInstallViewAdapter;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.MultiLangInfoModel;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.service.courseinstall.CourseInstallerService;
import org.digitalcampus.oppia.service.courseinstall.CourseInstallerServiceDelegate;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;

import androidTestFiles.TestRules.DaggerInjectMockUITest;
import androidTestFiles.Utils.Assertions.RecyclerViewItemCountAssertion;
import androidTestFiles.Utils.Assertions.StatusBadgeAssertion;
import androidTestFiles.Utils.CourseUtils;
import androidTestFiles.Utils.FileUtils;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import static androidTestFiles.Utils.Matchers.EspressoTestsMatchers.withDrawable;
import static androidTestFiles.Utils.Matchers.RecyclerViewMatcher.withRecyclerView;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

@RunWith(AndroidJUnit4.class)
public class DownloadActivityUITest extends DaggerInjectMockUITest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Mock
    CourseInstallRepository courseInstallRepository;
    @Mock
    CourseInstallerServiceDelegate courseInstallerServiceDelegate;

    private void givenThereAreSomeCourses(final int numberOfCourses, final CourseInstallViewAdapter course) throws Exception {

        CourseInstallViewAdapter[] courses = new CourseInstallViewAdapter[numberOfCourses];
        for (int i = 0; i < numberOfCourses; i++) {
            courses[i] = course;
        }
        givenThereAreSomeCourses(courses);

    }

    private void givenThereAreSomeCourses(final CourseInstallViewAdapter... coursesMock) throws Exception {

        doAnswer(invocationOnMock -> {
            Context ctx = (Context) invocationOnMock.getArguments()[0];
            BasicResult result = new BasicResult();
            result.setSuccess(true);
            result.setResultMessage("{}");
            ((DownloadActivity) ctx).apiRequestComplete(result);
            return null;
        }).when(courseInstallRepository).getCourseList(any(), anyString());


        doAnswer(invocationOnMock -> {
            ArrayList<CourseInstallViewAdapter> courses = (ArrayList<CourseInstallViewAdapter>) invocationOnMock.getArguments()[1];

            for (CourseInstallViewAdapter course : coursesMock) {
                courses.add(course);
            }
            return null;
        }).when(courseInstallRepository).refreshCourseList(any(), (ArrayList<CourseInstallViewAdapter>) any(),
                any(), anyString(), anyBoolean());

    }

    private CourseInstallViewAdapter getBaseCourse() {
        CourseInstallViewAdapter c = new CourseInstallViewAdapter("");
        c.setShortname("Mocked Course Name");
        c.setDownloadUrl("Mock URL");

        return c;
    }

    private void sendBroadcast(Context ctx, String action) {
        Intent intent = new Intent(CourseInstallerService.BROADCAST_ACTION);
        intent.putExtra(CourseInstallerService.SERVICE_ACTION, action);
        intent.putExtra(CourseInstallerService.SERVICE_URL, "Mock URL");
        intent.putExtra(CourseInstallerService.SERVICE_MESSAGE, "1");
        ctx.sendOrderedBroadcast(intent, null);
    }

    private Intent getMockTagCoursesIntent() {

        Tag tag = new Tag();
        tag.setId(1);
        tag.setName("any_tag");

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = new Intent(context, DownloadActivity.class);
        intent.putExtra(DownloadActivity.EXTRA_MODE, DownloadActivity.MODE_TAG_COURSES);
        intent.putExtra(DownloadActivity.EXTRA_TAG, tag);
        return intent;
    }

    @Test
    public void showDraftTextIfCourseIsDraft() throws Exception {
        CourseInstallViewAdapter c = getBaseCourse();
        c.setStatus(Course.STATUS_DRAFT);

        givenThereAreSomeCourses(2, c);

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.view_course_status))
                    .check(StatusBadgeAssertion.withText(context.getString(R.string.status_draft)));
        }
    }

    @Test
    public void dowsNotShowDraftTextIfCourseIsNotDraft() throws Exception {
        CourseInstallViewAdapter c = getBaseCourse();
        c.setStatus(Course.STATUS_LIVE);

        givenThereAreSomeCourses(2, c);

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.view_course_status))
                    .check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void showTitleIfExists() throws Exception {
        CourseInstallViewAdapter c = getBaseCourse();
        final String title = "Mock Title";
        c.setTitles(new ArrayList<Lang>() {{
            add(new Lang("en", title));
        }});


        givenThereAreSomeCourses(2, c);

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.course_title))
                    .check(matches(withText(title)));
        }
    }

    @Test
    public void showDefaultTitleIfNotExists() throws Exception {
        CourseInstallViewAdapter c = getBaseCourse();

        givenThereAreSomeCourses(2, c);

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.course_title))
                    .check(matches(withText(MultiLangInfoModel.DEFAULT_NOTITLE)));
        }
    }

    @Test
    public void showDescriptionIfExists() throws Exception {
        CourseInstallViewAdapter c = getBaseCourse();
        final String description = "Mock Description";
        c.setDescriptions(new ArrayList<Lang>() {{
            add(new Lang("en", description));
        }});

        givenThereAreSomeCourses(2, c);

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.course_description))
                    .check(matches(withText(description)));
        }
    }

    @Test
    public void showDefaultDescriptionIfNotExists() throws Exception {
        CourseInstallViewAdapter c = getBaseCourse();

        givenThereAreSomeCourses(2, c);

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.course_description))
                    .check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void showCourseAuthorIfExists() throws Exception {
        CourseInstallViewAdapter c = getBaseCourse();
        String authorName = "Mock Author";
        c.setAuthorName(authorName);
        c.setOrganisationName(authorName);

        givenThereAreSomeCourses(2, c);

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.course_author))
                    .check(matches(withText(authorName)));
        }

    }

    @Test
    public void doesNotShowCourseAuthorIfNotExists() throws Exception {

        givenThereAreSomeCourses(2, getBaseCourse());

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.course_author))
                    .check(matches(not(isDisplayed())));
        }

    }


    @Test
    public void showCancelButtonOnDownloadingCourse() throws Exception {

        givenThereAreSomeCourses(2, getBaseCourse());

        doAnswer(invocationOnMock -> {
            Context ctx = (Context) invocationOnMock.getArguments()[0];
            sendBroadcast(ctx, CourseInstallerService.ACTION_DOWNLOAD);
            return null;
        }).when(courseInstallerServiceDelegate).installCourse(any(), any(), any());

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.download_course_btn))
                    .perform(click())
                    .check(matches(withDrawable(R.drawable.ic_action_cancel)));
        }
    }

    @Test
    public void showCancelButtonOnInstallingCourse() throws Exception {

        CourseInstallViewAdapter c = getBaseCourse();

        givenThereAreSomeCourses(2, c);

        doAnswer(invocationOnMock -> {
            Context ctx = (Context) invocationOnMock.getArguments()[0];
            sendBroadcast(ctx, CourseInstallerService.ACTION_DOWNLOAD);
            sendBroadcast(ctx, CourseInstallerService.ACTION_INSTALL);
            return null;
        }).when(courseInstallerServiceDelegate).installCourse((Context) any(), (Intent) any(), (CourseInstallViewAdapter) any());

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.download_course_btn))
                    .perform(click())
                    .check(matches(withDrawable(R.drawable.ic_action_cancel)));
        }
    }

    @Test
    public void showProgressBarOnDownloadCourse() throws Exception {

        givenThereAreSomeCourses(2, getBaseCourse());

        doAnswer(invocationOnMock -> {
            Context ctx = (Context) invocationOnMock.getArguments()[0];
            sendBroadcast(ctx, CourseInstallerService.ACTION_DOWNLOAD);
            return null;
        }).when(courseInstallerServiceDelegate).installCourse((Context) any(), (Intent) any(), (CourseInstallViewAdapter) any());

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.download_course_btn))
                    .perform(click());

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.download_progress))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void showProgressBarOnInstallingCourse() throws Exception {

        givenThereAreSomeCourses(2, getBaseCourse());

        doAnswer(invocationOnMock -> {
            Context ctx = (Context) invocationOnMock.getArguments()[0];
            sendBroadcast(ctx, CourseInstallerService.ACTION_DOWNLOAD);
            sendBroadcast(ctx, CourseInstallerService.ACTION_INSTALL);
            return null;
        }).when(courseInstallerServiceDelegate).installCourse((Context) any(), (Intent) any(), (CourseInstallViewAdapter) any());

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.download_course_btn))
                    .perform(click());

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.download_progress))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void doesNotShowProgressBarOnNotInstallingNorDownloadingCourse() throws Exception {

        CourseInstallViewAdapter c = getBaseCourse();
        c.setDownloading(false);
        c.setInstalling(false);

        givenThereAreSomeCourses(2, c);

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.download_progress))
                    .check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void doesNotShowProgressBarOnCourseInstalled() throws Exception {

        givenThereAreSomeCourses(2, getBaseCourse());

        doAnswer(invocationOnMock -> {
            Context ctx = (Context) invocationOnMock.getArguments()[0];
            sendBroadcast(ctx, CourseInstallerService.ACTION_DOWNLOAD);
            sendBroadcast(ctx, CourseInstallerService.ACTION_INSTALL);
            sendBroadcast(ctx, CourseInstallerService.ACTION_COMPLETE);
            return null;
        }).when(courseInstallerServiceDelegate).installCourse((Context) any(), (Intent) any(), (CourseInstallViewAdapter) any());

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.download_course_btn))
                    .perform(click());

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.download_progress))
                    .check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void showDownloadButtonBeforeDownloadCourse() throws Exception {

        CourseInstallViewAdapter c = getBaseCourse();
        c.setDownloading(false);
        c.setInstalled(false);
        c.setToUpdate(false);

        givenThereAreSomeCourses(2, c);

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.download_course_btn))
                    .check(matches(withDrawable(R.drawable.ic_action_download)));
        }
    }

    @Test
    public void showUpdateButtonIfCourseIsToUpdate() throws Exception {

        CourseInstallViewAdapter c = getBaseCourse();
        c.setDownloading(false);
        c.setInstalled(true);
        c.setToUpdate(true);

        givenThereAreSomeCourses(2, c);

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.download_course_btn))
                    .check(matches(withDrawable(R.drawable.ic_action_refresh)));
        }
    }

    @Test
    public void buttonEnabledIfCourseIsToUpdate() throws Exception {

        CourseInstallViewAdapter c = getBaseCourse();
        c.setDownloading(false);
        c.setInstalled(true);
        c.setToUpdate(true);

        givenThereAreSomeCourses(2, c);

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.download_course_btn))
                    .check(matches(isEnabled()));
        }
    }

    @Test
    public void showAcceptButtonOnInstallComplete() throws Exception {

        givenThereAreSomeCourses(2, getBaseCourse());

        doAnswer(invocationOnMock -> {
            Context ctx = (Context) invocationOnMock.getArguments()[0];
            sendBroadcast(ctx, CourseInstallerService.ACTION_DOWNLOAD);
            sendBroadcast(ctx, CourseInstallerService.ACTION_INSTALL);
            sendBroadcast(ctx, CourseInstallerService.ACTION_COMPLETE);
            return null;
        }).when(courseInstallerServiceDelegate).installCourse((Context) any(), (Intent) any(), (CourseInstallViewAdapter) any());

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.download_course_btn))
                    .perform(click())
                    .check(matches(withDrawable(R.drawable.ic_action_accept)));
        }

    }

    @Test
    public void showAcceptButtonOnUpdateComplete() throws Exception {
        CourseInstallViewAdapter c = getBaseCourse();
        c.setInstalled(true);
        c.setToUpdate(true);

        givenThereAreSomeCourses(2, c);

        doAnswer(invocationOnMock -> {
            Context ctx = (Context) invocationOnMock.getArguments()[0];
            sendBroadcast(ctx, CourseInstallerService.ACTION_UPDATE);
            return null;
        }).when(courseInstallerServiceDelegate).updateCourse(any(), any(), any());

        doAnswer(invocationOnMock -> {
            Context ctx = (Context) invocationOnMock.getArguments()[0];
            sendBroadcast(ctx, CourseInstallerService.ACTION_INSTALL);
            sendBroadcast(ctx, CourseInstallerService.ACTION_COMPLETE);
            return null;
        }).when(courseInstallerServiceDelegate).installCourse(any(), any(), any());


        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.download_course_btn))
                    .perform(click())
                    .check(matches(withDrawable(R.drawable.ic_action_accept)));
        }
    }

    @Test
    public void buttonEnabledIfCourseIsNotInstalled() throws Exception {

        CourseInstallViewAdapter c = getBaseCourse();
        c.setDownloading(false);
        c.setInstalled(false);

        givenThereAreSomeCourses(2, c);

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

//        onView(withRecyclerView(R.id.recycler_tags).atPosition(0)).perform(click());

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.download_course_btn))
                    .check(matches(isEnabled()));
        }
    }


    @Test
    public void dontShowNewDownloadDisabledCourse() throws Exception {

        CourseInstallViewAdapter c1 = getBaseCourse();
        c1.setShortname("c1");
        c1.setTitles(Arrays.asList(new Lang("en", "Course1")));

        CourseInstallViewAdapter c2 = getBaseCourse();
        c1.setShortname("c2");
        c2.setStatus(Course.STATUS_NEW_DOWNLOADS_DISABLED);
        c2.setTitles(Arrays.asList(new Lang("en", "Course2")));

        givenThereAreSomeCourses(c1, c2);

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withId(R.id.recycler_tags)).check(new RecyclerViewItemCountAssertion(1));

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.course_title))
                    .check(matches(withText("Course1")));
        }
    }

    @Test
    public void showNewDownloadDisabledCourseIfItIsInstalled() throws Exception {

        CourseUtils.cleanUp();
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        FileUtils.copyZipFromAssetsPath(context, "common", "test_course.zip");

        BasicResult response = CourseUtils.runInstallCourseTask(context);
        assertTrue(response.isSuccess());

        CourseInstallViewAdapter c1 = getBaseCourse();
        c1.setShortname("c1");
        c1.setTitles(Arrays.asList(new Lang("en", "Course1")));

        CourseInstallViewAdapter c2 = getBaseCourse();
        c2.setShortname("test_course");
        c2.setStatus(Course.STATUS_NEW_DOWNLOADS_DISABLED);
        c2.setTitles(Arrays.asList(new Lang("en", "Course2")));

        givenThereAreSomeCourses(c1, c2);

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withId(R.id.recycler_tags)).check(new RecyclerViewItemCountAssertion(2));

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.course_title))
                    .check(matches(withText("Course1")));

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(1, R.id.course_title))
                    .check(matches(withText("Course2")));
        }
    }


    @Test
    public void dontShowReadOnlyCourse() throws Exception {

        CourseInstallViewAdapter c1 = getBaseCourse();
        c1.setShortname("c1");
        c1.setTitles(Arrays.asList(new Lang("en", "Course1")));

        CourseInstallViewAdapter c2 = getBaseCourse();
        c1.setShortname("c2");
        c2.setStatus(Course.STATUS_READ_ONLY);
        c2.setTitles(Arrays.asList(new Lang("en", "Course2")));

        givenThereAreSomeCourses(c1, c2);

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withId(R.id.recycler_tags)).check(new RecyclerViewItemCountAssertion(1));

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.course_title))
                    .check(matches(withText("Course1")));
        }
    }

    @Test
    public void showReadOnlyCourseIfItIsInstalled() throws Exception {

        CourseUtils.cleanUp();
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        FileUtils.copyZipFromAssetsPath(context, "common", "test_course.zip");

        BasicResult response = CourseUtils.runInstallCourseTask(context);
        assertTrue(response.isSuccess());

        CourseInstallViewAdapter c1 = getBaseCourse();
        c1.setShortname("c1");
        c1.setTitles(Arrays.asList(new Lang("en", "Course1")));

        CourseInstallViewAdapter c2 = getBaseCourse();
        c2.setShortname("test_course");
        c2.setStatus(Course.STATUS_READ_ONLY);
        c2.setTitles(Arrays.asList(new Lang("en", "Course2")));

        givenThereAreSomeCourses(c1, c2);

        try (ActivityScenario<DownloadActivity> scenario = ActivityScenario.launch(getMockTagCoursesIntent())) {

            onView(withId(R.id.recycler_tags)).check(new RecyclerViewItemCountAssertion(2));

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(0, R.id.course_title))
                    .check(matches(withText("Course1")));

            onView(withRecyclerView(R.id.recycler_tags)
                    .atPositionOnView(1, R.id.course_title))
                    .check(matches(withText("Course2")));
        }
    }
}
