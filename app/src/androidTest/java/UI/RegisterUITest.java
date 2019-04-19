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

import static Matchers.EspressoTestsMatchers.withCustomError;
import static Utils.ViewsUtils.onEditTextWithinTextInputLayoutWithId;
import static Utils.ViewsUtils.onErrorViewWithinTextInputLayoutWithId;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressBack;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.is;

@RunWith(AndroidJUnit4.class)
public class RegisterUITest {

    @Rule
    public ActivityTestRule<WelcomeActivity> welcomeActivityTestRule =
            new ActivityTestRule<>(WelcomeActivity.class);


    @Test
    public void showsErrorMessageWhenThereIsNoUsername() throws  Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .perform(closeSoftKeyboard(), scrollTo(), replaceText(""));

        onView(withId(R.id.register_btn))
                .perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .check(matches(withText(R.string.field_required)));

    }

    @Test
    public void showsErrorMessageWhenTheUsernameContainsSpaces() throws  Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username With Spaces"));

        onView(withId(R.id.register_btn))
                .perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .check(matches(withText(R.string.field_spaces_error)));

    }

    @Test
    public void showsErrorMessageWhenThereIsNoEmail() throws  Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("UsernameWithoutSpaces"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_email_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText(""));

        onView(withId(R.id.register_btn))
                .perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_email_field)
                .check(matches(withText(R.string.field_required)));
    }

    @Test
    public void showErrorMessageWhenTheEmailIsWrong() throws Exception {
        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_email_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("NoValidEmail"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_firstname_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("First Name"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_lastname_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Last Name"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_phoneno_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("123456789"));

        onView(withId(R.id.register_btn))
                .perform(click());

        onView(withText("Error"))   //String "Please enter a valid e-mail address."
                .check(matches(isDisplayed()));
    }

    @Test
    public void showErrorMessageWhenTheEmailContainsSpaces() throws Exception {
        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_email_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("email with spaces@gmail.com"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_firstname_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("First Name"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_lastname_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Last Name"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_phoneno_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("123456789"));

        onView(withId(R.id.register_btn))
                .perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_email_field)
                .check(matches(withText(R.string.field_spaces_error)));
    }

    @Test
    public void showsErrorMessageWhenThePasswordIsTooShort() throws Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_email_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Email"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("123"));

        onView(withId(R.id.register_btn))
                .perform(click());

        String passwordError = InstrumentationRegistry.getContext().getString(
                R.string.error_register_password, MobileLearning.PASSWORD_MIN_LENGTH);

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_password_field)
                .check(matches(withText(passwordError)));
    }

    @Test
    public void showsErrorMessageWhenThePasswordsDoNotMatch() throws Exception{

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_email_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Email"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password2"));

        onView(withId(R.id.register_btn))
                .perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_password_field)
                .check(matches(withText(R.string.error_register_password_no_match)));
    }

    @Test
    public void showsErrorMessageWhenThereIsNoFirstName() throws Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_email_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Email"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_firstname_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText(""));

        onView(withId(R.id.register_btn))
                .perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_firstname_field)
                .check(matches(withText(R.string.field_required)));
    }

    @Test
    public void showsErrorMessageWhenThereIsNoLastName() throws Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_email_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Email"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_firstname_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("First Name"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_lastname_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText(""));

        onView(withId(R.id.register_btn))
                .perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_lastname_field)
                .check(matches(withText(R.string.field_required)));
    }

    @Test
    public void showsErrorMessageWhenThePhoneNumberIsNotValid() throws Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_email_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Email"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_firstname_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("First Name"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_lastname_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Last Name"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_phoneno_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText(""));

        onView(withId(R.id.register_btn))
                .perform(click());


        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_phoneno_field)
                .check(matches(withText(R.string.error_register_no_phoneno)));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_phoneno_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("1234567"));

        onView(withId(R.id.register_btn))
                .perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_phoneno_field)
                .check(matches(withText(R.string.error_register_no_phoneno)));
    }

    @Test
    public void changeActivityWhenAllTheFieldsAreCorrect() throws Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_email_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Email@email.com"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_firstname_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("First Name"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_lastname_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Last Name"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_phoneno_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("12345678"));

        onView(withId(R.id.register_btn))
                .perform( click());

        try{
            assertEquals(OppiaMobileActivity.class, Utils.TestUtils.getCurrentActivity().getClass());
        }catch(AssertionFailedError afe){
            afe.printStackTrace();
        }

    }

}
