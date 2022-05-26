package androidTestFiles;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.gson.Gson;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.responses.CourseServer;
import org.digitalcampus.oppia.model.responses.CoursesServerResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;

import androidTestFiles.TestRules.DaggerInjectMockUITest;
import androidTestFiles.Utils.CourseUtils;
import androidTestFiles.Utils.UITestActionsUtils;
import androidTestFiles.database.TestDBHelper;

public class ReadOnlyTests extends DaggerInjectMockUITest {

    public static final String PATH_COMMON = "common";
    public static final String COURSE_FEEDBACK = "course-with-feedback.zip";
    public static final String COURSE_QUIZ = "course-with-quiz.zip";
    private static final String COURSE_FEEDBACK_SHORTNAME = "course-feedback";
    private static final String COURSE_QUIZ_SHORTNAME = "course-quiz";

    @Mock
    SharedPreferences prefs;
    @Mock
    SharedPreferences.Editor editor;
    private TestDBHelper testDBHelper;
    private static final long USER_ID_NONE = -1;
    private static final int COURSE_ID = 1;

    private void initMockEditor() {
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
        // First ensure to use in-memory database
        testDBHelper = new TestDBHelper(InstrumentationRegistry.getInstrumentation().getTargetContext());
        testDBHelper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        testDBHelper.tearDown();
        CourseUtils.cleanUp();
    }

    private void saveCacheCourse(String shortname, String status) {
        CourseServer course = new CourseServer();
        course.setShortname(shortname);
        course.setStatus(status);
        CoursesServerResponse coursesServerResponse = new CoursesServerResponse();
        coursesServerResponse.setCourses(Arrays.asList(course));
        String serialized = new Gson().toJson(coursesServerResponse);
        when(prefs.getString(eq(PrefsActivity.PREF_SERVER_COURSES_CACHE), anyString())).thenReturn(serialized);
    }


    private Intent getTestCourseIntent(String status) {
//        int courseId = testDBHelper.getDbHelper().getCourseID("ref-course");
        Course course = testDBHelper.getDbHelper().getCourseWithProgress(COURSE_ID, USER_ID_NONE);
        course.setStatus(status);
        Intent i = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), CourseIndexActivity.class);
        i.putExtra(Course.TAG, course);
        return i;
    }

    @Test
    public void showFeedbackIfCourseLive() throws Exception {

        installCourse(PATH_COMMON, COURSE_FEEDBACK);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIntent(Course.STATUS_READ_ONLY))) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 2);

        }
    }

}
