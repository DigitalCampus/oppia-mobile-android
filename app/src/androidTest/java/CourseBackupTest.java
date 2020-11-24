import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

import Utils.FileUtils;

import androidx.test.platform.app.InstrumentationRegistry;
import android.util.Log;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.model.Course;
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
import java.util.Locale;

import Utils.CourseUtils;
import database.BaseTestDB;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(Parameterized.class)
public class CourseBackupTest extends BaseTestDB {

    public static final String TAG = InstallDownloadedCoursesTest.class.getSimpleName();

    private final String CORRECT_COURSE = "Correct_Course.zip";

    private Context context;
    private SharedPreferences prefs;
    private Payload response;
    private StorageAccessStrategy storageStrategy;

    public CourseBackupTest(StorageAccessStrategy storageStrategy) {
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
    public void installCourse_correctBackup()throws Exception{

        CourseUtils.cleanUp();

        copyCourseFromAssets(CORRECT_COURSE);
        response = CourseUtils.runInstallCourseTask(context);//Run test task

        // Check if result is true
        assertTrue(response.isResult());

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

        File modulesPath = new File(Storage.getCoursesPath(InstrumentationRegistry.getInstrumentation().getTargetContext()));
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
        assertTrue(response.isResult());
        courseId = getDbHelper().getCourseID(shortName);
        c = getDbHelper().getCourse(courseId, userId);
        assertNotNull(c);   //Check that the course exists in the database


    }

    private void copyCourseFromAssets(String filename){
        FileUtils.copyZipFromAssets(context, filename);  //Copy course zip from assets to download path
    }
}
