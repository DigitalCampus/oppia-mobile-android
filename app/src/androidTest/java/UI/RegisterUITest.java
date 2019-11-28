package UI;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import junit.framework.AssertionFailedError;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static Utils.ViewsUtils.onEditTextWithinTextInputLayoutWithId;
import static Utils.ViewsUtils.onErrorViewWithinTextInputLayoutWithId;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;

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

        // LMH_custom_start
        // onEditTextWithinTextInputLayoutWithId(R.id.register_form_phoneno_field)
        //        .perform(closeSoftKeyboard(), scrollTo(), typeText("123456789"));
        // LMH_custom_end

        onView(withId(R.id.register_btn))
                .perform(click());

        onView(withText("Error"))   //String "Please enter a valid e-mail address."
                .check(matches(isDisplayed()));
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

        String passwordError = InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getString(
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

        // LMH_custom_start
        // onEditTextWithinTextInputLayoutWithId(R.id.register_form_phoneno_field)
        //        .perform(closeSoftKeyboard(), scrollTo(), typeText(""));
        // LMH_custom_end

        onView(withId(R.id.register_btn))
                .perform(click());


        // LMH_custom_start
        // onErrorViewWithinTextInputLayoutWithId(R.id.register_form_phoneno_field)
        //        .check(matches(withText(R.string.error_register_no_phoneno)));

        // onEditTextWithinTextInputLayoutWithId(R.id.register_form_phoneno_field)
        //        .perform(closeSoftKeyboard(), scrollTo(), typeText("1234567"));
        // LMH_custom_end
//
//        onView(withId(R.id.register_btn))
//                .perform(click());
//
        // LMH_custom_start
        // onErrorViewWithinTextInputLayoutWithId(R.id.register_form_phoneno_field)
        //        .check(matches(withText(R.string.error_register_no_phoneno)));
        // LMH_custom_end
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

        // LMH_custom_start
        // onEditTextWithinTextInputLayoutWithId(R.id.register_form_phoneno_field)
        //        .perform(closeSoftKeyboard(), scrollTo(), typeText("12345678"));
        // LMH_custom_end

        onView(withId(R.id.register_btn))
                .perform( click());

        try{
            assertEquals(MainActivity.class, Utils.TestUtils.getCurrentActivity().getClass());
        }catch(AssertionFailedError afe){
            afe.printStackTrace();
        }

    }

}
