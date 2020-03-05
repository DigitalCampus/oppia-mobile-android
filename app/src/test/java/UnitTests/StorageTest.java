package UnitTests;

import android.test.mock.MockContext;

import org.digitalcampus.oppia.utils.storage.Storage;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import oppia.utils.storage.MockStorageStrategy;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;


public class StorageTest {

    @Inject
    private MockContext mockContext;
    private MockStorageStrategy storageStrategy;

    @ClassRule
    public static TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    public void setUp() throws Exception {
//        mockContext = new MockContext();
        storageStrategy = new MockStorageStrategy();
        Storage.setStorageStrategy(storageStrategy);
    }

    @Test
    public void Storage_setStorageStrategy(){
        assertEquals(storageStrategy, Storage.getStorageStrategy());
    }

    @Test
    public void Storage_getCoursesPath(){
        String path = Storage.getCoursesPath(mockContext);
        assertEquals(tempFolder.getRoot()
                + File.separator
                + Storage.APP_COURSES_DIR_NAME
                + File.separator, path);
    }

    @Test
    public void Storage_getDownloadPath(){
        String path = Storage.getDownloadPath(mockContext);
        assertEquals(tempFolder.getRoot()
                + File.separator
                + Storage.APP_DOWNLOAD_DIR_NAME
                + File.separator, path);
    }

    @Test
    public void Storage_getMediaPath(){
        System.out.println(Storage.getMediaPath(mockContext));
        String path = Storage.getMediaPath(mockContext);
        assertEquals(tempFolder.getRoot()
                + File.separator
                + Storage.APP_MEDIA_DIR_NAME
                + File.separator, path);
    }

    @Test
    public void Storage_createFolderStructure(){
        Storage.createFolderStructure(mockContext);

        assertTrue(new File(tempFolder.getRoot() + File.separator + "modules").exists());
        assertTrue(new File(tempFolder.getRoot() + File.separator + "download").exists());
        assertTrue(new File(tempFolder.getRoot() + File.separator + "media").exists());
        assertTrue(new File(tempFolder.getRoot() + File.separator + ".nomedia").exists());
    }


//    @Test
    public void Storage_mediaFileExists(){
        try {
            //Existing file
            Storage.createFolderStructure(mockContext);
            File existingFile = new File(tempFolder.getRoot() + File.separator + "media" + File.separator + "ExistingFile.txt");
            existingFile.createNewFile();

            System.out.println(existingFile.getAbsolutePath());
            assertTrue(Storage.mediaFileExists(mockContext, existingFile.getName()));

            //Non existing file
            assertFalse(Storage.mediaFileExists(mockContext, "NonExistingFile.txt"));

        }catch (IOException ioe){
            ioe.printStackTrace();
        }
    }

    @Test
    public void Storage_createNoMediaFile(){
        Storage.createNoMediaFile(mockContext);

        assertTrue(new File(tempFolder.getRoot() + File.separator + ".nomedia").exists());
    }


}