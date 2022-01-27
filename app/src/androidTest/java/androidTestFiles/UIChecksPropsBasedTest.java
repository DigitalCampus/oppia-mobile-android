package androidTestFiles;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.PrivacyActivity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;

import androidTestFiles.TestRules.DaggerInjectMockUITest;
import androidTestFiles.Utils.CourseUtils;

@RunWith(AndroidJUnit4.class)
public class UIChecksPropsBasedTest extends DaggerInjectMockUITest {


    @Mock
    protected SharedPreferences prefs;

    @Mock
    protected CoursesRepository coursesRepository;

    private void givenThereAreSomeCourses(int numberOfCourses) {

        ArrayList<Course> courses = new ArrayList<>();

        for (int i = 0; i < numberOfCourses; i++) {
            courses.add(CourseUtils.createMockCourse());
        }

        when(coursesRepository.getCourses(any())).thenReturn(courses);

    }

    @Test
    public void checkSettingsMenuItemVisibility() throws Exception {

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            openDrawer();
            if (BuildConfig.MENU_ALLOW_SETTINGS) {
                onView(withId(R.id.menu_settings)).check(matches(isDisplayed()));
            } else {
                onView(withId(R.id.menu_settings)).check(doesNotExist());
            }
        }
    }

    @Test
    public void checkCourseDownloadMenuItemVisibility() throws Exception {

        when(prefs.getBoolean(eq(PrefsActivity.PREF_DOWNLOAD_ENABLED), anyBoolean()))
                .thenReturn(BuildConfig.MENU_ALLOW_COURSE_DOWNLOAD);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            openDrawer();
            if (BuildConfig.MENU_ALLOW_COURSE_DOWNLOAD) {
                onView(withId(R.id.menu_download)).check(matches(isDisplayed()));
            } else {
                onView(withId(R.id.menu_download)).check(doesNotExist());
            }
        }
    }

    @Test
    public void checkSyncMenuItemVisibility() throws Exception {

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            openDrawer();
            if (BuildConfig.MENU_ALLOW_SYNC) {
                onView(withId(R.id.menu_sync)).check(matches(isDisplayed()));
            } else {
                onView(withId(R.id.menu_sync)).check(doesNotExist());
            }
        }
    }


    @Test
    public void checkLanguageMenuItemVisibility() throws Exception {

        when(prefs.getBoolean(eq(PrefsActivity.PREF_CHANGE_LANGUAGE_ENABLED), anyBoolean()))
                .thenReturn(BuildConfig.MENU_ALLOW_LANGUAGE);

        givenThereAreSomeCourses(1);

        coursesRepository.getCourses(any()).get(0).setLangs(new ArrayList<Lang>() {{
            add(new Lang("en", "English"));
            add(new Lang("es", "Spanish"));
        }});

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            openDrawer();

            if (BuildConfig.MENU_ALLOW_LANGUAGE) {
                onView(withId(R.id.menu_language)).check(matches(isDisplayed()));
            } else {
                onView(withId(R.id.menu_language)).check(doesNotExist());
            }
        }
    }


    @Test
    public void checkEditProfileButtonVisibility() throws Exception {

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            openDrawer();
            onView(withId(R.id.btn_expand_profile_options)).perform(click());
            if (BuildConfig.MENU_ALLOW_EDIT_PROFILE) {
                onView(withId(R.id.btn_edit_profile)).check(matches(isDisplayed()));
            } else {
                onView(withId(R.id.btn_edit_profile)).check(matches(not(isDisplayed())));
            }
        }
    }

    @Test
    public void checkChangePasswordButtonVisibility() throws Exception {

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            openDrawer();
            onView(withId(R.id.btn_expand_profile_options)).perform(click());
            if (BuildConfig.MENU_ALLOW_CHANGE_PASSWORD) {
                onView(withId(R.id.btn_change_password)).check(matches(isDisplayed()));
            } else {
                onView(withId(R.id.btn_change_password)).check(matches(not(isDisplayed())));
            }
        }
    }

    @Test
    public void checkDeleteAccountButtonVisibility() throws Exception {

        when(prefs.getString(eq(PrefsActivity.PREF_USER_NAME), anyString())).thenReturn("test_user");

        try (ActivityScenario<PrivacyActivity> scenario = ActivityScenario.launch(PrivacyActivity.class)) {

            if (BuildConfig.DELETE_ACCOUNT_ENABLED) {
                onView(withId(R.id.delete_data_section)).perform(scrollTo()).check(matches(isDisplayed()));
            } else {
                onView(withId(R.id.delete_data_section)).check(matches(not(isDisplayed())));
            }
        }
    }
}
