package UI;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class WelcomeUITest {

    @Rule
    public ActivityTestRule<WelcomeActivity> welcomeActivityTestRule =
            new ActivityTestRule<>(WelcomeActivity.class);



    @Test
    public void showsLoginFragmentOnLoginButtonClick() throws Exception{
        onView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());

        onView(withId(R.id.login_btn))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void showsRegisterFragmentOnRegisterButtonClick() throws Exception{
        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.register_title))
                .check(matches(isDisplayed()));
    }

    @Test
    public void showsResetFragmentOnResetTabClicked() throws Exception{

        onView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());

        onView(withId(R.id.forgot_btn))
                .perform(scrollTo(), click());

        onView(withId(R.id.reset_btn))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

}
