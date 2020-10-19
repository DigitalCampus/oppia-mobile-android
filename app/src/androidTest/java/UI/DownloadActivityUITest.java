package UI;

import android.content.Context;
import android.content.Intent;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.DownloadActivity;
import org.digitalcampus.oppia.adapter.CourseInstallViewAdapter;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.di.AppModule;
import org.digitalcampus.oppia.model.CourseInstallRepository;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.MultiLangInfoModel;
import org.digitalcampus.oppia.service.courseinstall.CourseInstallerService;
import org.digitalcampus.oppia.service.courseinstall.CourseInstallerServiceDelegate;
import org.digitalcampus.oppia.task.Payload;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import it.cosenonjaviste.daggermock.DaggerMockRule;

import static Matchers.EspressoTestsMatchers.withDrawable;
import static Utils.RecyclerViewMatcher.withRecyclerView;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

@RunWith(AndroidJUnit4.class)
public class DownloadActivityUITest {

    @Rule
    public DaggerMockRule<AppComponent> daggerRule =
            new DaggerMockRule<>(AppComponent.class, new AppModule((App) InstrumentationRegistry.getInstrumentation()
                    .getTargetContext()
                    .getApplicationContext())).set(
                    component -> {
                        App app =
                                (App) InstrumentationRegistry.getInstrumentation()
                                        .getTargetContext()
                                        .getApplicationContext();
                        app.setComponent(component);
                    });

    @Rule
    public ActivityTestRule<DownloadActivity> downloadActivityTestRule =
            new ActivityTestRule<>(DownloadActivity.class, false, false);

    @Mock CourseInstallRepository courseInstallRepository;
    @Mock CourseInstallerServiceDelegate courseInstallerServiceDelegate;

    private void givenThereAreSomeCourses(final int numberOfCourses, final CourseInstallViewAdapter course) throws Exception{

        doAnswer(invocationOnMock -> {
            Context ctx = (Context) invocationOnMock.getArguments()[0];
            Payload response = new Payload();
            response.setResult(true);
            response.setResultResponse("{}");
            ((DownloadActivity) ctx).apiRequestComplete(response);
            return null;
        }).when(courseInstallRepository).getCourseList(any(), anyString());


        doAnswer(invocationOnMock -> {
            ArrayList<CourseInstallViewAdapter>  courses = (ArrayList<CourseInstallViewAdapter>) invocationOnMock.getArguments()[1];

            for(int i = 0; i < numberOfCourses; i++) {
                courses.add(course);
            }
            return null;
        }).when(courseInstallRepository).refreshCourseList(any(), (ArrayList<CourseInstallViewAdapter>) any(),
                any(), anyString(), anyBoolean());

    }

    private CourseInstallViewAdapter getBaseCourse(){
        CourseInstallViewAdapter c = new CourseInstallViewAdapter("");
        c.setShortname("Mocked Course Name");
        c.setDownloadUrl("Mock URL");

        return c;
    }

    private void sendBroadcast(Context ctx, String action){
        Intent intent = new Intent(CourseInstallerService.BROADCAST_ACTION);
        intent.putExtra(CourseInstallerService.SERVICE_ACTION, action);
        intent.putExtra(CourseInstallerService.SERVICE_URL, "Mock URL");
        intent.putExtra(CourseInstallerService.SERVICE_MESSAGE, "1");
        ctx.sendOrderedBroadcast(intent, null);
    }

