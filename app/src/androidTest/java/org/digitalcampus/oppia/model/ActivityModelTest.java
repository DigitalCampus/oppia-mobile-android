package org.digitalcampus.oppia.model;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.exception.GamificationEventNotFound;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;;

@RunWith(AndroidJUnit4.class)
public class ActivityModelTest {

    @Test
    public void getImageFilePathTest(){
        Activity a = new Activity();
        a.setImageFile("myimage.jpg");
        //  without separator
        String response = a.getImageFilePath("test");
        assertEquals("test/myimage.jpg", response);

        // with separator
        response = a.getImageFilePath("test/");
        assertEquals("test/myimage.jpg", response);
    }

    @Test
    public void attemptedTest(){
        Activity a = new Activity();
        a.setAttempted(false);

        assertEquals(false, a.isAttempted());
    }

    @Test
    public void getDefaultImageQuizTest(){
        Activity a = new Activity();
        a.setActType("quiz");

        assertEquals(R.drawable.default_icon_quiz, a.getDefaultResourceImage());
    }

    @Test
    public void getDefaultImagePageWithMediaTest(){
        Activity a = new Activity();
        a.setActType("page");
        Media m = new Media();
        m.setDigest("abcd");
        List<Media> mediaList = new ArrayList<>();
        mediaList.add(m);
        a.setMedia(mediaList);

        assertEquals(R.drawable.default_icon_video, a.getDefaultResourceImage());
    }

    @Test
    public void getDefaultImagePageNoMediaTest(){
        Activity a = new Activity();
        a.setActType("page");
        assertEquals(R.drawable.default_icon_activity, a.getDefaultResourceImage());
    }

    @Test
    public void getMedia(){
        Activity a = new Activity();
        a.setActType("page");
        Media m = new Media();
        m.setDigest("abcd");
        m.setFilename("myvideo.mp4");
        List<Media> mediaList = new ArrayList<>();
        mediaList.add(m);
        a.setMedia(mediaList);

        assertEquals(1, a.getMedia().size());
        assertEquals(m, a.getMedia("myvideo.mp4"));
        assertEquals(null, a.getMedia("none.mp4"));
    }

    @Test
    public void getLocation(){
        Activity a = new Activity();
        a.setActType("page");
        assertEquals(null, a.getLocation("en"));

        Lang l = new Lang("en", "my content");
        l.setLocation("mfile.html");
        List<Lang> langList = new ArrayList<>();
        langList.add(l);
        a.setLocations(langList);

        assertEquals("my content", a.getLocation("en"));
        assertEquals("my content", a.getLocation("en-US"));
        assertEquals("my content", a.getLocation("en_UK"));
        assertEquals("my content", a.getLocation("fi"));

        Lang lFi = new Lang("fi", "kiitos");
        lFi.setLocation("mfile_fi.html");
        langList.add(lFi);
        a.setLocations(langList);

        assertEquals("kiitos", a.getLocation("fi"));
    }

    @Test
    public void getMimeType(){
        Activity a = new Activity();
        a.setMimeType("text/html");
        assertEquals("text/html", a.getMimeType());
    }

    @Test
    public void getContents(){
        Activity a = new Activity();
        a.setActType("page");
        assertEquals("No content", a.getContents("en"));

        Lang l = new Lang("en", "my content");
        List<Lang> langList = new ArrayList<>();
        langList.add(l);
        a.setContents(langList);

        assertEquals("my content", a.getContents("en"));
        assertEquals("my content", a.getContents("en-US"));
        assertEquals("my content", a.getContents("en_UK"));
        assertEquals("my content", a.getContents("fi"));

        Lang lFi = new Lang("fi", "kiitos");
        langList.add(lFi);
        a.setContents(langList);

        assertEquals("kiitos", a.getContents("fi"));
    }

    @Test
    public void findGamificationEvent() {
        Activity a = new Activity();
        a.setActType("page");

        // TODO not got this working right yet
        // assertThrows(a.findGamificationEvent("fi"), GamificationEventNotFound);
        GamificationEvent ge = new GamificationEvent();
        ge.setEvent("completed");
        List<GamificationEvent> geList = new ArrayList<>();
        geList.add(ge);
        a.setGamificationEvents(geList);

        // TODO this part is not the best way either
        try {
            assertEquals(ge, a.findGamificationEvent("completed"));
        } catch (GamificationEventNotFound genf){
            // do nothing
        }
    }

}
