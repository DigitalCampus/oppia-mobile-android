package androidTestFiles.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.mockito.Matchers.eq;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseQuizAttemptsActivity;
import org.digitalcampus.oppia.model.QuizStats;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidTestFiles.database.BaseTestDB;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

@RunWith(AndroidJUnit4.class)
public class CourseQuizAttemptsActivityTest extends BaseTestDB {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private Context context;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    }

    private void addData() {
        getTestDataManager().addUsers();
        getTestDataManager().addCourse(1, "test-course");
        getTestDataManager().addActivity("1234", 1);
    }

    private Intent getCourseQuizAttemptsIntent(int numAttempts, float averageScore, float maxScore) {

        QuizStats qs = new QuizStats();
        qs.setSectionTitle("my section");
        qs.setQuizTitle("my quiz");
        qs.setDigest("1234");
        qs.setNumAttempts(numAttempts);
        qs.setAverageScore(averageScore);
        qs.setMaxScore(maxScore);
        Intent intent = new Intent(context, CourseQuizAttemptsActivity.class);
        intent.putExtra(QuizStats.TAG, qs);
        return intent;
    }


    @Test
    public void testActivityOpenWithAttempts() throws InterruptedException {

        addData();

        float avgScore = 70;
        float maxScore = 100;

        Intent intent = getCourseQuizAttemptsIntent(10, avgScore, maxScore);

        try (ActivityScenario<CourseQuizAttemptsActivity> scenario = ActivityScenario.launch(intent)) {

            int avgPercent = Math.round(avgScore * 100.0f / Math.max(1, maxScore));
            onView(withId(R.id.highlight_average)).check(matches(withText(avgPercent + "%")));
            onView(withId(R.id.empty_state)).check(matches(not(isDisplayed())));
            onView((withId(R.id.retake_quiz_btn))).check(matches(isDisplayed()));
        }

    }


    @Test
    public void testActivityOpenWithNoAttempts() throws InterruptedException {

        addData();

        Intent intent = getCourseQuizAttemptsIntent(0, 0, 0);

        try (ActivityScenario<CourseQuizAttemptsActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.highlight_average)).check(matches(withText("-")));
            onView(withId(R.id.empty_state)).check(matches(isDisplayed()));
            onView((withId(R.id.retake_quiz_btn))).check(matches(not(isDisplayed())));
        }
    }

}
