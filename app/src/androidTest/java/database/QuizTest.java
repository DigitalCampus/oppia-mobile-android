package database;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class QuizTest {

    private Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

    }

    @Test
    public void getUnexportedQuizAttempts() {

        DbHelper dbHelper = DbHelper.getInstance(context);

        TestData testData = new TestData();
        testData.load(dbHelper);
        QuizAttempt qa = new QuizAttempt();
        qa.setCourseId(1);
        qa.setUserId(1);
        dbHelper.insertQuizAttempt(qa);

        ArrayList<QuizAttempt> quizAttempts = (ArrayList<QuizAttempt>) dbHelper.getUnexportedQuizAttempts(1);
        assertEquals(1, quizAttempts.size());

    }

    // getGlobalQuizAttempts
    @Test
    public void getGlobalQuizAttempts() {

        DbHelper dbHelper = DbHelper.getInstance(context);

        // TODO - add some test data here to really check
        List<QuizAttempt> quizAttempts = (ArrayList<QuizAttempt>) dbHelper.getGlobalQuizAttempts(1, "en");
        assertEquals(0, quizAttempts.size());

    }

    // TODO getQuizAttempt
    // TODO getAllQuizAttemtps
    // TODO getQuizAttempts
    // TODO insertQuizAttempt
    // TODO updateQuizAttempt
    // TODO insertQuizAttempts
    // TODO getUnsentQuizAttempts
    // TODO getUnexportedQuizAttempts
    // TODO markQuizSubmitted
    // TODO deleteQuizAttempts
    // TODO isQuizFirstAttempt
    // TODO isQuizFirstAttemptToday
}
