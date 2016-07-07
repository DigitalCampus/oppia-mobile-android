import android.provider.MediaStore;

import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;


public class FileUtilsTests {

    @Rule
    private TemporaryFolder folder = new TemporaryFolder();
    private static final int FILES_COUNT = 5;

    @Test
    public void FileUtils_unzipFiles_getCorrectFiles(){

        File zipFile;
        boolean result = false;
        try {
            zipFile = createTestZipFile();

            if (zipFile != null) {
                result = FileUtils.unzipFiles(zipFile.getParentFile().getAbsolutePath(),
                        zipFile.getName(),
                        zipFile.getParentFile().getAbsolutePath());
            }

            assertTrue(result);
            assertEquals(FILES_COUNT + 1, zipFile.getParentFile().listFiles().length);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void FileUtils_cleanDir_emptyFolder(){


        try {
            File tempFolder = folder.newFolder("tempFolder");

            for (int i = 0; i < FILES_COUNT; i++) {
                String filename = "test_file" + i + ".txt";
                new File(tempFolder + File.separator + filename).createNewFile();
            }

            FileUtils.cleanDir(tempFolder);

            assertEquals(0, tempFolder.listFiles().length);

        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    @Test
    public void FileUtils_deleteDir(){
        try{
            File tempFolder = folder.newFolder("tempFolder");

            assertTrue(FileUtils.deleteDir(tempFolder));
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }

    @Test
    public void FileUtils_dirSize(){
        File f = new File("non_exists_file.txt");
        assertEquals(0, FileUtils.dirSize(f));

        try {
            File tempFolder = folder.newFolder("tempFolder");

            for (int i = 0; i < FILES_COUNT; i++) {
                String filename = "test_file" + i + ".txt";
                new File(tempFolder + File.separator + filename).createNewFile();
            }
        }catch(IOException ioe){
            ioe.printStackTrace();
        }

    }

    private File createTestZipFile() throws IOException {

        //Create the files that will be zipped

        File zipFile = null;
        File[] files = new File[FILES_COUNT];
        try {
            for (int i = 0; i < FILES_COUNT; i++) {
                String filename = "test_file" + i + ".txt";
                files[i] = folder.newFile(filename);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedInputStream is;
        ZipOutputStream out = null;
        try {
            zipFile = folder.newFile("test.zip");
            out = new ZipOutputStream((new BufferedOutputStream(new FileOutputStream(zipFile))));

            byte data[] = new byte[FileUtils.BUFFER_SIZE];

            for(int i = 0; i < files.length; i++){
                FileInputStream fis = new FileInputStream(files[i]);
                is = new BufferedInputStream(fis, FileUtils.BUFFER_SIZE);
                try{
                    ZipEntry entry = new ZipEntry(files[i].getName());
                    out.putNextEntry(entry);

                    int count;
                    while ((count = is.read(data, 0, FileUtils.BUFFER_SIZE)) != -1){
                        out.write(data, 0, count);
                    }
                }catch(IOException ioe){
                    ioe.printStackTrace();
                }finally{
                    is.close();
                    System.out.println(files[i].delete());
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally{
            out.close();
        }

        return zipFile;

    }
}
