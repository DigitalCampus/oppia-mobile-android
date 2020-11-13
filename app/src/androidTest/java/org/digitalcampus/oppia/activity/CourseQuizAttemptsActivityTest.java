package org.digitalcampus.oppia.activity;

import android.content.Context;
import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.digitalcampus.oppia.model.QuizStats;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import database.BaseTestDB;

@RunWith(AndroidJUnit4.class)
public class CourseQuizAttemptsActivityTest extends BaseTestDB {


    private Context context;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    }

    @Rule
    public ActivityTestRule<CourseQuizAttemptsActivity> courseQuizAttemptsActivityTestRule =
            new ActivityTestRule<>(CourseQuizAttemptsActivity.class, false, false);

    @Test
    public void testActivityOpenNoBundle() {
        courseQuizAttemptsActivityTestRule.launchActivity(null);
    }

    @Test
    public void testActivityOpenWithAttempts() {

        getTestDataManager().addUsers();
        getTestDataManager().addCourses();

        QuizStats qs = new QuizStats();
        qs.setSectionTitle("my section");
        qs.setQuizTitle("my quiz");
        qs.setDigest("1234");
        qs.setNumAttempts(10);
        qs.setAverageScore(7);
        Intent intent = new Intent();
        intent.putExtra(QuizStats.TAG, qs);
        courseQuizAttemptsActivityTestRule.launchActivity(intent);

        // TODO assertions
    }

    @Test
    public void testActivityOpenWithNoAttempts() {

        getTestDataManager().addUsers();
        getTestDataManager().addCourses();

        QuizStats qs = new QuizStats();
        qs.setSectionTitle("my section");
        qs.setQuizTitle("my quiz");
        qs.setDigest("1234");
        qs.setNumAttempts(0);
        qs.setAverageScore(0);
        Intent intent = new Intent();
        intent.putExtra(QuizStats.TAG, qs);
        courseQuizAttemptsActivityTestRule.launchActivity(intent);

        // TODO assertions
    }

}
