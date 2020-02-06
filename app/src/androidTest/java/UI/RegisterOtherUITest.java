package UI;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import junit.framework.AssertionFailedError;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.application.App;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static Utils.ViewsUtils.onEditTextWithinTextInputLayoutWithId;
import static Utils.ViewsUtils.onErrorViewWithinTextInputLayoutWithId;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
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
public class RegisterOtherUITest {

    @Rule
    public ActivityTestRule<WelcomeActivity> welcomeActivityTestRule =
            new ActivityTestRule<>(WelcomeActivity.class);


    private void enterValidRequiredData() {

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .perform(scrollTo(), typeText("Username"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_email_field)
                .perform(scrollTo(), typeText("valid@email.is"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                .perform(scrollTo(), typeText("password1"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field)
                .perform(scrollTo(), typeText("password1"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_firstname_field)
                .perform(scrollTo(), typeText("First Name"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_lastname_field)
                .perform(scrollTo(), typeText("Last Name"));
    }


    @Test
    public void showsErrorMessageWhenThereIsNoUsername() throws Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.btn_reg_other)).perform(click());

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .perform(closeSoftKeyboard(), scrollTo(), clearText());

        onView(withId(R.id.btn_register_perform))
                .perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .check(matches(withText(R.string.field_required)));

    }

    @Test
    public void showsErrorMessageWhenTheUsernameContainsSpaces() throws Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.btn_reg_other)).perform(click());

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username With Spaces"));

        onView(withId(R.id.btn_register_perform))
                .perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .check(matches(withText(R.string.field_spaces_error)));

    }


    @Test
    public void showErrorMessageWhenTheEmailContainsSpaces() throws Exception {
        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.btn_reg_other)).perform(click());

        enterValidRequiredData();

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_email_field)
                .perform(closeSoftKeyboard(), scrollTo(), replaceText("email with spaces@gmail.com"));

        onView(withId(R.id.btn_register_perform))
                .perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_email_field)
                .check(matches(withText(R.string.field_spaces_error)));
    }

    @Test
    public void showsErrorMessageWhenThePasswordIsTooShort() throws Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.btn_reg_other)).perform(click());

        enterValidRequiredData();

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                .perform(closeSoftKeyboard(), scrollTo(), replaceText("123"));

        onView(withId(R.id.btn_register_perform))
                .perform(click());

        String passwordError = InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getString(
                R.string.error_register_password, App.PASSWORD_MIN_LENGTH);

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_password_field)
                .check(matches(withText(passwordError)));
    }

    @Test
    public void showsErrorMessageWhenThePasswordsDoNotMatch() throws Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.btn_reg_other)).perform(click());

        enterValidRequiredData();

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field)
                .perform(closeSoftKeyboard(), scrollTo(), replaceText("password2"));

        onView(withId(R.id.btn_register_perform))
                .perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_password_field)
                .check(matches(withText(R.string.error_register_password_no_match)));
    }

    @Test
    public void showsErrorMessageWhenThereIsNoFirstName() throws Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.btn_reg_other)).perform(click());

        enterValidRequiredData();

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_firstname_field)
                .perform(closeSoftKeyboard(), scrollTo(), replaceText(""));

        onView(withId(R.id.btn_register_perform))
                .perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_firstname_field)
                .check(matches(withText(R.string.field_required)));
    }

    @Test
    public void showsErrorMessageWhenThereIsNoLastName() throws Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.btn_reg_other)).perform(click());

        enterValidRequiredData();

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_lastname_field)
                .perform(closeSoftKeyboard(), scrollTo(), replaceText(""));

        onView(withId(R.id.btn_register_perform))
                .perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_lastname_field)
                .check(matches(withText(R.string.field_required)));
    }

    @Test
    public void showsErrorMessageWhenThePhoneNumberIsNotValid() throws Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.btn_reg_other)).perform(click());

        enterValidRequiredData();

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_phoneno_field)
                .perform(closeSoftKeyboard(), scrollTo(), replaceText("9*/.02"));

        onView(withId(R.id.btn_register_perform))
                .perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_phoneno_field)
                .check(matches(withText(R.string.error_register_no_phoneno)));

    }


    @Test
    public void changeActivityWhenAllTheFieldsAreCorrect() throws Exception {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.btn_reg_other)).perform(click());

        enterValidRequiredData();

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_phoneno_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("12345678"));

        onView(withId(R.id.btn_register_perform))
                .perform(click());

        try {
            assertEquals(MainActivity.class, Utils.TestUtils.getCurrentActivity().getClass());
        } catch (AssertionFailedError afe) {
            // If server returns any error:
            onView(withText(R.string.error)).check(matches(isDisplayed()));
        }

    }

}
