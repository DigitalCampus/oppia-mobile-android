package org.digitalcampus.oppia.model;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
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
