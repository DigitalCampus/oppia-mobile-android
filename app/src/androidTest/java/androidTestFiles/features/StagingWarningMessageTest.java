package androidTestFiles.features;

import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static androidTestFiles.utils.UITestActionsUtils.waitForView;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import androidTestFiles.utils.CourseUtils;
import androidTestFiles.utils.parent.DaggerInjectMockUITest;

@RunWith(AndroidJUnit4.class)
public class StagingWarningMessageTest extends DaggerInjectMockUITest {


    @Mock
    SharedPreferences prefs;
    @Mock
    SharedPreferences.Editor editor;
    private Context context;


    private void initMockEditor() {
        when(prefs.edit()).thenReturn(editor);
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);
    }

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        initMockEditor();

    }

    @After
    public void tearDown() throws Exception {
        CourseUtils.cleanUp();
    }

    @Test
    public void showMessageIfStagingServer() throws Exception {

        when(prefs.getString(eq(PrefsActivity.PREF_SERVER), anyString())).thenReturn("https://staging.server.com");

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            waitForView(withId(R.id.view_staging_warning)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void hideMessageIfLiveServer() throws Exception {

        when(prefs.getString(eq(PrefsActivity.PREF_SERVER), anyString())).thenReturn("https://live.server.com");

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            waitForView(withId(R.id.view_staging_warning)).check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void checkChangeServerUrlFromStagingToLive() throws Exception {

        when(prefs.getString(eq(PrefsActivity.PREF_SERVER), anyString())).thenReturn("https://staging.server.com");

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            waitForView(withId(R.id.view_staging_warning)).check(matches(isDisplayed()));
            openDrawer();
            waitForView(withText(R.string.menu_settings)).perform(click());
            when(prefs.getString(eq(PrefsActivity.PREF_SERVER), anyString())).thenReturn("https://live.server.com");
            pressBack();
            waitForView(withId(R.id.view_staging_warning)).check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void checkChangeServerUrlFromLiveToStaging() throws Exception {

        when(prefs.getString(eq(PrefsActivity.PREF_SERVER), anyString())).thenReturn("https://live.server.com");

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            waitForView(withId(R.id.view_staging_warning)).check(matches(not(isDisplayed())));
            openDrawer();
            waitForView(withText(R.string.menu_settings)).perform(click());
            when(prefs.getString(eq(PrefsActivity.PREF_SERVER), anyString())).thenReturn("https://staging.server.com");
            pressBack();
            waitForView(withId(R.id.view_staging_warning)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void checkDismissLogic() throws Exception {

        when(prefs.getString(eq(PrefsActivity.PREF_SERVER), anyString())).thenReturn("https://staging.server.com");

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            waitForView(withId(R.id.view_staging_warning)).check(matches(isDisplayed()));
            waitForView(withId(R.id.img_close_staging_warning)).perform(click());
            waitForView(withId(R.id.view_staging_warning)).check(matches(not(isDisplayed())));
            openDrawer();
            waitForView(withText(R.string.menu_settings)).perform(click());
            pressBack();
            waitForView(withId(R.id.view_staging_warning)).check(matches(not(isDisplayed())));
        }
    }


}
