import android.app.Fragment;
import android.content.res.Resources;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.res.ResourcesCompat;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.fragments.RegisterFragment;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.core.deps.guava.base.Predicates.not;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Alberto on 27/06/2016.
 */
@RunWith(AndroidJUnit4.class)
public class RegisterUITest {

    @Rule
    public ActivityTestRule<WelcomeActivity> welcomeActivityTestRule =
            new ActivityTestRule<WelcomeActivity>(WelcomeActivity.class);

    @Test
    public void clickRegisterButton_UsernameEmpty() throws  Exception {

        onView(withId(R.id.welcome_register))
                .perform(click());

        onView(withId(R.id.register_form_username_field))
                .perform(typeText(""));

        onView(withId(R.id.register_btn))
                .perform(scrollTo(), click());

        onView(withText(R.string.error_register_no_username))
                .check(matches(isDisplayed()));
    }

    @Test
    public void clickRegisterButton_UsernameWithSpaces() throws  Exception {

        onView(withId(R.id.welcome_register))
                .perform(click());

        onView(withId(R.id.register_form_username_field))
                .perform(typeText("Username With Spaces"), closeSoftKeyboard());

        onView(withId(R.id.register_btn))
                .perform(scrollTo(), click());

        onView(withText(R.string.error_register_username_spaces))
                .check(matches(isDisplayed()));
    }

    @Test
    public void clickRegisterButton_EmailEmpty() throws  Exception {

        onView(withId(R.id.welcome_register))
                .perform(click());

        onView(withId(R.id.register_form_username_field))
                .perform(typeText("UsernameWithoutSpaces"), closeSoftKeyboard());

        onView(withId(R.id.register_form_email_field))
                .perform(typeText(""));

        onView(withId(R.id.register_btn))
                .perform(scrollTo(), click());

        onView(withText(R.string.error_register_no_email))
                .check(matches(isDisplayed()));
    }


    @Test
    public void clickRegisterButton_PasswordTooShort() throws  Exception {

        onView(withId(R.id.welcome_register))
                .perform(click());

        onView(withId(R.id.register_form_username_field))
                .perform(typeText("Username"), closeSoftKeyboard());

        onView(withId(R.id.register_form_email_field))
                .perform(typeText("Email"), closeSoftKeyboard());

        onView(withId(R.id.register_form_password_field))
                .perform(typeText("123"), closeSoftKeyboard());

        onView(withId(R.id.register_btn))
                .perform(scrollTo(), click());

        onView(withText(String.format(InstrumentationRegistry.getTargetContext().getString(R.string.error_register_password),  MobileLearning.PASSWORD_MIN_LENGTH )))
                .check(matches(isDisplayed()));
    }

}
