package androidTestFiles.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertEquals;
import android.content.Context;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.AboutActivity;
import org.digitalcampus.oppia.activity.PrivacyActivity;
import org.digitalcampus.oppia.analytics.Analytics;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class PrivacyActivityTest {

    private Context context;

    @Before
    public void setUp() {
        Intents.init();
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void privacySection_clickPrivacyPolicyOption_moveToAboutActivity() {

        try (ActivityScenario<PrivacyActivity> scenario = ActivityScenario.launch(PrivacyActivity.class)) {
            onView(allOf(withId(R.id.about_privacy_policy),
                    withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
                    .perform(click());

            intended(hasComponent(AboutActivity.class.getName()));

        }
    }

    @Ignore("Terms and Service button is hidden")
    @Test
    public void privacySection_clickTermsOfServiceOption_moveToAboutActivity() {

        try (ActivityScenario<PrivacyActivity> scenario = ActivityScenario.launch(PrivacyActivity.class)) {
            onView(allOf(withId(R.id.about_privacy_terms),
                    withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
                    .perform(click());

            intended(hasComponent(AboutActivity.class.getName()));

        }
    }

    @Test
    public void privacySection_clickDataCollectedOption_moveToAboutActivity() {

        try (ActivityScenario<PrivacyActivity> scenario = ActivityScenario.launch(PrivacyActivity.class)) {
            onView(allOf(withId(R.id.about_privacy_what),
                    withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
                    .perform(click());

            intended(hasComponent(AboutActivity.class.getName()));

        }
    }

    @Test
    public void privacySection_clickDataUsedOption_moveToAboutActivity() {

        try (ActivityScenario<PrivacyActivity> scenario = ActivityScenario.launch(PrivacyActivity.class)) {
            onView(allOf(withId(R.id.about_privacy_why),
                    withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
                    .perform(click());

            intended(hasComponent(AboutActivity.class.getName()));

        }
    }

    @Test
    public void analyticsSection_clickBugReportsOption_moveToAboutActivity() {

        try (ActivityScenario<PrivacyActivity> scenario = ActivityScenario.launch(PrivacyActivity.class)) {
            Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            boolean originalBugReportValue = Analytics.isBugReportEnabled(context);

            onView(allOf(withId(R.id.bugreport_checkbox),
                    withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
                    .perform(click());

            boolean newBugReportValue = Analytics.isBugReportEnabled(context);

            assertEquals(!originalBugReportValue, newBugReportValue);
        }
    }

    @Test
    public void analyticsSection_clickTrackingOption_moveToAboutActivity() {

        try (ActivityScenario<PrivacyActivity> scenario = ActivityScenario.launch(PrivacyActivity.class)) {
            Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            boolean originalTrackingValue = Analytics.isTrackingEnabled(context);

            onView(allOf(withId(R.id.analytics_checkbox),
                    withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)))
                    .perform(click());

            boolean newTrackingValue = Analytics.isTrackingEnabled(context);

            assertEquals(!originalTrackingValue, newTrackingValue);
        }
    }


}
