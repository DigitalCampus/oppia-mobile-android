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
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.CustomField;
import org.digitalcampus.oppia.model.CustomValue;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.digitalcampus.oppia.utils.MetaDataUtils;
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

public class RegisterTask extends APIRequestTask<Payload, Object, Payload> {

	private static final String SUBMIT_ERROR = "submit_error";

	private RegisterListener mStateListener;

    public RegisterTask(Context ctx) { super(ctx); }
    public RegisterTask(Context ctx, ApiEndpoint api) { super(ctx, api); }

    public interface RegisterListener{
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
        if (!user.isOfflineRegister()){
            saveUser = submitUserToServer(user, payload, true);
        }

        if (saveUser){
            DbHelper db = DbHelper.getInstance(ctx);
            boolean usernameExists = db.isUser(user.getUsername()) != -1;
            if (!usernameExists) {
                // add or update user in db
                db.addOrUpdateUser(user);
                payload.setResult(true);
                payload.setResultResponse(ctx.getString(R.string.register_complete));
            } else {
                payload.setResult(false);
                payload.setResultResponseDataError(SUBMIT_ERROR, ctx.getString(R.string.register_username_exists));
            }
        }

		return payload;
	}


	public boolean submitUserToServer(User u, Payload payload, boolean updateProgress){
        try {
            if (updateProgress){
                // update progress dialog
                publishProgress(ctx.getString(R.string.register_process));
            }

            // add post params
            JSONObject json = new JSONObject();
            json.put("username", u.getUsername());
            json.put("password", u.getPassword());
            json.put("passwordagain", u.getPasswordAgain());

            json.put("firstname", u.getFirstname());
            json.put("lastname", u.getLastname());
            json.put("jobtitle", u.getJobTitle());
            json.put("organisation", u.getOrganisation());
            json.put("phoneno", u.getPhoneNo());

            List<CustomField> cFields = DbHelper.getInstance(ctx).getCustomFields();
            for (CustomField field : cFields){
                CustomValue value = u.getCustomField(field.getKey());
                if (value != null){
                    json.put(field.getKey(), value.getValue());
                }
            }

            OkHttpClient client = HTTPClientUtils.getClient(ctx);
            Request request = new Request.Builder()
                    .url(apiEndpoint.getFullURL(ctx, Paths.REGISTER_PATH))
                    .post(RequestBody.create(HTTPClientUtils.MEDIA_TYPE_JSON, json.toString()))
                    .build();

            // make request
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                JSONObject jsonResp = new JSONObject(response.body().string());
                u.setApiKey(jsonResp.getString("api_key"));
                u.setOfflineRegister(false);
                try {
                    u.setBadges(jsonResp.getInt("badges"));
                } catch (JSONException e){
                    u.setBadges(0);
                }

                try {
                    u.setScoringEnabled(jsonResp.getBoolean("scoring"));
                    u.setBadgingEnabled(jsonResp.getBoolean("badging"));
                } catch (JSONException e){
                    u.setScoringEnabled(true);
                    u.setBadgingEnabled(true);
                }

                try {
                    JSONObject metadata = jsonResp.getJSONObject("metadata");
                    MetaDataUtils mu = new MetaDataUtils(ctx);
                    mu.saveMetaData(metadata, prefs);
                } catch (JSONException e) {
                    Log.d(TAG, "JSONException:", e);
                    Mint.logException(e);
                }

                u.setFirstname(jsonResp.getString("first_name"));
                u.setLastname(jsonResp.getString("last_name"));
                return true;
            }
            else{
                switch (response.code()) {
                    case 400:
                        String bodyResponse = response.body().string();
                        payload.setResult(false);
                        payload.setResponseData(new ArrayList<Object>(Arrays.asList(SUBMIT_ERROR)));
                        payload.setResultResponse(bodyResponse);
                        Log.d(TAG, bodyResponse);
                        break;
                    default:
                        payload.setResult(false);
                        payload.setResultResponse(ctx.getString(R.string.error_connection));
                }
            }

        } catch(javax.net.ssl.SSLHandshakeException e) {
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
		synchronized (this) {
			if (mStateListener != null) {
                User user = (User) response.getData().get(0);
			    if (response.isResult()){
			        mStateListener.onSubmitComplete(user);
                    return;
			    }

                String errorMessage = response.getResultResponse();
			    if ((response.getResponseData() != null) && (response.getResponseData().size()>0)){
			        String data = (String) response.getResponseData().get(0);
			        if (data.equals(SUBMIT_ERROR)){
                        try {
                            JSONObject jo = new JSONObject(errorMessage);
                            errorMessage = jo.getString("error");
                        } catch (JSONException je) {
                            Log.d(TAG, je.getMessage());
                        }
			            mStateListener.onSubmitError(errorMessage);
                        return;
                    }
                }

				mStateListener.onConnectionError(errorMessage, user);
			}
		}
	}

	public void setRegisterListener(RegisterListener srl) {
		synchronized (this) {
			mStateListener = srl;
		}
	}
}
