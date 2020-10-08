package org.digitalcampus.oppia.model;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class TrackerLogTest {

    @Test
    public void getAndSetTest(){

        DateTime dt = new DateTime(2020,9,29,9,0,0);
        TrackerLog tl = new TrackerLog();
        tl.setCompleted(true);
        tl.setContent("my content");
        tl.setCourseId(1);
        tl.setDatetime(dt);
        tl.setDigest("abcd");
        tl.setEvent("completed");
        tl.setPoints(10);
        tl.setSubmitted(false);
        tl.setType("page");
        tl.setUserId(15);

        assertEquals(true, tl.isCompleted());
        assertEquals("my content", tl.getContent());
        assertEquals(1, tl.getCourseId());
        assertEquals(dt ,tl.getDatetime());
        assertEquals("2020-09-29 09:00:00", tl.getDateTimeString());
        assertEquals("abcd", tl.getDigest());
        assertEquals("completed", tl.getEvent());
        assertEquals(10, tl.getPoints());
        assertEquals(false, tl.isSubmitted());
        assertEquals("page", tl.getType());
        assertEquals(15, tl.getUserId());

        assertEquals("my content", tl.toString());

    }

    @Test
    public void asJSONCollectionStringTest(){
        Collection<TrackerLog> c = new ArrayList<>();
        TrackerLog tl1 = new TrackerLog();
        tl1.setContent("content1");
        c.add(tl1);

        TrackerLog tl2 = new TrackerLog();
        tl2.setContent("content2");
        c.add(tl2);

        assertEquals("[content1,content2]", TrackerLog.asJSONCollectionString(c));

    }
}
