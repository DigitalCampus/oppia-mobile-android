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

import Utils.CourseUtils;
import it.cosenonjaviste.daggermock.DaggerMockRule;

import static Matchers.EspressoTestsMatchers.withDrawable;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
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


    @Mock CourseInstallRepository courseInstallRepository;
    @Mock CourseInstallerServiceDelegate courseInstallerServiceDelegate;

    private void givenThereAreSomeCourses(final int numberOfCourses) throws Exception{

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
                    courses.add(new CourseIntallViewAdapter("") {{
                        setShortname("Mocked Course Name");
                        setAuthorName("Mock Author");
                        setDownloadUrl("Mock URL");
                    }});
                }
                return null;
            }
        }).when(courseInstallRepository).refreshCourseList((Context) any(), (ArrayList<CourseIntallViewAdapter>) any(),
                (JSONObject) any(), anyString(), anyBoolean());

    }

    @Test
    public void showProgressBarOnDownloadCourse() throws Exception {

        givenThereAreSomeCourses(2);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Context ctx = (Context) invocationOnMock.getArguments()[0];
                Intent downloadIntent = new Intent(CourseIntallerService.BROADCAST_ACTION);
                downloadIntent.putExtra(CourseIntallerService.SERVICE_ACTION, CourseIntallerService.ACTION_DOWNLOAD);
                downloadIntent.putExtra(CourseIntallerService.SERVICE_URL, "Mock URL");
                downloadIntent.putExtra(CourseIntallerService.SERVICE_MESSAGE, "1");
                ctx.sendOrderedBroadcast(downloadIntent, null);

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
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

    }

    @Test
    public void showProgressBarOnInstallCourse() throws Exception {

        givenThereAreSomeCourses(2);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Context ctx = (Context) invocationOnMock.getArguments()[0];
                Intent downloadIntent = new Intent(CourseIntallerService.BROADCAST_ACTION);
                downloadIntent.putExtra(CourseIntallerService.SERVICE_ACTION, CourseIntallerService.ACTION_INSTALL);
                downloadIntent.putExtra(CourseIntallerService.SERVICE_URL, "Mock URL");
                downloadIntent.putExtra(CourseIntallerService.SERVICE_MESSAGE, "1");
                ctx.sendOrderedBroadcast(downloadIntent, null);

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
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

    }

    @Test
    public void doesNotShowProgressBarOnCompleteCourse() throws Exception {

        givenThereAreSomeCourses(2);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Context ctx = (Context) invocationOnMock.getArguments()[0];
                Intent downloadIntent = new Intent(CourseIntallerService.BROADCAST_ACTION);
                downloadIntent.putExtra(CourseIntallerService.SERVICE_ACTION, CourseIntallerService.ACTION_COMPLETE);
                downloadIntent.putExtra(CourseIntallerService.SERVICE_URL, "Mock URL");
                downloadIntent.putExtra(CourseIntallerService.SERVICE_MESSAGE, "1");
                ctx.sendOrderedBroadcast(downloadIntent, null);

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
                .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));

    }

    @Test
    public void showCancelButtonOnDownloadCourse() throws Exception {

        givenThereAreSomeCourses(2);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Context ctx = (Context) invocationOnMock.getArguments()[0];
                Intent downloadIntent = new Intent(CourseIntallerService.BROADCAST_ACTION);
                downloadIntent.putExtra(CourseIntallerService.SERVICE_ACTION, CourseIntallerService.ACTION_DOWNLOAD);
                downloadIntent.putExtra(CourseIntallerService.SERVICE_URL, "Mock URL");
                downloadIntent.putExtra(CourseIntallerService.SERVICE_MESSAGE, "1");
                ctx.sendOrderedBroadcast(downloadIntent, null);

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
                .onChildView(withId(R.id.download_course_btn))
                .check(matches(withDrawable(R.drawable.ic_action_cancel)));

    }

    @Test
    public void showDownloadButtonBeforeDownloadCourse() throws Exception {

        givenThereAreSomeCourses(2);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Context ctx = (Context) invocationOnMock.getArguments()[0];
                Intent downloadIntent = new Intent(CourseIntallerService.BROADCAST_ACTION);
                downloadIntent.putExtra(CourseIntallerService.SERVICE_ACTION, CourseIntallerService.ACTION_DOWNLOAD);
                downloadIntent.putExtra(CourseIntallerService.SERVICE_URL, "Mock URL");
                downloadIntent.putExtra(CourseIntallerService.SERVICE_MESSAGE, "1");
                ctx.sendOrderedBroadcast(downloadIntent, null);

                return null;
            }
        }).when(courseInstallerServiceDelegate).installCourse((Context) any(), (Intent) any(), (CourseIntallViewAdapter) any());

        tagSelectActivityTestRule.launchActivity(null);

        onData(anything())
                .inAdapterView(withId(R.id.tag_list))
                .atPosition(0)
                .onChildView(withId(R.id.download_course_btn))
                .check(matches(withDrawable(R.drawable.ic_action_download)));

    }

    @Test
    public void showAcceptButtonOnInstallComplete() throws Exception {

        givenThereAreSomeCourses(2);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Context ctx = (Context) invocationOnMock.getArguments()[0];
                Intent downloadIntent = new Intent(CourseIntallerService.BROADCAST_ACTION);
                downloadIntent.putExtra(CourseIntallerService.SERVICE_ACTION, CourseIntallerService.ACTION_COMPLETE);
                downloadIntent.putExtra(CourseIntallerService.SERVICE_URL, "Mock URL");
                downloadIntent.putExtra(CourseIntallerService.SERVICE_MESSAGE, "1");
                ctx.sendOrderedBroadcast(downloadIntent, null);

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
                .onChildView(withId(R.id.download_course_btn))
                .check(matches(withDrawable(R.drawable.ic_action_accept)));

    }

}
