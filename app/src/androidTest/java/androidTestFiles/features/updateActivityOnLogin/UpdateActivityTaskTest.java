package androidTestFiles.features.updateActivityOnLogin;

import static junit.framework.Assert.assertEquals;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.listener.UpdateActivityListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.model.TrackerLog;
import org.digitalcampus.oppia.task.UpdateCourseActivityTask;
import org.digitalcampus.oppia.task.result.EntityResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import androidTestFiles.database.TestDBHelper;
import androidTestFiles.utils.FileUtils;
import androidTestFiles.utils.MockApiEndpoint;
import androidTestFiles.utils.parent.MockedApiEndpointTaskTest;

public class UpdateActivityTaskTest extends MockedApiEndpointTaskTest {

    private static final String VALID_ACTIVITY_TRACKERS = "responses/response_200_course_activity_trackers.xml";
    private static final String VALID_ACTIVITY_QUIZ_ATTEMPTS = "responses/response_200_course_activity_quiz_attempts.xml";

    private Context context;
    private TestDBHelper testDBHelper;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        testDBHelper = new TestDBHelper(context);
        testDBHelper.setUp();
    }

    @After
    public void tearDown() throws Exception {
        if (mockServer != null) mockServer.shutdown();
        testDBHelper.tearDown();
    }


    private void launchUpdateActivityTask(Course course) {
        final CountDownLatch signal = new CountDownLatch(1);  //Control AsyncTask sincronization for testing

        UpdateCourseActivityTask task = new UpdateCourseActivityTask(context, 1, new MockApiEndpoint(mockServer), false);
        task.setUpdateActivityListener(new UpdateActivityListener() {
            @Override
            public void updateActivityComplete(EntityResult<List<Course>> result) {

                signal.countDown();
            }

            @Override
            public void updateActivityProgressUpdate(DownloadProgress dp) {

            }

            @Override
            public void apiKeyInvalidated() {

                signal.countDown();
            }
        });

        task.execute(Arrays.asList(course));

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void checkTrackersUpdate() throws Exception {

        testDBHelper.getTestDataManager().addUsers();
        App.getPrefs(context).edit().putString(PrefsActivity.PREF_USER_NAME, "user1").apply();
        Course course = testDBHelper.getTestDataManager().addCourse(1, "mock-course");
        testDBHelper.getDbHelper().insertTracker(1, "000", null, null, true, "activity_completed", 10);
        testDBHelper.getDbHelper().insertTracker(1, "111", null, null, true, "activity_completed", 10, "2022-10-24 10:54:38");

        List<TrackerLog> trackersBefore = testDBHelper.getDbHelper().getAllTrackers(1);
        assertEquals(2, trackersBefore.size());
        assertEquals(10, trackersBefore.stream().filter(t -> t.getDigest().equals("000")).findFirst().get().getPoints());
        assertEquals(10, trackersBefore.stream().filter(t -> t.getDigest().equals("111")).findFirst().get().getPoints());

        startServer(200, FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), VALID_ACTIVITY_TRACKERS));

        launchUpdateActivityTask(course);

        List<TrackerLog> trackersAfter = testDBHelper.getDbHelper().getAllTrackers(1);

        assertEquals(3, trackersAfter.size());
        assertEquals(10, trackersAfter.stream().filter(t -> t.getDigest().equals("000")).findFirst().get().getPoints());
        assertEquals(20, trackersAfter.stream().filter(t -> t.getDigest().equals("111")).findFirst().get().getPoints());
        assertEquals(30, trackersAfter.stream().filter(t -> t.getDigest().equals("222")).findFirst().get().getPoints());
    }

    @Test
    public void checkQuizAttemptsUpdate() throws Exception {

        testDBHelper.getTestDataManager().addUsers();
        App.getPrefs(context).edit().putString(PrefsActivity.PREF_USER_NAME, "user1").commit();
        Course course = testDBHelper.getTestDataManager().addCourse(1, "mock-course");
        addMockQuizAttemps();

        List<QuizAttempt> qaBefore = testDBHelper.getDbHelper().getUserQuizAttempts(1);
        assertEquals(2, qaBefore.size());
        assertEquals(10, qaBefore.stream().filter(qa -> qa.getActivityDigest().equals("000")).findFirst().get().getPoints());
        assertEquals(10, qaBefore.stream().filter(qa -> qa.getActivityDigest().equals("111")).findFirst().get().getPoints());

        startServer(200, FileUtils.getStringFromFile(
                InstrumentationRegistry.getInstrumentation().getContext(), VALID_ACTIVITY_QUIZ_ATTEMPTS));

        launchUpdateActivityTask(course);

        List<QuizAttempt> qaAfter = testDBHelper.getDbHelper().getUserQuizAttempts(1);

        assertEquals(3, qaAfter.size());
        assertEquals(10, qaAfter.stream().filter(qa -> qa.getActivityDigest().equals("000")).findFirst().get().getPoints());
        assertEquals(20, qaAfter.stream().filter(qa -> qa.getActivityDigest().equals("111")).findFirst().get().getPoints());
        assertEquals(30, qaAfter.stream().filter(qa -> qa.getActivityDigest().equals("222")).findFirst().get().getPoints());
    }

    private void addMockQuizAttemps() {
        QuizAttempt quizAttempt1 = new QuizAttempt();
        quizAttempt1.setUserId(1);
        quizAttempt1.setActivityDigest("000");
        quizAttempt1.setDateTimeFromString("2022-10-27 00:00:00");
        quizAttempt1.setPoints(10);

        QuizAttempt quizAttempt2 = new QuizAttempt();
        quizAttempt2.setUserId(1);
        quizAttempt2.setActivityDigest("111");
        quizAttempt2.setDateTimeFromString("2022-10-27 14:24:21");
        quizAttempt2.setPoints(10);

        testDBHelper.getDbHelper().insertQuizAttempts(Arrays.asList(quizAttempt1, quizAttempt2));
    }
}
