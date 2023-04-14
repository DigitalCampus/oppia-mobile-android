package androidTestFiles.activities;

import android.Manifest;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidTestFiles.utils.parent.MockedApiEndpointTest;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

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

import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class WelcomeActivityUITest extends MockedApiEndpointTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Rule
    public ActivityScenarioRule<WelcomeActivity> welcomeActivityTestRule =
            new ActivityScenarioRule<>(WelcomeActivity.class);

    @Test
    public void showsLoginFragmentOnLoginButtonClick() throws InterruptedException {
        waitForView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());

        // Wait for viewpager transition
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(
                        () ->
                                waitForView(ViewMatchers.withId(R.id.login_btn))
                                        .check(matches(isCompletelyDisplayed()))
                );

        waitForView(withId(R.id.login_btn))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void showsRegisterFragmentOnRegisterButtonClick() throws InterruptedException {

        if (!BuildConfig.ALLOW_REGISTER_USER) {
            return;
        }

        waitForView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(
                        () ->
                                waitForView(ViewMatchers.withId(R.id.register_title))
                                        .check(matches(isCompletelyDisplayed()))
                );

        waitForView(withId(R.id.register_title))
                .check(matches(isDisplayed()));
    }

    @Test
    public void showsResetFragmentOnResetTabClicked() throws InterruptedException {

        waitForView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());

        waitForView(withId(R.id.btn_reset_password))
                .perform(scrollTo(), click());

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(
                        () ->
                                waitForView(ViewMatchers.withId(R.id.reset_btn))
                                        .check(matches(isCompletelyDisplayed()))
                );

        waitForView(withId(R.id.reset_btn))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void resetPassword_goToLoginScreenAfterSuccess() throws InterruptedException {

        startServer(200, null, 0);

        waitForView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());

        waitForView(withId(R.id.btn_reset_password))
                .perform(scrollTo(), click());

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(
                        () ->
                                waitForView(ViewMatchers.withId(R.id.reset_btn))
                                        .check(matches(isCompletelyDisplayed()))
                );

        waitForView(withId(R.id.reset_username_field)).perform(typeText("username"), closeSoftKeyboard());

        waitForView(withId(R.id.reset_btn)).perform(click());

        waitForView(withText(R.string.ok)).perform(click());

        waitForView(withId(R.id.login_btn))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void resetPassword_goToLoginScreenAfterError() throws InterruptedException {

        startServer(400, null, 0);

        waitForView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());

        waitForView(withId(R.id.btn_reset_password))
                .perform(scrollTo(), click());

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(
                        () ->
                                waitForView(ViewMatchers.withId(R.id.reset_btn))
                                        .check(matches(isCompletelyDisplayed()))
                );

        waitForView(withId(R.id.reset_username_field)).perform(typeText("username"), closeSoftKeyboard());

        waitForView(withId(R.id.reset_btn)).perform(click());

        waitForView(withText(R.string.close)).perform(click());

        waitForView(withId(R.id.reset_btn))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void rememberUsername_goToLoginScreenAfterSuccess() throws InterruptedException {

        startServer(200, null, 0);

        waitForView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());

        waitForView(withId(R.id.btn_remember_username))
                .perform(scrollTo(), click());

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(
                        () ->
                                waitForView(ViewMatchers.withId(R.id.edit_email))
                                        .check(matches(isCompletelyDisplayed()))
                );

        waitForView(withId(R.id.edit_email)).perform(typeText("valid@email.is"), closeSoftKeyboard());

        waitForView(withId(R.id.btn_remember_username)).perform(click());

        waitForView(withText(R.string.ok)).perform(click());

        waitForView(withId(R.id.login_btn))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void rememberUsername_showsErrorIfEmailPatterInvalid() throws InterruptedException {

        startServer(200, null, 0);

        waitForView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());

        waitForView(withId(R.id.btn_remember_username))
                .perform(scrollTo(), click());

        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(
                        () ->
                                waitForView(ViewMatchers.withId(R.id.edit_email))
                                        .check(matches(isCompletelyDisplayed()))
                );

        waitForView(withId(R.id.edit_email)).perform(typeText("wrong-email"), closeSoftKeyboard());

        waitForView(withId(R.id.btn_remember_username)).perform(click());

        waitForView(withText(R.string.error_register_email))
                .check(matches(isDisplayed()));
    }

    @Test
    public void rememberUsername_goToLoginScreenAfterError() throws InterruptedException {

        startServer(400, null, 0);

        waitForView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());

        waitForView(withId(R.id.btn_remember_username))
                .perform(scrollTo(), click());
        ;
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(
                        () ->
                                waitForView(ViewMatchers.withId(R.id.edit_email))
                                        .check(matches(isCompletelyDisplayed()))
                );

        waitForView(withId(R.id.edit_email)).perform(typeText("valid@email.is"), closeSoftKeyboard());

        waitForView(withId(R.id.btn_remember_username)).perform(click());

        waitForView(withText(R.string.close)).perform(click());

        waitForView(withId(R.id.btn_remember_username))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }
}
