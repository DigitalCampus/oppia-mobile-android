/*
 * This file is part of OppiaMobile - https://digital-campus.org/
 *
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

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

    public final static String TAG = TagRepository.class.getSimpleName();

    private final static String JSON_PROPERTY_TAGS = "tags";
    private final static String JSON_PROPERTY_NAME = "name";
    private final static String JSON_PROPERTY_ID = "id";
    private final static String JSON_PROPERTY_COUNT = "count";
    private final static String JSON_PROPERTY_DESCRIPTION = "description";
    private final static String JSON_PROPERTY_ICON = "icon";
    private final static String JSON_PROPERTY_HIGHLIGHT = "highlight";
    private final static String JSON_PROPERTY_ORDER_PRIORITY = "order_priority";

    public void getTagList(Context ctx){
        APIUserRequestTask task = new APIUserRequestTask(ctx);
        Payload p = new Payload(MobileLearning.SERVER_TAG_PATH);
        task.setAPIRequestListener((APIRequestListener) ctx);
        task.execute(p);
    }

    public void refreshTagList(ArrayList<Tag> tags, JSONObject json) throws JSONException{

        for (int i = 0; i < (json.getJSONArray(JSON_PROPERTY_TAGS).length()); i++) {
            JSONObject json_obj = (JSONObject) json.getJSONArray(JSON_PROPERTY_TAGS).get(i);
            Tag t = new Tag();
            t.setName(json_obj.getString(JSON_PROPERTY_NAME));
            t.setId(json_obj.getInt(JSON_PROPERTY_ID));
            t.setCount(json_obj.getInt(JSON_PROPERTY_COUNT));
            // Description
            if (json_obj.has(JSON_PROPERTY_DESCRIPTION) && !json_obj.isNull(JSON_PROPERTY_DESCRIPTION)){
                t.setDescription(json_obj.getString(JSON_PROPERTY_DESCRIPTION));
            }
            // icon
            if (json_obj.has(JSON_PROPERTY_ICON) && !json_obj.isNull(JSON_PROPERTY_ICON)){
                t.setIcon(json_obj.getString(JSON_PROPERTY_ICON));
            }
            // highlight
            if (json_obj.has(JSON_PROPERTY_HIGHLIGHT) && !json_obj.isNull(JSON_PROPERTY_HIGHLIGHT)){
                t.setHighlight(json_obj.getBoolean(JSON_PROPERTY_HIGHLIGHT));
            }
            // order priority
            if (json_obj.has(JSON_PROPERTY_ORDER_PRIORITY) && !json_obj.isNull(JSON_PROPERTY_ORDER_PRIORITY)){
                t.setOrderPriority(json_obj.getInt(JSON_PROPERTY_ORDER_PRIORITY));
            }
            tags.add(t);
        }
    }
}
