package UI;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.OppiaMobileActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import TestRules.DisableAnimationsRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class LoginUITest {

    @Rule
    public ActivityTestRule<WelcomeActivity> welcomeActivityTestRule =
            new ActivityTestRule<>(WelcomeActivity.class);

    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();


    @Test
    public void showsErrorMessageWhenThereIsNoUsername() throws Exception{
        onView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());

        onView(withId(R.id.login_btn))
                .perform(scrollTo(), click());

        onView(withText(R.string.error_no_username))
                .check(matches(isDisplayed()));
    }

    @Test
    public void showsErrorMessageWhenTheUsernameOrPasswordAreWrong() throws Exception{
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
    public void changeActivityWhenTheCredentialsAreCorrect() throws Exception{

       onView(withId(R.id.welcome_login))
               .perform(scrollTo(), click());

       onView(withId(R.id.login_username_field))
               .perform(closeSoftKeyboard(), scrollTo(), typeText("aaaaaaa"));

       onView(withId(R.id.login_password_field))
               .perform(closeSoftKeyboard(), scrollTo(), typeText("aaaaaaa"));

       onView(withId(R.id.login_btn))
               .perform(scrollTo(), click());

       assertEquals(OppiaMobileActivity.class, Utils.TestUtils.getCurrentActivity().getClass());
    }
}
