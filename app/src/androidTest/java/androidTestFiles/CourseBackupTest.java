package androidTestFiles;

import android.content.Context;
import android.util.Log;

import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.Locale;

import androidTestFiles.Utils.CourseUtils;
import androidTestFiles.Utils.FileUtils;
import androidTestFiles.database.BaseTestDB;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class CourseBackupTest extends BaseTestDB {

    public static final String TAG = InstallDownloadedCoursesTest.class.getSimpleName();

    private final String CORRECT_COURSE = "Correct_Course.zip";

    private Context context;
    private BasicResult response;
    private StorageAccessStrategy storageStrategy;

    public CourseBackupTest(StorageAccessStrategy storageStrategy) {
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
    }

    //Run test once for every StorageStrategy (Internal, External)
    public void setStorageStrategy(){

        Log.v(TAG, "Using Strategy: " + storageStrategy.getStorageType());
        Storage.setStorageStrategy(storageStrategy);

        when(prefs.getString(eq(PrefsActivity.PREF_STORAGE_OPTION), anyString())).thenReturn(storageStrategy.getStorageType());
    }


    @Test
    public void installCourse_correctBackup()throws Exception{

        CourseUtils.cleanUp();

        copyCourseFromAssets(CORRECT_COURSE);
        response = CourseUtils.runInstallCourseTask(context);//Run test task

        // Check if result is true
        assertTrue(response.isSuccess());

        // Check that the course backup exists
        File backupsPath = new File(Storage.getCourseBackupPath(context));
        assertTrue(backupsPath.exists());
        String[] children = backupsPath.list();
        assertEquals(1, children.length);

        // Copy the backup file to the download folder
        String backup = children[0];
        File backupFile = new File(backupsPath, backup);
        File downloadPath = new File(Storage.getDownloadPath(context));
        FileUtils.copyFileToDir(backupFile, downloadPath, false);

        File modulesPath = new File(Storage.getCoursesPath(context));
        assertTrue(modulesPath.exists());
        children = modulesPath.list();
        assertEquals(1, children.length);  //Check that the course exists in the "modules" directory

        long userId = getDbHelper().getUserId(SessionManager.getUsername(context));
        String shortName = children[0].toLowerCase(Locale.US);
        long courseId = getDbHelper().getCourseID(shortName);
        Course c = getDbHelper().getCourse(courseId, userId);
        assertNotNull(c);   //Check that the course exists in the database

        DeleteCourseTest.deleteTestCourse(c, context);

        // Check that when a course is deleted, the backup is removed as well
        children = backupsPath.list();
        assertEquals(0, children.length);

        // Try to install again from backup file
        response = CourseUtils.runInstallCourseTask(context);//Run test task
        // Check if result is true
        assertTrue(response.isSuccess());
        courseId = getDbHelper().getCourseID(shortName);
        c = getDbHelper().getCourse(courseId, userId);
        assertNotNull(c);   //Check that the course exists in the database


    }

    private void copyCourseFromAssets(String filename){
        FileUtils.copyZipFromAssets(context, filename);  //Copy course zip from assets to download path
    }
}
