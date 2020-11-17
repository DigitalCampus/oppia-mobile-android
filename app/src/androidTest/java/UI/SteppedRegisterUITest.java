package UI;

import junit.framework.AssertionFailedError;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.CustomField;
import org.digitalcampus.oppia.model.CustomFieldsRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

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
public class SteppedRegisterUITest extends MockedApiEndpointTest {

    private static final String VALID_REGISTER_RESPONSE = "responses/response_200_register.json";
    private static final String REGISTER_STEPS_NORMAL = "customfields/custom_fields.json";

    @Mock
    protected CustomFieldsRepository customFieldsRepo;

    @Rule
    public ActivityTestRule<WelcomeActivity> welcomeActivityTestRule =
            new ActivityTestRule<>(WelcomeActivity.class, false, false);

    @Before
    public void setUp() throws Exception {

    }

    private void setRegisterSteps(String JsonDeclarationPath) throws Exception {
        String fieldsData = Utils.FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), JsonDeclarationPath);
        CustomField.loadCustomFields(InstrumentationRegistry.getInstrumentation().getContext(), fieldsData);
        List<CustomField.RegisterFormStep> steps = CustomField.parseRegisterSteps(fieldsData);
        DbHelper db = DbHelper.getInstance(InstrumentationRegistry.getInstrumentation().getContext());
        List<CustomField> profileCustomFields = db.getCustomFields();
        when(customFieldsRepo.getRegisterSteps(any())).thenReturn(steps);
        when(customFieldsRepo.getAll(any())).thenReturn(profileCustomFields);
    }

    @Test
    public void showsStepperIfSteppedRegistration() throws  Exception {

        setRegisterSteps(REGISTER_STEPS_NORMAL);
        welcomeActivityTestRule.launchActivity(null);

        onView(withId(R.id.welcome_register)).perform(scrollTo(), click());

        onView(withId(R.id.frame_stepper_indicator)).check(matches(isDisplayed()));
        onView(withText("First step")).check(matches(isDisplayed()));
    }

    @Test
    public void dontAdvanceStepIfFieldErrors() throws  Exception {

        setRegisterSteps(REGISTER_STEPS_NORMAL);
        welcomeActivityTestRule.launchActivity(null);

        onView(withId(R.id.welcome_register)).perform(scrollTo(), click());

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .perform(closeSoftKeyboard(), scrollTo(), replaceText(""));

        onView(withId(R.id.next_btn)).perform(click());

        // Check we're still in the first step
        onView(withText("First step")).check(matches(isDisplayed()));

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .check(matches(withText(R.string.field_required)));
    }

    @Test
    public void advanceNextStepIfAllFieldsCorrect() throws  Exception {

        setRegisterSteps(REGISTER_STEPS_NORMAL);
        welcomeActivityTestRule.launchActivity(null);

        onView(withId(R.id.welcome_register)).perform(scrollTo(), click());

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onView(withId(R.id.next_btn)).perform(click());

        // Check we're still in the first step
        onView(withText("Second step")).check(matches(isDisplayed()));

    }

}
