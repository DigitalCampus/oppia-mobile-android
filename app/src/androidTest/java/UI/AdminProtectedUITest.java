package UI;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.PreferenceMatchers.withKey;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class AdminProtectedUITest {

    @Rule
    public ActivityTestRule<PrefsActivity> prefsActivity = new ActivityTestRule<>(PrefsActivity.class);


    private void checkAdminPasswordDialogIsDisplayed() {
        onView(withText(R.string.admin_password_required))
                .check(matches(isDisplayed()));
    }

    @Test
    public void requestAdminPasswordOnCheckboxAdminProtectionClick() throws Exception {

        onData(withKey(PrefsActivity.PREF_ADMIN_PROTECTION))
                .perform(scrollTo(), click());

        checkAdminPasswordDialogIsDisplayed();
    }

    @Test
    public void requestAdminPasswordOnEditTextChangeAdminPassClick() throws Exception {

        onData(withKey(PrefsActivity.PREF_ADMIN_PASSWORD))
                .perform(scrollTo(), click());

        onView(withId(android.R.id.edit)).perform(clearText(), typeText("any_pass"), closeSoftKeyboard());

        onView(withId(android.R.id.button1)).perform(click());

        checkAdminPasswordDialogIsDisplayed();
    }

    @Test
    public void requestAdminPasswordOnEditTextChangeServerEndpointClick() throws Exception {

        onData(withKey(PrefsActivity.PREF_SERVER))
                .perform(scrollTo(), click());

        onView(withId(android.R.id.edit)).perform(clearText(), typeText("http://any.server"), closeSoftKeyboard());

        onView(withId(android.R.id.button1)).perform(click());

        checkAdminPasswordDialogIsDisplayed();
    }


    FINISH THIS

    private void checkAdminPasswordDialogIsNOTDisplayed() {
        onView(withText(R.string.admin_password_required))
                .check(doesNotExist());
    }
}
