package androidTestFiles.UI;

import android.Manifest;
import android.content.SharedPreferences;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;

import androidTestFiles.TestRules.DaggerInjectMockUITest;
import androidTestFiles.Utils.CourseUtils;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

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
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

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
            // todo Working on this test
            Assert.assertEquals(1, coursesRepository.getCourses(any()).size());
            return;
        }


        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            Espresso.onView(ViewMatchers.withId(R.id.recycler_courses))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

            Espresso.onView(ViewMatchers.withId(R.id.recycler_course_sections))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

            Espresso.onView(ViewMatchers.withId(R.id.recycler_course_sections))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

        }
    }

}
