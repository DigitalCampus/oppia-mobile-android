package org.digitalcampus.oppia.model;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class BadgeModelTest {

    @Test
    public void setAndGetTestEmptyConstructor() {
        Badge b = new Badge();
        b.setDateTime("2020-09-28 16:00:00");
        b.setDescription("badge desc");
        b.setIcon("myicon.jpg");

        assertEquals("myicon.jpg", b.getIcon());
        assertEquals("badge desc", b.getDescription());
        assertEquals("2020-09-28", b.getDateAsString());

    }

    @Test
    public void setAndGetTestWithConstructor() {
        DateTime dt = new DateTime(2020,9,29,9,0,0);
        Badge b = new Badge(dt, "new badge");

        assertEquals(null, b.getIcon());
        assertEquals("new badge", b.getDescription());
        assertEquals("2020-09-29", b.getDateAsString());

    }
}
