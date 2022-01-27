package androidTestFiles.UI;

import android.Manifest;
import android.content.SharedPreferences;

import junit.framework.AssertionFailedError;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.AnalyticsOptinActivity;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.model.CustomFieldsRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;

import androidTestFiles.Utils.MockedApiEndpointTest;
import androidTestFiles.Utils.TestUtils;
import androidx.preference.PreferenceManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import static androidTestFiles.Utils.ViewsUtils.onEditTextWithinTextInputLayoutWithId;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class AnalyticsOptinUITest extends MockedApiEndpointTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private static final String VALID_LOGIN_REGISTER_RESPONSE = "responses/response_200_register.json";

    @Mock
    protected CustomFieldsRepository customFieldsRepo;

    @Mock
    private SharedPreferences prefsMock;

    @Before
    public void setUp() throws Exception {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(
                InstrumentationRegistry.getInstrumentation().getTargetContext());

        prefs.edit().remove(PrefsActivity.PREF_ANALYTICS_INITIAL_PROMPT).commit();

        when(customFieldsRepo.getAll(any())).thenReturn(new ArrayList<>());

        when(prefsMock.getBoolean(eq(PrefsActivity.PREF_LOGOUT_ENABLED), anyBoolean())).thenReturn(true);
    }

    @Test
    public void checkAnalyticsOptinScreenAndValuesAreCorrectOnPerUserBasis() throws Exception {

        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {

            performValidLogin("username-1");
            assertEquals(AnalyticsOptinActivity.class, TestUtils.getCurrentActivity().getClass());
            selectAnalyticsOptions(R.id.analytics_checkbox);
            assertEquals(MainActivity.class, TestUtils.getCurrentActivity().getClass());

            performLogout();

            performValidLogin("username-2");
            assertEquals(AnalyticsOptinActivity.class, TestUtils.getCurrentActivity().getClass());
            selectAnalyticsOptions(R.id.analytics_checkbox, R.id.bugreport_checkbox);
            assertEquals(MainActivity.class, TestUtils.getCurrentActivity().getClass());

            performLogout();

            performValidLogin("username-1");
            assertEquals(MainActivity.class, TestUtils.getCurrentActivity().getClass());
            openPrivacyScreen();
            onView(withId(R.id.analytics_checkbox)).perform(scrollTo()).check(matches(isChecked()));
            onView(withId(R.id.bugreport_checkbox)).perform(scrollTo()).check(matches(isNotChecked()));

            pressBack();
            performLogout();

            performValidLogin("username-2");
            assertEquals(MainActivity.class, TestUtils.getCurrentActivity().getClass());
            openPrivacyScreen();
            onView(withId(R.id.analytics_checkbox)).perform(scrollTo()).check(matches(isChecked()));
            onView(withId(R.id.bugreport_checkbox)).perform(scrollTo()).check(matches(isChecked()));
        }
    }

    private void openPrivacyScreen() {

        onView(withId(R.id.drawer)).perform(DrawerActions.open());
        onView(withId(R.id.navigation_view)).perform(NavigationViewActions.navigateTo(R.id.menu_privacy));
    }

    private void selectAnalyticsOptions(int... checkbox_ids) {
        for (int checkbox_id : checkbox_ids) {
            onView(withId(checkbox_id)).perform(click());
        }
        onView(withId(R.id.continue_button)).perform(click());
    }

    private void performLogout() {

        onView(withId(R.id.drawer)).perform(DrawerActions.open());
        onView(withId(R.id.btn_expand_profile_options)).perform(click());
        onView(withId(R.id.btn_logout)).perform(click());
        onView(withText(R.string.yes)).perform(click());

    }

    private void performValidLogin(String username) {

        startServer(200, VALID_LOGIN_REGISTER_RESPONSE, 0);
        onView(withId(R.id.welcome_login)).perform(scrollTo(), click());
        onView(withId(R.id.login_username_field)).perform(closeSoftKeyboard(), scrollTo(), typeText(username));
        onView(withId(R.id.login_password_field))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("valid_password"));
        onView(withId(R.id.login_btn)).perform(scrollTo(), click());
    }

    @Test
    public void checkAnalyticsOptinScreenAppearsAfterNewUserRegistration() throws Exception {

        startServer(200, VALID_LOGIN_REGISTER_RESPONSE, 0);
        try (ActivityScenario<WelcomeActivity> scenario = ActivityScenario.launch(WelcomeActivity.class)) {

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
                    .perform(click());

            try {
                assertEquals(AnalyticsOptinActivity.class, TestUtils.getCurrentActivity().getClass());
            } catch (AssertionFailedError afe) {
                // If server returns any error:
                onView(withText(R.string.error)).check(matches(isDisplayed()));
            }
        }
    }

}
