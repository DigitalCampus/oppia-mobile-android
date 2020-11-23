import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.util.Log;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.task.InstallDownloadedCoursesTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.storage.ExternalStorageStrategy;
import org.digitalcampus.oppia.utils.storage.InternalStorageStrategy;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import Utils.CourseUtils;
import Utils.FileUtils;
import database.BaseTestDB;

import androidx.test.platform.app.InstrumentationRegistry;

import static Utils.CourseUtils.runInstallCourseTask;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
@RunWith(Parameterized.class)
public class InstallDownloadedCoursesTest extends BaseTestDB {
    public static final String TAG = InstallDownloadedCoursesTest.class.getSimpleName();


    private final String CORRECT_COURSE = "Correct_Course.zip";
    private final String EXISTING_COURSE = "Existing_Course.zip";
    private final String ALREADY_INSTALLED_COURSE = "";
    private final String INCORRECT_COURSE = "Incorrect_Course.zip";
    private final String NOXML_COURSE = "NoXML_Course.zip";
    private final String MALFORMEDXML_COURSE = "MalformedXML_Course.zip";
    private final String INSECURE_COURSE = "Insecure_Course.zip";

    private Context context;
    private SharedPreferences prefs;
    private Payload response;
    private StorageAccessStrategy storageStrategy;

    public InstallDownloadedCoursesTest(StorageAccessStrategy storageStrategy) {
        this.storageStrategy = storageStrategy;
    }

    @Parameterized.Parameters
    public static StorageAccessStrategy[] storageStrategies() {
        return new StorageAccessStrategy[]{ new InternalStorageStrategy(), new ExternalStorageStrategy()};
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
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
    public void installCourse_correctCourse()throws Exception{

        CourseUtils.cleanUp();
        copyCourseFromAssets(CORRECT_COURSE);
        response = runInstallCourseTask(context);//Run test task

        //Check if result is true
        assertTrue(response.isResult());

        File modulesPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        assertTrue(modulesPath.exists());
        String[] children = modulesPath.list();
        assertEquals(1, children.length);  //Check that the course exists in the "modules" directory

        File downloadPath = new File(Storage.getDownloadPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        assertEquals(0, downloadPath.list().length);    //Check that the course does not exists in the "downloads" directory

        String shortName = children.length != 0 ? children[0].toLowerCase(Locale.US) : "";
        Course c = CourseUtils.getCourseFromDatabase(context, shortName);
        assertNotNull(c);   //Check that the course exists in the database

        String title = c.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));

    }

    @Test
    public void installCourse_existingCourse()throws Exception{

        CourseUtils.cleanUp();

        copyCourseFromAssets(EXISTING_COURSE);
        response = runInstallCourseTask(context);
        copyCourseFromAssets(EXISTING_COURSE);
        response = runInstallCourseTask(context);

        //Check that it failed
        assertFalse(response.isResult());

        File modulesPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        assertTrue(modulesPath.exists());
        String[] children = modulesPath.list();
        assertEquals(1, children.length);  //Check that the course exists in the "modules" directory

        File downloadPath = new File(Storage.getDownloadPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        assertEquals(0, downloadPath.list().length);    //Check that the course does not exists in the "downloads" directory

        String shortName = children.length != 0 ? children[0].toLowerCase(Locale.US) : "";
        Course c = CourseUtils.getCourseFromDatabase(context, shortName);
        assertNotNull(c);   //Check that the course exists in the database

        String title = c.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));

