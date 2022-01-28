package androidTestFiles.UI.trackers;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.equalTo;

import android.Manifest;
import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.ActivityLogActivity;
import org.digitalcampus.oppia.database.DbHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidTestFiles.TestRules.DaggerInjectMockUITest;
import androidTestFiles.Utils.Assertions.RecyclerViewItemCountAssertion;
import androidTestFiles.Utils.MockedApiEndpointTest;
import androidTestFiles.database.sampledata.UserData;
import androidx.test.rule.GrantPermissionRule;

@RunWith(AndroidJUnit4.class)
public class ExportTrackersTest extends DaggerInjectMockUITest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

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
    public void exportTrackers_success() throws Exception {

        db.insertTracker(1, "anyDigest", "anyData", "anyType", true, "anyEvent", 10);

        try (ActivityScenario<ActivityLogActivity> scenario = ActivityScenario.launch(ActivityLogActivity.class)) {

            assertThat(db.getUnexportedTrackersCount(), equalTo(1));
            onView(withId(R.id.highlight_to_export)).check(matches(withText("1")));

            onView(withId(R.id.export_btn)).perform(click());
            onView(withText(R.string.close)).perform(click());

            assertThat(db.getUnexportedTrackersCount(), equalTo(0));
            onView(withId(R.id.highlight_to_export)).check(matches(withText("0")));

            onView(withId(R.id.exported_files_list)).check(new RecyclerViewItemCountAssertion(1));

        }
    }

}
