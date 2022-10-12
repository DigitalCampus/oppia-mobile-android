package testFiles.model;

import static org.junit.Assert.assertEquals;

import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Section;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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
