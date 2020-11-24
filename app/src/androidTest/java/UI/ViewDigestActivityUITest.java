package UI;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.ViewDigestActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.di.AppModule;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.CompleteCourseProvider;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.ParseCourseXMLTask;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import androidx.test.espresso.core.internal.deps.guava.collect.Iterables;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import androidx.test.runner.lifecycle.Stage;
import androidx.test.uiautomator.UiDevice;

import junit.framework.Assert;

import Utils.CourseUtils;
import Utils.MockedApiEndpointTest;
import Utils.TestUtils;
import it.cosenonjaviste.daggermock.DaggerMockRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.Espresso.pressBackUnconditionally;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

@RunWith(AndroidJUnit4.class)
public class ViewDigestActivityUITest extends MockedApiEndpointTest {

    private static final String VALID_COURSE_INFO_RESPONSE = "responses/response_200_course_info.json";

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
    public ActivityTestRule<ViewDigestActivity> viewDigestActivityTestRule =
            new ActivityTestRule<>(ViewDigestActivity.class, false, false);


    @Mock
    CoursesRepository coursesRepository;
    @Mock
    CompleteCourseProvider completeCourseProvider;
    @Mock
    User user;

    private Uri.Builder getBaseUriBuilder() {
        return new Uri.Builder()
                .scheme("https")
                .authority("demo.oppia-mobile.org")
                .appendPath("view");
    }

    private Uri getUriForDigest(String digest) {
        return getBaseUriBuilder()
                .appendQueryParameter("digest", digest)
                .build();
    }

    private Uri getUriForCourse(String courseShortname) {
        return getBaseUriBuilder()
                .appendQueryParameter("course", courseShortname)
                .build();
    }


    @Test
    public void showActivityWhenCorrectDigest() throws Exception {

        doAnswer(invocationOnMock -> {
            Activity act = new Activity();
            act.setDigest("XXXXX");
            return act;
        }).when(coursesRepository).getActivityByDigest(any(), anyString());

        doAnswer(invocationOnMock -> new Course("")).when(coursesRepository).getCourse(any(), anyLong(), anyLong());

        doAnswer(invocation -> "test").when(user).getUsername();

        Instrumentation.ActivityMonitor am = new Instrumentation.ActivityMonitor("org.digitalcampus.oppia.activity.CourseIndexActivity", null, true);
        InstrumentationRegistry.getInstrumentation().addMonitor(am);

        String digest = "XXXXX";
        Intent startIntent = new Intent(Intent.ACTION_VIEW, getUriForDigest(digest));
        viewDigestActivityTestRule.launchActivity(startIntent);

        assertTrue(InstrumentationRegistry.getInstrumentation().checkMonitorHit(am, 1));

    }

    @Test
    public void showErrorWhenIncorrectDigest() throws Exception {

        doAnswer(invocationOnMock -> null).when(coursesRepository).getActivityByDigest(any(), anyString());

        String digest = "XXXXX";
        Intent startIntent = new Intent(Intent.ACTION_VIEW, getUriForDigest(digest));
        viewDigestActivityTestRule.launchActivity(startIntent);

        onView(withId(R.id.course_card))
                .check(matches(not(isDisplayed())));
    }

    @Test
    public void showErrorWhenNoDigest() throws Exception {

        viewDigestActivityTestRule.launchActivity(null);

        onView(withId(R.id.course_card))
                .check(matches(not(isDisplayed())));

        onView(withId(R.id.error_text))
                .check(matches(isDisplayed()));

    }


    @Test
    public void showGoToLoginButtonWhenUserNotLoggedIn() throws Exception {

        doAnswer(invocation -> null).when(user).getUsername();

        Intent startIntent = new Intent(Intent.ACTION_VIEW, getUriForCourse("xx"));
        viewDigestActivityTestRule.launchActivity(startIntent);

        onView(withId(R.id.course_card))
                .check(matches(not(isDisplayed())));

        onView(withId(R.id.error_text))
                .check(matches(isDisplayed()));

        onView(withId(R.id.btn_login_register))
                .check(matches(isDisplayed()));
    }

    @Test
    public void hideLoginButtonWhenSuccessfulLoginOrRegister() throws Exception {

        startServer(200, null, 0);

        doAnswer(invocationOnMock -> null).when(coursesRepository).getCourseByShortname((Context) any(), anyString(), anyLong());

        doAnswer(invocation -> null).when(user).getUsername();

        Intent startIntent = new Intent(Intent.ACTION_VIEW, getUriForCourse("xx"));
        viewDigestActivityTestRule.launchActivity(startIntent);

        onView(withId(R.id.btn_login_register)).perform(click());

        android.app.Activity welcomeActivity = TestUtils.getCurrentActivity();

        assertEquals(WelcomeActivity.class, welcomeActivity.getClass());

        doAnswer(invocation -> "any_username").when(user).getUsername();
        welcomeActivity.setResult(android.app.Activity.RESULT_OK);
        welcomeActivity.finish();

        android.app.Activity activity = TestUtils.getCurrentActivity();

        assertEquals(ViewDigestActivity.class, activity.getClass());

        onView(withId(R.id.btn_login_register))
                .check(matches(not(isDisplayed())));


    }



