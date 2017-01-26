
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.MultiLangInfo;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;


import TestRules.DisableAnimationsRule;

import static junit.framework.Assert.assertEquals;


@RunWith(AndroidJUnit4.class)
public class MultiLangInfoTest {

    @Rule
    public DisableAnimationsRule disableAnimationsRule = new DisableAnimationsRule();


    private Context context;

    @Before
    public void setUp() throws Exception {
        context = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void MultiLangInfo_correctTitles() throws Exception {
        MultiLangInfo multiLangInfo = new MultiLangInfo();

        String englishTitle = "English Title";
        String spanishTitle = "Titulo Español";

        ArrayList<Lang> titles = new ArrayList<>();
        titles.add(new Lang("en", englishTitle));
        titles.add(new Lang("es", spanishTitle));
        multiLangInfo.setTitles(titles);

        String json = multiLangInfo.getTitleJSONString();


        assertEquals(englishTitle, multiLangInfo.getTitle("en"));
        assertEquals(spanishTitle, multiLangInfo.getTitle("es"));
    }

    @Test
    public void MultiLangInfo_NoTitles() throws Exception {
        MultiLangInfo multiLangInfo = new MultiLangInfo();

        assertEquals(context.getString(R.string.no_title_set), multiLangInfo.getTitle("en"));
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

    @Test
    public void MultiLangInfo_malformedJSONString() throws Exception{
        MultiLangInfo multiLangInfo = new MultiLangInfo();

        String malformedJSONString = "[{\"en\":\"EnglishTitle\"}, {\"es\": ]";

        multiLangInfo.setTitlesFromJSONString(malformedJSONString);

        assertEquals(context.getString(R.string.no_title_set), multiLangInfo.getTitle("en"));
    }

    @Test
    public void MultiLangInfo_wellFormedJSONString() throws Exception{
        MultiLangInfo multiLangInfo = new MultiLangInfo();

        String englishTitle = "English Title";
        String spanishTitle = "Titulo Español";

        String wellFormedJSONString = "[{\"en\":\"" + englishTitle + "\"}, {\"es\":\"" + spanishTitle + "\"}]";
        multiLangInfo.setTitlesFromJSONString(wellFormedJSONString);

        assertEquals(englishTitle, multiLangInfo.getTitle("en"));
        assertEquals(spanishTitle, multiLangInfo.getTitle("es"));
    }

    @Test
    public void MultiLangInfo_emptyJSONString() throws Exception {
        MultiLangInfo multiLangInfo = new MultiLangInfo();

        String wellFormedJSONString = "[]";
        multiLangInfo.setTitlesFromJSONString(wellFormedJSONString);

        assertEquals(context.getString(R.string.no_title_set), multiLangInfo.getTitle("en"));
    }

    @Test
    public void MultiLangInfo_NoDescription() throws Exception {
        MultiLangInfo multiLangInfo = new MultiLangInfo();

        assertEquals(context.getString(R.string.no_description_set), multiLangInfo.getDescription("en"));
    }
}
