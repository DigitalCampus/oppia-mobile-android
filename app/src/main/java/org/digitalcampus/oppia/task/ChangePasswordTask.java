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

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.CustomField;
import org.digitalcampus.oppia.model.CustomValue;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.task.result.EntityResult;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChangePasswordTask extends APIRequestTask<String, String, BasicResult> {

    private class UserSubmitError extends User {}

    private ResponseListener responseListener;

    public ChangePasswordTask(Context ctx, ApiEndpoint api) {
        super(ctx, api);
    }

    public interface ResponseListener {
        void onSuccess();

        void onError(String error);
    }

    @Override
    protected BasicResult doInBackground(String... params) {

        String password1 = params[0];
        String password2 = params[1];

        BasicResult result = new BasicResult();
        try {
            // add post params
            JSONObject json = new JSONObject();
            json.put("new_password1", password1);
            json.put("new_password2", password2);

            OkHttpClient client = HTTPClientUtils.getClient(ctx);
            Request request = createRequestBuilderWithUserAuth(apiEndpoint.getFullURL(ctx, Paths.CHANGE_PASSWORD_PATH))
                    .post(RequestBody.create(json.toString(), HTTPClientUtils.MEDIA_TYPE_JSON))
                    .build();

            // make request
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                updateLocalPassword(password1);
                result.setSuccess(true);
            } else if (response.code() == 400) {
                String bodyResponse = response.body().string();
                result.setSuccess(false);
                JSONObject jsonObjectError = new JSONObject(bodyResponse);
                result.setResultMessage(jsonObjectError.toString(4));
                Log.d(TAG, bodyResponse);
            } else {
                result.setSuccess(false);
                result.setResultMessage(ctx.getString(R.string.error_connection));
            }

        } catch (javax.net.ssl.SSLHandshakeException e) {
            Log.d(TAG, "SSLHandshakeException:", e);
            Analytics.logException(e);
            result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.error_connection_ssl));
        } catch (UnsupportedEncodingException e) {
            result.setSuccess(false);

            result.setResultMessage(ctx.getString(R.string.error_connection));
        } catch (IOException e) {
            result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.error_connection_required));
        } catch (JSONException e) {
            Analytics.logException(e);
            Log.d(TAG, "JSONException:", e);
            result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.error_processing_response));
        }

        return result;
    }

    private void updateLocalPassword(String password) {
        long userId = SessionManager.getUserId(ctx);
        User user = new User();
        user.setPassword(password);
        DbHelper.getInstance(ctx).changeUserPassword(userId, user.getPasswordEncrypted());
    }


    @Override
    protected void onPostExecute(BasicResult result) {

        synchronized (this) {
            if (responseListener != null) {

                if (result.isSuccess()) {
                    responseListener.onSuccess();
                } else {
                    responseListener.onError(result.getResultMessage());
                }

            }
        }
    }


    public void setResponseListener(ResponseListener srl) {
        synchronized (this) {
            responseListener = srl;
        }
    }
}
