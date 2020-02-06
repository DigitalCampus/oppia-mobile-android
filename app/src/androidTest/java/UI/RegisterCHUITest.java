package UI;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import junit.framework.AssertionFailedError;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.model.County;
import org.digitalcampus.oppia.model.District;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static Utils.ViewsUtils.onEditTextWithinTextInputLayoutWithId;
import static Utils.ViewsUtils.onErrorViewWithinTextInputLayoutWithId;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class RegisterCHUITest {

    @Rule
    public ActivityTestRule<WelcomeActivity> welcomeActivityTestRule =
            new ActivityTestRule<>(WelcomeActivity.class);


    private void openCHRegistrationForm() {

        onView(withId(R.id.welcome_register))
                .perform(scrollTo(), click());

        onView(withId(R.id.btn_reg_cha)).perform(click());
    }

    private void enterValidDataStep1() {

        onEditTextWithinTextInputLayoutWithId(R.id.edit_reg_ch_first_name)
                .perform(scrollTo(), typeText("First Name"));

        onEditTextWithinTextInputLayoutWithId(R.id.edit_reg_ch_last_name)
                .perform(scrollTo(), typeText("Last Name"));

        onEditTextWithinTextInputLayoutWithId(R.id.edit_reg_ch_employee_id)
                .perform(scrollTo(), typeText("Username"));

    }

    private void enterValidDataStep2() {

        onEditTextWithinTextInputLayoutWithId(R.id.edit_reg_ch_password)
                .perform(scrollTo(), typeText("password1"));

        onEditTextWithinTextInputLayoutWithId(R.id.edit_reg_ch_password_again)
                .perform(scrollTo(), typeText("password1"));
    }

    private void enterValidDataStep3() {

        onView(withId(R.id.spinner_counties)).perform(click());
        onData(allOf(is(instanceOf(County.class)))).atPosition(1).perform(click());

        onView(withId(R.id.spinner_districts)).perform(click());
        onData(allOf(is(instanceOf(District.class)))).atPosition(1).perform(click());
    }

    private void checkScreenVisible(int screenNumber) {

        switch (screenNumber) {
            case 0:
                onView(withId(R.id.view_reg_ch_screen_0)).check(matches(isDisplayed()));
                onView(withId(R.id.view_reg_ch_screen_1)).check(matches(not(isDisplayed())));
                onView(withId(R.id.view_reg_ch_screen_2)).check(matches(not(isDisplayed())));
                break;

            case 1:
                onView(withId(R.id.view_reg_ch_screen_0)).check(matches(not(isDisplayed())));
                onView(withId(R.id.view_reg_ch_screen_1)).check(matches(isDisplayed()));
                onView(withId(R.id.view_reg_ch_screen_2)).check(matches(not(isDisplayed())));
                break;

            case 2:
                onView(withId(R.id.view_reg_ch_screen_0)).check(matches(not(isDisplayed())));
                onView(withId(R.id.view_reg_ch_screen_1)).check(matches(not(isDisplayed())));
                onView(withId(R.id.view_reg_ch_screen_2)).check(matches(isDisplayed()));
                break;
        }
    }


    @Test
    public void checkOnlyFirstScreenIsDisplayed() throws Exception {

        openCHRegistrationForm();

        checkScreenVisible(0);
    }

    @Test
    public void returnsToMainScreenWhenPreviousButtonClick() throws Exception {
        openCHRegistrationForm();
        onView(withId(R.id.btn_register_ch_previous)).perform(click());
        onView(withText(R.string.register_select_role_explanation)).check(matches(isDisplayed()));
    }

    @Test
    public void showsErrorMessageWhenAnyFieldOfFirstScreenIsEmpty() throws Exception {

        openCHRegistrationForm();

        onView(withId(R.id.btn_register_ch_next))
                .perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.edit_reg_ch_first_name)
                .check(matches(withText(R.string.field_required)));

        // Fill first name and check
        onEditTextWithinTextInputLayoutWithId(R.id.edit_reg_ch_first_name)
                .perform(scrollTo(), typeText("First Name"));

        onView(withId(R.id.btn_register_ch_next))
                .perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.edit_reg_ch_last_name)
                .check(matches(withText(R.string.field_required)));

        // Fill last name and check
        onEditTextWithinTextInputLayoutWithId(R.id.edit_reg_ch_last_name)
                .perform(scrollTo(), typeText("Last Name"));

        onView(withId(R.id.btn_register_ch_next))
                .perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.edit_reg_ch_employee_id)
                .check(matches(withText(R.string.field_required)));

        // Fill employee id and check
        onEditTextWithinTextInputLayoutWithId(R.id.edit_reg_ch_employee_id)
                .perform(scrollTo(), typeText("111"));

        onView(withId(R.id.btn_register_ch_next))
                .perform(click());

        checkScreenVisible(1);

    }


    @Test
    public void showsErrorWhenEmplyeeIDcontainsSpaces() throws Exception {

        openCHRegistrationForm();

        enterValidDataStep1();

        onEditTextWithinTextInputLayoutWithId(R.id.edit_reg_ch_employee_id)
                .perform(scrollTo(), replaceText("111 with space"));

        onView(withId(R.id.btn_register_ch_next))
                .perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.edit_reg_ch_employee_id)
                .check(matches(withText(R.string.field_spaces_error)));
    }


    @Test
    public void showsErrorWhenPasswordIsTooShort() throws Exception {

        openCHRegistrationForm();

        enterValidDataStep1();

        onView(withId(R.id.btn_register_ch_next))
                .perform(click());

        onEditTextWithinTextInputLayoutWithId(R.id.edit_reg_ch_password)
                .perform(scrollTo(), typeText("123"));

        onEditTextWithinTextInputLayoutWithId(R.id.edit_reg_ch_password_again)
                .perform(scrollTo(), typeText("123"));

        onView(withId(R.id.btn_register_ch_next))
                .perform(click());

        String passwordError = InstrumentationRegistry.getInstrumentation().getTargetContext().getResources().getString(
                R.string.error_register_password, App.PASSWORD_MIN_LENGTH);

        onErrorViewWithinTextInputLayoutWithId(R.id.edit_reg_ch_password)
                .check(matches(withText(passwordError)));

    }

    @Test
    public void showsErrorWhenPasswordsDoesNotMatch() throws Exception {

        openCHRegistrationForm();

        enterValidDataStep1();

        onView(withId(R.id.btn_register_ch_next))
                .perform(click());

        onEditTextWithinTextInputLayoutWithId(R.id.edit_reg_ch_password)
                .perform(scrollTo(), typeText("aaabbb"));

        onEditTextWithinTextInputLayoutWithId(R.id.edit_reg_ch_password_again)
                .perform(scrollTo(), typeText("aaaccc"));

        onView(withId(R.id.btn_register_ch_next))
                .perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.edit_reg_ch_password_again)
                .check(matches(withText(R.string.error_register_password_no_match)));

    }

    @Test
    public void checkFullScreen1FlowWhenPasswordsAreOK() throws Exception {
        openCHRegistrationForm();
        enterValidDataStep1();
        onView(withId(R.id.btn_register_ch_next))
                .perform(click());
        enterValidDataStep2();
        onView(withId(R.id.btn_register_ch_next))
                .perform(click());
        checkScreenVisible(2);
        onView(withId(R.id.btn_register_perform)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_register_ch_next)).check(matches(not(isDisplayed())));
        onView(withId(R.id.btn_register_ch_previous))
                .perform(click());
        checkScreenVisible(1);
        onView(withId(R.id.btn_register_ch_next)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_register_perform)).check(matches(not(isDisplayed())));
        onView(withId(R.id.btn_register_ch_previous))
                .perform(click());
        checkScreenVisible(0);
    }

    @Test
    public void checkDistrictDisablesWhenNoCountyIsSelected() throws Exception {
        openCHRegistrationForm();
        enterValidDataStep1();
        onView(withId(R.id.btn_register_ch_next))
                .perform(click());
        enterValidDataStep2();
        onView(withId(R.id.btn_register_ch_next))
                .perform(click());

        onView(withId(R.id.spinner_districts)).check(matches(not(isEnabled())));

        onView(withId(R.id.spinner_counties)).perform(click());
        onData(allOf(is(instanceOf(County.class)))).atPosition(1).perform(click());

        onView(withId(R.id.spinner_districts)).check(matches(isEnabled()));

        onView(withId(R.id.spinner_districts)).perform(click());
        onData(allOf(is(instanceOf(District.class)))).atPosition(1).perform(click());

        onView(withId(R.id.spinner_counties)).perform(click());
        onData(allOf(is(instanceOf(County.class)))).atPosition(0).perform(click());

        onView(withId(R.id.spinner_districts)).check(matches(not(isEnabled())));
        onView(withId(R.id.spinner_districts)).check(matches(withSpinnerText(R.string.select_district)));
    }


    @Test
    public void checkShowErrorWhenCountyOrDistrictIsNotSelected() throws Exception {
        openCHRegistrationForm();
        enterValidDataStep1();
        onView(withId(R.id.btn_register_ch_next))
                .perform(click());
        enterValidDataStep2();
        onView(withId(R.id.btn_register_ch_next))
                .perform(click());

        onView(withId(R.id.btn_register_perform)).perform(click());

        // Check toast message
        onView(withText(R.string.please_select_county_district))
                .inRoot(withDecorView(not(is(welcomeActivityTestRule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));

        onView(withId(R.id.spinner_counties)).perform(click());
        onData(allOf(is(instanceOf(County.class)))).atPosition(1).perform(click());

        onView(withId(R.id.btn_register_perform)).perform(click());

        onView(withText(R.string.please_select_county_district))
                .inRoot(withDecorView(not(is(welcomeActivityTestRule.getActivity().getWindow().getDecorView()))))
                .check(matches(isDisplayed()));

    }

    @Test
    public void entersMainActivityWhenRegisterSuccess() throws Exception {
        openCHRegistrationForm();
        enterValidDataStep1();
        onView(withId(R.id.btn_register_ch_next))
                .perform(click());
        enterValidDataStep2();
        onView(withId(R.id.btn_register_ch_next))
                .perform(click());

        enterValidDataStep3();

        onView(withId(R.id.btn_register_perform)).perform(click());

        onView(withText(R.string.please_select_county_district))
                .inRoot(withDecorView(not(is(welcomeActivityTestRule.getActivity().getWindow().getDecorView()))))
                .check(doesNotExist());

        try {
            assertEquals(MainActivity.class, Utils.TestUtils.getCurrentActivity().getClass());
        } catch (AssertionFailedError afe) {
            // If server returns any error:
            onView(withText(R.string.error)).check(matches(isDisplayed()));
        }


    }


}
