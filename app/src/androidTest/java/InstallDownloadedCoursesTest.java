import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.task.InstallDownloadedCoursesTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import Utils.CourseUtils;
import Utils.FileUtils;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class InstallDownloadedCoursesTest {
    private final String CORRECT_COURSE = "Correct_Course.zip";
    private final String ALREADY_INSTALLED_COURSE = "";
    private final String INCORRECT_COURSE = "Incorrect_Course.zip";
    private final String NOXML_COURSE = "NoXML_Course.zip";

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
    public void installCourse_correctCourse()throws Exception{
        String filename = CORRECT_COURSE;

        FileUtils.copyZipFromAssets(filename);  //Copy course zip from assets to download path

        String title = CourseUtils.getCourseTitle(context);
        String shortTitle = CourseUtils.getCourseShortTitle(context);

        runInstallCourseTask();//Run test task

        signal.await();

        assertTrue(response.isResult());
        assertEquals(response.getResultResponse(), context.getString(R.string.install_course_complete, title));
        File initialPath = new File(Storage.getDownloadPath(InstrumentationRegistry.getTargetContext()), filename);
        assertFalse(initialPath.exists());



        File finalPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getTargetContext()), shortTitle);
        assertTrue(finalPath.exists());
    }

    @Test
    public void installCourse_existingCourse()throws Exception{
        String filename = CORRECT_COURSE;

        String title = "";
        String shortTitle = "";
        for(int i = 0; i < 2; i++){
            FileUtils.copyZipFromAssets(filename); //Copy course zip from assets to download path
            title = CourseUtils.getCourseTitle(context);
            shortTitle = CourseUtils.getCourseShortTitle(context);
            runInstallCourseTask(); //Run test task
        }

        signal.await();

        assertFalse(response.isResult());
        assertEquals(response.getResultResponse(), context.getString(R.string.error_latest_already_installed, title));

        File initialPath = new File(Storage.getDownloadPath(InstrumentationRegistry.getTargetContext()), filename);
        assertFalse(initialPath.exists());



        File finalPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getTargetContext()), shortTitle);
        assertTrue(finalPath.exists());
    }

    @Test
    public void installCourse_errorInstallingCourse()throws Exception{
        String filename = INCORRECT_COURSE;

        FileUtils.copyZipFromAssets(filename);  //Copy course zip from assets to download path

        String title = CourseUtils.getCourseTitle(context);
        String shortTitle = CourseUtils.getCourseShortTitle(context);

        runInstallCourseTask();     //Run test task
        signal.await();

        assertFalse(response.isResult());
        assertEquals(response.getResultResponse(), context.getString(R.string.error_installing_course, title));

        File initialPath = new File(Storage.getDownloadPath(InstrumentationRegistry.getTargetContext()), filename);
        assertFalse(initialPath.exists());


        File finalPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getTargetContext()), shortTitle);
        assertFalse(finalPath.exists());

    }

    @Test
    public void installCourse_incorrectCourse()throws Exception{
        String filename = NOXML_COURSE;

        FileUtils.copyZipFromAssets(filename);  //Copy course zip from assets to download path

        String title = CourseUtils.getCourseTitle(context);
        String shortTitle = CourseUtils.getCourseShortTitle(context);

        runInstallCourseTask();     //Run test task
        signal.await();

        assertFalse(response.isResult());
        assertEquals(response.getResultResponse(), null);

        File initialPath = new File(Storage.getDownloadPath(InstrumentationRegistry.getTargetContext()), filename);
        assertTrue(initialPath.exists());


        File finalPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getTargetContext()), shortTitle);
        assertFalse(finalPath.exists());

    }


    private void runInstallCourseTask(){
        ArrayList<Object> data = new ArrayList<>();
        Payload payload = new Payload(data);
        InstallDownloadedCoursesTask imTask = new InstallDownloadedCoursesTask(context);
        imTask.setInstallerListener(new InstallCourseListener() {
            @Override
            public void downloadComplete(Payload p) {  }

            @Override
            public void downloadProgressUpdate(DownloadProgress dp) {  }

            @Override
            public void installComplete(Payload r) {
                response = r;
                signal.countDown();
            }

            @Override
            public void installProgressUpdate(DownloadProgress dp) {  }
        });
        imTask.execute(payload);
    }

}
