package org.digitalcampus.oppia.model;


import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class CourseMetaPageModelTest {

    @Test
    public void constructorTest(){
        assertEquals("CourseMetaPage", CourseMetaPage.TAG);
        CourseMetaPage cmp = new CourseMetaPage();
        assertEquals(null, cmp.getLang("en"));
    }

    @Test
    public void getAndSetTest(){
        CourseMetaPage cmp = new CourseMetaPage();
        cmp.setId(123);
        assertEquals(123, cmp.getId());

        Lang l = new Lang("en", "my content");
        cmp.addLang(l);

        assertEquals(l, cmp.getLang("en"));
        assertEquals(l, cmp.getLang("en-US"));
        assertEquals(l, cmp.getLang("en_UK"));
        assertEquals(l, cmp.getLang("fi"));

        Lang lFi = new Lang("fi", "kiitos");
        cmp.addLang(lFi);

        assertEquals(l, cmp.getLang("es"));
        assertEquals(lFi, cmp.getLang("fi"));

    }
}
