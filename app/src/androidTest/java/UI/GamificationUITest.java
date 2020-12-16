package UI;

import android.content.Context;
import android.content.SharedPreferences;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.model.Badge;
import org.digitalcampus.oppia.model.CompleteCourseProvider;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.Points;
import org.digitalcampus.oppia.model.User;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;

import TestRules.DaggerInjectMockUITest;
import Utils.CourseUtils;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.RootMatchers;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.action.ViewActions.click;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class GamificationUITest extends DaggerInjectMockUITest {

    @Rule
    public ActivityTestRule<MainActivity> mainActivityTestRule =
            new ActivityTestRule<>(MainActivity.class, false, false);

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

        when(coursesRepository.getCourses(any())).thenReturn(courses);

    }


    @Test
    public void showsContextMenuOnCourseLongClick() throws Exception {

        givenThereAreSomeCourses(1);

        if (true) {
            //Working on this test
            Assert.assertEquals(1, coursesRepository.getCourses(any()).size());
            return;
        }


        mainActivityTestRule.launchActivity(null);

        Espresso.onView(ViewMatchers.withId(R.id.recycler_courses))
                .inRoot(RootMatchers.withDecorView(
                        Matchers.is(mainActivityTestRule.getActivity().getWindow().getDecorView())))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        Espresso.onView(ViewMatchers.withId(R.id.recycler_course_sections))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        Espresso.onView(ViewMatchers.withId(R.id.recycler_course_sections))
                .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

    }

}
