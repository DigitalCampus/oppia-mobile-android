package UI;

import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
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

@RunWith(AndroidJUnit4.class)
public class ResetUITest {

    @Rule
    public ActivityTestRule<WelcomeActivity> welcomeActivityTestRule =
            new ActivityTestRule<WelcomeActivity>(WelcomeActivity.class);


    @Test
    public void  showsErrorMessageWhenThereIsNoUsername() throws  Exception {

        onView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());
        onView(withId(R.id.forgot_btn))
                .perform(click());

        onView(withId(R.id.reset_username_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText(""));

        onView(withId(R.id.reset_btn))
                .perform(scrollTo(), click());

        onView(withText(R.string.error_register_no_username))
                .check(matches(isDisplayed()));
    }

    @Test
    public void clickResetButton_WrongUsername() throws  Exception {
        onView(withId(R.id.welcome_login))
                .perform(scrollTo(), click());
        onView(withId(R.id.forgot_btn))
                .perform(click());

        onView(withId(R.id.reset_username_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("WrongUsername"));

        onView(withId(R.id.reset_btn))
                .perform(scrollTo(), click());

        onView(withText(R.string.error_reset))
                .check(matches(isDisplayed()));
    }
}
