package testFiles.model;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.digitalcampus.oppia.model.CourseMetaPage;
import org.digitalcampus.oppia.model.Lang;
import org.junit.Test;

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
