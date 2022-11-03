package testFiles.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.MultiLangInfoModel;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.ArrayList;

public class MultiLangInfoModelTest {


    @Test
    public void MultiLangInfo_correctTitles() throws Exception {
        MultiLangInfoModel multiLangInfo = new MultiLangInfoModel();

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
        MultiLangInfoModel multiLangInfo = new MultiLangInfoModel();

        assertEquals(MultiLangInfoModel.DEFAULT_NOTITLE, multiLangInfo.getTitle("en"));
    }

    @Test
    public void MultiLangInfo_nonExistingTitle() throws Exception{
        MultiLangInfoModel multiLangInfo = new MultiLangInfoModel();

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
        MultiLangInfoModel multiLangInfo = new MultiLangInfoModel();

        String malformedJSONString = "[{\"en\":\"EnglishTitle\"}, {\"es\": ]";

        multiLangInfo.setTitlesFromJSONString(malformedJSONString);

        assertEquals(MultiLangInfoModel.DEFAULT_NOTITLE, multiLangInfo.getTitle("en"));
    }

    @Test
    public void MultiLangInfo_wellFormedJSONString() throws Exception{
        MultiLangInfoModel multiLangInfo = new MultiLangInfoModel();

        String englishTitle = "English Title";
        String spanishTitle = "Titulo Español";

        String wellFormedJSONString = "[{\"en\":\"" + englishTitle + "\"}, {\"es\":\"" + spanishTitle + "\"}]";
        multiLangInfo.setTitlesFromJSONString(wellFormedJSONString);

        assertEquals(englishTitle, multiLangInfo.getTitle("en"));
        assertEquals(spanishTitle, multiLangInfo.getTitle("es"));
    }

    @Test
    public void MultiLangInfo_emptyJSONString() throws Exception {
        MultiLangInfoModel multiLangInfo = new MultiLangInfoModel();

        String wellFormedJSONString = "[]";
        multiLangInfo.setTitlesFromJSONString(wellFormedJSONString);

        assertEquals(MultiLangInfoModel.DEFAULT_NOTITLE, multiLangInfo.getTitle("en"));
    }

    @Test
    public void MultiLangInfo_NoDescription() throws Exception {
        MultiLangInfoModel multiLangInfo = new MultiLangInfoModel();
        assertNull(multiLangInfo.getDescription("en"));
    }
    
    @Test
    public void jsonTitlesTest(){

        MultiLangInfoModel mlim = new MultiLangInfoModel();

        String json = "{ \"en\": \"my title\", \"es\": \"mi titular\"}";
        try {
            JSONObject jsonObj = new JSONObject(json);
            mlim.setTitlesFromJSONObjectMap(jsonObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assertEquals("my title", mlim.getTitle("en"));
        assertEquals("mi titular", mlim.getTitle("es"));
    }

    @Test
    public void jsonDescriptionsTest(){

        MultiLangInfoModel mlim = new MultiLangInfoModel();

        String json = "{ \"en\": \"my desc\", \"es\": \"mi description\"}";
        try {
            JSONObject jsonObj = new JSONObject(json);
            mlim.setDescriptionsFromJSONObjectMap(jsonObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assertEquals("my desc", mlim.getDescription("en"));
        assertEquals("mi description", mlim.getDescription("es"));
    }
}
