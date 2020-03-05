package UI;

import Utils.MockedApiEndpointTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class LoginUITest extends MockedApiEndpointTest {

    private static final String VALID_LOGIN_RESPONSE = "responses/response_201_login.json";
    private static final String WRONG_CREDENTIALS_RESPONSE = "responses/response_400_login.json";

    @Rule
    public ActivityTestRule<WelcomeActivity> welcomeActivityTestRule =
            new ActivityTestRule<>(WelcomeActivity.class, false, false);


    @Test
    public void showsErrorMessageWhenThereIsNoUsername() throws Exception{
        welcomeActivityTestRule.launchActivity(null);

        onView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());

        onView(withId(R.id.login_btn))
                .perform(scrollTo(), click());

        onView(withText(R.string.error_no_username))
                .check(matches(isDisplayed()));
    }

    @Test
    public void showsErrorMessageWhenTheUsernameOrPasswordAreWrong() throws Exception{

        startServer(400, WRONG_CREDENTIALS_RESPONSE, 0);
        welcomeActivityTestRule.launchActivity(null);

        onView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());

        onView(withId(R.id.login_username_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("WrongUsername"));

        onView(withId(R.id.login_password_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("WrongPassword"));

        onView(withId(R.id.login_btn))
                .perform(scrollTo(), click());

        onView(withText(R.string.error_login))
                .check(matches(isDisplayed()));
    }

    @Test
    public void changeActivityWhenTheCredentialsAreCorrect() throws Exception {

        startServer(200, VALID_LOGIN_RESPONSE, 0);
        welcomeActivityTestRule.launchActivity(null);

        onView(withId(R.id.welcome_login))
               .perform(scrollTo(), click());

        onView(withId(R.id.login_username_field))
               .perform(closeSoftKeyboard(), scrollTo(), typeText("valid_username"));

        onView(withId(R.id.login_password_field))
               .perform(closeSoftKeyboard(), scrollTo(), typeText("valid_password"));

        onView(withId(R.id.login_btn))
               .perform(scrollTo(), click());

        assertEquals(MainActivity.class, Utils.TestUtils.getCurrentActivity().getClass());
    }


}
