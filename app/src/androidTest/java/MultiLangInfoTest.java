
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.MultiLangInfo;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import Utils.CourseUtils;

import static junit.framework.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class MultiLangInfoTest {


    @Test
    public void MultiLangInfo_correctTitles() throws Exception {
        MultiLangInfo multiLangInfo = new MultiLangInfo();

        String englishTitle = "English Title";
        String spanishTitle = "Titulo Español";

        ArrayList<Lang> titles = new ArrayList<>();
        titles.add(new Lang("en", englishTitle));
        titles.add(new Lang("es", spanishTitle));
        multiLangInfo.setTitles(titles);

        assertEquals(englishTitle, multiLangInfo.getTitle("en"));
        assertEquals(spanishTitle, multiLangInfo.getTitle("es"));
    }

    @Test
    public void MultiLangInfo_NoTitles() throws Exception {
        MultiLangInfo multiLangInfo = new MultiLangInfo();

        assertEquals("No title set", multiLangInfo.getTitle("en"));
    }

    @Test
    public void MultiLangInfo_nonExistingTitle() throws Exception{
        MultiLangInfo multiLangInfo = new MultiLangInfo();

        String firstTitle = "First Title";
        String secondTitle = "Second Title";

        ArrayList<Lang> titles = new ArrayList<>();
        titles.add(new Lang("en", firstTitle));
        titles.add(new Lang("en", secondTitle));
        multiLangInfo.setTitles(titles);


        assertEquals(firstTitle, multiLangInfo.getTitle("es"));
    }
}
