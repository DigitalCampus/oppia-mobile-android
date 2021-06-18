package androidTestFiles;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.listener.UpdateActivityListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.task.InstallDownloadedCoursesTask;
import org.digitalcampus.oppia.task.UpdateCourseActivityTask;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.task.result.EntityResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import androidTestFiles.Utils.CourseUtils;
import androidTestFiles.Utils.FileUtils;

import static org.junit.Assert.assertTrue;

/**
@RunWith(AndroidJUnit4.class)
public class UpdateCourseActivityTest {
    private final String CORRECT_COURSE = "Correct_Course.zip";
    private Context context;
    private CountDownLatch signal;
    private BasicResult response;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
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

        InstallDownloadedCoursesTask imTask = new InstallDownloadedCoursesTask(context);
        imTask.execute();


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
        task.setUpdateActivityListener(new UpdateActivityListener() {
            @Override
            public void apiKeyInvalidated() {  }

            @Override
            public void updateActivityComplete(EntityResult<Course> result) {
                response = result;
                signal.countDown();
            }

            @Override
            public void updateActivityProgressUpdate(DownloadProgress dp) {

            }
        });
        task.execute(tempCourse);

    }
}
*/