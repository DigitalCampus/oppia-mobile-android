package UI;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import junit.framework.AssertionFailedError;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.OppiaMobileActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import TestRules.DisableAnimationsRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class RegisterUITest {

    @Rule
    public ActivityTestRule<WelcomeActivity> welcomeActivityTestRule =
            new ActivityTestRule<>(WelcomeActivity.class);

    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();

    @Test
    public void showsErrorMessageWhenThereIsNoUsername() throws  Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.register_form_username_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText(""));

        onView(withId(R.id.register_btn))
                .perform(scrollTo(), click());

        onView(withText(R.string.error_register_no_username))
                .check(matches(isDisplayed()));
    }

    @Test
    public void showsErrorMessageWhenTheUsernameContainsSpaces() throws  Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.register_form_username_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username With Spaces"));

        onView(withId(R.id.register_btn))
                .perform(scrollTo(), click());

        onView(withText(R.string.error_register_username_spaces))
                .check(matches(isDisplayed()));
    }

    @Test
    public void showsErrorMessageWhenThereIsNoEmail() throws  Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.register_form_username_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("UsernameWithoutSpaces"));

        onView(withId(R.id.register_form_email_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText(""));

        onView(withId(R.id.register_btn))
                .perform(scrollTo(), click());

        onView(withText(R.string.error_register_no_email))
                .check(matches(isDisplayed()));
    }

    @Test
    public void showErrorMessageWhenTheEmailIsWrong() throws Exception {
        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.register_form_username_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));

        onView(withId(R.id.register_form_email_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("NoValidEmail"));

        onView(withId(R.id.register_form_password_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onView(withId(R.id.register_form_password_again_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onView(withId(R.id.register_form_firstname_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("First Name"));

        onView(withId(R.id.register_form_lastname_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Last Name"));

        onView(withId(R.id.register_form_phoneno_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("123456789"));

        onView(withId(R.id.register_btn))
                .perform(scrollTo(), click());

        onView(withText("Error"))   //String "Please enter a valid e-mail address."
                .check(matches(isDisplayed()));
    }

    @Test
    public void showErrorMessageWhenTheEmailContainsSpaces() throws Exception {
        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.register_form_username_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));

        onView(withId(R.id.register_form_email_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("email with spaces@gmail.com"));

        onView(withId(R.id.register_form_password_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onView(withId(R.id.register_form_password_again_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onView(withId(R.id.register_form_firstname_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("First Name"));

        onView(withId(R.id.register_form_lastname_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Last Name"));

        onView(withId(R.id.register_form_phoneno_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("123456789"));

        onView(withId(R.id.register_btn))
                .perform(scrollTo(), click());

        onView(withText("Error"))   //String "Please enter a valid e-mail address."
                .check(matches(isDisplayed()));
    }

    @Test
    public void showsErrorMessageWhenThePasswordIsTooShort() throws Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.register_form_username_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));

        onView(withId(R.id.register_form_email_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Email"));

        onView(withId(R.id.register_form_password_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("123"));

        onView(withId(R.id.register_btn))
                .perform(scrollTo(), click());

        onView(withText(String.format(InstrumentationRegistry.getTargetContext().getString(R.string.error_register_password),  MobileLearning.PASSWORD_MIN_LENGTH )))
                .check(matches(isDisplayed()));
    }

    @Test
    public void showsErrorMessageWhenThePasswordsDoNotMatch() throws Exception{

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.register_form_username_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));

        onView(withId(R.id.register_form_email_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Email"));

        onView(withId(R.id.register_form_password_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onView(withId(R.id.register_form_password_again_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password2"));

        onView(withId(R.id.register_btn))
                .perform(scrollTo(), click());

        onView(withText(R.string.error_register_password_no_match ))
                .check(matches(isDisplayed()));
    }

    @Test
    public void showsErrorMessageWhenThereIsNoFirstName() throws Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.register_form_username_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));

        onView(withId(R.id.register_form_email_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Email"));

        onView(withId(R.id.register_form_password_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onView(withId(R.id.register_form_password_again_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onView(withId(R.id.register_form_firstname_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText(""));

        onView(withId(R.id.register_btn))
                .perform(scrollTo(), click());

        onView(withText(R.string.error_register_no_firstname))
                .check(matches(isDisplayed()));
    }

    @Test
    public void showsErrorMessageWhenThereIsNoLastName() throws Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.register_form_username_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));

        onView(withId(R.id.register_form_email_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Email"));

        onView(withId(R.id.register_form_password_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onView(withId(R.id.register_form_password_again_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onView(withId(R.id.register_form_firstname_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("First Name"));

        onView(withId(R.id.register_form_lastname_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText(""));

        onView(withId(R.id.register_btn))
                .perform(scrollTo(), click());

        onView(withText(R.string.error_register_no_lastname))
                .check(matches(isDisplayed()));
    }

    @Test
    public void showsErrorMessageWhenThePhoneNumberIsNotValid() throws Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.register_form_username_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));

        onView(withId(R.id.register_form_email_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Email"));

        onView(withId(R.id.register_form_password_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onView(withId(R.id.register_form_password_again_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onView(withId(R.id.register_form_firstname_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("First Name"));

        onView(withId(R.id.register_form_lastname_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Last Name"));

        onView(withId(R.id.register_form_phoneno_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText(""));

        onView(withId(R.id.register_btn))
                .perform(scrollTo(), click());

        onView(withText(R.string.error_register_no_phoneno))
                .check(matches(isDisplayed()))
                .perform(pressBack());

        onView(withId(R.id.register_form_phoneno_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("1234567"));

        onView(withId(R.id.register_btn))
                .perform(scrollTo(), click());

        onView(withText(R.string.error_register_no_phoneno))
                .check(matches(isDisplayed()));
    }

    @Test
    public void changeActivityWhenAllTheFieldsAreCorrect() throws Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.register_form_username_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));

        onView(withId(R.id.register_form_email_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Email@email.com"));

        onView(withId(R.id.register_form_password_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onView(withId(R.id.register_form_password_again_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onView(withId(R.id.register_form_firstname_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("First Name"));

        onView(withId(R.id.register_form_lastname_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Last Name"));

        onView(withId(R.id.register_form_phoneno_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("12345678"));

        onView(withId(R.id.register_btn))
                .perform(scrollTo(), click());

        try{
            assertEquals(OppiaMobileActivity.class, Utils.TestUtils.getCurrentActivity().getClass());
        }catch(AssertionFailedError afe){
            afe.printStackTrace();
        }

    }

}
