package org.digitalcampus.oppia.model;


import android.content.Context;

import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.Payload;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TagRepository {


    public void getTagList(Context ctx){
        APIUserRequestTask task = new APIUserRequestTask(ctx);
        Payload p = new Payload(MobileLearning.SERVER_TAG_PATH);
        task.setAPIRequestListener((APIRequestListener) ctx);
        task.execute(p);
    }


    public void refreshTagList(ArrayList<Tag> tags, JSONObject json) throws JSONException{

        for (int i = 0; i < (json.getJSONArray("tags").length()); i++) {
            JSONObject json_obj = (JSONObject) json.getJSONArray("tags").get(i);
            Tag t = new Tag();
            t.setName(json_obj.getString("name"));
            t.setId(json_obj.getInt("id"));
            t.setCount(json_obj.getInt("count"));
            // Description
            if (json_obj.has("description") && !json_obj.isNull("description")){
                t.setDescription(json_obj.getString("description"));
            }
            // icon
            if (json_obj.has("icon") && !json_obj.isNull("icon")){
                t.setIcon(json_obj.getString("icon"));
            }
            // highlight
            if (json_obj.has("highlight") && !json_obj.isNull("highlight")){
                t.setHighlight(json_obj.getBoolean("highlight"));
            }
            // order priority
            if (json_obj.has("order_priority") && !json_obj.isNull("order_priority")){
                t.setOrderPriority(json_obj.getInt("order_priority"));
            }
            tags.add(t);
        }
    }
}
