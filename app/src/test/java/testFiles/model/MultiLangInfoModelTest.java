package testFiles.model;

import static org.junit.Assert.assertEquals;

import org.digitalcampus.oppia.model.MultiLangInfoModel;
import org.jarjar.apache.commons.codec.binary.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class MultiLangInfoModelTest {

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
