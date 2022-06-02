package androidTestFiles;

import android.Manifest;
import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategy;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.List;
import java.util.Locale;

import androidTestFiles.Utils.CourseUtils;
import androidTestFiles.Utils.FileUtils;
import androidTestFiles.database.BaseTestDB;
import androidx.test.rule.GrantPermissionRule;

import static androidTestFiles.Utils.CourseUtils.runInstallCourseTask;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class InstallDownloadedCoursesTest extends BaseTestDB {
    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);

    public static final String TAG = InstallDownloadedCoursesTest.class.getSimpleName();

    private final String CORRECT_COURSE = "Correct_Course.zip";
    private final String EXISTING_COURSE = "Existing_Course.zip";
    private final String UPDATED_COURSE = "Updated_Course.zip";
    private final String INCORRECT_COURSE = "Incorrect_Course.zip";
    private final String NOXML_COURSE = "NoXML_Course.zip";
    private final String MALFORMEDXML_COURSE = "MalformedXML_Course.zip";
    private final String INSECURE_COURSE = "Insecure_Course.zip";

    private Context context;
    private BasicResult response;
    private StorageAccessStrategy storageStrategy;

    public InstallDownloadedCoursesTest(StorageAccessStrategy storageStrategy) {
        this.storageStrategy = storageStrategy;
    }

    @Parameterized.Parameters
    public static StorageAccessStrategy[] storageStrategies() {
        return FileUtils.getStorageStrategiesBasedOnDeviceAvailableStorage();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        setStorageStrategy();

        CourseUtils.cleanUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        CourseUtils.cleanUp();
    }

    //Run test once for every StorageStrategy (Internal, External)
    public void setStorageStrategy(){

        Log.v(TAG, "Using Strategy: " + storageStrategy.getStorageType());
        Storage.setStorageStrategy(storageStrategy);

        when(prefs.getString(eq(PrefsActivity.PREF_STORAGE_OPTION), anyString())).thenReturn(storageStrategy.getStorageType());

    }

    @Test
    public void installCourse_correctCourse()throws Exception{

        CourseUtils.cleanUp();
        copyCourseFromAssets(CORRECT_COURSE);
        response = runInstallCourseTask(context);//Run test task

        //Check if result is true
        assertTrue(response.isSuccess());

        File modulesPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        assertTrue(modulesPath.exists());
        String[] children = modulesPath.list();
        assertEquals(1, children.length);  //Check that the course exists in the "modules" directory

        File downloadPath = new File(Storage.getDownloadPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        assertEquals(0, downloadPath.list().length);    //Check that the course does not exists in the "downloads" directory

        String shortName = children.length != 0 ? children[0].toLowerCase(Locale.US) : "";
        Course c = CourseUtils.getCourseFromDatabase(context, shortName);
        assertNotNull(c);   //Check that the course exists in the database

        List<Media> media = getDbHelper().getCourseMedia(c.getCourseId());
        assertEquals(2, media.size());

    }

    @Test
    public void installCourse_updateCourse()throws Exception{

        CourseUtils.cleanUp();
        copyCourseFromAssets(CORRECT_COURSE);
        response = runInstallCourseTask(context);//Run test task

        //Check if result is true
        assertTrue(response.isSuccess());

        copyCourseFromAssets(UPDATED_COURSE);
        response = runInstallCourseTask(context);//Run test task
        assertTrue(response.isSuccess());

        File modulesPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        assertTrue(modulesPath.exists());
        String[] children = modulesPath.list();
        assertEquals(1, children.length);  //Check that the course exists in the "modules" directory

        File downloadPath = new File(Storage.getDownloadPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        assertEquals(0, downloadPath.list().length);    //Check that the course does not exists in the "downloads" directory

        String shortName = children.length != 0 ? children[0].toLowerCase(Locale.US) : "";
        Course c = CourseUtils.getCourseFromDatabase(context, shortName);
        assertNotNull(c);   //Check that the course exists in the database
        assertEquals(c.getVersionId(), 20180704171235.0); //Check version updated

        List<Media> media = getDbHelper().getCourseMedia(c.getCourseId());
        assertEquals(2, media.size());
    }


    @Test
    public void installCourse_existingCourse()throws Exception{

        CourseUtils.cleanUp();

        copyCourseFromAssets(EXISTING_COURSE);
        response = runInstallCourseTask(context);
        copyCourseFromAssets(EXISTING_COURSE);
        response = runInstallCourseTask(context);

        //Check that it failed
        assertFalse(response.isSuccess());

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
        assertEquals(context.getString(R.string.error_latest_already_installed, title), response.getResultMessage());

    }

    @Test
    public void installCourse_noXMLCourse()throws Exception{

        CourseUtils.cleanUp();
        copyCourseFromAssets(NOXML_COURSE);
        response = runInstallCourseTask(context);

        //Check if result is true
        assertFalse(response.isSuccess());
        //Check if the resultResponse is correct
        assertEquals(context.getString(R.string.error_installing_course, NOXML_COURSE), response.getResultMessage());

        File downloadPath = new File(Storage.getDownloadPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        assertEquals(0, downloadPath.list().length); //Check that the course does not exists in the "downloads" directory

        File modulesPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        assertTrue(modulesPath.exists());
        String[] children = modulesPath.list();
        assertEquals(0, children.length);    //Check that the course does not exists in the "modules" directory

        String shortName = children.length != 0 ? children[0].toLowerCase(Locale.US) : "";
        long courseId = getDbHelper().getCourseID(shortName);
        long userId = getDbHelper().getUserId(SessionManager.getUsername(context));
        Course c = getDbHelper().getCourseWithProgress(courseId, userId);
        assertNull(c);   //Check that the course exists in the database

    }

    @Test
    public void installCourse_insecureCourse()throws Exception {

        CourseUtils.cleanUp();
        copyCourseFromAssets(INSECURE_COURSE);
        response = runInstallCourseTask(context);//Run test task

        assertFalse(response.isSuccess());
    }

  /*  @Test
    public void installCourse_malformedXMLCourse()throws Exception{
        String filename = MALFORMEDXML_COURSE;

        CourseUtils.cleanUp();

        FileUtils.copyZipFromAssets(context, filename);  //Copy course zip from assets to download path

        runInstallCourseTask();//Run test task

        signal.await();

        //Check if result is true
        assertFalse(response.isSuccess());
        //Check if the resultResponse is correct
        assertEquals(context.getString(R.string.error_installing_course, filename), response.getResultMessage());
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
        assertFalse(response.isSuccess());
        //Check if the resultResponse is correct
        assertEquals(context.getString(R.string.error_installing_course, INCORRECT_COURSE), response.getResultMessage());

        File downloadPath = new File(Storage.getDownloadPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        assertEquals(0, downloadPath.list().length); //Check that the course does not exists in the "downloads" directory

        File modulesPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        assertTrue(modulesPath.exists());
        String[] children = modulesPath.list();
        assertEquals(0, children.length);    //Check that the course does not exists in the "modules" directory

        String shortName = children.length != 0 ? children[0].toLowerCase(Locale.US) : "";

        long courseId = getDbHelper().getCourseID(shortName);
        long userId = getDbHelper().getUserId(SessionManager.getUsername(context));
        Course c = getDbHelper().getCourseWithProgress(courseId, userId);
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
        Course c = getDbHelper().getCourseWithProgress(courseId, userId);
        assertNotNull(c);                   //Check that the course exists in the database
        getDbHelper().deleteCourse((int) courseId);    //Delete course from database
        c = getDbHelper().getCourseWithProgress(courseId, userId);
        assertNull(c);                      //Check that the course does not exists in the database

        installCourse_correctCourse();
    }

    private void copyCourseFromAssets(String filename){
        FileUtils.copyZipFromAssets(context, filename);  //Copy course zip from assets to download path
    }

}
