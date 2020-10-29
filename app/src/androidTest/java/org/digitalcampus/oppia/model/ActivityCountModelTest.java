package org.digitalcampus.oppia.model;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ActivityCountModelTest {

    @Test
    public void getAndSetTest(){

        ActivityCount ac = new ActivityCount();

        Map<String, Integer> typeCount = new LinkedHashMap<>();
        typeCount.put("mykey", 123);
        typeCount.put("another", 999);

        ac.setTypeCount(typeCount);

        assertEquals(123, ac.getValueForType("mykey"));
        assertEquals(0, ac.getValueForType("invalidkey"));
    }
}
