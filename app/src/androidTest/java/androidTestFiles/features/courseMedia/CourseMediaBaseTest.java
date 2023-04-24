package androidTestFiles.features.courseMedia;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.utils.storage.Storage;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.io.File;

import androidTestFiles.utils.parent.BaseTest;
import androidTestFiles.utils.parent.DaggerInjectMockUITest;
import androidTestFiles.utils.CourseUtils;
import androidTestFiles.utils.FileUtils;
import androidTestFiles.database.TestDBHelper;

@RunWith(AndroidJUnit4.class)
public abstract class CourseMediaBaseTest extends DaggerInjectMockUITest {


    protected Context context;
    protected TestDBHelper testDBHelper;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // First ensure to use in-memory database
        testDBHelper = new TestDBHelper(InstrumentationRegistry.getInstrumentation().getTargetContext());
        testDBHelper.setUp();

        CourseUtils.cleanUp();
    }



    @After
    public void tearDown() throws Exception {
        testDBHelper.tearDown();
        CourseUtils.cleanUp();
    }

    protected void copyCourseFromAssets(String filename){
        FileUtils.copyZipFromAssetsPath(context, BaseTest.PATH_COURSES_MEDIA_TESTS, filename);  //Copy course zip from assets to download path
    }

    protected void copyMediaFromAssets(String filename, String destinationFilename){
        File mediaPath = new File(Storage.getMediaPath(context));
        FileUtils.copyFileFromAssets(context, BaseTest.PATH_COURSES_MEDIA_TESTS, filename, mediaPath, destinationFilename);  //Copy course zip from assets to download path
    }

    protected void copyMediaFromAssets(String filename){
        copyMediaFromAssets(filename, filename);
    }
}
