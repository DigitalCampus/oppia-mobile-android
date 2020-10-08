package org.digitalcampus.oppia.model;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
@RunWith(AndroidJUnit4.class)
public class TagModelTest {

    @Test
    public void getAndSetTest(){

        Tag t = new Tag();

        t.setCount(5);
        assertEquals(5, t.getCount());

        t.setCount(-1);
        assertEquals(0, t.getCount());

        t.setCount(0);
        assertEquals(0, t.getCount());

        List<Course> courseList = new ArrayList<>();
        Course c = new Course();
        c.setCourseId(1);
        courseList.add(c);
        t.setCourses(courseList);
        assertEquals(1, t.getCourses().size());

        t.setId(123);
        assertEquals(123, t.getId());

        t.setOrderPriority(5);
        assertEquals(5, t.getOrderPriority());

        t.setIcon("myicon.jpg");
        assertEquals("myicon.jpg", t.getIcon());

        t.setHighlight(false);
        assertEquals(false, t.isHighlight());

        t.setHighlight(true);
        assertEquals(true, t.isHighlight());
    }
}
