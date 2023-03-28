package androidTestFiles.activities;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.is;

import static androidTestFiles.utils.UITestActionsUtils.waitForView;
import static androidTestFiles.utils.parent.BaseTest.MEDIA_FILE_VIDEO_TEST_1;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.utils.mediaplayer.VideoPlayerActivity;
import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidTestFiles.features.courseMedia.CourseMediaBaseTest;

@RunWith(AndroidJUnit4.class)
public class VideoPlayerActivityTest extends CourseMediaBaseTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private static final String MEDIA_TEST_FILENAME = "video-test-1.mp4";
    private static final int MEDIA_TEST_LENGHT_SECONDS = 4;


    private Intent getTestVideoActivityIntent() {

        copyMediaFromAssets(MEDIA_FILE_VIDEO_TEST_1);

        Course course = new Course("");
        course.setShortname("mock-course");

        Activity activity = new Activity();
        List<Media> mediaList = new ArrayList<>();
        mediaList.add(new Media(MEDIA_TEST_FILENAME, MEDIA_TEST_LENGHT_SECONDS));
        activity.setMedia(mediaList);
        activity.setDigest("xxx");

        Intent videoActivityIntent = new Intent(context, VideoPlayerActivity.class);
        Bundle tb = new Bundle();
        tb.putSerializable(VideoPlayerActivity.MEDIA_TAG, MEDIA_TEST_FILENAME);
        tb.putSerializable(Activity.TAG, activity);
        tb.putSerializable(Course.TAG, course);
        videoActivityIntent.putExtras(tb);
        return videoActivityIntent;
    }

    @Test
    public void checkVideoNotCompleted() throws InterruptedException {

        Intent videoActivityIntent = getTestVideoActivityIntent();

        try (ActivityScenario<VideoPlayerActivity> scenario = ActivityScenario.launchActivityForResult(videoActivityIntent)) {

            waitForView(isRoot()).perform(waitFor(TimeUnit.SECONDS.toMillis(MEDIA_TEST_LENGHT_SECONDS - 2)));

            Espresso.pressBackUnconditionally();

            assertThat(scenario.getResult().getResultCode(), is(android.app.Activity.RESULT_CANCELED));

        }

    }

    @Test
    public void checkVideoCompleted() throws InterruptedException {

        Intent videoActivityIntent = getTestVideoActivityIntent();

        try (ActivityScenario<VideoPlayerActivity> scenario = ActivityScenario.launchActivityForResult(videoActivityIntent)) {

            waitForView(isRoot()).perform(waitFor(TimeUnit.SECONDS.toMillis(MEDIA_TEST_LENGHT_SECONDS + 2)));

            waitForView(withId(R.id.continue_button)).perform(click());

            assertThat(scenario.getResult().getResultCode(), is(android.app.Activity.RESULT_OK));

        }

    }

    public static ViewAction waitFor(final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for " + millis + " milliseconds.";
            }

            @Override
            public void perform(UiController uiController, final View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }
}
