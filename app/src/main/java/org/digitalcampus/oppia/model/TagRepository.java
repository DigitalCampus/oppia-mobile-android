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

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
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
    private static final String JSON_PROPERTY_COUNT_NEW_DOWNLOADS_ENABLED = "count_new_downloads_enabled";
    private static final String JSON_PROPERTY_COURSE_STATUSES = "course_statuses";


    public void getTagList(Context ctx, ApiEndpoint api) {

        ((AppActivity) ctx).showProgressDialog(ctx.getString(R.string.loading));

        APIUserRequestTask task = new APIUserRequestTask(ctx, api);
        String url = Paths.SERVER_TAG_PATH;
        task.setAPIRequestListener((APIRequestListener) ctx);
        task.execute(url);
    }

    public void refreshTagList(List<Tag> tags, JSONObject json, List<String> installedCoursesNames) throws JSONException {

        for (int i = 0; i < (json.getJSONArray(JSON_PROPERTY_TAGS).length()); i++) {
            JSONObject jsonObj = (JSONObject) json.getJSONArray(JSON_PROPERTY_TAGS).get(i);
            Tag t = new Tag();
            t.setName(jsonObj.getString(JSON_PROPERTY_NAME));
            t.setId(jsonObj.getInt(JSON_PROPERTY_ID));
            t.setCount(jsonObj.getInt(JSON_PROPERTY_COUNT));
            // Description
            if (jsonObj.has(JSON_PROPERTY_DESCRIPTION) && !jsonObj.isNull(JSON_PROPERTY_DESCRIPTION)) {
                t.setDescription(jsonObj.getString(JSON_PROPERTY_DESCRIPTION));
            }
            // icon
            if (jsonObj.has(JSON_PROPERTY_ICON) && !jsonObj.isNull(JSON_PROPERTY_ICON)) {
                t.setIcon(jsonObj.getString(JSON_PROPERTY_ICON));
            }
            // highlight
            if (jsonObj.has(JSON_PROPERTY_HIGHLIGHT) && !jsonObj.isNull(JSON_PROPERTY_HIGHLIGHT)) {
                t.setHighlight(jsonObj.getBoolean(JSON_PROPERTY_HIGHLIGHT));
            }
            // order priority
            if (jsonObj.has(JSON_PROPERTY_ORDER_PRIORITY) && !jsonObj.isNull(JSON_PROPERTY_ORDER_PRIORITY)) {
                t.setOrderPriority(jsonObj.getInt(JSON_PROPERTY_ORDER_PRIORITY));
            }
            // Count new downloads enabled
            if (jsonObj.has(JSON_PROPERTY_COUNT_NEW_DOWNLOADS_ENABLED) && !jsonObj.isNull(JSON_PROPERTY_COUNT_NEW_DOWNLOADS_ENABLED)) {
                t.setCountNewDownloadEnabled(jsonObj.getInt(JSON_PROPERTY_COUNT_NEW_DOWNLOADS_ENABLED));
            }

            if (jsonObj.has(JSON_PROPERTY_COURSE_STATUSES) && !jsonObj.isNull(JSON_PROPERTY_COURSE_STATUSES)) {
                t.setCountAvailable(t.getCountNewDownloadEnabled());
                JSONObject jObjCourseStatuses = jsonObj.getJSONObject(JSON_PROPERTY_COURSE_STATUSES);
                Iterator<String> keys = jObjCourseStatuses.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    String value = jObjCourseStatuses.getString(key);

                    if (isCourseInstalled(key, installedCoursesNames)) {
                        if(Course.STATUS_NEW_DOWNLOADS_DISABLED.equals(value)){
                            t.incrementCountAvailable();
                        }
                    } else {
                        if(Course.STATUS_READ_ONLY.equals(value)){
                            t.decrementCountAvailable();
                        }
                    }
                }
            } else {
                t.setCountAvailable(t.getCountNewDownloadEnabled() > -1 ? t.getCountNewDownloadEnabled() : t.getCount());
            }

            if (t.getCountAvailable() > 0) {
                tags.add(t);
            }
        }
    }

    private boolean isCourseInstalled(String name, List<String> installedCoursesNames) {
        return installedCoursesNames.contains(name);
    }
}
