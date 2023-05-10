package androidTestFiles.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static androidTestFiles.features.authentication.login.LoginUITest.VALID_LOGIN_RESPONSE;
import static androidTestFiles.utils.ViewsUtils.onEditTextWithinTextInputLayoutWithId;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.ChangePasswordActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidTestFiles.utils.parent.MockedApiEndpointTest;

@RunWith(AndroidJUnit4.class)
public class ChangePasswordActivityTest extends MockedApiEndpointTest {

    @Test
    public void changePassword_samePassword_success() {
        startServer(200, VALID_LOGIN_RESPONSE, 0);

        try (ActivityScenario<ChangePasswordActivity> scenario = ActivityScenario.launch(ChangePasswordActivity.class)) {
            onEditTextWithinTextInputLayoutWithId(R.id.field_password)
                    .perform(scrollTo(), clearText(), typeText("newPassword"));

            onEditTextWithinTextInputLayoutWithId(R.id.field_password_repeat)
                    .perform(scrollTo(), clearText(), typeText("newPassword"));

            onView(withId(R.id.btn_save_new_password)).check(matches(isDisplayed())).perform(click());

            onView(withText(R.string.change_password_success)).inRoot(isDialog()).check(matches(isDisplayed()));
        }
    }

    @Test
    public void changePassword_differentPasswords_failure() {
        startServer(200, VALID_LOGIN_RESPONSE, 0);

        try (ActivityScenario<ChangePasswordActivity> scenario = ActivityScenario.launch(ChangePasswordActivity.class)) {
            onEditTextWithinTextInputLayoutWithId(R.id.field_password)
                    .perform(scrollTo(), clearText(), typeText("newPassword"));

            onEditTextWithinTextInputLayoutWithId(R.id.field_password_repeat)
                    .perform(scrollTo(), clearText(), typeText("newPasswordDifferent"));

            onView(withId(R.id.btn_save_new_password)).check(matches(isDisplayed()))
                    .perform(closeSoftKeyboard(), click());

            onView(withText(R.string.error_register_password_no_match)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void changePassword_noConnectivity_failure() {
        startServer(400, ERROR_MESSAGE_BODY, 0);

        try (ActivityScenario<ChangePasswordActivity> scenario = ActivityScenario.launch(ChangePasswordActivity.class)) {
            onEditTextWithinTextInputLayoutWithId(R.id.field_password)
                    .perform(scrollTo(), clearText(), typeText("newPassword"));

            onEditTextWithinTextInputLayoutWithId(R.id.field_password_repeat)
                    .perform(scrollTo(), clearText(), typeText("newPassword"));

            onView(withId(R.id.btn_save_new_password)).check(matches(isDisplayed())).perform(click());

            onView(withText(R.string.error_processing_response)).check(matches(isDisplayed()));
        }
    }
}
