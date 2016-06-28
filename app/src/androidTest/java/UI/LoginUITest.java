package UI;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.OppiaMobileActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;

/**
 * Created by Alberto on 28/06/2016.
 */
@RunWith(AndroidJUnit4.class)
public class LoginUITest {

    @Rule
    public ActivityTestRule<WelcomeActivity> welcomeActivityTestRule =
            new ActivityTestRule<WelcomeActivity>(WelcomeActivity.class);


    @Test
    public void clickLoginButton_NoUser() throws Exception{
        onView(withId(R.id.welcome_login))
                .perform(click());

        onView(withId(R.id.login_btn))
                .perform(click());

        onView(withText(R.string.error_no_username))
                .check(matches(isDisplayed()));
    }

    @Test
    public void clickLoginButton_WrongUser() throws Exception{
        onView(withId(R.id.welcome_login))
                .perform(click());

        onView(withId(R.id.login_username_field))
                .perform(typeText("WrongUsername"), closeSoftKeyboard());

        onView(withId(R.id.login_password_field))
                .perform(typeText("WrongPassword"), closeSoftKeyboard());

        onView(withId(R.id.login_btn))
                .perform(click());

        onView(withText(R.string.error_login))
                .check(matches(isDisplayed()));
    }

    @Test
    public void clickLoginButton_CorrectUser() throws Exception{

       try {
           new RegisterUITest().clickRegisterButton_RegisterSuccessful();
       }catch(Exception e) {
           onView(withText("Error"))
                   .check(matches(isDisplayed()))
                   .perform(pressBack());
       } finally {

           onView(withId(R.id.activity_about_pager))
                   .perform(swipeLeft());

           onView(withId(R.id.login_username_field))
                   .perform(typeText("Username"), closeSoftKeyboard());

           onView(withId(R.id.login_password_field))
                   .perform(typeText("password1"), closeSoftKeyboard());

           onView(withId(R.id.login_btn))
                   .perform(click());

           assertEquals(Utils.TestUtils.getCurrentActivity().getClass(), OppiaMobileActivity.class);
       }
    }
}
