package androidTestFiles.UI;

import android.app.UiAutomation;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;

import androidx.preference.Preference;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.WelcomeActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.utils.UIUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;


import androidTestFiles.TestRules.DaggerInjectMockUITest;
import androidTestFiles.Utils.CourseUtils;
import androidTestFiles.Utils.TestUtils;

import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.matcher.PreferenceMatchers.withKey;
import static androidx.test.espresso.matcher.PreferenceMatchers.withTitle;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class PrefsActivityUITest extends DaggerInjectMockUITest {

    @Rule
    public ActivityTestRule<PrefsActivity> prefsActivityTestRule =
            new ActivityTestRule<>(PrefsActivity.class, false, false);

    @Mock
    CoursesRepository coursesRepository;
    @Mock
    SharedPreferences prefs;
    @Mock
    SharedPreferences.Editor editor;


    @Before
    public void setUp() throws Exception {
        initMockEditor();
        when(prefs.edit()).thenReturn(editor);
    }

    private void initMockEditor() {
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);
    }

    private void givenThereAreSomeCourses(int numberOfCourses) {

        ArrayList<Course> courses = new ArrayList<>();

        for (int i = 0; i < numberOfCourses; i++) {
            courses.add(CourseUtils.createMockCourse());
        }

        when(coursesRepository.getCourses((Context) any())).thenReturn(courses);

    }

    private int getCoursesCount() {
        return coursesRepository.getCourses((Context) any()).size();
    }



    @Test
    public void showsChangeLanguageOptionIfThereAreCoursesWithManyLanguages() throws Exception {

        givenThereAreSomeCourses(1);

        coursesRepository.getCourses((Context) any()).get(0).setLangs(new ArrayList<Lang>() {{
            add(new Lang("en", "English"));
            add(new Lang("es", "Spanish"));
        }});

        prefsActivityTestRule.launchActivity(null);

        onView(withId(androidx.preference.R.id.recycler_view))
                .perform(RecyclerViewActions.actionOnItem(hasDescendant(withText(R.string.prefDisplay_title)),
                        click()));

        onView(withText(R.string.prefLanguage)).check(matches(isDisplayed()));
    }

    @Test
    public void hideChangeLanguageOptionIfThereAreCoursesWithOnlyOneLanguage() throws Exception {

        givenThereAreSomeCourses(1);

        coursesRepository.getCourses((Context) any()).get(0).setLangs(new ArrayList<Lang>() {{
            add(new Lang("en", "English"));
        }});

        prefsActivityTestRule.launchActivity(null);

        onView(withText(R.string.prefLanguage)).check(doesNotExist());
    }


    @Test
    public void goToMainActivityIfUserDontModifyServerUrl() throws InterruptedException {

        when(prefs.getString(eq(PrefsActivity.PREF_USER_NAME), anyString())).thenReturn("test_user");

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.drawer))
                    .check(matches(isClosed(Gravity.LEFT)))
                    .perform(DrawerActions.open());

            onView(withText(R.string.menu_settings)).perform(click());

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(
                            hasDescendant(withText(R.string.prefAdvanced_title)), click()));

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(
                            hasDescendant(withText(R.string.prefServer)), click()));

            closeSoftKeyboard();

            onView(withText("OK"))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()))
                    .perform(click());

            onView(withText(R.string.cancel))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()))
                    .perform(click());

            pressBack();
            pressBack();

            assertEquals(MainActivity.class, TestUtils.getCurrentActivity().getClass());

        }

    }



    @Test
    public void goToWelcomeActivityIfUserModifyServerUrl() throws InterruptedException {

        when(prefs.getString(eq(PrefsActivity.PREF_USER_NAME), anyString())).thenReturn("test_user");

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            onView(withId(R.id.drawer))
                    .check(matches(isClosed(Gravity.LEFT)))
                    .perform(DrawerActions.open());

            onView(withText(R.string.menu_settings)).perform(click());

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(
                            hasDescendant(withText(R.string.prefAdvanced_title)), click()));

            onView(withId(androidx.preference.R.id.recycler_view))
                    .perform(RecyclerViewActions.actionOnItem(
                            hasDescendant(withText(R.string.prefServer)), click()));

            closeSoftKeyboard();

            onView(withText("OK"))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()))
                    .perform(click());

            onView(withText(R.string.accept))
                    .inRoot(isDialog())
                    .check(matches(isDisplayed()))
                    .perform(click());

            pressBack();
            pressBack();

            assertEquals(WelcomeActivity.class, TestUtils.getCurrentActivity().getClass());

        }

    }
}
