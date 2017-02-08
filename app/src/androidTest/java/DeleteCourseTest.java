import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.core.deps.guava.collect.ArrayListMultimap;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.listener.DeleteCourseListener;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.task.DeleteCourseTask;
import org.digitalcampus.oppia.task.InstallDownloadedCoursesTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.storage.ExternalStorageStrategy;
import org.digitalcampus.oppia.utils.storage.InternalStorageStrategy;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategy;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import TestRules.DisableAnimationsRule;
import Utils.CourseUtils;
import Utils.FileUtils;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNotEquals;

@RunWith(Parameterized.class)
public class DeleteCourseTest {
    public static final String TAG = DeleteCourseTest.class.getSimpleName();
    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();

    private final String CORRECT_COURSE = "Correct_Course.zip";

    private Context context;
    private SharedPreferences prefs;
    private Payload response;
    private StorageAccessStrategy storageStrategy;

    public DeleteCourseTest(StorageAccessStrategy storageStrategy) {
        this.storageStrategy = storageStrategy;
    }

    @Parameterized.Parameters
    public static StorageAccessStrategy[] storageStrategies() {
        return new StorageAccessStrategy[]{ new InternalStorageStrategy(), new ExternalStorageStrategy()};
    }

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getTargetContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        setStorageStrategy();
    }

    //Run test once for every StorageStrategy (Internal, External)
    public void setStorageStrategy(){

        Log.v(TAG, "Using Strategy: " + storageStrategy.getStorageType());
        Storage.setStorageStrategy(storageStrategy);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PrefsActivity.PREF_STORAGE_OPTION, storageStrategy.getStorageType());
        storageStrategy.updateStorageLocation(context);
        editor.commit();
    }

    @Test
    public void deleteCourse_success() throws Exception{
        CourseUtils.cleanUp();

        installTestCourse();

        File modulesPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getTargetContext()));
        assertTrue(modulesPath.exists());
        String[] children = modulesPath.list();
        assertEquals(1, children.length);  //Check that the course exists in the "modules" directory


        DbHelper db = DbHelper.getInstance(context);
        long userId = db.getUserId(SessionManager.getUsername(context));
        String shortName = children.length != 0 ? children[0].toLowerCase(Locale.US) : "";
        long courseId = db.getCourseID(shortName);
        Course c = db.getCourse(courseId, userId);
        assertNotNull(c);   //Check that the course exists in the database

        deleteTestCourse(c);

        assertTrue(response.isResult());

        c = db.getCourse(courseId, userId);
        assertNull(c);   //Check that the course does not exists in the database

        assertEquals(0, modulesPath.list().length);    //Check that the course does not exists in the "modules" directory

    }

    @Test
    public void deleteCourse_nonExistingCourse() throws Exception{

        CourseUtils.cleanUp();

        File modulesPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getTargetContext()));
        assertTrue(modulesPath.exists());
        String[] children = modulesPath.list();
        assertEquals(0, children.length); //Check that the course does not exists in the "modules" directory

        DbHelper db = DbHelper.getInstance(context);
        long userId = db.getUserId(SessionManager.getUsername(context));
        String shortName = children.length != 0 ? children[0].toLowerCase(Locale.US) : "";
        long courseId = db.getCourseID(shortName);
        Course c = db.getCourse(courseId, userId);
        assertNull(c);   //Check that the course does not exists in the database

        deleteTestCourse(c);

        c = db.getCourse(courseId, userId);
        assertNull(c);   //Check that the course does not exists in the database

        assertEquals(0, modulesPath.list().length);    //Check that the course does not exists in the "modules" directory
    }

    @Test
    //Install a course that is already in the database but not in the storage system
    public void deleteCourse_courseAlreadyOnDatabase() throws Exception {
        CourseUtils.cleanUp();

        installTestCourse();

        File modulesPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getTargetContext()));
        assertTrue(modulesPath.exists());
        String[] children = modulesPath.list();
        assertEquals(1, children.length);  //Check that the course exists in the "modules" directory
        File finalPath = new File(modulesPath, children[0]);
        org.digitalcampus.oppia.utils.storage.FileUtils.deleteDir(finalPath);  //Remove course folder
        assertFalse(finalPath.exists());


        DbHelper db = DbHelper.getInstance(context);
        long userId = db.getUserId(SessionManager.getUsername(context));
        String shortName = children.length != 0 ? children[0].toLowerCase(Locale.US) : "";
        long courseId = db.getCourseID(shortName);
        Course c = db.getCourse(courseId, userId);
        assertNotNull(c);   //Check that the course exists in the database

        deleteTestCourse(c);

        c = db.getCourse(courseId, userId);
        assertNull(c);   //Check that the course does not exists in the database


        assertEquals(0, modulesPath.list().length);    //Check that the course does not exists in the "modules" directory


    }

    private void installTestCourse(){
        //Proceed with the installation of the course

        final CountDownLatch signal = new CountDownLatch(1);  //Control AsyncTask sincronization for testing

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

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private void deleteTestCourse(Course course){

        final CountDownLatch signal = new CountDownLatch(1);  //Control AsyncTask sincronization for testing

        DeleteCourseTask task = new DeleteCourseTask(context);
        ArrayList<Object> payloadData = new ArrayList<>();
        payloadData.add(course);
        Payload p = new Payload(payloadData);
        task.setOnDeleteCourseListener(new DeleteCourseListener() {
            @Override
            public void onCourseDeletionComplete(Payload r) {
                response = r;
                signal.countDown();
            }
        });
        task.execute(p);

        try {
            signal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
