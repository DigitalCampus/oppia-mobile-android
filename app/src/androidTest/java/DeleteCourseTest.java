import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.core.deps.guava.collect.ArrayListMultimap;
import android.support.test.runner.AndroidJUnit4;

import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.DeleteCourseListener;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.task.DeleteCourseTask;
import org.digitalcampus.oppia.task.InstallDownloadedCoursesTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import TestRules.DisableAnimationsRule;
import Utils.CourseUtils;
import Utils.FileUtils;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DeleteCourseTest {

    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();

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
    public void deleteCourse_success() throws Exception{

        String shortTitle = "correct_course";

        CourseUtils.cleanUp();

        installTestCourse();

        File modulesPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getTargetContext()));
        String[] children = modulesPath.list();
        assertTrue(children.length > 0);
        assertThat(CORRECT_COURSE, containsString(children[0]));  //Check that the course exists in the "modules" directory


        DbHelper db = DbHelper.getInstance(context);
        long userId = db.getUserId(SessionManager.getUsername(context));
        long courseId = db.getCourseID(shortTitle);
        Course c = db.getCourse(courseId, userId);
        assertNotNull(c);   //Check that the course exists in the database

        DeleteCourseTask task = new DeleteCourseTask(context);
        ArrayList<Object> payloadData = new ArrayList<>();
        payloadData.add(c);
        Payload p = new Payload(payloadData);
        task.setOnDeleteCourseListener(new DeleteCourseListener() {
            @Override
            public void onCourseDeletionComplete(Payload r) {
                response = r;
                signal.countDown();
            }
        });
        task.execute(p);

        signal.await();

        assertTrue(response.isResult());

        c = db.getCourse(courseId, userId);
        assertNull(c);   //Check that the course does not exists in the database

        assertTrue(modulesPath.list().length == 0);    //Check that the course does not exists in the "downloads" directory

    }

    @Test
    public void deleteCourse_nonExistingCourse() throws Exception{
        String shortTitle = "correct_course";

        CourseUtils.cleanUp();

        File finalPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getTargetContext()), shortTitle);
        assertFalse(finalPath.exists());  //Check that the course does not exists in the "modules" directory


        DbHelper db = DbHelper.getInstance(context);
        long userId = db.getUserId(SessionManager.getUsername(context));
        long courseId = db.getCourseID(shortTitle);
        Course c = db.getCourse(courseId, userId);
        assertNull(c);   //Check that the course does not exists in the database

        DeleteCourseTask task = new DeleteCourseTask(context);
        ArrayList<Object> payloadData = new ArrayList<>();
        payloadData.add(c);
        Payload p = new Payload(payloadData);
        task.setOnDeleteCourseListener(new DeleteCourseListener() {
            @Override
            public void onCourseDeletionComplete(Payload r) {
                response = r;
                signal.countDown();
            }
        });
        task.execute(p);

        signal.await();

        c = db.getCourse(courseId, userId);
        assertFalse(finalPath.exists());    //Check that the course does not exists in the "modules" directory
        assertNull(c);   //Check that the course does not exists in the database
    }

    @Test
    public void deleteCourse_courseAlreadyOnDatabase() throws Exception {
        //Install a course that is already in the database but not in the storage system
        CourseUtils.cleanUp();

        installTestCourse();

        String shortTitle = "correct_course";
        File finalPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getTargetContext()), shortTitle);
        finalPath.delete();     //Remove course folder

        DbHelper db = DbHelper.getInstance(context);
        long userId = db.getUserId(SessionManager.getUsername(context));
        long courseId = db.getCourseID(shortTitle);
        Course c = db.getCourse(courseId, userId);
        assertNotNull(c);   //Check that the course exists in the database

        DeleteCourseTask task = new DeleteCourseTask(context);
        ArrayList<Object> payloadData = new ArrayList<>();
        payloadData.add(c);
        Payload p = new Payload(payloadData);
        task.setOnDeleteCourseListener(new DeleteCourseListener() {
            @Override
            public void onCourseDeletionComplete(Payload r) {
                response = r;
                signal.countDown();
            }
        });
        task.execute(p);

        signal.await();

        c = db.getCourse(courseId, userId);
        assertFalse(finalPath.exists());    //Check that the course does not exists in the "modules" directory
        assertNull(c);   //Check that the course does not exists in the database


    }

    private void installTestCourse(){
        //Proceed with the installation of the course
        try {
            String filename = CORRECT_COURSE;

            CourseUtils.cleanUp();

            FileUtils.copyZipFromAssets(context, filename);  //Copy course zip from assets to download path

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

            signal.await();

            signal = new CountDownLatch(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

}
