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

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.CourseInstallViewAdapter;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CourseInfoTask extends APIRequestTask<Payload, Object, Payload> {

    private CourseInfoListener listener;

    public CourseInfoTask(Context ctx, ApiEndpoint api) {
        super(ctx, api);
    }

    public interface CourseInfoListener {
        void onSuccess(CourseInstallViewAdapter course);
        void onError(String error);
    }

    @Override
    protected Payload doInBackground(Payload... params) {

        Payload payload = params[0];
        String courseShortname = (String) payload.getData().get(0);
        payload.getResponseData().clear();

        try {
            String url = apiEndpoint.getFullURL(ctx, String.format(Paths.COURSE_INFO_PATH, courseShortname));
            OkHttpClient client = HTTPClientUtils.getClient(ctx);
            Request request = createRequestBuilderWithUserAuth(url).get().build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                CourseInstallViewAdapter course = parseCourse(response.body().string());
                payload.setResponseData(Collections.singletonList(course));
                payload.setResult(true);
                payload.setResultResponse(ctx.getString(R.string.reset_complete));
            } else {
                payload.setResult(false);
                if (response.code() == 400) {
                    payload.setResultResponse(ctx.getString(R.string.error_reset));
                } else if (response.code() == 404){
                    payload.setResultResponse(ctx.getString(R.string.open_digest_errors_course_not_found));
                } else {
                    payload.setResultResponse(ctx.getString(R.string.error_connection));
                }
            }

        } catch (IOException e) {
            payload.setResult(false);
            payload.setResultResponse(ctx.getString(R.string.error_connection));
        } catch (JSONException e) {
            Mint.logException(e);
            Log.d(TAG, "JSONException:", e);
            payload.setResult(false);
            payload.setResultResponse(ctx.getString(R.string.error_processing_response));
        }
        return payload;
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
    protected void onPostExecute(Payload response) {
        synchronized (this) {
            if (listener != null) {
                if (response.isResult()) {
                    listener.onSuccess((CourseInstallViewAdapter) response.getResponseData().get(0));
                } else {
                    listener.onError(response.getResultResponse());
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
