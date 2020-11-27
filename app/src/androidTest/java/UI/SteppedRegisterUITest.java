package UI;

import android.widget.Spinner;

import junit.framework.AssertionFailedError;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.CustomField;
import org.digitalcampus.oppia.model.CustomFieldsRepository;
import org.digitalcampus.oppia.utils.ui.fields.ValidableTextInputLayout;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.List;

import Utils.MockedApiEndpointTest;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import static Utils.ViewsUtils.onEditTextWithinTextInputLayout;
import static Utils.ViewsUtils.onEditTextWithinTextInputLayoutWithId;
import static Utils.ViewsUtils.onErrorViewWithinTextInputLayoutWithId;
import static Utils.ViewsUtils.withHintInInputLayout;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class SteppedRegisterUITest extends MockedApiEndpointTest {

    private static final String VALID_REGISTER_RESPONSE = "responses/response_200_register.json";
    private static final String BASIC_DEPENDANT_STEPS = "customfields/dependant_fields.json";
    private static final String REGISTER_STEPS_NORMAL = "customfields/custom_fields.json";
    private static final String ADVANCED_SCENARIOS = "customfields/advanced_scenarios.json";

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

    private void openRegisterScreen(){
        welcomeActivityTestRule.launchActivity(null);
        onView(withId(R.id.welcome_register)).perform(scrollTo(), click());
    }

    @Test
    public void showsStepperIfSteppedRegistration() throws  Exception {

        setRegisterSteps(REGISTER_STEPS_NORMAL);
        openRegisterScreen();

        onView(withId(R.id.frame_stepper_indicator)).check(matches(isDisplayed()));
        onView(withText("First step")).check(matches(isDisplayed()));
    }

    @Test
    public void dontAdvanceStepIfFieldErrors() throws  Exception {

        setRegisterSteps(REGISTER_STEPS_NORMAL);
        openRegisterScreen();

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
        openRegisterScreen();

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));
        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));
        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onView(withId(R.id.next_btn)).perform(click());

        onView(withText("Second step")).check(matches(isDisplayed()));

    }


    @Test
    public void keepValuesGoingStepsBack() throws  Exception {
        setRegisterSteps(REGISTER_STEPS_NORMAL);
        openRegisterScreen();

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));
        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));
        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onView(withId(R.id.next_btn)).perform(click());
        onView(withText("Second step")).check(matches(isDisplayed()));
        onView(withId(R.id.prev_btn)).perform(click());
        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .check(matches(withText("Username")));

    }

    @Test
    public void ShowDependantFieldOnSameStep() throws  Exception {

        setRegisterSteps(BASIC_DEPENDANT_STEPS);
        openRegisterScreen();

        onView(allOf(instanceOf(Spinner.class), hasSibling(withText(startsWith("Position")))))
            .perform(scrollTo())
            .check(matches(isDisplayed()))
            .perform(click());

        onView(withText("Other")).perform(click());
        onView(allOf(instanceOf(ValidableTextInputLayout.class),
                withHintInInputLayout(startsWith("Please specify"))))
                .check(matches(isDisplayed()));
    }

    @Test
    public void UpdateDependantCollection() throws  Exception {

        setRegisterSteps(ADVANCED_SCENARIOS);
        openRegisterScreen();

        onView(allOf(instanceOf(Spinner.class), hasSibling(withText(startsWith("Select county")))))
                .perform(scrollTo()).perform(click());
        onView(withText("Area1")).perform(click());

        onView(allOf(instanceOf(Spinner.class), hasSibling(withText(startsWith("Select district")))))
                .check(matches(isDisplayed()))
                .perform(click());
        onView(withText("region1"))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(allOf(instanceOf(Spinner.class), hasSibling(withText(startsWith("Select county")))))
                .perform(scrollTo()).perform(click());
        onView(withText("Area2")).perform(click());

        onView(allOf(instanceOf(Spinner.class), hasSibling(withText(startsWith("Select district")))))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .perform(click());
        onView(withText("region3"))
                .check(matches(isDisplayed()))
                .perform(click());
    }


    @Test
    public void ShowDependantFieldOnDifferentStep() throws  Exception {

        setRegisterSteps(ADVANCED_SCENARIOS);
        openRegisterScreen();

        onView(allOf(instanceOf(Spinner.class), hasSibling(withText(startsWith("Select county")))))
                .perform(scrollTo()).perform(click());
        onView(withText("Area1")).perform(click());

        onView(withId(R.id.next_btn)).perform(click());

        onView(withText("Area1 dependant"))
                .check(matches(isDisplayed()));

        //Now we go back and select a different value to check that it is not displayed
        onView(withId(R.id.prev_btn)).perform(click());

        onView(allOf(instanceOf(Spinner.class), hasSibling(withText(startsWith("Select county")))))
                .perform(scrollTo()).perform(click());
        onView(withText("Area2")).perform(click());

        onView(withText("Area1 dependant"))
                .check(matches(not(isDisplayed())));

    }

    @Test
    public void showDependantStepWithValue() throws  Exception {

        setRegisterSteps(BASIC_DEPENDANT_STEPS);
        openRegisterScreen();

        onView(allOf(instanceOf(Spinner.class), hasSibling(withText(startsWith("Position")))))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withText("Other")).perform(click());
        onEditTextWithinTextInputLayout(allOf(instanceOf(ValidableTextInputLayout.class),
                withHintInInputLayout(startsWith("Please specify"))))
                .perform(closeSoftKeyboard(), scrollTo(), typeText("None"));

        onView(withId(R.id.next_btn)).perform(click());

        onView(withText("Other role info")).check(matches(isDisplayed()));

    }

    @Test
    public void showDependantStepWithNegatedValue() throws  Exception {

        setRegisterSteps(BASIC_DEPENDANT_STEPS);
        openRegisterScreen();

        onView(allOf(instanceOf(Spinner.class), hasSibling(withText(startsWith("Position")))))
                .perform(scrollTo())
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withText("Role2")).perform(click());

        onView(withId(R.id.next_btn)).perform(click());

        onView(withText("Personal info")).check(matches(isDisplayed()));

    }

    @Test
    public void ShowRegisterButtonOnLastStep() throws  Exception {

        setRegisterSteps(REGISTER_STEPS_NORMAL);
        openRegisterScreen();

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));
        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));
        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onView(withId(R.id.next_btn)).perform(click());

        onView(allOf(instanceOf(Spinner.class), hasSibling(withText(startsWith("Position")))))
                .perform(scrollTo())
                .perform(click());
        onView(withText("Role2")).perform(click());

        onView(withId(R.id.next_btn)).perform(click());

        onView(withId(R.id.next_btn)).check(matches(not(isDisplayed())));
        onView(withId(R.id.register_btn)).check(matches(isDisplayed()));

        onView(withId(R.id.prev_btn)).perform(click());

        onView(withId(R.id.next_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.register_btn)).check(matches(not(isDisplayed())));

    }


    @Test
    public void showErrorOnLastStepBeforeSubmit() throws  Exception {

        setRegisterSteps(REGISTER_STEPS_NORMAL);
        openRegisterScreen();

        onEditTextWithinTextInputLayoutWithId(R.id.register_form_username_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("Username"));
        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));
        onEditTextWithinTextInputLayoutWithId(R.id.register_form_password_again_field)
                .perform(closeSoftKeyboard(), scrollTo(), typeText("password1"));

        onView(withId(R.id.next_btn)).perform(click());

        onView(allOf(instanceOf(Spinner.class), hasSibling(withText(startsWith("Position")))))
                .perform(scrollTo())
                .perform(click());
        onView(withText("Role2")).perform(click());

        onView(withId(R.id.next_btn)).perform(click());

        onView(withId(R.id.register_btn)).perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.register_form_firstname_field)
                .check(matches(withText(R.string.field_required)));
    }


}
