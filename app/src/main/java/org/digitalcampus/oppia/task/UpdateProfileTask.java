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

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.CustomField;
import org.digitalcampus.oppia.model.CustomValue;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpdateProfileTask extends APIRequestTask<Payload, String, Payload> {

    private static final String SUBMIT_ERROR = "submit_error";

    private ResponseListener responseListener;
    private ProgressDialog progressDialog;

    public UpdateProfileTask(Context ctx, ApiEndpoint api) {
        super(ctx, api);
    }

    public interface ResponseListener {
        void onSubmitComplete(User u);

        void onSubmitError(String error);

        void onConnectionError(String error, User u);
    }

    @Override
    protected Payload doInBackground(Payload... params) {

        Payload payload = params[0];
        User user = (User) payload.getData().get(0);
        payload.getResponseData().clear();

        boolean saveUser = true;
        if (!user.isOfflineRegister()) {
            saveUser = submitUserToServer(user, payload, true);
        }

        if (saveUser) {

            DbHelper db = DbHelper.getInstance(ctx);

            db.addOrUpdateUser(user);
            payload.setResult(true);
            payload.setResultResponse(ctx.getString(R.string.profile_updated_successfuly));

        }

        return payload;
    }


    private boolean submitUserToServer(User u, Payload payload, boolean updateProgress) {
        try {
            if (updateProgress) {
                // update progress dialog
                publishProgress(ctx.getString(R.string.loading));
            }

            // add post params
            JSONObject json = new JSONObject();
            json.put("email", u.getEmail());
            json.put("first_name", u.getFirstname());
            json.put("last_name", u.getLastname());
            json.put("job_title", u.getJobTitle());
            json.put("organisation", u.getOrganisation());

            List<CustomField> cFields = DbHelper.getInstance(ctx).getCustomFields();
            for (CustomField field : cFields){
                CustomValue value = u.getCustomField(field.getKey());
                if (value != null){
                    json.put(field.getKey(), value.getValue());
                }
            }

            OkHttpClient client = HTTPClientUtils.getClient(ctx);
            Request request = createRequestBuilderWithUserAuth(apiEndpoint.getFullURL(ctx, Paths.UPDATE_PROFILE_PATH))
                    .post(RequestBody.create(json.toString(), HTTPClientUtils.MEDIA_TYPE_JSON))
                    .build();

            // make request
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return true;
            } else if (response.code() == 400) {
                String bodyResponse = response.body().string();
                payload.setResult(false);
                payload.setResponseData(new ArrayList<>(Arrays.asList(SUBMIT_ERROR)));
                payload.setResultResponse(bodyResponse);
                Log.d(TAG, bodyResponse);
            } else {
                payload.setResult(false);
                payload.setResultResponse(ctx.getString(R.string.error_connection));
            }

        } catch (javax.net.ssl.SSLHandshakeException e) {
            Log.d(TAG, "SSLHandshakeException:", e);
            Mint.logException(e);
            payload.setResult(false);
            payload.setResultResponse(ctx.getString(R.string.error_connection_ssl));
        } catch (UnsupportedEncodingException e) {
            payload.setResult(false);

            payload.setResultResponse(ctx.getString(R.string.error_connection));
        } catch (IOException e) {
            payload.setResult(false);
            payload.setResultResponse(ctx.getString(R.string.error_connection_required));
        } catch (JSONException e) {
            Mint.logException(e);
            Log.d(TAG, "JSONException:", e);
            payload.setResult(false);
            payload.setResultResponse(ctx.getString(R.string.error_processing_response));
        }
        return false;
    }

    @Override
    protected void onPostExecute(Payload response) {

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        synchronized (this) {
            if (responseListener != null) {
                User user = (User) response.getData().get(0);
                if (response.isResult()) {
                    responseListener.onSubmitComplete(user);
                    return;
                }

                String errorMessage = response.getResultResponse();
                if (response.getResponseData() != null && !response.getResponseData().isEmpty()) {
                    String data = (String) response.getResponseData().get(0);
                    if (data.equals(SUBMIT_ERROR)) {
                        try {
                            JSONObject jo = new JSONObject(errorMessage);
                            errorMessage = jo.getString("error");
                        } catch (JSONException je) {
                            Log.d(TAG, je.getMessage());
                        }
                        responseListener.onSubmitError(errorMessage);
                        return;
                    }
                }

                responseListener.onConnectionError(errorMessage, user);
            }
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        progressDialog = ProgressDialog.show(ctx, null, values[0]);

    }

    public void setResponseListener(ResponseListener srl) {
        synchronized (this) {
            responseListener = srl;
        }
    }
}
