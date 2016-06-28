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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.splunk.mint.Mint;

import org.apache.http.client.ClientProtocolException;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.RemoteApiEndpoint;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegisterTask extends APIRequestTask<Payload, Object, Payload> {

	public static final String TAG = RegisterTask.class.getSimpleName();

	private SubmitListener mStateListener;

    public RegisterTask(Context ctx) { super(ctx); }
    public RegisterTask(Context ctx, ApiEndpoint api) { super(ctx, api); }

    @Override
	protected Payload doInBackground(Payload... params) {

		Payload payload = params[0];
		User u = (User) payload.getData().get(0);

		try {
			// update progress dialog
			publishProgress(ctx.getString(R.string.register_process));
			// add post params
			JSONObject json = new JSONObject();
			json.put("username", u.getUsername());
            json.put("password", u.getPassword());
            json.put("passwordagain",u.getPasswordAgain());
            json.put("email",u.getEmail());
            json.put("firstname",u.getFirstname());
            json.put("lastname",u.getLastname());
            json.put("jobtitle",u.getJobTitle());
            json.put("organisation",u.getOrganisation());
            json.put("phoneno",u.getPhoneNo());

            OkHttpClient client = HTTPClientUtils.getClient(ctx);
            Request request = new Request.Builder()
                    .url(apiEndpoint.getFullURL(ctx, MobileLearning.REGISTER_PATH))
                    .post(RequestBody.create(HTTPClientUtils.MEDIA_TYPE_JSON, json.toString()))
                    .build();


			// make request
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                JSONObject jsonResp = new JSONObject(response.body().string());
                u.setApiKey(jsonResp.getString("api_key"));
                try {
                    u.setPoints(jsonResp.getInt("points"));
                    u.setBadges(jsonResp.getInt("badges"));
                } catch (JSONException e){
                    u.setPoints(0);
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
                    e.printStackTrace();
                }
                u.setFirstname(jsonResp.getString("first_name"));
                u.setLastname(jsonResp.getString("last_name"));

                // add or update user in db
                DbHelper db = DbHelper.getInstance(ctx);
                db.addOrUpdateUser(u);

                payload.setResult(true);
                payload.setResultResponse(ctx.getString(R.string.register_complete));
            }
            else{
                switch (response.code()) {
                    case 400:
                        payload.setResult(false);
                        payload.setResultResponse(response.body().string());
                        break;
                    default:
                        payload.setResult(false);
                        payload.setResultResponse(ctx.getString(R.string.error_connection));
                }
            }

		} catch (UnsupportedEncodingException | ClientProtocolException e) {
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (IOException e) {
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (JSONException e) {
			Mint.logException(e);
			e.printStackTrace();
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_processing_response));
		} 
		return payload;
	}

	@Override
	protected void onPostExecute(Payload response) {
		synchronized (this) {
			if (mStateListener != null) {
				mStateListener.submitComplete(response);
			}
		}
	}

	public void setRegisterListener(SubmitListener srl) {
		synchronized (this) {
			mStateListener = srl;
		}
	}
}
