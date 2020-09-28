package database.model;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Media;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

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

}
