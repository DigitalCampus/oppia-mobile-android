package androidTestFiles.features.authentication.resetPassword;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.awaitility.Awaitility.await;
import static androidTestFiles.utils.UITestActionsUtils.waitForView;

import android.Manifest;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

import androidTestFiles.utils.parent.MockedApiEndpointTest;

@RunWith(AndroidJUnit4.class)
public class ResetPasswordUITest extends MockedApiEndpointTest {

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private static final String ERROR_RESET_RESPONSE = "responses/response_400_reset_password.json";

    @Test
    public void showsErrorMessageWhenThereIsNoUsername() throws Exception {

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {

            waitForView(withId(R.id.welcome_login))
                    .perform(scrollTo(), click());
            waitForView(withId(R.id.btn_reset_password))
                    .perform(click());

            await().atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(
                            () ->
                                    waitForView(ViewMatchers.withId(R.id.reset_username_field))
                                            .check(matches(isCompletelyDisplayed()))
                    );

            waitForView(withId(R.id.reset_username_field))
                    .perform(closeSoftKeyboard(), scrollTo(), typeText(""));

            waitForView(withId(R.id.reset_btn))
                    .perform(closeSoftKeyboard(), scrollTo(), click());

            waitForView(withText(R.string.error_register_no_username))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void clickResetButton_WrongUsername() throws Exception {

        startServer(400, ERROR_RESET_RESPONSE, 0);

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {

            waitForView(withId(R.id.welcome_login))
                    .perform(scrollTo(), click());
            waitForView(withId(R.id.btn_reset_password))
                    .perform(click());

            await().atMost(5, TimeUnit.SECONDS)
                    .untilAsserted(
                            () ->
                                    waitForView(ViewMatchers.withId(R.id.reset_username_field))
                                            .check(matches(isCompletelyDisplayed()))
                    );

            waitForView(withId(R.id.reset_username_field))
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("WrongUsername"));

            waitForView(withId(R.id.reset_btn))
                    .perform(closeSoftKeyboard(), scrollTo(), click());

            waitForView(withText(R.string.error_reset_password))
                    .check(matches(isDisplayed()));
        }
    }
}
