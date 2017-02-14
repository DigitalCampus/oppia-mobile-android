package UI;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.DownloadActivity;
import org.digitalcampus.oppia.adapter.CourseIntallViewAdapter;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.di.AppModule;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CourseInstallRepository;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.MultiLangInfo;
import org.digitalcampus.oppia.service.CourseInstallerServiceDelegate;
import org.digitalcampus.oppia.service.CourseIntallerService;
import org.digitalcampus.oppia.task.Payload;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;

import TestRules.DisableAnimationsRule;
import Utils.CourseUtils;
import it.cosenonjaviste.daggermock.DaggerMockRule;

import static Matchers.EspressoTestsMatchers.withDrawable;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.core.IsNot.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class DownloadActivityUITest {

    @Rule
    public DaggerMockRule<AppComponent> daggerRule =
            new DaggerMockRule<>(AppComponent.class, new AppModule((MobileLearning) InstrumentationRegistry.getInstrumentation()
                    .getTargetContext()
                    .getApplicationContext())).set(
                    new DaggerMockRule.ComponentSetter<AppComponent>() {
                        @Override public void setComponent(AppComponent component) {
                            MobileLearning app =
                                    (MobileLearning) InstrumentationRegistry.getInstrumentation()
                                            .getTargetContext()
                                            .getApplicationContext();
                            app.setComponent(component);
                        }
                    });

    @Rule
    public ActivityTestRule<DownloadActivity> tagSelectActivityTestRule =
            new ActivityTestRule<>(DownloadActivity.class, false, false);

    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();


    @Mock CourseInstallRepository courseInstallRepository;
    @Mock CourseInstallerServiceDelegate courseInstallerServiceDelegate;

    private void givenThereAreSomeCourses(final int numberOfCourses, final CourseIntallViewAdapter course) throws Exception{

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Context ctx = (Context) invocationOnMock.getArguments()[0];
                Payload response = new Payload();
                response.setResult(true);
                response.setResultResponse("{}");
                ((DownloadActivity) ctx).apiRequestComplete(response);
                return null;
            }
        }).when(courseInstallRepository).getCourseList((Context) any(), anyString());


        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                ArrayList<CourseIntallViewAdapter>  courses = (ArrayList<CourseIntallViewAdapter>) invocationOnMock.getArguments()[1];

                for(int i = 0; i < numberOfCourses; i++) {
                    courses.add(course);
                }
                return null;
            }
        }).when(courseInstallRepository).refreshCourseList((Context) any(), (ArrayList<CourseIntallViewAdapter>) any(),
                (JSONObject) any(), anyString(), anyBoolean());

    }

    private CourseIntallViewAdapter getBaseCourse(){
        CourseIntallViewAdapter c = new CourseIntallViewAdapter("");
        c.setShortname("Mocked Course Name");
        c.setDownloadUrl("Mock URL");

        return c;
    }

    private void sendBroadcast(Context ctx, String action){
        Intent intent = new Intent(CourseIntallerService.BROADCAST_ACTION);
        intent.putExtra(CourseIntallerService.SERVICE_ACTION, action);
        intent.putExtra(CourseIntallerService.SERVICE_URL, "Mock URL");
        intent.putExtra(CourseIntallerService.SERVICE_MESSAGE, "1");
        ctx.sendOrderedBroadcast(intent, null);
    }

    @Test
    public void showDraftTextIfCourseIsDraft() throws Exception{
        CourseIntallViewAdapter c = getBaseCourse();
        c.setDraft(true);

        givenThereAreSomeCourses(2, c);

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.course_draft))
                .check(matches(withText(R.string.course_draft)));
    }

    @Test
    public void dowsNotShowDraftTextIfCourseIsNotDraft() throws Exception{
        CourseIntallViewAdapter c = getBaseCourse();
        c.setDraft(false);

        givenThereAreSomeCourses(2, c);

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.course_draft))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void showTitleIfExists() throws Exception{
        CourseIntallViewAdapter c = getBaseCourse();
        final String title =  "Mock Title";
        c.setMultiLangInfo(new MultiLangInfo(){{
            setTitles(new ArrayList<Lang>() {{
                add(new Lang("en", title));
            }});
        }});

        givenThereAreSomeCourses(2, c);

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.course_title))
                .check(matches(withText(title)));
    }

    @Test
    public void showDefaultTitleIfNotExists() throws Exception{
        CourseIntallViewAdapter c = getBaseCourse();

        givenThereAreSomeCourses(2, c);

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.course_title))
                .check(matches(withText(R.string.no_title_set)));
    }

    @Test
    public void showDescriptionIfExists() throws Exception{
        CourseIntallViewAdapter c = getBaseCourse();
        final String description =  "Mock Description";
        c.setMultiLangInfo(new MultiLangInfo(){{
            setDescriptions(new ArrayList<Lang>() {{
                add(new Lang("en", description));
            }});
        }});

        givenThereAreSomeCourses(2, c);

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.course_description))
                .check(matches(withText(description)));
    }

    @Test
    public void showDefaultDescriptionIfNotExists() throws Exception{
        CourseIntallViewAdapter c = getBaseCourse();

        givenThereAreSomeCourses(2, c);

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.course_description))
                .check(matches(withText(R.string.no_description_set)));
    }

    @Test
    public void showCourseAuthorIfExists() throws Exception{
        CourseIntallViewAdapter c = getBaseCourse();
        String authorName = "Mock Author";
        c.setAuthorName(authorName);

        givenThereAreSomeCourses(2, c);

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.course_author))
                .check(matches(withText(authorName)));
    }

    @Test
    public void doesNotShowCourseAuthorIfNotExists() throws Exception{

        givenThereAreSomeCourses(2, getBaseCourse());

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.course_author))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void showCancelButtonOnDownloadingCourse() throws Exception {

        givenThereAreSomeCourses(2, getBaseCourse());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Context ctx = (Context) invocationOnMock.getArguments()[0];
                sendBroadcast(ctx, CourseIntallerService.ACTION_DOWNLOAD);
                return null;
            }
        }).when(courseInstallerServiceDelegate).installCourse((Context) any(), (Intent) any(), (CourseIntallViewAdapter) any());

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.download_course_btn))
                .perform(click())
                .check(matches(withDrawable(R.drawable.ic_action_cancel)));

    }

    @Test
    public void showCancelButtonOnInstallingCourse() throws Exception {

        CourseIntallViewAdapter c = getBaseCourse();

        givenThereAreSomeCourses(2, c);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Context ctx = (Context) invocationOnMock.getArguments()[0];
                sendBroadcast(ctx, CourseIntallerService.ACTION_DOWNLOAD);
                sendBroadcast(ctx, CourseIntallerService.ACTION_INSTALL);
                return null;
            }
        }).when(courseInstallerServiceDelegate).installCourse((Context) any(), (Intent) any(), (CourseIntallViewAdapter) any());

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.download_course_btn))
                .perform(click())
                .check(matches(withDrawable(R.drawable.ic_action_cancel)));

    }

    @Test
    public void showProgressBarOnDownloadCourse() throws Exception {

        givenThereAreSomeCourses(2, getBaseCourse());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Context ctx = (Context) invocationOnMock.getArguments()[0];
                sendBroadcast(ctx, CourseIntallerService.ACTION_DOWNLOAD);
                return null;
            }
        }).when(courseInstallerServiceDelegate).installCourse((Context) any(), (Intent) any(), (CourseIntallViewAdapter) any());

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.course_progress_bar))
                .check(doesNotExist());

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.download_course_btn))
                .perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.download_progress))
                .check(matches(isDisplayed()));

    }

    @Test
    public void showProgressBarOnInstallingCourse() throws Exception {

        givenThereAreSomeCourses(2, getBaseCourse());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Context ctx = (Context) invocationOnMock.getArguments()[0];
                sendBroadcast(ctx, CourseIntallerService.ACTION_DOWNLOAD);
                sendBroadcast(ctx, CourseIntallerService.ACTION_INSTALL);
                return null;
            }
        }).when(courseInstallerServiceDelegate).installCourse((Context) any(), (Intent) any(), (CourseIntallViewAdapter) any());

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.download_course_btn))
                .perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.download_progress))
                .check(matches(isDisplayed()));

    }

    @Test
    public void doesNotShowProgressBarOnNotInstallingNorDownloadingCourse() throws Exception {

        CourseIntallViewAdapter c = getBaseCourse();
        c.setDownloading(false);
        c.setInstalling(false);

        givenThereAreSomeCourses(2, c);

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.download_progress))
                .check(matches(not(isDisplayed())));

    }

    @Test
    public void doesNotShowProgressBarOnCourseInstalled() throws Exception {

        givenThereAreSomeCourses(2, getBaseCourse());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Context ctx = (Context) invocationOnMock.getArguments()[0];
                sendBroadcast(ctx, CourseIntallerService.ACTION_DOWNLOAD);
                sendBroadcast(ctx, CourseIntallerService.ACTION_INSTALL);
                sendBroadcast(ctx, CourseIntallerService.ACTION_COMPLETE);
                return null;
            }
        }).when(courseInstallerServiceDelegate).installCourse((Context) any(), (Intent) any(), (CourseIntallViewAdapter) any());

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.download_course_btn))
                .perform(click());


        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.download_progress))
                .check(matches(not(isDisplayed())));

    }

    @Test
    public void showDownloadButtonBeforeDownloadCourse() throws Exception {

        CourseIntallViewAdapter c = getBaseCourse();
        c.setDownloading(false);
        c.setInstalled(false);
        c.setToUpdate(false);

        givenThereAreSomeCourses(2, c);

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.download_course_btn))
                .check(matches(withDrawable(R.drawable.ic_action_download)));

    }

    @Test
    public void showUpdateButtonIfCourseIsToUpdate() throws Exception {

        CourseIntallViewAdapter c = getBaseCourse();
        c.setDownloading(false);
        c.setInstalled(true);
        c.setToUpdate(true);

        givenThereAreSomeCourses(2, c);

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.download_course_btn))
                .check(matches(withDrawable(R.drawable.ic_action_refresh)));
    }

    @Test
    public void buttonEnabledIfCourseIsToUpdate() throws Exception {

        CourseIntallViewAdapter c = getBaseCourse();
        c.setDownloading(false);
        c.setInstalled(true);
        c.setToUpdate(true);

        givenThereAreSomeCourses(2, c);

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.download_course_btn))
                .check(matches(isEnabled()));
    }

    @Test
    public void showUpdateButtonIfCourseIsToUpdateSchedule() throws Exception {

        CourseIntallViewAdapter c = getBaseCourse();
        c.setDownloading(false);
        c.setInstalled(true);
        c.setToUpdate(false);
        c.setToUpdateSchedule(true);

        givenThereAreSomeCourses(2, c);

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.download_course_btn))
                .check(matches(withDrawable(R.drawable.ic_action_refresh)));
    }

    @Test
    public void buttonEnabledIfCourseIsToUpdateSchedule() throws Exception {

        CourseIntallViewAdapter c = getBaseCourse();
        c.setDownloading(false);
        c.setInstalled(true);
        c.setToUpdate(false);
        c.setToUpdateSchedule(true);

        givenThereAreSomeCourses(2, c);

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.download_course_btn))
                .check(matches(isEnabled()));
    }

    @Test
    public void showAcceptButtonOnInstallComplete() throws Exception {

        givenThereAreSomeCourses(2, getBaseCourse());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Context ctx = (Context) invocationOnMock.getArguments()[0];
                sendBroadcast(ctx, CourseIntallerService.ACTION_DOWNLOAD);
                sendBroadcast(ctx, CourseIntallerService.ACTION_INSTALL);
                sendBroadcast(ctx, CourseIntallerService.ACTION_COMPLETE);
                return null;
            }
        }).when(courseInstallerServiceDelegate).installCourse((Context) any(), (Intent) any(), (CourseIntallViewAdapter) any());

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.download_course_btn))
                .perform(click())
                .check(matches(withDrawable(R.drawable.ic_action_accept)));

    }

    @Test
    public void showAcceptButtonOnUpdateComplete() throws Exception {
        CourseIntallViewAdapter c = getBaseCourse();
        c.setInstalled(true);
        c.setToUpdate(true);

        givenThereAreSomeCourses(2, c);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Context ctx = (Context) invocationOnMock.getArguments()[0];
                sendBroadcast(ctx, CourseIntallerService.ACTION_UPDATE);
                return null;
            }
        }).when(courseInstallerServiceDelegate).updateCourse((Context) any(), (Intent) any(), (CourseIntallViewAdapter) any());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Context ctx = (Context) invocationOnMock.getArguments()[0];
                sendBroadcast(ctx, CourseIntallerService.ACTION_INSTALL);
                sendBroadcast(ctx, CourseIntallerService.ACTION_COMPLETE);
                return null;
            }
        }).when(courseInstallerServiceDelegate).installCourse((Context) any(), (Intent) any(), (CourseIntallViewAdapter) any());


        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.download_course_btn))
                .perform(click())
                .check(matches(withDrawable(R.drawable.ic_action_accept)));

    }

    @Test
    public void buttonDisabledIfCourseIsNotToUpdateNorToUpdateSchedule() throws Exception {

        CourseIntallViewAdapter c = getBaseCourse();
        c.setDownloading(false);
        c.setInstalled(true);
        c.setToUpdate(false);
        c.setToUpdateSchedule(false);

        givenThereAreSomeCourses(2, c);

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.download_course_btn))
                .check(matches(not(isEnabled())));

    }

    @Test
    public void buttonEnabledIfCourseIsNotInstalled() throws Exception {

        CourseIntallViewAdapter c = getBaseCourse();
        c.setDownloading(false);
        c.setInstalled(false);

        givenThereAreSomeCourses(2, c);

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.download_course_btn))
                .check(matches(isEnabled()));
    }
    

}
