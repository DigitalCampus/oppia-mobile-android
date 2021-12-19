package androidTestFiles.org.digitalcampus.oppia.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.activity.DeviceListActivity;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.CompleteCourseProvider;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.Section;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;

import androidTestFiles.TestRules.DaggerInjectMockUITest;
import androidTestFiles.Utils.TestUtils;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static org.digitalcampus.oppia.activity.CourseIndexActivity.JUMPTO_TAG;
import static org.hamcrest.Matchers.allOf;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class CourseIndexActivityUITest extends DaggerInjectMockUITest {

    public static String COURSE_TITLE = "Test course";
    public static String MULTILANG_TITLE = "[{\"en\":\"English title\", \"fi\":\"Suomi title\"}]";

    @Mock
    SharedPreferences prefs;
    @Mock
    SharedPreferences.Editor editor;
    @Mock
    CompleteCourseProvider courseProvider;

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
        when(courseProvider.getCompleteCourseSync(any(), any())).thenReturn(getMockCourse());
    }

    public void givenACorruptedCourse(){
        doAnswer(invocationOnMock -> {
            Context ctx = (Context) invocationOnMock.getArguments()[0];
            ((CourseIndexActivity) ctx).onParseError();
            return null;
        }).when(courseProvider).getCompleteCourseAsync(any(), any());
    }

    public void givenACorrectCourse(CompleteCourse c){
        doAnswer(invocationOnMock -> {
            Context ctx = (Context) invocationOnMock.getArguments()[0];
            ((CourseIndexActivity) ctx).onParseComplete(c);
            return null;
        }).when(courseProvider).getCompleteCourseAsync(any(), any());
    }

    private CompleteCourse getMockCourse(){
        CompleteCourse course = new CompleteCourse();
        course.setShortname("courseactivity_test");
        ArrayList<Lang> langs = new ArrayList<>();
        langs.add(new Lang("en", COURSE_TITLE));
        course.setTitles(langs);
        course.setCourseId(0);
        course.getSections().add(getMockSection("First section", 5, "page", "Activity1.", "aaa"));
        course.getSections().add(getMockSection("Second section", 5, "page", "Activity2.", "bbb"));
        course.getSections().add(getMockSection("Third section", 5, "page", "Activity3.", "ccc"));
        return course;
    }

    private Section getMockSection(String sectionTitle, int numActivities, String type, String actTitle, String digestPrefix){
        Section s = new Section();
        for (int i=0; i<numActivities; i++){
            Activity act = new Activity();
            act.setActType(type);
            act.setDigest(digestPrefix + i);
            if (actTitle != null){
                act.setTitlesFromJSONString("[{\"en\":\"" + actTitle + i + "\"}]");
            }
            s.addActivity(act);
            s.setTitlesFromJSONString("[{\"en\":\"" + sectionTitle + "\"}]");
        }

        return s;
    }

    private Intent getIntentParams(Course course, String jumpToDigest){
        Intent i = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), CourseIndexActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(Course.TAG, course);
        if (jumpToDigest != null){
            bundle.putSerializable(JUMPTO_TAG, jumpToDigest);
        }
        i.putExtras(bundle);
        return i;
    }

    @Test
    public void showErrorMessageWhenCourseLoadFails() throws Exception {
        Course c = getMockCourse();
        givenACorruptedCourse();
        Intent i = getIntentParams(c, null);

        try (ActivityScenario<DeviceListActivity> scenario = ActivityScenario.launch(i)) {
            onView(withText(R.string.error_reading_xml)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void goToActivityWhenClicked() throws Exception {
        CompleteCourse c = getMockCourse();
        Intent i = getIntentParams(c, null);
        givenACorrectCourse(c);

        try (ActivityScenario<DeviceListActivity> scenario = ActivityScenario.launch(i)) {

            onView(withText("Activity1.4")).check(matches(isDisplayed())).perform(click());
            assertEquals(CourseActivity.class, TestUtils.getCurrentActivity().getClass());
        }
    }

    @Test
    public void sectionIconHiddenIfNotIcon() throws Exception {
        CompleteCourse c = getMockCourse();
        Intent i = getIntentParams(c, null);
        givenACorrectCourse(c);

        try (ActivityScenario<DeviceListActivity> scenario = ActivityScenario.launch(i)) {

            onView(allOf(withId(R.id.section_icon), hasSibling(withChild(withText(("First section"))))))
                    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        }
    }

    @Test
    public void sectionIconShown() throws Exception {
        CompleteCourse c = getMockCourse();
        Intent i = getIntentParams(c, null);
        givenACorrectCourse(c);
        c.getSections().get(0).setImageFile("test.png");

        try (ActivityScenario<DeviceListActivity> scenario = ActivityScenario.launch(i)) {

            onView(allOf(withId(R.id.section_icon), hasSibling(withChild(withText(("First section"))))))
                    .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        }
    }


    @Test
    public void goToActivityIfDigestGiven() throws Exception {
        CompleteCourse c = getMockCourse();
        Intent i = getIntentParams(c, "bbb2");
        givenACorrectCourse(c);

        try (ActivityScenario<DeviceListActivity> scenario = ActivityScenario.launch(i)) {
            assertEquals(CourseActivity.class, TestUtils.getCurrentActivity().getClass());
        }
    }

    @Test
    public void dontLaunchActivityWhenWrongDigestGiven() throws Exception {
        CompleteCourse c = getMockCourse();
        Intent i = getIntentParams(c, "nonexistent_digest");
        givenACorrectCourse(c);

        try (ActivityScenario<DeviceListActivity> scenario = ActivityScenario.launch(i)) {
            assertEquals(CourseIndexActivity.class, TestUtils.getCurrentActivity().getClass());
        }
    }


}
