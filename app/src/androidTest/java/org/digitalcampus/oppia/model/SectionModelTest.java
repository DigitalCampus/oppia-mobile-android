package org.digitalcampus.oppia.model;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class SectionModelTest {

    @Test
    public void getAndSetTest(){

        Section s = new Section();

        s.setImageFile("myimage.jpg");
        assertEquals("myimage.jpg", s. getImageFile());

        List<Activity> actList = new ArrayList<>();
        Activity a1 = new Activity();
        a1.setDigest("abcd");
        a1.setCompleted(true);
        actList.add(a1);
        Activity a2 = new Activity();
        a2.setDigest("efgh");
        a2.setCompleted(false);
        actList.add(a2);

        s.setActivities(actList);

        assertEquals(a1, s.getActivity("abcd"));
        assertEquals(a2, s.getActivity("efgh"));
        assertEquals(null, s.getActivity("ijkl"));
        assertEquals(1, s.getCompletedActivities());
    }
}
