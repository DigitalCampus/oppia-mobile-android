package testFiles.model;

import static org.junit.Assert.assertEquals;

import org.digitalcampus.oppia.model.ActivityType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ActivityTypeModelTest {

    @Test
    public void getAndSetTest(){

        ActivityType at = new ActivityType("my activitytype", "play", 123, false);

        assertEquals("my activitytype", at.toString());

        at.setName("my new name");
        assertEquals("my new name", at.getName());

        at.setType("my type");
        assertEquals("my type", at.getType());

        at.setEnabled(true);
        assertEquals(true, at.isEnabled());

        at.setColor(456);
        assertEquals(456, at.getColor());

        List<Integer> valList = new ArrayList<>();
        valList.add(88);

        at.setValues(valList);
        assertEquals(1, at.getValues().size());


    }
}