    @Test
    public void showDraftTextIfCourseIsDraft() throws Exception{
        CourseInstallViewAdapter c = getBaseCourse();
        c.setDraft(true);

        givenThereAreSomeCourses(2, c);

        downloadActivityTestRule.launchActivity(null);

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.course_draft))
                .check(matches(withText(R.string.course_draft)));

    }

    @Test
    public void dowsNotShowDraftTextIfCourseIsNotDraft() throws Exception{
        CourseInstallViewAdapter c = getBaseCourse();
        c.setDraft(false);

        givenThereAreSomeCourses(2, c);

        downloadActivityTestRule.launchActivity(null);

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.course_draft))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void showTitleIfExists() throws Exception{
        CourseInstallViewAdapter c = getBaseCourse();
        final String title =  "Mock Title";
        c.setTitles(new ArrayList<Lang>() {{
            add(new Lang("en", title));
        }});


        givenThereAreSomeCourses(2, c);

        downloadActivityTestRule.launchActivity(null);

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.course_title))
                .check(matches(withText(title)));

    }

    @Test
    public void showDefaultTitleIfNotExists() throws Exception{
        CourseInstallViewAdapter c = getBaseCourse();

        givenThereAreSomeCourses(2, c);

        downloadActivityTestRule.launchActivity(null);

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.course_title))
                .check(matches(withText(MultiLangInfoModel.DEFAULT_NOTITLE)));

    }

    @Test
    public void showDescriptionIfExists() throws Exception{
        CourseInstallViewAdapter c = getBaseCourse();
        final String description =  "Mock Description";
        c.setDescriptions(new ArrayList<Lang>() {{
            add(new Lang("en", description));
        }});

        givenThereAreSomeCourses(2, c);

        downloadActivityTestRule.launchActivity(null);

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.course_description))
                .check(matches(withText(description)));
    }

    @Test
    public void showDefaultDescriptionIfNotExists() throws Exception{
        CourseInstallViewAdapter c = getBaseCourse();

        givenThereAreSomeCourses(2, c);

        downloadActivityTestRule.launchActivity(null);

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.course_description))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void showCourseAuthorIfExists() throws Exception{
        CourseInstallViewAdapter c = getBaseCourse();
        String authorName = "Mock Author";
        c.setAuthorName(authorName);
        c.setOrganisationName(authorName);

        givenThereAreSomeCourses(2, c);

        downloadActivityTestRule.launchActivity(null);

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.course_author))
                .check(matches(withText(authorName)));

    }

    @Test
    public void doesNotShowCourseAuthorIfNotExists() throws Exception{

        givenThereAreSomeCourses(2, getBaseCourse());

        downloadActivityTestRule.launchActivity(null);

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.course_author))
                .check(matches(not(isDisplayed())));

    }

    @Test
    public void showCancelButtonOnDownloadingCourse() throws Exception {

        givenThereAreSomeCourses(2, getBaseCourse());

        doAnswer(invocationOnMock -> {
            Context ctx = (Context) invocationOnMock.getArguments()[0];
            sendBroadcast(ctx, CourseInstallerService.ACTION_DOWNLOAD);
            return null;
        }).when(courseInstallerServiceDelegate).installCourse((Context) any(), (Intent) any(), (CourseInstallViewAdapter) any());

        downloadActivityTestRule.launchActivity(null);

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.download_course_btn))
                .perform(click())
                .check(matches(withDrawable(R.drawable.ic_action_cancel)));

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

        downloadActivityTestRule.launchActivity(null);

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.download_course_btn))
                .perform(click())
                .check(matches(withDrawable(R.drawable.ic_action_cancel)));

    }

    @Test
    public void showProgressBarOnDownloadCourse() throws Exception {

        givenThereAreSomeCourses(2, getBaseCourse());

        doAnswer(invocationOnMock -> {
            Context ctx = (Context) invocationOnMock.getArguments()[0];
            sendBroadcast(ctx, CourseInstallerService.ACTION_DOWNLOAD);
            return null;
        }).when(courseInstallerServiceDelegate).installCourse((Context) any(), (Intent) any(), (CourseInstallViewAdapter) any());

        downloadActivityTestRule.launchActivity(null);

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.download_course_btn))
                .perform(click());

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.download_progress))
                .check(matches(isDisplayed()));

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

        downloadActivityTestRule.launchActivity(null);

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.download_course_btn))
                .perform(click());

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.download_progress))
                .check(matches(isDisplayed()));

    }

    @Test
    public void doesNotShowProgressBarOnNotInstallingNorDownloadingCourse() throws Exception {

        CourseInstallViewAdapter c = getBaseCourse();
        c.setDownloading(false);
        c.setInstalling(false);

        givenThereAreSomeCourses(2, c);

        downloadActivityTestRule.launchActivity(null);

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.download_progress))
                .check(matches(not(isDisplayed())));

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

        downloadActivityTestRule.launchActivity(null);

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.download_course_btn))
                .perform(click());

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.download_progress))
                .check(matches(not(isDisplayed())));

    }

    @Test
    public void showDownloadButtonBeforeDownloadCourse() throws Exception {

        CourseInstallViewAdapter c = getBaseCourse();
        c.setDownloading(false);
        c.setInstalled(false);
        c.setToUpdate(false);

        givenThereAreSomeCourses(2, c);

        downloadActivityTestRule.launchActivity(null);

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.download_course_btn))
                .check(matches(withDrawable(R.drawable.ic_action_download)));

    }

    @Test
    public void showUpdateButtonIfCourseIsToUpdate() throws Exception {

        CourseInstallViewAdapter c = getBaseCourse();
        c.setDownloading(false);
        c.setInstalled(true);
        c.setToUpdate(true);

        givenThereAreSomeCourses(2, c);

        downloadActivityTestRule.launchActivity(null);

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.download_course_btn))
                .check(matches(withDrawable(R.drawable.ic_action_refresh)));

    }

    @Test
    public void buttonEnabledIfCourseIsToUpdate() throws Exception {

        CourseInstallViewAdapter c = getBaseCourse();
        c.setDownloading(false);
        c.setInstalled(true);
        c.setToUpdate(true);

        givenThereAreSomeCourses(2, c);

        downloadActivityTestRule.launchActivity(null);

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.download_course_btn))
                .check(matches(isEnabled()));

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

        downloadActivityTestRule.launchActivity(null);

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.download_course_btn))
                .perform(click())
                .check(matches(withDrawable(R.drawable.ic_action_accept)));


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


        downloadActivityTestRule.launchActivity(null);

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.download_course_btn))
                .perform(click())
                .check(matches(withDrawable(R.drawable.ic_action_accept)));

    }

    @Test
    public void buttonEnabledIfCourseIsNotInstalled() throws Exception {

        CourseInstallViewAdapter c = getBaseCourse();
        c.setDownloading(false);
        c.setInstalled(false);

        givenThereAreSomeCourses(2, c);

        downloadActivityTestRule.launchActivity(null);

//        onView(withRecyclerView(R.id.recycler_tags).atPosition(0)).perform(click());

        onView(withRecyclerView(R.id.recycler_tags)
                .atPositionOnView(0, R.id.download_course_btn))
                .check(matches(isEnabled()));

    }
    

}
