package UI;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.di.AppModule;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.Section;
import org.hamcrest.core.AllOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;
import it.cosenonjaviste.daggermock.DaggerMockRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.ActivityResultMatchers.hasResultCode;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

public class CourseActivityUITest {

    public static String COURSE_TITLE = "Test course";
    public static String MULTILANG_TITLE = "[{\"en\":\"English title\", \"fi\":\"Suomi title\"}]";

    @Mock
    SharedPreferences prefs;
    @Mock
    SharedPreferences.Editor editor;

    @Rule
    public DaggerMockRule<AppComponent> daggerRule =
            new DaggerMockRule<>(AppComponent.class, new AppModule((App) InstrumentationRegistry.getInstrumentation()
                    .getTargetContext()
                    .getApplicationContext())).set(
                    component -> {
                        App app =
                                (App) InstrumentationRegistry.getInstrumentation()
                                        .getTargetContext()
                                        .getApplicationContext();
                        app.setComponent(component);
                    });

    @Rule
    public ActivityTestRule<CourseActivity> courseActivityTestRule =
            new ActivityTestRule<>(CourseActivity.class, false, false);

    private void initMockEditor() {
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);
    }

    @Before
    public void setUp() throws Exception {
        initMockEditor();
        when(prefs.edit()).thenReturn(editor);
    }

    private Course getMockCourse(){
        Course course = new Course();
        course.setShortname("courseactivity_test");
        ArrayList<Lang> langs = new ArrayList<>();
        langs.add(new Lang("en", COURSE_TITLE));
        course.setTitles(langs);
        course.setCourseId(0);
        return course;
    }

    private Section getMockSection(int numActivities){
        return getMockSection(numActivities, "page",null);
    }

    private Section getMockSection(int numActivities, String type, String actTitle){
        Section s = new Section();
        for (int i=0; i<numActivities; i++){
            Activity act = new Activity();
            act.setActType(type);
            act.setDigest("aaaaaa");
            if (actTitle != null){
                act.setTitlesFromJSONString("[{\"en\":\"" + actTitle + i + "\"}]");
            }

            s.addActivity(act);
        }

        return s;
    }

    private Intent getIntentParams(Course course, Section s, int position, boolean isBaseline){
        Intent i = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Section.TAG, s);
        bundle.putSerializable(Course.TAG, course);
        bundle.putSerializable(CourseActivity.NUM_ACTIVITY_TAG, position);
        if (isBaseline){
            bundle.putSerializable(CourseActivity.BASELINE_TAG, true);
        }
        i.putExtras(bundle);
        return i;
    }

    @Test
    public void openCourseActivity() throws Exception {

        Intent i = getIntentParams(getMockCourse(), getMockSection(3, "page", "Test"), 0, false);
        courseActivityTestRule.launchActivity(i);
        onView(allOf(isAssignableFrom(TextView.class), withParent(isAssignableFrom(Toolbar.class)))).check(matches(withText(COURSE_TITLE)));

        //Check the three activity tabs where added
        onView(AllOf.allOf(withText("Test0"), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                isDescendantOfA(withId(R.id.tabs_toolbar))))
                .check(matches(isDisplayed()));

        onView(AllOf.allOf(withText("Test1"), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                isDescendantOfA(withId(R.id.tabs_toolbar))))
                .check(matches(isDisplayed()));

        onView(AllOf.allOf(withText("Test2"), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                isDescendantOfA(withId(R.id.tabs_toolbar))))
                .check(matches(isDisplayed()));
    }


    // Trying to open an activity with a wrong index position withing the section
    @Test
    public void openCourseActivityWrongPosition() throws Exception {

        Intent i = getIntentParams(getMockCourse(), getMockSection(3), 5, false);
        courseActivityTestRule.launchActivity(i);

        assertThat(courseActivityTestRule.getActivityResult(), hasResultCode(android.app.Activity.RESULT_CANCELED));
    }

    @Test
    public void openBaselineActivity() throws Exception {

        Intent i = getIntentParams(getMockCourse(), getMockSection(1), 0, true);
        courseActivityTestRule.launchActivity(i);
        onView(allOf(isAssignableFrom(TextView.class), withParent(isAssignableFrom(Toolbar.class)))).check(matches(withText(COURSE_TITLE)));
    }

    @Test
    public void changeTabWithSequencingToLockedActivity() throws Exception {
        Course c = getMockCourse();
        c.setSequencingMode(Course.SEQUENCING_MODE_COURSE);
        Section section = getMockSection(3, "page", "Test");

        Intent i = getIntentParams(c, section, 0, false);
        courseActivityTestRule.launchActivity(i);

        onView(AllOf.allOf(withText("Test2"), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                isDescendantOfA(withId(R.id.tabs_toolbar))))
                .perform(click());

        onView(withText(R.string.sequencing_dialog_title)).check(matches(isDisplayed()));
    }

    @Test
    public void changeTabWithSequencingToUnlockedActivity() throws Exception {
        Course c = getMockCourse();
        c.setSequencingMode(Course.SEQUENCING_MODE_COURSE);
        Section section = getMockSection(3, "page", "Test");

        Intent i = getIntentParams(c, section, 2, false);
        courseActivityTestRule.launchActivity(i);

        onView(AllOf.allOf(withText("Test0"), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                isDescendantOfA(withId(R.id.tabs_toolbar))))
                .perform(click());

        onView(withText(R.string.sequencing_dialog_title)).check(doesNotExist());
    }

    @Test
    public void allActivityTypes() throws Exception {

        List<String> types = Arrays.asList("page", "quiz", "resource", "feedback", "url");
        Section section = getMockSection(types.size(), "page", "Test");
        for (int t=0; t<types.size(); t++){
            section.getActivities().get(t).setActType(types.get(t));
        }
        Intent i = getIntentParams(getMockCourse(), section, 0, false);
        courseActivityTestRule.launchActivity(i);

    }

    @Test
    public void unknownActivityType() throws Exception {

        Section section = getMockSection(3, "page", "Test");
        Activity first = section.getActivities().get(0);
        first.setActType("madeUpType");

        Intent i = getIntentParams(getMockCourse(),  section, 0, false);
        courseActivityTestRule.launchActivity(i);

        //Check that the tab of the activity with unknown type has not been added
        onView(AllOf.allOf(withText("Test0"), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                isDescendantOfA(withId(R.id.tabs_toolbar))))
                .check(doesNotExist());
    }


    @Test
    public void changeLanguage() throws Exception {

        Course c = getMockCourse();
        Section section = getMockSection(3, "page", "Test");
        Activity first = section.getActivities().get(0);
        ArrayList<Lang> langs = new ArrayList<>();
        langs.add(new Lang("en", "English"));
        langs.add(new Lang("fi", "Finnish"));
        c.setLangs(langs);
        first.setTitles(new ArrayList<>());
        first.setTitlesFromJSONString(MULTILANG_TITLE);

        //Mock the calls to sharedPrefs when the value is set
        when(prefs.getString(eq(PrefsActivity.PREF_LANGUAGE), anyString())).thenReturn("en");

        Intent i = getIntentParams(c, section, 0, false);
        courseActivityTestRule.launchActivity(i);

        onView(AllOf.allOf(withText("English title"), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                isDescendantOfA(withId(R.id.tabs_toolbar))))
                .check(matches(isDisplayed()));

        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getInstrumentation().getTargetContext());
        onView(anyOf(withText(R.string.menu_language), withId(R.id.menu_language))).perform(click());

        when(prefs.getString(eq(PrefsActivity.PREF_LANGUAGE), anyString())).thenReturn("fi");
        onView(withText("suomi")).perform(click());

        onView(AllOf.allOf(withText("Suomi title"), withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE),
                isDescendantOfA(withId(R.id.tabs_toolbar))))
                .check(matches(isDisplayed()));
    }

}
