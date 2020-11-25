package UI;

import android.content.Intent;
import android.os.Bundle;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.utils.mediaplayer.VideoPlayerActivity;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class VideoUITest extends CourseMediaBaseTest {

    private static final String MEDIA_TEST_FILENAME = "video-test-1.mp4";
    private static final int MEDIA_TEST_LENGHT_SECONDS = 4;


    private Intent getTestVideoActivityIntent() {

        copyMediaFromAssets(MEDIA_FILE_VIDEO_TEST_1);

        Course course = new Course("");
        course.setShortname("Mock Course");

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

        try (ActivityScenario<VideoPlayerActivity> scenario = ActivityScenario.launch(videoActivityIntent)) {

            Thread.sleep((MEDIA_TEST_LENGHT_SECONDS - 1) * 1000);

            Espresso.pressBackUnconditionally();

            assertThat(scenario.getResult().getResultCode(), is(android.app.Activity.RESULT_CANCELED));

        }

    }

    @Test
    public void checkVideoCompleted() throws InterruptedException {

        Intent videoActivityIntent = getTestVideoActivityIntent();

        try (ActivityScenario<VideoPlayerActivity> scenario = ActivityScenario.launch(videoActivityIntent)) {

            Thread.sleep((MEDIA_TEST_LENGHT_SECONDS + 1) * 1000);

            onView(withId(R.id.continue_button)).perform(click());

            assertThat(scenario.getResult().getResultCode(), is(android.app.Activity.RESULT_OK));

        }

    }
}
