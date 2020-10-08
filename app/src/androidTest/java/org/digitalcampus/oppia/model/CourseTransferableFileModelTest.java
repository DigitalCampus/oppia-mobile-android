package org.digitalcampus.oppia.model;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class CourseTransferableFileModelTest {

    @Test
    public void getAndSetTest(){

        CourseTransferableFile ctf = new CourseTransferableFile();

        List<String> rm = new ArrayList<>();
        rm.add("string 1");
        rm.add("string 2");
        ctf.setRelatedMedia(rm);
        assertEquals(2, ctf.getRelatedMedia().size());

        ctf.setTitle("  my title  ");
        assertEquals("my title", ctf.getTitle());

        ctf.setShortname("shortname");
        assertEquals("shortname", ctf.getShortname());

        ctf.setVersionId((double) 123456);
        assertEquals(123456, ctf.getVersionId(),0);

        ctf.setFilename("myfile.txt");
        assertEquals("myfile.txt", ctf.getFilename());

        ctf.setFileSize(999999);
        ctf.setRelatedFilesize(1234);
        assertEquals(999999, ctf.getFileSize());
        assertEquals("977.8 KB", ctf.getDisplayFileSize());

        ctf.setType(CourseTransferableFile.TYPE_ACTIVITY_LOG);
        assertEquals("activity", ctf.getType());

        ctf.setFilename("myfile_202009301532.txt");
        assertEquals("2020/09/30 15:32", ctf.getDisplayDateTimeFromFilename());

        assertEquals(1878414046, ctf.hashCode());
        ctf.setFilename(null);
        assertEquals(0, ctf.hashCode());

        ctf.setType(CourseTransferableFile.TYPE_COURSE_BACKUP);
        assertEquals("shortname", ctf.getNotificationName());

        ctf.setFilename("123_myfile_202009301532.txt");
        ctf.setType(CourseTransferableFile.TYPE_ACTIVITY_LOG);
        assertEquals("123 log", ctf.getNotificationName());

        ctf.setFile(null);
        assertEquals(null, ctf.getFile());

        ctf.setTitleFromFilename();
        assertEquals("123", ctf.getTitle());

        ctf.setType("other");
        ctf.setTitleFromFilename();
        assertEquals("", ctf.getTitle());

        CourseTransferableFile equalCTF = new CourseTransferableFile();
        equalCTF.setFilename("123_myfile_202009301532.txt");
        assertEquals(true, ctf.equals(equalCTF));

        CourseTransferableFile nonEqualCTF = new CourseTransferableFile();
        nonEqualCTF.setFilename("filename2.txt");
        assertEquals(false, ctf.equals(nonEqualCTF));

        assertEquals(false, ctf.equals("not a CTF"));

        ctf.setRelatedMedia(null);
        assertEquals(0, ctf.getRelatedMedia().size());

        ctf.setType("other");
        assertEquals(null, ctf.getNotificationName());
    }
}
