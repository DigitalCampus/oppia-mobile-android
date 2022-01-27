package androidTestFiles;

import android.Manifest;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.model.TrackerLog;
import org.digitalcampus.oppia.task.ExportActivityTask;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import androidTestFiles.database.BaseTestDB;
import androidx.test.rule.GrantPermissionRule;

@RunWith(AndroidJUnit4.class)
public class ExportActivityTaskTest extends BaseTestDB {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    private static final int NUM_QUIZ_ATTEMPTS_TEST = 2;
    private static final int NUM_TRACKER_TEST = 3;

    @After
    public void cleanActivityFiles() throws Exception {

        cleanDir(Storage.getActivityPath(getContext()));
        cleanDir(Storage.getActivityFullExportPath(getContext()));

    }

    private void cleanDir(String path) {
        File activityPath = new File(path);
        if (activityPath.exists() && activityPath.isDirectory()) {
            for (File file : activityPath.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
    }


    @Test
    public void exportUnexportedActivity() throws Exception {

        final CountDownLatch signal = new CountDownLatch(1);

        getTestDataManager().addQuizAttempts(NUM_QUIZ_ATTEMPTS_TEST);
        getTestDataManager().addTrackers(NUM_TRACKER_TEST);

        List<QuizAttempt> quizAttemptsBefore = getDbHelper().getUnexportedQuizAttempts(1);
        assertEquals(NUM_QUIZ_ATTEMPTS_TEST, quizAttemptsBefore.size());

        List<TrackerLog> trackersBefore = getDbHelper().getUnexportedTrackers(1);
        assertEquals(NUM_TRACKER_TEST, trackersBefore.size());

        final String[] exportedFilename = new String[1];

        ExportActivityTask task = new ExportActivityTask(getContext());
        task.setListener(result -> {
            assertTrue(result.isSuccess());
            exportedFilename[0] = result.getResultMessage();
            signal.countDown();
        });
        task.execute(ExportActivityTask.UNEXPORTED_ACTIVITY);

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        checkJsonFile(Storage.getActivityPath(getContext()), exportedFilename[0]
                , NUM_QUIZ_ATTEMPTS_TEST, NUM_TRACKER_TEST);

        List<QuizAttempt> quizAttemptsAfter = getDbHelper().getUnexportedQuizAttempts(1);
        assertEquals(0, quizAttemptsAfter.size());

        List<TrackerLog> trackersAfter = getDbHelper().getUnexportedTrackers(1);
        assertEquals(0, trackersAfter.size());

    }

    @Test
    public void exportUnexportedActivityWithNullQuizAttemptData() throws Exception {

        final CountDownLatch signal = new CountDownLatch(1);

        getTestDataManager().addQuizAttemptsWithNullData(NUM_QUIZ_ATTEMPTS_TEST);
        getTestDataManager().addTrackers(NUM_TRACKER_TEST);

        List<QuizAttempt> quizAttemptsBefore = getDbHelper().getUnexportedQuizAttempts(1);
        assertEquals(NUM_QUIZ_ATTEMPTS_TEST, quizAttemptsBefore.size());

        List<TrackerLog> trackersBefore = getDbHelper().getUnexportedTrackers(1);
        assertEquals(NUM_TRACKER_TEST, trackersBefore.size());

        final String[] exportedFilename = new String[1];

        ExportActivityTask task = new ExportActivityTask(getContext());
        task.setListener(result -> {
            assertTrue(result.isSuccess());
            exportedFilename[0] = result.getResultMessage();
            signal.countDown();
        });
        task.execute(ExportActivityTask.UNEXPORTED_ACTIVITY);

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        checkJsonFile(Storage.getActivityPath(getContext()), exportedFilename[0]
                , 0, NUM_TRACKER_TEST);

        List<QuizAttempt> quizAttemptsAfter = getDbHelper().getUnexportedQuizAttempts(1);
        assertEquals(0, quizAttemptsAfter.size());

        List<TrackerLog> trackersAfter = getDbHelper().getUnexportedTrackers(1);
        assertEquals(0, trackersAfter.size());

    }

    @Test
    public void exportFullActivityUnexported() throws Exception {

        final CountDownLatch signal = new CountDownLatch(1);

        getTestDataManager().addQuizAttempts(NUM_QUIZ_ATTEMPTS_TEST);
        getTestDataManager().addTrackers(NUM_TRACKER_TEST);

        List<QuizAttempt> quizAttemptsBefore = getDbHelper().getUnexportedQuizAttempts(1);
        assertEquals(NUM_QUIZ_ATTEMPTS_TEST, quizAttemptsBefore.size());

        List<TrackerLog> trackersBefore = getDbHelper().getUnexportedTrackers(1);
        assertEquals(NUM_TRACKER_TEST, trackersBefore.size());

        final String[] exportedFilename = new String[1];

        ExportActivityTask task = new ExportActivityTask(getContext());
        task.setListener(result -> {
            assertTrue(result.isSuccess());
            exportedFilename[0] = result.getResultMessage();
            signal.countDown();
        });
        task.execute(ExportActivityTask.FULL_EXPORT_ACTIVTY);

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        checkJsonFile(Storage.getActivityFullExportPath(getContext()), exportedFilename[0]
                , NUM_QUIZ_ATTEMPTS_TEST, NUM_TRACKER_TEST);

        List<QuizAttempt> quizAttemptsAfter = getDbHelper().getUnexportedQuizAttempts(1);
        assertEquals(NUM_QUIZ_ATTEMPTS_TEST, quizAttemptsAfter.size());

        List<TrackerLog> trackersAfter = getDbHelper().getUnexportedTrackers(1);
        assertEquals(NUM_TRACKER_TEST, trackersAfter.size());

    }

    @Test
    public void exportFullActivityAlreadyExported() throws Exception {

        final CountDownLatch signal = new CountDownLatch(1);

        getTestDataManager().addQuizAttempts(NUM_QUIZ_ATTEMPTS_TEST);
        getTestDataManager().addTrackers(NUM_TRACKER_TEST);

        List<QuizAttempt> quizAttemptsBefore = getDbHelper().getUnexportedQuizAttempts(1);
        assertEquals(NUM_QUIZ_ATTEMPTS_TEST, quizAttemptsBefore.size());

        List<TrackerLog> trackersBefore = getDbHelper().getUnexportedTrackers(1);
        assertEquals(NUM_TRACKER_TEST, trackersBefore.size());

        getDbHelper().markLogsAndQuizzesExported();
        getDbHelper().markLogsAndQuizzesSubmitted();

        List<QuizAttempt> quizAttemptsBefore2 = getDbHelper().getUnexportedQuizAttempts(1);
        assertEquals(0, quizAttemptsBefore2.size());

        List<TrackerLog> trackersBefore2 = getDbHelper().getUnexportedTrackers(1);
        assertEquals(0, trackersBefore2.size());

        final String[] exportedFilename = new String[1];

        ExportActivityTask task = new ExportActivityTask(getContext());
        task.setListener(result -> {
            assertTrue(result.isSuccess());
            exportedFilename[0] = result.getResultMessage();
            signal.countDown();
        });
        task.execute(ExportActivityTask.FULL_EXPORT_ACTIVTY);

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        checkJsonFile(Storage.getActivityFullExportPath(getContext()), exportedFilename[0]
                , NUM_QUIZ_ATTEMPTS_TEST, NUM_TRACKER_TEST);

        List<QuizAttempt> quizAttemptsAfter = getDbHelper().getUnexportedQuizAttempts(1);
        assertEquals(0, quizAttemptsAfter.size());

        List<TrackerLog> trackersAfter = getDbHelper().getUnexportedTrackers(1);
        assertEquals(0, trackersAfter.size());

    }

    @Test
    public void exportFullActivityWithNullQuizAttemptData() throws Exception {

        final CountDownLatch signal = new CountDownLatch(1);

        getTestDataManager().addQuizAttemptsWithNullData(NUM_QUIZ_ATTEMPTS_TEST);
        getTestDataManager().addTrackers(NUM_TRACKER_TEST);

        List<QuizAttempt> quizAttemptsBefore = getDbHelper().getUnexportedQuizAttempts(1);
        assertEquals(NUM_QUIZ_ATTEMPTS_TEST, quizAttemptsBefore.size());

        List<TrackerLog> trackersBefore = getDbHelper().getUnexportedTrackers(1);
        assertEquals(NUM_TRACKER_TEST, trackersBefore.size());

        final String[] exportedFilename = new String[1];

        ExportActivityTask task = new ExportActivityTask(getContext());
        task.setListener(result -> {
            assertTrue(result.isSuccess());
            exportedFilename[0] = result.getResultMessage();
            signal.countDown();
        });
        task.execute(ExportActivityTask.FULL_EXPORT_ACTIVTY);

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        checkJsonFile(Storage.getActivityFullExportPath(getContext()), exportedFilename[0]
                , 0, NUM_TRACKER_TEST);

        List<QuizAttempt> quizAttemptsAfter = getDbHelper().getUnexportedQuizAttempts(1);
        assertEquals(NUM_QUIZ_ATTEMPTS_TEST, quizAttemptsAfter.size());

        List<TrackerLog> trackersAfter = getDbHelper().getUnexportedTrackers(1);
        assertEquals(NUM_TRACKER_TEST, trackersAfter.size());

    }

    private void checkJsonFile(String path, String exportedFilename, int numQuizAttempts, int numTrackers) throws Exception {

        File activityPath = new File(path);
        File exportedActivityFile = new File(activityPath, exportedFilename);
        assertTrue(exportedActivityFile.exists());
        String contentJson = FileUtils.readFile(exportedActivityFile);
        JSONObject jsonObject = new JSONObject(contentJson);
        JSONObject jsonUser1 = jsonObject.getJSONArray("users").getJSONObject(0);
        assertEquals(numQuizAttempts, jsonUser1.getJSONArray("quizresponses").length());
        assertEquals(numTrackers, jsonUser1.getJSONArray("trackers").length());
    }

}
