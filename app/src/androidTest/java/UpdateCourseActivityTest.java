import android.app.ProgressDialog;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.UpdateActivityListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.task.InstallDownloadedCoursesTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.UpdateCourseActivityTask;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import Utils.CourseUtils;
import Utils.FileUtils;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/*@RunWith(AndroidJUnit4.class)
public class UpdateCourseActivityTest {
    private final String CORRECT_COURSE = "Correct_Course.zip";

    private Context context;
    private CountDownLatch signal;
    private Payload response;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getTargetContext();
        signal = new CountDownLatch(1);
    }

    @After
    public void tearDown() throws Exception {
        signal.countDown();
    }

    @Test
    public void updateActivity_UpdateSuccessful()throws Exception{
        String filename = CORRECT_COURSE;

        CourseUtils.cleanUp();

        FileUtils.copyZipFromAssets(filename);  //Copy course zip from assets to download path


        ArrayList<Object> data = new ArrayList<>();
        Payload payload = new Payload(data);
        InstallDownloadedCoursesTask imTask = new InstallDownloadedCoursesTask(context);
        imTask.execute(payload);


        DbHelper db = DbHelper.getInstance(context);
        Course tempCourse = db.getAllCourses().get(0);
        long userId = db.getUserId(SessionManager.getUsername(context));

        runUpdateCourseActivityTask(userId, tempCourse);//Run test task

        signal.await();

        //Check if result is true
        assertTrue(response.isResult());



    }

    private void runUpdateCourseActivityTask(long userId, Course tempCourse){

        UpdateCourseActivityTask task = new UpdateCourseActivityTask(context, userId);
        ArrayList<Object> payloadData = new ArrayList<>();
        payloadData.add(tempCourse);
        Payload p = new Payload(payloadData);
        task.setUpdateActivityListener(new UpdateActivityListener() {
            @Override
            public void apiKeyInvalidated() {  }

            @Override
            public void updateActivityComplete(Payload p) {
                response = p;
                signal.countDown();
            }

            @Override
            public void updateActivityProgressUpdate(DownloadProgress dp) {

            }
        });
        task.execute(p);

    }
}
*/