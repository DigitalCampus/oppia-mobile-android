package androidTestFiles.UI;

import android.Manifest;
import android.content.SharedPreferences;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.MainActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidTestFiles.TestRules.DaggerInjectMockUITest;
import androidTestFiles.Utils.CourseUtils;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import static androidTestFiles.Matchers.EspressoTestsMatchers.withDrawable;
import static androidTestFiles.Matchers.RecyclerViewMatcher.withRecyclerView;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class CoursesUpdateDeleteTest extends DaggerInjectMockUITest {
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
            Course mockCourse = CourseUtils.createMockCourse("test-" + (i+1));
            mockCourse.setVersionId(0d);
            courses.add(mockCourse);
        }

        when(coursesRepository.getCourses(any())).thenReturn(courses);

    }

    private void setCachedCourses(Map<String, Double> coursesVersions) throws JSONException {

        JSONArray jsonArray = new JSONArray();
        for (Map.Entry<String, Double> entry : coursesVersions.entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();
            JSONObject jObjCourse = new JSONObject();
            jObjCourse.put("shortname", key);
            jObjCourse.put("version", value);
            jsonArray.put(jObjCourse);
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("courses", jsonArray);
        String json = jsonObject.toString();

        when(prefs.getString(eq(PrefsActivity.PREF_SERVER_COURSES_CACHE), anyString())).thenReturn(json);

    }

    @Test
    public void dontShowIconIfNoUpdates() throws Exception {
        givenThereAreSomeCourses(1);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            onView(withRecyclerView(R.id.recycler_courses)
                    .atPositionOnView(0, R.id.img_sync_status))
                    .check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void showUpdateIcon() throws Exception {

        givenThereAreSomeCourses(2);

        Map<String, Double> coursesVersions = new HashMap<>();
        coursesVersions.put("test-1", 0d);
        coursesVersions.put("test-2", 1d);
        setCachedCourses(coursesVersions);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            onView(withRecyclerView(R.id.recycler_courses)
                    .atPositionOnView(0, R.id.img_sync_status))
                    .check(matches(not(isDisplayed())));

            onView(withRecyclerView(R.id.recycler_courses)
                    .atPositionOnView(1, R.id.img_sync_status))
                    .check(matches(withDrawable(R.drawable.ic_action_refresh)));
        }
    }

    @Test
    public void showDeleteIcon() throws Exception {

        givenThereAreSomeCourses(2);

        Map<String, Double> coursesVersions = new HashMap<>();
        coursesVersions.put("test-1", 0d);
        setCachedCourses(coursesVersions);

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {

            onView(withRecyclerView(R.id.recycler_courses)
                    .atPositionOnView(0, R.id.img_sync_status))
                    .check(matches(not(isDisplayed())));

            onView(withRecyclerView(R.id.recycler_courses)
                    .atPositionOnView(1, R.id.img_sync_status))
                    .check(matches(withDrawable(R.drawable.dialog_ic_action_delete)));
        }
    }
}
