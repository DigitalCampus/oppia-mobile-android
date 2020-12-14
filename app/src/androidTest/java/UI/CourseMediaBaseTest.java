package UI;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.utils.storage.ExternalStorageStrategy;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.io.File;

import Utils.CourseUtils;
import Utils.FileUtils;
import database.TestDBHelper;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public abstract class CourseMediaBaseTest {

    protected static final String COURSE_WITH_NO_MEDIA = "Course_with_no_media.zip";
    protected static final String COURSE_WITH_MEDIA_1 = "Course_with_media_1.zip";
    protected static final String COURSE_WITH_MEDIA_2 = "Course_with_media_2.zip";

    protected static final String MEDIA_FILE_VIDEO_TEST_1 = "video-test-1.mp4";
    protected static final String MEDIA_FILE_VIDEO_TEST_2 = "video-test-2.mp4";

    protected Context context;
    protected TestDBHelper testDBHelper;

    @Mock
    SharedPreferences prefs;
    @Mock
    SharedPreferences.Editor editor;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        initMockEditor();

        // First ensure to use in-memory database
        testDBHelper = new TestDBHelper(InstrumentationRegistry.getInstrumentation().getTargetContext());
        testDBHelper.setUp();

        StorageAccessStrategy storageStrategy = new ExternalStorageStrategy();
        Storage.setStorageStrategy(storageStrategy);

        when(prefs.getString(eq(PrefsActivity.PREF_STORAGE_OPTION), anyString())).thenReturn(storageStrategy.getStorageType());

        CourseUtils.cleanUp();
    }

    private void initMockEditor() {
        when(prefs.edit()).thenReturn(editor);
        when(editor.putString(anyString(), anyString())).thenReturn(editor);
        when(editor.putLong(anyString(), anyLong())).thenReturn(editor);
        when(editor.putBoolean(anyString(), anyBoolean())).thenReturn(editor);
        when(editor.putInt(anyString(), anyInt())).thenReturn(editor);
    }


    @After
    public void tearDown() throws Exception {
        testDBHelper.tearDown();
//        CourseUtils.cleanUp();
    }

    protected void copyCourseFromAssets(String filename){
        FileUtils.copyZipFromAssetsPath(context, "courses_media", filename);  //Copy course zip from assets to download path
    }

    protected void copyMediaFromAssets(String filename){
        File mediaPath = new File(Storage.getMediaPath(context));
        FileUtils.copyFileFromAssets(context, "courses_media", filename, mediaPath);  //Copy course zip from assets to download path
    }
}
