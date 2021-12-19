package androidTestFiles.UI;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidTestFiles.Utils.MockedApiEndpointTest;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class ResetUITest extends MockedApiEndpointTest {

    private static final String ERROR_RESET_RESPONSE = "responses/response_400_reset.json";


    @Test
    public void showsErrorMessageWhenThereIsNoUsername() throws Exception {

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {

            onView(withId(R.id.welcome_login))
                    .perform(scrollTo(), click());
            onView(withId(R.id.btn_reset_password))
                    .perform(click());

            onView(withId(R.id.reset_username_field))
                    .perform(closeSoftKeyboard(), scrollTo(), typeText(""));

            onView(withId(R.id.reset_btn))
                    .perform(scrollTo(), click());

            onView(withText(R.string.error_register_no_username))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void clickResetButton_WrongUsername() throws Exception {

        startServer(400, ERROR_RESET_RESPONSE, 0);

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {

            onView(withId(R.id.welcome_login))
                    .perform(scrollTo(), click());
            onView(withId(R.id.btn_reset_password))
                    .perform(click());

            onView(withId(R.id.reset_username_field))
                    .perform(closeSoftKeyboard(), scrollTo(), typeText("WrongUsername"));

            onView(withId(R.id.reset_btn))
                    .perform(scrollTo(), click());

            onView(withText(R.string.error_reset_password))
                    .check(matches(isDisplayed()));
        }
    }
}
