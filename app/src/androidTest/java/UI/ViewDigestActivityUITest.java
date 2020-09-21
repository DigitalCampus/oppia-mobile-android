package UI;

import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.ViewDigestActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.di.AppModule;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.User;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import it.cosenonjaviste.daggermock.DaggerMockRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;

@RunWith(AndroidJUnit4.class)
public class ViewDigestActivityUITest {

    @Rule public DaggerMockRule<AppComponent> daggerRule =
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


    @Mock CoursesRepository coursesRepository;
    @Mock User user;

    private Uri getUriForDigest(String digest){
        return new Uri.Builder()
                .scheme("https")
                .authority("demo.oppia-mobile.org")
                .appendPath("view")
                .appendQueryParameter("digest", digest)
                .build();
    }



    @Test
    public void showActivityWhenCorrectDigest() throws Exception {

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) {
                Activity act = new Activity();
                act.setDigest("XXXXX");
                return act;
            }
        }).when(coursesRepository).getActivityByDigest(any(), anyString());

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) {
                return new Course("");
            }
        }).when(coursesRepository).getCourse((Context) any(), anyLong(), anyLong());

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
    public void showErrorWhenNoUserConnected() throws Exception {

        user = null;
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
    }

}
