package UI;

import junit.framework.AssertionFailedError;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.model.CustomFieldsRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;

import Utils.MockedApiEndpointTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class NotSteppedRegisterUITest extends MockedApiEndpointTest {

    private static final String VALID_REGISTER_RESPONSE = "responses/response_200_register.json";

    @Mock
    protected CustomFieldsRepository customFieldsRepo;

    @Rule
    public ActivityTestRule<WelcomeActivity> welcomeActivityTestRule =
            new ActivityTestRule<>(WelcomeActivity.class, false, false);

    @Before
    public void setUp() throws Exception {
        when(customFieldsRepo.getAll(any())).thenReturn(new ArrayList<>());
    }

    @Test
    public void showsErrorMessageWhenThereIsNoUsername() throws  Exception {
        welcomeActivityTestRule.launchActivity(null);

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
    public void showsErrorMessageWhenTheUsernameIsTooShort() throws  Exception {
        welcomeActivityTestRule.launchActivity(null);

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Us"));

        onView(withId(R.id.register_btn))
                .perform(click());

        String usernameLengthError = InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getString(
                R.string.error_register_username_lenght, App.USERNAME_MIN_CHARACTERS);

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .check(matches(withText(usernameLengthError)));

    }

    @Test
    public void showsErrorMessageWhenTheUsernameContainsSpaces() throws  Exception {
        welcomeActivityTestRule.launchActivity(null);

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

        welcomeActivityTestRule.launchActivity(null);

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

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_email_field)
                .check(matches(withText(R.string.error_register_email)));
    }

    @Test
    public void showErrorMessageWhenTheEmailContainsSpaces() throws Exception {

        welcomeActivityTestRule.launchActivity(null);

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
        welcomeActivityTestRule.launchActivity(null);

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
                R.string.error_register_password, App.PASSWORD_MIN_LENGTH);

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_password_field)
                .check(matches(withText(passwordError)));
    }

    @Test
    public void showsErrorMessageWhenThePasswordsDoNotMatch() throws Exception{
        welcomeActivityTestRule.launchActivity(null);

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
        welcomeActivityTestRule.launchActivity(null);

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

        welcomeActivityTestRule.launchActivity(null);

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
        welcomeActivityTestRule.launchActivity(null);

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
                .perform(closeSoftKeyboard(), scrollTo(), typeText("1234"));

        onView(withId(R.id.register_btn))
                .perform(click(), closeSoftKeyboard());

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_phoneno_field)
                .check(matches(withText(R.string.error_register_no_phoneno)));

    }

    @Test
    public void changeActivityWhenAllTheFieldsAreCorrect() throws Exception {

        startServer(200, VALID_REGISTER_RESPONSE, 0);
        welcomeActivityTestRule.launchActivity(null);

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

        onView(withId(R.id.register_btn))
                .perform( click());

        try {
            assertEquals(MainActivity.class, Utils.TestUtils.getCurrentActivity().getClass());
        } catch (AssertionFailedError afe) {
            // If server returns any error:
            onView(withText(R.string.error)).check(matches(isDisplayed()));
        }

    }

}
