package androidTestFiles.features;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static androidTestFiles.utils.matchers.RecyclerViewMatcher.withRecyclerView;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.gson.Gson;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.responses.CourseServer;
import org.digitalcampus.oppia.model.responses.CoursesServerResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;

import androidTestFiles.utils.parent.DaggerInjectMockUITest;
import androidTestFiles.utils.assertions.StatusBadgeAssertion;
import androidTestFiles.utils.CourseUtils;

public class StatusBadgeTests extends DaggerInjectMockUITest {

    public static final String PATH_COMMON = "common";
    public static final String COURSE_QUIZ = "course-with-quiz.zip";
    private static final String COURSE_QUIZ_SHORTNAME = "course-with-quiz";

    @Mock
    SharedPreferences prefs;
    @Mock
    SharedPreferences.Editor editor;

    private Context context;

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

        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    }

    @After
    public void tearDown() throws Exception {
        CourseUtils.cleanUp();
    }

    private void mockCourseCache(String shortname, String status) {
        CourseServer course = new CourseServer();
        course.setShortname(shortname);
        course.setStatus(status);
        course.setVersion(1.0);
        CoursesServerResponse coursesServerResponse = new CoursesServerResponse();
        coursesServerResponse.setCourses(Arrays.asList(course));
        String serialized = new Gson().toJson(coursesServerResponse);
        when(prefs.getString(eq(PrefsActivity.PREF_SERVER_COURSES_CACHE), anyString())).thenReturn(serialized);
    }



    @Test
    public void setCorrectReadOnlyStatusBadge() throws Exception {

        installCourse(PATH_COMMON, COURSE_QUIZ);

        mockCourseCache(COURSE_QUIZ_SHORTNAME, Course.STATUS_READ_ONLY);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            onView(withRecyclerView(R.id.recycler_courses)
                    .atPositionOnView(0, R.id.view_course_status))
                    .check(StatusBadgeAssertion.withText(context.getString(R.string.status_read_only)));
        }
    }

    @Test
    public void hideLiveStatusBadge() throws Exception {

        installCourse(PATH_COMMON, COURSE_QUIZ);

        mockCourseCache(COURSE_QUIZ_SHORTNAME, Course.STATUS_LIVE);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            onView(withRecyclerView(R.id.recycler_courses)
                    .atPositionOnView(0, R.id.view_course_status))
                    .check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void hideNewDownloadsDisabledStatusBadge() throws Exception {

        installCourse(PATH_COMMON, COURSE_QUIZ);

        mockCourseCache(COURSE_QUIZ_SHORTNAME, Course.STATUS_NEW_DOWNLOADS_DISABLED);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            onView(withRecyclerView(R.id.recycler_courses)
                    .atPositionOnView(0, R.id.view_course_status))
                    .check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void setCorrectDraftStatusBadge() throws Exception {

        installCourse(PATH_COMMON, COURSE_QUIZ);

        mockCourseCache(COURSE_QUIZ_SHORTNAME, Course.STATUS_DRAFT);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            onView(withRecyclerView(R.id.recycler_courses)
                    .atPositionOnView(0, R.id.view_course_status))
                    .check(StatusBadgeAssertion.withText(context.getString(R.string.status_draft)));
        }
    }
}
