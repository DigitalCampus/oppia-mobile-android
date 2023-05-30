package testFiles.model;

import static org.junit.Assert.assertEquals;

import org.digitalcampus.oppia.model.OfflineCourseFile;
import org.junit.Test;

import java.io.File;
import java.util.Objects;

public class OfflineCourseFileModelTest {

    @Test
    public void detectFileTypeTest_courseFile() {
        String courseFileZip = "tests/courses/Correct_Course.zip";
        String filePath = Objects.requireNonNull(getClass().getClassLoader()).getResource(courseFileZip).getFile();
        File targetFile = new File(filePath);
        OfflineCourseFile ocf = new OfflineCourseFile(targetFile);
        assertEquals(OfflineCourseFile.FileType.COURSE, ocf.getType());
    }

    @Test
    public void detectFileTypeTest_mediaFile() {
        String mediaFileZip = "tests/media/correct_media.zip";
        String filePath = Objects.requireNonNull(getClass().getClassLoader()).getResource(mediaFileZip).getFile();
        File targetFile = new File(filePath);
        OfflineCourseFile ocf = new OfflineCourseFile(targetFile);
        assertEquals(OfflineCourseFile.FileType.MEDIA, ocf.getType());
    }

    @Test
    public void detectFileTypeTest_invalidZipFile() {
        String invalidFile = "tests/media/incorrect_media.zip";
        String filePath = Objects.requireNonNull(getClass().getClassLoader()).getResource(invalidFile).getFile();
        File targetFile = new File(filePath);
        OfflineCourseFile ocf = new OfflineCourseFile(targetFile);
        assertEquals(OfflineCourseFile.FileType.INVALID, ocf.getType());
    }

    @Test
    public void detectFileTypeTest_invalidFile() {
        String invalidFile = "tests/tags/tags_original.json";
        String filePath = Objects.requireNonNull(getClass().getClassLoader()).getResource(invalidFile).getFile();
        File targetFile = new File(filePath);
        OfflineCourseFile ocf = new OfflineCourseFile(targetFile);
        assertEquals(OfflineCourseFile.FileType.INVALID, ocf.getType());
    }


}
