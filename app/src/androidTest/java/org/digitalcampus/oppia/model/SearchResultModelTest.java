package org.digitalcampus.oppia.model;


import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class SearchResultModelTest {

    @Test
    public void getAndSetTest(){
        assertEquals("SearchResult", SearchResult.TAG);

        SearchResult sr = new SearchResult();

        Course c = new Course();
        c.setCourseId(1);
        sr.setCourse(c);

        Activity a = new Activity();
        a.setActId(2);
        sr.setActivity(a);

        Section s = new Section();
        s.setImageFile("myimage.jpg");
        sr.setSection(s);

        assertEquals(c, sr.getCourse());
        assertEquals(a, sr.getActivity());
        assertEquals(s, sr.getSection());

        sr.setRank(156);
        assertEquals(156, sr.getRank(),0);
    }
}
