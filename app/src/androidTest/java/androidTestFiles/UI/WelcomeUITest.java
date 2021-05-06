package androidTestFiles.UI;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidTestFiles.Utils.MockedApiEndpointTest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class WelcomeUITest extends MockedApiEndpointTest {

    @Rule
    public ActivityTestRule<WelcomeActivity> welcomeActivityTestRule =
            new ActivityTestRule<>(WelcomeActivity.class);

    @Test
    public void showsLoginFragmentOnLoginButtonClick(){
        onView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());

        onView(withId(R.id.login_btn))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void showsRegisterFragmentOnRegisterButtonClick() {
        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.register_title))
                .check(matches(isDisplayed()));
    }

    @Test
    public void showsResetFragmentOnResetTabClicked() {

        onView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());

        onView(withId(R.id.btn_reset_password))
                .perform(scrollTo(), click());

        onView(withId(R.id.reset_btn))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void resetPassword_goToLoginScreenAfterSuccess() {

        startServer(200, null, 0);

        onView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());

        onView(withId(R.id.btn_reset_password))
                .perform(scrollTo(), click());

        onView(withId(R.id.reset_username_field)).perform(typeText("username"));

        onView(withId(R.id.reset_btn)).perform(click());

        onView(withText(R.string.ok)).perform(click());

        onView(withId(R.id.login_btn))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void resetPassword_goToLoginScreenAfterError() {

        startServer(400, null, 0);

        onView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());

        onView(withId(R.id.btn_reset_password))
                .perform(scrollTo(), click());

        onView(withId(R.id.reset_username_field)).perform(typeText("username"));

        onView(withId(R.id.reset_btn)).perform(click());

        onView(withText(R.string.close)).perform(click());

        onView(withId(R.id.reset_btn))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void rememberUsername_goToLoginScreenAfterSuccess() {

        startServer(200, null, 0);

        onView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());

        onView(withId(R.id.btn_remember_username))
                .perform(scrollTo(), click());

        onView(withId(R.id.edit_email)).perform(typeText("valid@email.is"));

        onView(withId(R.id.btn_remember_username)).perform(click());

        onView(withText(R.string.ok)).perform(click());

        onView(withId(R.id.login_btn))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void rememberUsername_showsErrorIfEmailPatterInvalid() {

        startServer(200, null, 0);

        onView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());

        onView(withId(R.id.btn_remember_username))
                .perform(scrollTo(), click());

        onView(withId(R.id.edit_email)).perform(typeText("wrong-email"));

        onView(withId(R.id.btn_remember_username)).perform(click());

        onView(withText(R.string.error_register_email))
                .check(matches(isDisplayed()));
    }

    @Test
    public void rememberUsername_goToLoginScreenAfterError() {

        startServer(400, null, 0);

        onView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());

        onView(withId(R.id.btn_remember_username))
                .perform(scrollTo(), click());

        onView(withId(R.id.edit_email)).perform(typeText("valid@email.is"));

        onView(withId(R.id.btn_remember_username)).perform(click());

        onView(withText(R.string.close)).perform(click());

        onView(withId(R.id.btn_remember_username))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }
}
