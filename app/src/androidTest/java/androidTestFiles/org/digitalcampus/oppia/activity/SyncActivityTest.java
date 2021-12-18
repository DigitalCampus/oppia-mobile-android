package androidTestFiles.org.digitalcampus.oppia.activity;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.digitalcampus.oppia.activity.ScorecardActivity;
import org.digitalcampus.oppia.activity.SyncActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SyncActivityTest {

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    }

    @Test
    public void testActivityOpenNoBundle() {
        try (ActivityScenario<SyncActivity> scenario = ActivityScenario.launch(SyncActivity.class)) {

        }
    }
}
