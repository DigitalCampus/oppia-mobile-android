package androidTestFiles.features;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static androidTestFiles.utils.parent.BaseTest.COURSE_FEEDBACK;
import static androidTestFiles.utils.parent.BaseTest.COURSE_QUIZ;
import static androidTestFiles.utils.parent.BaseTest.COURSE_QUIZ_SHORTNAME;
import static androidTestFiles.utils.parent.BaseTest.PATH_COMMON_TESTS;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.gson.Gson;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseIndexActivity;
import org.digitalcampus.oppia.activity.CourseQuizAttemptsActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.QuizAttemptActivity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.model.QuizAttemptRepository;
import org.digitalcampus.oppia.model.QuizStats;
import org.digitalcampus.oppia.model.responses.CourseServer;
import org.digitalcampus.oppia.model.responses.CoursesServerResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Locale;

import androidTestFiles.database.TestDBHelper;
import androidTestFiles.utils.CourseUtils;
import androidTestFiles.utils.UITestActionsUtils;
import androidTestFiles.utils.parent.DaggerInjectMockUITest;

public class ReadOnlyTests extends DaggerInjectMockUITest {

    @Mock
    SharedPreferences prefs;
    @Mock
    SharedPreferences.Editor editor;

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    QuizAttemptRepository attemptsRepository;

    private TestDBHelper testDBHelper;
    private static final long USER_ID_NONE = -1;
    private static final int COURSE_ID = 1;
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

        // First ensure to use in-memory database
        testDBHelper = new TestDBHelper(context);
        testDBHelper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        testDBHelper.tearDown();
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


    private Intent getTestCourseIndexIntent(String status) {
        Course course = testDBHelper.getDbHelper().getCourseWithProgress(COURSE_ID, USER_ID_NONE);
        course.setStatus(status);
        Intent i = new Intent(InstrumentationRegistry.getInstrumentation().getTargetContext(), CourseIndexActivity.class);
        i.putExtra(Course.TAG, course);
        return i;
    }