        //Check if the resultResponse is correct
        assertEquals(context.getString(R.string.error_latest_already_installed, title), response.getResultResponse());

    }

    @Test
    public void installCourse_noXMLCourse()throws Exception{

        CourseUtils.cleanUp();
        copyCourseFromAssets(NOXML_COURSE);
        response = runInstallCourseTask(context);

        //Check if result is true
        assertFalse(response.isResult());
        //Check if the resultResponse is correct
        assertEquals(context.getString(R.string.error_installing_course, NOXML_COURSE), response.getResultResponse());

        File downloadPath = new File(Storage.getDownloadPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        assertEquals(0, downloadPath.list().length); //Check that the course does not exists in the "downloads" directory

        File modulesPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        assertTrue(modulesPath.exists());
        String[] children = modulesPath.list();
        assertEquals(0, children.length);    //Check that the course does not exists in the "modules" directory

        String shortName = children.length != 0 ? children[0].toLowerCase(Locale.US) : "";
        long courseId = getDbHelper().getCourseID(shortName);
        long userId = getDbHelper().getUserId(SessionManager.getUsername(context));
        Course c = getDbHelper().getCourse(courseId, userId);
        assertNull(c);   //Check that the course exists in the database

    }

    @Test
    public void installCourse_insecureCourse()throws Exception {

        CourseUtils.cleanUp();
        copyCourseFromAssets(INSECURE_COURSE);
        response = runInstallCourseTask(context);//Run test task

        assertFalse(response.isResult());
    }

  /*  @Test
    public void installCourse_malformedXMLCourse()throws Exception{
        String filename = MALFORMEDXML_COURSE;

        CourseUtils.cleanUp();

        FileUtils.copyZipFromAssets(context, filename);  //Copy course zip from assets to download path

        runInstallCourseTask();//Run test task

        signal.await();

        //Check if result is true
        assertFalse(response.isResult());
        //Check if the resultResponse is correct
        assertEquals(context.getString(R.string.error_installing_course, filename), response.getResultResponse());
        File initialPath = new File(Storage.getDownloadPath(InstrumentationRegistry.getInstrumentation().getTargetContext()), filename);
        assertFalse(initialPath.exists());  //Check that the course does not exists in the "downloads" directory


        String shortTitle = "malformedxml_course";
        File finalPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getInstrumentation().getTargetContext()), shortTitle);
        assertFalse(finalPath.exists()); //Check that the course exists in the "modules" directory

        DbHelper db = DbHelper.getInstance(context);
        long courseId = getDbHelper().getCourseID(shortTitle);
        long userId = getDbHelper().getUserId(SessionManager.getUsername(context));
        Course c = getDbHelper().getCourse(courseId, userId);
        assertNull(c);   //Check that the course exists in the database

    }*/

    @Test
    public void installCourse_errorInstallingCourse()throws Exception{

        CourseUtils.cleanUp();
        copyCourseFromAssets(INCORRECT_COURSE);
        response = runInstallCourseTask(context);

        //Check if result is false
        assertFalse(response.isResult());
        //Check if the resultResponse is correct
        assertEquals(context.getString(R.string.error_installing_course, INCORRECT_COURSE), response.getResultResponse());

        File downloadPath = new File(Storage.getDownloadPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        assertEquals(0, downloadPath.list().length); //Check that the course does not exists in the "downloads" directory

        File modulesPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        assertTrue(modulesPath.exists());
        String[] children = modulesPath.list();
        assertEquals(0, children.length);    //Check that the course does not exists in the "modules" directory

        String shortName = children.length != 0 ? children[0].toLowerCase(Locale.US) : "";

        long courseId = getDbHelper().getCourseID(shortName);
        long userId = getDbHelper().getUserId(SessionManager.getUsername(context));
        Course c = getDbHelper().getCourse(courseId, userId);
        assertNull(c);   //Check that the course does not exists in the database

    }

   /* @Test
    public void installCourse_incorrectCourses()throws Exception{

        CourseUtils.cleanUp();

        String[] filenames = new String[] {
                INCORRECT_COURSE,
                NOXML_COURSE,
                MALFORMEDXML_COURSE
        };


        for(String filename : filenames) {
            FileUtils.copyZipFromAssets(context, filename);  //Copy course zip from assets to download path
        }

        runInstallCourseTask();     //Run test task

        signal.await();

        File initialPath = new File(Storage.getDownloadPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        String[] children = initialPath.list();
        assertEquals(0, children.length);


        File finalPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        children = finalPath.list();
        assertEquals(0, children.length);

    }*/

    @Test
    public void installCourse_courseAlreadyInStorage()throws Exception {
        //Install a course that is already in storage system but not in database
        CourseUtils.cleanUp();
        copyCourseFromAssets(CORRECT_COURSE);
        response = runInstallCourseTask(context);

        File modulesPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        assertTrue(modulesPath.exists());
        String[] children = modulesPath.list();
        assertEquals(1, children.length);    //Check that the course exists in the "modules" directory

        String shortName = children.length != 0 ? children[0].toLowerCase(Locale.US) : "";

        long courseId = getDbHelper().getCourseID(shortName);
        long userId = getDbHelper().getUserId(SessionManager.getUsername(context));
        Course c = getDbHelper().getCourse(courseId, userId);
        assertNotNull(c);                   //Check that the course exists in the database
        getDbHelper().deleteCourse((int) courseId);    //Delete course from database
        c = getDbHelper().getCourse(courseId, userId);
        assertNull(c);                      //Check that the course does not exists in the database

        installCourse_correctCourse();
    }

    private void copyCourseFromAssets(String filename){
        FileUtils.copyZipFromAssets(context, filename);  //Copy course zip from assets to download path
    }

}
