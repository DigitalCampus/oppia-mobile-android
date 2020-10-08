package org.digitalcampus.oppia.model;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class TagRepositoryTest {

    @Test
    public void refreshTagListTest(){
        String tagListString = "{\"tags\": [" +
                "{\"count\": 2, \"description\": \"\", \"highlight\": true, \"icon\": \"myicon.jpg\", \"id\": 1, \"name\": \"tag1\", \"order_priority\": 0 }," +
                "{\"count\": 2, \"description\": null, \"highlight\": true, \"icon\": null, \"id\": 2, \"name\": \"nulldesc\", \"order_priority\": 0 }," +
                "{\"count\": 2, \"highlight\": true, \"icon\": null, \"id\": 3, \"name\": \"nodesc\", \"order_priority\": 0 }," +
                "{\"count\": 2, \"description\": null, \"highlight\": true, \"icon\": null, \"id\": 4, \"name\": \"nullicon\", \"order_priority\": 0 }," +
                "{\"count\": 2, \"description\": null, \"highlight\": true, \"id\": 5, \"name\": \"noicon\", \"order_priority\": 0 }," +
                "{\"count\": 2, \"description\": null, \"highlight\": null, \"icon\": null, \"id\": 6, \"name\": \"nullhighlight\", \"order_priority\": 0 }," +
                "{\"count\": 2, \"description\": null, \"id\": 7, \"name\": \"nohighlight\", \"order_priority\": 0 }," +
                "{\"count\": 2, \"description\": null, \"highlight\": null, \"icon\": null, \"id\": 8, \"name\": \"nullpriority\", \"order_priority\": null }," +
                "{\"count\": 2, \"description\": null, \"id\": 9, \"name\": \"nopriority\"}" +
            "]}";

        TagRepository tr = new TagRepository();
        List<Tag> tagList = new ArrayList<>();

        try {
            JSONObject o = new JSONObject(tagListString);
            tr.refreshTagList(tagList, o);
        } catch (JSONException jsone){
            // pass
            assertEquals(-1, tagList.size());
        }

        assertEquals(9, tagList.size());


    }
}
