package UI;

import android.content.Context;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.EditProfileActivity;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.model.CustomFieldsRepository;
import org.digitalcampus.oppia.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;

import Utils.MockedApiEndpointTest;
import database.TestDBHelper;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import static Utils.ViewsUtils.onEditTextWithinTextInputLayoutWithId;
import static Utils.ViewsUtils.onErrorViewWithinTextInputLayoutWithId;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class EditProfileUITest extends MockedApiEndpointTest {

    private static final String VALID_EMAIL = "test2@oppia.org";
    private static final String VALID_FIRST_NAME = "First Name";
    private static final String VALID_LAST_NAME = "Last Name";
    private static final String VALID_ORGANIZATION = "A organization";
    private static final String VALID_JOB_TITLE = "A job title";

    @Mock
    protected User user;

    @Mock
    protected CustomFieldsRepository customFieldsRepo;

    @Rule
    public ActivityTestRule<EditProfileActivity> editProfileActivityTestRule =
            new ActivityTestRule<>(EditProfileActivity.class, false, false);
    private TestDBHelper testDBHelper;

    @Before
    public void setUp() throws Exception {
        when(customFieldsRepo.getAll(any())).thenReturn(new ArrayList<>());

        testDBHelper = new TestDBHelper(InstrumentationRegistry.getInstrumentation().getTargetContext());
        testDBHelper.setUp();

    }

    @After
    public void tearDown() throws Exception {
        testDBHelper.tearDown();
    }

    private void enterValidData() {

        onEditTextWithinTextInputLayoutWithId(R.id.field_email)
                .perform(scrollTo(), clearText(), typeText(VALID_EMAIL));

        onEditTextWithinTextInputLayoutWithId(R.id.field_firstname)
                .perform(scrollTo(), clearText(), typeText(VALID_FIRST_NAME));

        onEditTextWithinTextInputLayoutWithId(R.id.field_lastname)
                .perform(scrollTo(), clearText(), typeText(VALID_LAST_NAME));

        onEditTextWithinTextInputLayoutWithId(R.id.field_organisation)
                .perform(scrollTo(), clearText(), typeText(VALID_ORGANIZATION));

        onEditTextWithinTextInputLayoutWithId(R.id.field_jobtitle)
                .perform(scrollTo(), clearText(), typeText(VALID_JOB_TITLE));

    }

    @Test
    public void checkErrorLabelsOnEmptyFields() throws Exception {

        editProfileActivityTestRule.launchActivity(null);

        onEditTextWithinTextInputLayoutWithId(R.id.field_email)
                .perform(scrollTo(), clearText());
        onEditTextWithinTextInputLayoutWithId(R.id.field_firstname)
                .perform(scrollTo(), clearText());
        onEditTextWithinTextInputLayoutWithId(R.id.field_lastname)
                .perform(scrollTo(), clearText(), closeSoftKeyboard());

        onView(withId(R.id.btn_save_profile)).perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.field_firstname)
                .check(matches(withText(R.string.field_required)));

        onErrorViewWithinTextInputLayoutWithId(R.id.field_lastname)
                .check(matches(withText(R.string.field_required)));

    }

    @Test
    public void checkErrorLabelOnInvalidEmailFormat() throws Exception {

        editProfileActivityTestRule.launchActivity(null);

        onEditTextWithinTextInputLayoutWithId(R.id.field_email)
                .perform(scrollTo(), clearText(), typeText("wrong-email-format"), closeSoftKeyboard());

        onView(withId(R.id.btn_save_profile)).perform(click());

        onErrorViewWithinTextInputLayoutWithId(R.id.field_email)
                .check(matches(withText(R.string.error_register_email)));

    }

    @Test
    public void checkDataSavedWhenServerSuccessResponse() throws Exception {

        startServer(200, null, 0);

        editProfileActivityTestRule.launchActivity(null);

        enterValidData();
        closeSoftKeyboard();
        onView(withId(R.id.btn_save_profile)).perform(click());

        Context context = InstrumentationRegistry.getInstrumentation().getContext();

        try {
            User user1 = testDBHelper.getDbHelper().getUser(SessionManager.getUsername(context));
            assertEquals(VALID_EMAIL, user1.getEmail());
            assertEquals(VALID_FIRST_NAME, user1.getFirstname());
            assertEquals(VALID_LAST_NAME, user1.getLastname());
            assertEquals(VALID_ORGANIZATION, user1.getOrganisation());
            assertEquals(VALID_JOB_TITLE, user1.getJobTitle());
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void checkShowsSubmitErrorMessageWhenServerError400Response() throws Exception {

        startServer(400, ERROR_MESSAGE_BODY, 0);

        editProfileActivityTestRule.launchActivity(null);

        enterValidData();

        onView(withId(R.id.btn_save_profile)).perform(click());

        onView(withText("Error message"))
                .inRoot(withDecorView(not(editProfileActivityTestRule.getActivity().getWindow().getDecorView())))
                .check(matches(isDisplayed()));

    }


    @Test
    public void checkShowsSubmitErrorMessageWhenServerError500Response() throws Exception {

        startServer(500, ERROR_MESSAGE_BODY, 0);

        editProfileActivityTestRule.launchActivity(null);

        enterValidData();

        onView(withId(R.id.btn_save_profile)).perform(click());

        onView(withText(R.string.error_connection))
                .inRoot(withDecorView(not(editProfileActivityTestRule.getActivity().getWindow().getDecorView())))
                .check(matches(isDisplayed()));

    }

    // TODO test with real server that the request and its parameters are ok?
}
