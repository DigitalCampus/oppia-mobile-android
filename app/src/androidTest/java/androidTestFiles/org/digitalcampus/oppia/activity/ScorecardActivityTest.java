package androidTestFiles.org.digitalcampus.oppia.activity;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.digitalcampus.oppia.activity.ScorecardActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(AndroidJUnit4.class)
public class ScorecardActivityTest {

    @Test
    public void testActivityOpen() {
        try (ActivityScenario<ScorecardActivity> scenario = ActivityScenario.launch(ScorecardActivity.class)) {

        }
    }
}
