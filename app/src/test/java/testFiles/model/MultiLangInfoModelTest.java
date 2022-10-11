package testFiles.model;

import static org.junit.Assert.assertEquals;

import org.digitalcampus.oppia.model.MultiLangInfoModel;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class MultiLangInfoModelTest {

    @Test
    public void jsonTitlesTest(){

        MultiLangInfoModel mlim = new MultiLangInfoModel();

        String json = "{ \"titles\" : [{ \"en\": \"my title\"}, { \"es\": \"mi titular\"}] }";
        try {
            JSONObject jsonObj = new JSONObject(json);
            mlim.setTitlesFromJSONObjectMap(jsonObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assertEquals("[{\"en\":\"my title\"},{\"es\":\"mi titular\"}]", mlim.getTitle("en"));
    }

    @Test
    public void jsonDescriptionsTest(){

        MultiLangInfoModel mlim = new MultiLangInfoModel();

        String json = "{ \"descriptions\" : [{ \"en\": \"my desc\"}, { \"es\": \"mi description\"}] }";
        try {
            JSONObject jsonObj = new JSONObject(json);
            mlim.setDescriptionsFromJSONObjectMap(jsonObj);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        assertEquals("[{\"en\":\"my desc\"},{\"es\":\"mi description\"}]", mlim.getDescription("en"));
    }
}
