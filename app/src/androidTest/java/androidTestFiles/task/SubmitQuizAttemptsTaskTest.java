package androidTestFiles.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.SubmitQuizAttemptsTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import androidTestFiles.database.TestDataManager;
import androidTestFiles.utils.MockApiEndpoint;
import androidTestFiles.utils.parent.MockedApiEndpointTaskTest;


@RunWith(AndroidJUnit4.class)
public class SubmitQuizAttemptsTaskTest extends MockedApiEndpointTaskTest {

    private Context context;
    private DbHelper db;
    private List<QuizAttempt> quizAttempts;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        db = DbHelper.getInstance(context);
        TestDataManager testDataManager = new TestDataManager(db);
        testDataManager.addUsers();
        setUpQuizAttempts();
    }

    @After
    public void tearDown() throws Exception {
        if (mockServer!=null)
            mockServer.shutdown();
    }

    private void setUpQuizAttempts() throws UserNotFoundException {
        User testUser = db.getUser("user1");

        quizAttempts = new ArrayList<>();
        QuizAttempt quizAttempt1 = new QuizAttempt();
        quizAttempt1.setId(1);
        quizAttempt1.setUser(testUser);
        quizAttempt1.setUserId(1);
        quizAttempt1.setData("");
        quizAttempt1.setActivityDigest("TestActivityDigest");
        quizAttempt1.setCourseId(1L);
        quizAttempt1.setQuizTitle("Quiz1");
        quizAttempt1.setSent(false);
        quizAttempts.add(quizAttempt1);

    }

    private void submitQuizAttempts(List<QuizAttempt> quizAttempts, boolean expectedResult) {
        final CountDownLatch signal = new CountDownLatch(1);

        SubmitQuizAttemptsTask task = new SubmitQuizAttemptsTask(context, new MockApiEndpoint(mockServer));
        task.setResponseListener(result -> {
            assertEquals(expectedResult, result.isSuccess());
            signal.countDown();
        });

        task.execute(quizAttempts);

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean isQuizAttemptSent(){
        return db.getAllQuizAttempts().get(0).isSent();
    }

    @Test
    public void submitQuizAttempt_attemptSubmitted() {
        startServer(201, "{'badges':3}");

        db.insertQuizAttempts(quizAttempts);
        assertFalse(isQuizAttemptSent());

        submitQuizAttempts(quizAttempts, true);

        assertTrue(isQuizAttemptSent());
    }

    @Test
    public void submitQuizAttempt_noBadges_attemptNotSubmitted() {
        startServer(201, "");

        db.insertQuizAttempts(quizAttempts);
        assertFalse(isQuizAttemptSent());

        submitQuizAttempts(quizAttempts, false);

        assertFalse(isQuizAttemptSent());
    }

    @Test
    public void submitQuizAttempt_badRequest_attemptSubmitted() {
        startServer(400, "");

        db.insertQuizAttempts(quizAttempts);
        assertFalse(isQuizAttemptSent());

        submitQuizAttempts(quizAttempts, false);

        assertTrue(isQuizAttemptSent());
    }

    @Test
    public void submitQuizAttempt_serverError_attemptSubmitted() {
        startServer(500, "");

        db.insertQuizAttempts(quizAttempts);
        assertFalse(isQuizAttemptSent());

        submitQuizAttempts(quizAttempts, false);

        assertTrue(isQuizAttemptSent());
    }
}
