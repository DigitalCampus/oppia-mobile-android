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

package org.digitalcampus.oppia.task;

import android.content.Context;
import android.util.Log;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.CourseInstallViewAdapter;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.task.result.EntityResult;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CourseInfoTask extends APIRequestTask<String, Void, EntityResult<CourseInstallViewAdapter>> {

    private CourseInfoListener listener;

    public CourseInfoTask(Context ctx, ApiEndpoint api) {
        super(ctx, api);
    }

    public interface CourseInfoListener {
        void onSuccess(CourseInstallViewAdapter course);
        void onError(String error);
    }

    @Override
    protected EntityResult<CourseInstallViewAdapter> doInBackground(String... params) {

        EntityResult<CourseInstallViewAdapter> result = new EntityResult<>();
        String courseShortname = params[0];

        try {
            String url = apiEndpoint.getFullURL(ctx, String.format(Paths.COURSE_INFO_PATH, courseShortname));
            OkHttpClient client = HTTPClientUtils.getClient(ctx);
            Request request = createRequestBuilderWithUserAuth(url).get().build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                CourseInstallViewAdapter course = parseCourse(response.body().string());
                result.setEntity(course);
                result.setSuccess(true);
                result.setResultMessage(ctx.getString(R.string.reset_password_complete));
            } else {
                result.setSuccess(false);
                if (response.code() == 400) {
                    result.setResultMessage(ctx.getString(R.string.error_reset_password));
                } else if (response.code() == 404){
                    result.setResultMessage(ctx.getString(R.string.open_digest_errors_course_not_found));
                } else {
                    result.setResultMessage(ctx.getString(R.string.error_connection));
                }
            }

        } catch (IOException e) {
            result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.error_connection));
        } catch (JSONException e) {
            Analytics.logException(e);
            Log.d(TAG, "JSONException:", e);
            result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.error_processing_response));
        }
        return result;
    }

    private CourseInstallViewAdapter parseCourse(String courseJsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(courseJsonString);
        CourseInstallViewAdapter course = new CourseInstallViewAdapter("");
        course.setCourseId(jsonObject.getInt("id"));
        course.setShortname(jsonObject.getString("shortname"));
        course.setTitlesFromJSONObjectMap(jsonObject.getJSONObject("title"));
        course.setDescriptionsFromJSONObjectMap(jsonObject.getJSONObject("description"));
        course.setDraft(jsonObject.getBoolean("is_draft"));
        course.setVersionId(jsonObject.getDouble("version"));
        course.setDownloadUrl(jsonObject.getString("url"));
        course.setAuthorName(jsonObject.getString("author"));
        return course;

    }

    @Override
    protected void onPostExecute(EntityResult<CourseInstallViewAdapter> result) {
        synchronized (this) {
            if (listener != null) {
                if (result.isSuccess()) {
                    listener.onSuccess(result.getEntity());
                } else {
                    listener.onError(result.getResultMessage());
                }
            }
        }
    }

    public void setListener(CourseInfoListener listener) {
        synchronized (this) {
            this.listener = listener;
        }
    }
}
