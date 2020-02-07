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

import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.Payload;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class TagRepository {

    public static final String TAG = TagRepository.class.getSimpleName();

    private static final String JSON_PROPERTY_TAGS = "tags";
    private static final String JSON_PROPERTY_NAME = "name";
    private static final String JSON_PROPERTY_ID = "id";
    private static final String JSON_PROPERTY_COUNT = "count";
    private static final String JSON_PROPERTY_DESCRIPTION = "description";
    private static final String JSON_PROPERTY_ICON = "icon";
    private static final String JSON_PROPERTY_HIGHLIGHT = "highlight";
    private static final String JSON_PROPERTY_ORDER_PRIORITY = "order_priority";

    public void getTagList(Context ctx){
        APIUserRequestTask task = new APIUserRequestTask(ctx);
        Payload p = new Payload(Paths.SERVER_TAG_PATH);
        task.setAPIRequestListener((APIRequestListener) ctx);
        task.execute(p);
    }

    public void refreshTagList(List<Tag> tags, JSONObject json) throws JSONException{

        for (int i = 0; i < (json.getJSONArray(JSON_PROPERTY_TAGS).length()); i++) {
            JSONObject jsonObj = (JSONObject) json.getJSONArray(JSON_PROPERTY_TAGS).get(i);
            Tag t = new Tag();
            t.setName(jsonObj.getString(JSON_PROPERTY_NAME));
            t.setId(jsonObj.getInt(JSON_PROPERTY_ID));
            t.setCount(jsonObj.getInt(JSON_PROPERTY_COUNT));
            // Description
            if (jsonObj.has(JSON_PROPERTY_DESCRIPTION) && !jsonObj.isNull(JSON_PROPERTY_DESCRIPTION)){
                t.setDescription(jsonObj.getString(JSON_PROPERTY_DESCRIPTION));
            }
            // icon
            if (jsonObj.has(JSON_PROPERTY_ICON) && !jsonObj.isNull(JSON_PROPERTY_ICON)){
                t.setIcon(jsonObj.getString(JSON_PROPERTY_ICON));
            }
            // highlight
            if (jsonObj.has(JSON_PROPERTY_HIGHLIGHT) && !jsonObj.isNull(JSON_PROPERTY_HIGHLIGHT)){
                t.setHighlight(jsonObj.getBoolean(JSON_PROPERTY_HIGHLIGHT));
            }
            // order priority
            if (jsonObj.has(JSON_PROPERTY_ORDER_PRIORITY) && !jsonObj.isNull(JSON_PROPERTY_ORDER_PRIORITY)){
                t.setOrderPriority(jsonObj.getInt(JSON_PROPERTY_ORDER_PRIORITY));
            }
            tags.add(t);
        }
    }
}