    // COURSE PARAMETER


    @Test
    public void showActivityWhenCourseIsInstalled() throws Exception {

        doAnswer(invocationOnMock -> new Course()).when(coursesRepository).getCourseByShortname((Context) any(), anyString(), anyLong());

        doAnswer(invocation -> "test").when(user).getUsername();

        Instrumentation.ActivityMonitor am = new Instrumentation.ActivityMonitor("org.digitalcampus.oppia.activity.CourseIndexActivity", null, true);
        InstrumentationRegistry.getInstrumentation().addMonitor(am);

        String course = "XXXXX";
        Intent startIntent = new Intent(Intent.ACTION_VIEW, getUriForCourse(course));
        viewDigestActivityTestRule.launchActivity(startIntent);

        assertTrue(InstrumentationRegistry.getInstrumentation().checkMonitorHit(am, 1));
    }

    @Test
    public void showDownloadButtonIfCourseIsNotInstalled_ValidCourse() throws Exception {

        startServer(200, VALID_COURSE_INFO_RESPONSE, 0);

        doAnswer(invocationOnMock -> null).when(coursesRepository).getCourseByShortname((Context) any(), anyString(), anyLong());

        doAnswer(invocation -> "test").when(user).getUsername();

        Intent startIntent = new Intent(Intent.ACTION_VIEW, getUriForCourse("xx"));
        viewDigestActivityTestRule.launchActivity(startIntent);

        onView(withId(R.id.download_course_btn))
                .check(matches(isDisplayed()));

    }

    @Test
    public void showErrorIfCourseIsNotInstalled_InvalidCourse() throws Exception {

        startServer(200, null, 0);

        doAnswer(invocationOnMock -> null).when(coursesRepository).getCourseByShortname((Context) any(), anyString(), anyLong());

        doAnswer(invocation -> "test").when(user).getUsername();

        Intent startIntent = new Intent(Intent.ACTION_VIEW, getUriForCourse("xx"));
        viewDigestActivityTestRule.launchActivity(startIntent);

        onView(withId(R.id.download_course_btn))
                .check(matches(not(isDisplayed())));

        onView(withId(R.id.error_text))
                .check(matches(isDisplayed()));

    }


    @Test
    public void goBackToWeblinkSourceWhenBackButtonPressed() throws Exception {

        final CompleteCourse completeCourse = CourseUtils.createMockCompleteCourse(5, 7);
        Mockito.doAnswer((Answer<Void>) invocation -> {
            Context ctx = (Context) invocation.getArguments()[0];
            ((ParseCourseXMLTask.OnParseXmlListener) ctx).onParseComplete(completeCourse);

            return null;

        }).when(completeCourseProvider).getCompleteCourseAsync(any(Context.class), any(Course.class));

        doAnswer(invocationOnMock -> completeCourse).when(coursesRepository).getCourseByShortname((Context) any(), anyString(), anyLong());

        doAnswer(invocation -> "test").when(user).getUsername();

        String course = completeCourse.getShortname();
        Intent startIntent = new Intent(Intent.ACTION_VIEW, getUriForCourse(course));
        viewDigestActivityTestRule.launchActivity(startIntent);

        android.app.Activity courseIndexActivity = TestUtils.getCurrentActivity();
        assertEquals(CourseIndexActivity.class, courseIndexActivity.getClass());

        pressBackUnconditionally();

        assertTrue(courseIndexActivity.isDestroyed());
    }


    @Test
    public void goToMainActivityWhenHomeButtonPressed() throws Exception {

        final CompleteCourse completeCourse = CourseUtils.createMockCompleteCourse(5, 7);
        Mockito.doAnswer((Answer<Void>) invocation -> {
            Context ctx = (Context) invocation.getArguments()[0];
            ((ParseCourseXMLTask.OnParseXmlListener) ctx).onParseComplete(completeCourse);

            return null;

        }).when(completeCourseProvider).getCompleteCourseAsync(any(Context.class), any(Course.class));


        doAnswer(invocationOnMock -> completeCourse).when(coursesRepository).getCourseByShortname((Context) any(), anyString(), anyLong());

        doAnswer(invocation -> "test").when(user).getUsername();

        String course = completeCourse.getShortname();
        Intent startIntent = new Intent(Intent.ACTION_VIEW, getUriForCourse(course));
        viewDigestActivityTestRule.launchActivity(startIntent);

        assertEquals(CourseIndexActivity.class, TestUtils.getCurrentActivity().getClass());

        onView(withContentDescription(R.string.abc_action_bar_up_description)).perform(click());

        assertEquals(MainActivity.class, TestUtils.getCurrentActivity().getClass());
    }

}
