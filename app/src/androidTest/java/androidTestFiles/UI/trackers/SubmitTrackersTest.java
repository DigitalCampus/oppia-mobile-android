package androidTestFiles.UI.trackers;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.ActivityLogActivity;
import org.digitalcampus.oppia.database.DbHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.equalTo;

import androidTestFiles.Utils.MockedApiEndpointTest;
import androidTestFiles.database.sampledata.UserData;

@RunWith(AndroidJUnit4.class)
public class SubmitTrackersTest extends MockedApiEndpointTest {

    private static final String VALID_RESPONSE = "responses/response_200_trackers.json";

    private Context context;
    private DbHelper db;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        db = DbHelper.getInstance(context);

        UserData.loadData(context);
    }

    @After
    public void tearDown() throws Exception {
        UserData.deleteData(context);
    }


    @Test
    public void submitTrackers_success() throws Exception {

        startServer(200, VALID_RESPONSE, 0);

        db.insertTracker(1, "anyDigest", "anyData", "anyType", true, "anyEvent", 10);

        try (ActivityScenario<ActivityLogActivity> scenario = ActivityScenario.launch(ActivityLogActivity.class)) {

            assertThat(db.getUnsentTrackersCount(), equalTo(1));
            onView(withId(R.id.highlight_to_submit)).check(matches(withText("1")));

            onView(withId(R.id.submit_btn)).perform(click());

            assertThat(db.getUnsentTrackersCount(), equalTo(0));
            onView(withId(R.id.highlight_to_submit)).check(matches(withText("0")));

        }
    }

    @Test
    public void submitTrackers_error() throws Exception {

        startServer(500, EMPTY_JSON, 0);

        db.insertTracker(1, "anyDigest", "anyData", "anyType", true, "anyEvent", 10);

        try (ActivityScenario<ActivityLogActivity> scenario = ActivityScenario.launch(ActivityLogActivity.class)) {

            assertThat(db.getUnsentTrackersCount(), equalTo(1));
            onView(withId(R.id.highlight_to_submit)).check(matches(withText("1")));

            onView(withId(R.id.submit_btn)).perform(click());

            assertThat(db.getUnsentTrackersCount(), equalTo(1));
            onView(withId(R.id.highlight_to_submit)).check(matches(withText("1")));

        }
    }
}
