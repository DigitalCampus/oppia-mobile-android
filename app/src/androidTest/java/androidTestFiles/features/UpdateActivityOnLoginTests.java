package androidTestFiles.features;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBackUnconditionally;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import androidTestFiles.utils.CourseUtils;
import androidTestFiles.utils.TestUtils;
import androidTestFiles.utils.parent.MockedApiEndpointTest;

public class UpdateActivityOnLoginTests extends MockedApiEndpointTest {

    @Mock
    SharedPreferences prefs;
    @Mock
    SharedPreferences.Editor editor;
    @Mock
    CoursesRepository coursesRepository;

    private Context context;

    private void initMockEditor() {
        when(editor.remove(anyString())).thenReturn(editor);
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);
    }

    @Before
    public void setUp() throws Exception {
        CourseUtils.cleanUp();
        initMockEditor();
        when(prefs.edit()).thenReturn(editor);

        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    }

    @After
    public void tearDown() throws Exception {
        CourseUtils.cleanUp();
    }


    @Test
    public void checkExtraParameterIfFirstLogin() {
        // ActivityScenario, check intent extra
    }

    @Test
    public void checkExtraParameterIfNotFirstLogin() {

    }



    @Test
    public void updateActivityIfFirstLoginAndUpdateValueOptional() {
        // launchInContainer
    }

    @Test
    public void updateActivityIfFirstLoginAndUpdateValueForze() {

    }

    @Test
    public void dontUpdateActivityIfFirstLoginAndUpdateValueNone() {

    }

    @Test
    public void dontUpdateActivityIfNotFirstLoginAndUpdateValueOptional() {

        String option = context.getString(R.string.update_activity_on_login_value_optional);
        when(prefs.getString(eq(PrefsActivity.PREF_UPDATE_ACTIVITY_ON_LOGIN), anyString())).thenReturn(option);

        startServer(200, null);

    }


    @Test
    public void avoidUpdateActivityAgainWhenBackToScreen() {

    }


    @Test
    public void checkErrorMessageWhenNoConnection() {

    }

    @Test
    public void checkErrorMessageWhenRequestFailsAndUpdateValueOptional() {

    }

    @Test
    public void checkErrorMessageWhenRequestFailsAndUpdateValueForce() {

    }


    @Test
    public void checkAutoLogoutWhenChangeUpdateActivityOnLoginOption() throws InterruptedException {

        launchCheckAutologoutProcess(context.getString(R.string.update_activity_on_login_value_none),
                context.getString(R.string.force), WelcomeActivity.class, true);

        launchCheckAutologoutProcess(context.getString(R.string.update_activity_on_login_value_none),
                context.getString(R.string.optional), WelcomeActivity.class, true);

        launchCheckAutologoutProcess(context.getString(R.string.update_activity_on_login_value_optional),
                context.getString(R.string.force), MainActivity.class, false);

        launchCheckAutologoutProcess(context.getString(R.string.update_activity_on_login_value_optional),
                context.getString(R.string.none), MainActivity.class, false);

    }

    public void launchCheckAutologoutProcess(String initialValue, String finalOptionText,
                                             Class expectedActivityClass, boolean closeWarningDialog) {

        if (!BuildConfig.MENU_ALLOW_SETTINGS) {
            return;
        }

        when(prefs.getString(eq(PrefsActivity.PREF_USER_NAME), anyString())).thenReturn("test_user");
        when(prefs.getBoolean(eq(PrefsActivity.PREF_ADMIN_PROTECTION), anyBoolean())).thenReturn(false);
        when(prefs.getString(eq(PrefsActivity.PREF_TEST_ACTION_PROTECTED), anyString())).thenReturn("false");
        when(prefs.getString(eq(PrefsActivity.PREF_UPDATE_ACTIVITY_ON_LOGIN), anyString())).thenReturn(initialValue);

        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(
                PrefsActivity.PREF_UPDATE_ACTIVITY_ON_LOGIN, initialValue).apply();

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.drawer))
                    .check(matches(isClosed(Gravity.START)))
                    .perform(DrawerActions.open());

            onView(withText(R.string.menu_settings)).perform(click());

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(
                            hasDescendant(withText(R.string.prefAdvanced_title)), click()));

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(
                            hasDescendant(withText(R.string.pref_update_activity_on_login)), click()));

            onView(withText(finalOptionText)).perform(click());

            if (closeWarningDialog) {
                onView(withText(R.string.accept))
                        .inRoot(isDialog())
                        .check(matches(isDisplayed()))
                        .perform(click());
            }

            pressBackUnconditionally();
            pressBackUnconditionally();

            assertEquals(expectedActivityClass, TestUtils.getCurrentActivity().getClass());

        }

    }

}
