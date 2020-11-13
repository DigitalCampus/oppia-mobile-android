package org.digitalcampus.oppia.activity;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class QuizAttemptActivityTest {

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    }

    @Rule
    public ActivityTestRule<QuizAttemptActivity> quizAttemptActivityTestRule =
            new ActivityTestRule<>(QuizAttemptActivity.class, false, false);

    @Test
    public void testActivityOpenNoBundle() {
        quizAttemptActivityTestRule.launchActivity(null);
    }

    @Test
    public void testActivityOpenWithBundle() {
        // TODO stub - to complete, this is not working yet, gives a NullPointerException
        /*
        DbHelper db = DbHelper.getInstance(context);
        db.getReadableDatabase(); // To force migration if needed;
        TestData td = new TestData();
        td.load(db);
        QuizAttempt qa = new QuizAttempt();
        qa.setSectionTitle("my section");
        qa.setQuizTitle("my quiz");
        qa.setCourseId(100);
        qa.setUserId(1);

        Intent intent = new Intent();
        intent.putExtra(QuizAttempt.TAG, qa);

        quizAttemptActivityTestRule.launchActivity(intent);
        */

    }
}
