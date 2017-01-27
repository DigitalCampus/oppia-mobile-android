package UI;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import TestRules.DisableAnimationsRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class WelcomeUITest {

    @Rule
    public ActivityTestRule<WelcomeActivity> welcomeActivityTestRule =
            new ActivityTestRule<WelcomeActivity>(WelcomeActivity.class);

    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();

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

        onView(withId(R.id.register_btn))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

    @Test
    public void showsResetFragmentOnResetTabClicked() throws Exception{
        onView(withText(R.string.tab_title_reset))
                .perform(scrollTo(), click());

        onView(withId(R.id.reset_btn))
                .perform(scrollTo())
                .check(matches(isDisplayed()));
    }

}
