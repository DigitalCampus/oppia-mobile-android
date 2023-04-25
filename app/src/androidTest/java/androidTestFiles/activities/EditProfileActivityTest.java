package androidTestFiles.activities;

import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static androidTestFiles.utils.UITestActionsUtils.waitForView;
import static androidTestFiles.utils.ViewsUtils.isToast;
import static androidTestFiles.utils.ViewsUtils.onEditTextWithinTextInputLayoutWithId;
import static androidTestFiles.utils.ViewsUtils.onErrorViewWithinTextInputLayoutWithId;

import android.Manifest;
import android.content.Context;
import android.os.Build;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.EditProfileActivity;
import org.digitalcampus.oppia.application.SessionManager;
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

import androidTestFiles.utils.parent.MockedApiEndpointTest;
import androidTestFiles.database.TestDBHelper;

@RunWith(AndroidJUnit4.class)
public class EditProfileActivityTest extends MockedApiEndpointTest {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private static final String VALID_EMAIL = "test2@oppia.org";
    private static final String VALID_FIRST_NAME = "First Name";
    private static final String VALID_LAST_NAME = "Last Name";
    private static final String VALID_ORGANIZATION = "A organization";
    private static final String VALID_JOB_TITLE = "A job title";
    public static final String VALID_PROFILE_RESPONSE = "responses/response_200_profile.json";

    @Mock
    protected User user;

    @Mock
    protected CustomFieldsRepository customFieldsRepo;

    private TestDBHelper testDBHelper;

    @Before
    public void setUp() throws Exception {
        when(customFieldsRepo.getAll(any())).thenReturn(new ArrayList<>());

        testDBHelper = new TestDBHelper(InstrumentationRegistry.getInstrumentation().getTargetContext());
        testDBHelper.setUp();

        startServer(200, VALID_PROFILE_RESPONSE, 0);
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
                .perform(scrollTo(), clearText(), typeText(VALID_JOB_TITLE), closeSoftKeyboard());

    }

    @Test
    public void checkErrorLabelsOnEmptyFields() throws Exception {

        try (ActivityScenario<EditProfileActivity> scenario = ActivityScenario.launch(EditProfileActivity.class)) {

            onEditTextWithinTextInputLayoutWithId(R.id.field_email)
                    .perform(scrollTo(), clearText());
            onEditTextWithinTextInputLayoutWithId(R.id.field_firstname)
                    .perform(scrollTo(), clearText());
            onEditTextWithinTextInputLayoutWithId(R.id.field_lastname)
                    .perform(scrollTo(), clearText(), closeSoftKeyboard());

            waitForView(withId(R.id.btn_save_profile)).perform(click());

            onErrorViewWithinTextInputLayoutWithId(R.id.field_firstname)
                    .check(matches(withText(R.string.field_required)));

            onErrorViewWithinTextInputLayoutWithId(R.id.field_lastname)
                    .check(matches(withText(R.string.field_required)));
        }
    }

    @Test
    public void checkErrorLabelOnInvalidEmailFormat() throws Exception {

        try (ActivityScenario<EditProfileActivity> scenario = ActivityScenario.launch(EditProfileActivity.class)) {

            onEditTextWithinTextInputLayoutWithId(R.id.field_email)
                    .perform(scrollTo(), clearText(), typeText("wrong-email-format"), closeSoftKeyboard());

            waitForView(withId(R.id.btn_save_profile)).perform(click());

            onErrorViewWithinTextInputLayoutWithId(R.id.field_email)
                    .check(matches(withText(R.string.error_register_email)));
        }
    }

    @Test
    @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.Q)
    // Skipping test for API >= 30 until a fix for asserting Toast messages is found.
    // https://oppia.atlassian.net/browse/OPPIA-1130
    public void checkShowsSubmitErrorMessageWhenServerError400Response() throws Exception {

        enqueueResponse(400, ERROR_MESSAGE_BODY, 0);

        try (ActivityScenario<EditProfileActivity> scenario = ActivityScenario.launch(EditProfileActivity.class)) {

            enterValidData();

            waitForView(withId(R.id.btn_save_profile)).perform(click());

            waitForView(withText("Error message"))
                    .inRoot(isToast())
                    .check(matches(isDisplayed()));


        }
    }

    @Test
    public void checkDataSavedWhenServerSuccessResponse() throws Exception {

        enqueueResponse(200, null, 0);

        try (ActivityScenario<EditProfileActivity> scenario = ActivityScenario.launch(EditProfileActivity.class)) {

            enterValidData();
            closeSoftKeyboard();
            waitForView(withId(R.id.btn_save_profile)).perform(click());

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
    }


    @Test
    @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.Q)
    // Skipping test for API >= 30 until a fix for asserting Toast messages is found.
    // https://oppia.atlassian.net/browse/OPPIA-1130
    public void checkShowsSubmitErrorMessageWhenServerError500Response() throws Exception {

        enqueueResponse(500, ERROR_MESSAGE_BODY, 0);

        try (ActivityScenario<EditProfileActivity> scenario = ActivityScenario.launch(EditProfileActivity.class)) {

            enterValidData();

            waitForView(withId(R.id.btn_save_profile)).perform(click());

            waitForView(withText(R.string.error_connection))
                    .inRoot(isToast())
                    .check(matches(isDisplayed()));
        }
    }

}
