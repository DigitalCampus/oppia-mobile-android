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
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.exception.UserNotFoundException;
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

public class LoginTask extends APIRequestTask<Payload, Object, Payload> {

	public static final String TAG = LoginTask.class.getSimpleName();

	private SubmitListener mStateListener;


    public LoginTask(Context ctx) { super(ctx); }
    public LoginTask(Context ctx, ApiEndpoint api) { super(ctx, api); }

    @Override
	protected Payload doInBackground(Payload... params) {
        Payload payload = params[0];
		User u = (User) payload.getData().get(0);
		
		// firstly try to login locally
		DbHelper db0 = DbHelper.getInstance(ctx);
		try {
			User localUser = db0.getUser(u.getUsername());

			Log.d(TAG,"logged pw: " + localUser.getPasswordEncrypted());
			Log.d(TAG,"entered pw: " + u.getPasswordEncrypted());
			
			if (SessionManager.isUserApiKeyValid(ctx, u.getUsername()) &&
                    localUser.getPasswordEncrypted().equals(u.getPasswordEncrypted())){
				payload.setResult(true);
				payload.setResultResponse(ctx.getString(R.string.login_complete));
				return payload;
			}
		} catch (UserNotFoundException unfe) {
			// Just ignore - means that user isn't already registered on the device
		}

        try {
			// update progress dialog
			publishProgress(ctx.getString(R.string.login_process));

            JSONObject json = new JSONObject();
            json.put("username", u.getUsername());
            json.put("password", u.getPassword());

            OkHttpClient client = HTTPClientUtils.getClient(ctx);
            Request request = new Request.Builder()
                    .url(apiEndpoint.getFullURL(ctx, MobileLearning.LOGIN_PATH))
                    .post(RequestBody.create(HTTPClientUtils.MEDIA_TYPE_JSON, json.toString()))
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                JSONObject jsonResp = new JSONObject(response.body().string());
                u.setApiKey(jsonResp.getString("api_key"));
                u.setFirstname(jsonResp.getString("first_name"));
                u.setLastname(jsonResp.getString("last_name"));
                try {
                    // TODO_GAMIFICATION - remove commented lines
                    //u.setPoints(jsonResp.getInt("points"));
                    u.setBadges(jsonResp.getInt("badges"));
                } catch (JSONException e){
                    //u.setPoints(0);
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
                DbHelper db = DbHelper.getInstance(ctx);
                db.addOrUpdateUser(u);
                payload.setResult(true);
                payload.setResultResponse(ctx.getString(R.string.login_complete));
            }
            else{
                if (response.code() == 400 || response.code() == 401){
                    payload.setResult(false);
                    payload.setResultResponse(ctx.getString(R.string.error_login));
                }
                else{
                    payload.setResult(false);
                    payload.setResultResponse(ctx.getString(R.string.error_connection));
                }
            }

		} catch(javax.net.ssl.SSLHandshakeException e) {
            e.printStackTrace();
            payload.setResult(false);
            payload.setResultResponse(ctx.getString(R.string.error_connection_ssl));
        }catch (UnsupportedEncodingException e) {
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (IOException e) {
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection_required));
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
	
	public void setLoginListener(SubmitListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }

}