    @Test
    public void showFeedbackIfCourseLive() throws Exception {

        installCourse(PATH_COMMON_TESTS, COURSE_FEEDBACK);
        String status = Course.STATUS_LIVE;

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIndexIntent(status))) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 2);
            onView(withId(R.id.quiz_unavailable)).check(doesNotExist());
        }
    }

    @Test
    public void showFeedbackNotAvailableIfCourseReadOnly() throws Exception {

        installCourse(PATH_COMMON_TESTS, COURSE_FEEDBACK);
        String status = Course.STATUS_READ_ONLY;

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIndexIntent(status))) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 2);
            onView(withId(R.id.quiz_unavailable)).check(matches(withText(
                    context.getString(R.string.read_only_answer_unavailable_message,
                            context.getString(R.string.feedback).toLowerCase(Locale.ROOT)))));
        }
    }


    @Test
    public void showQuizIfCourseLive() throws Exception {

        installCourse(PATH_COMMON_TESTS, COURSE_QUIZ);
        String status = Course.STATUS_LIVE;

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIndexIntent(status))) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 2);
            onView(withId(R.id.quiz_unavailable)).check(doesNotExist());
        }
    }

    @Test
    public void showQuizNotAvailableIfCourseReadOnly() throws Exception {

        installCourse(PATH_COMMON_TESTS, COURSE_QUIZ);
        String status = Course.STATUS_READ_ONLY;

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIndexIntent(status))) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 2);
            onView(withId(R.id.quiz_unavailable)).check(matches(withText(
                    context.getString(R.string.read_only_answer_unavailable_message,
                            context.getString(R.string.quiz).toLowerCase(Locale.ROOT)))));

        }
    }

    @Test
    public void dontShowViewAttempsButtonIfQuizNotAvailableHasNoAttemps() throws Exception {

        installCourse(PATH_COMMON_TESTS, COURSE_QUIZ);
        String status = Course.STATUS_READ_ONLY;

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIndexIntent(status))) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 2);
            onView(withId(R.id.btn_quiz_unavailable)).check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void showViewAttempsButtonIfQuizNotAvailableHasAttemps() throws Exception {

        installCourse(PATH_COMMON_TESTS, COURSE_QUIZ);
        String status = Course.STATUS_READ_ONLY;

        QuizStats qs = new QuizStats();
        qs.setNumAttempts(1);

        when(attemptsRepository.getQuizAttemptStats(any(), anyString())).thenReturn(qs);

        try (ActivityScenario<CourseIndexActivity> scenario = ActivityScenario.launch(getTestCourseIndexIntent(status))) {
            UITestActionsUtils.clickRecyclerViewPosition(R.id.recycler_course_sections, 2);
            onView(withId(R.id.btn_quiz_unavailable)).check(matches(isDisplayed()));
        }
    }



    private Intent getTestCourseQuizAttemptsIntent(int numAttempts) {
        QuizStats qs = new QuizStats();
        qs.setNumAttempts(numAttempts);
        qs.setDigest("4a9f4ca6bbec6e1142c0b8cf233a4d95");
        Intent i = new Intent(context, CourseQuizAttemptsActivity.class);
        i.putExtra(QuizStats.TAG, qs);
        return i;
    }


    @Test
    public void showRetakeQuizButtonIfNotReadOnlyCourse_CourseQuizAttemptsActivity_WithAttempts() throws Exception {

        installCourse(PATH_COMMON_TESTS, COURSE_QUIZ);

        mockCourseCache(COURSE_QUIZ_SHORTNAME, Course.STATUS_LIVE);

        try (ActivityScenario<CourseQuizAttemptsActivity> scenario = ActivityScenario.launch(getTestCourseQuizAttemptsIntent(1))) {
            onView(withId(R.id.retake_quiz_btn)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_take_quiz)).check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void showTakeQuizButtonIfNotReadOnlyCourse_CourseQuizAttemptsActivity_WithNoAttempts() throws Exception {

        installCourse(PATH_COMMON_TESTS, COURSE_QUIZ);

        mockCourseCache(COURSE_QUIZ_SHORTNAME, Course.STATUS_LIVE);

        try (ActivityScenario<CourseQuizAttemptsActivity> scenario = ActivityScenario.launch(getTestCourseQuizAttemptsIntent(0))) {
            onView(withId(R.id.retake_quiz_btn)).check(matches(not(isDisplayed())));
            onView(withId(R.id.btn_take_quiz)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void dontShowRetakeQuizButtonIfReadOnlyCourse_CourseQuizAttemptsActivity() throws Exception {

        installCourse(PATH_COMMON_TESTS, COURSE_QUIZ);

        mockCourseCache(COURSE_QUIZ_SHORTNAME, Course.STATUS_READ_ONLY);

        try (ActivityScenario<CourseQuizAttemptsActivity> scenario = ActivityScenario.launch(getTestCourseQuizAttemptsIntent(0))) {
            onView(withId(R.id.retake_quiz_btn)).check(matches(not(isDisplayed())));
            onView(withId(R.id.btn_take_quiz)).check(matches(not(isDisplayed())));
        }
    }


    private Intent getTestQuizAttemptsIntent() {
        QuizAttempt quizAttempt = new QuizAttempt();
        quizAttempt.setActivityDigest("4a9f4ca6bbec6e1142c0b8cf233a4d95");
        quizAttempt.setCourseId(COURSE_ID);
        Intent i = new Intent(context, QuizAttemptActivity.class);
        i.putExtra(QuizAttempt.TAG, quizAttempt);
        i.putExtra(CourseQuizAttemptsActivity.SHOW_ATTEMPT_BUTTON, true);
        return i;
    }

    @Test
    public void showRetakeQuizButtonIfNotReadOnlyCourse_QuizAttemptActivity() throws Exception {

        installCourse(PATH_COMMON_TESTS, COURSE_QUIZ);

        mockCourseCache(COURSE_QUIZ_SHORTNAME, Course.STATUS_LIVE);

        try (ActivityScenario<QuizAttemptActivity> scenario = ActivityScenario.launch(getTestQuizAttemptsIntent())) {
            onView(withId(R.id.retake_quiz_btn)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void dontShowRetakeQuizButtonIfReadOnlyCourse_QuizAttemptActivity() throws Exception {

        installCourse(PATH_COMMON_TESTS, COURSE_QUIZ);

        mockCourseCache(COURSE_QUIZ_SHORTNAME, Course.STATUS_READ_ONLY);

        try (ActivityScenario<QuizAttemptActivity> scenario = ActivityScenario.launch(getTestQuizAttemptsIntent())) {
            onView(withId(R.id.retake_quiz_btn)).check(matches(not(isDisplayed())));
        }
    }



}
