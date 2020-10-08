package org.digitalcampus.oppia.model;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class TrackerLogModelTest {

    @Test
    public void getAndSetTest(){
        TrackerLog tl = new TrackerLog();
        tl.setId(123456);
        assertEquals(123456, tl.getId());
    }
}
