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
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.listener.SubmitEntityListener;
import org.digitalcampus.oppia.model.CustomField;
import org.digitalcampus.oppia.model.CustomValue;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.result.EntityResult;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginTask extends APIRequestTask<User, Object, EntityResult<User>> {

	private SubmitEntityListener mStateListener;

    public LoginTask(Context ctx, ApiEndpoint api) { super(ctx, api); }

    @Override
	protected EntityResult<User> doInBackground(User... params) {

		User user = params[0];

        EntityResult<User> result = new EntityResult<>();
        result.setEntity(user);
		
		// firstly try to login locally
		try {
			User localUser = DbHelper.getInstance(ctx).getUser(user.getUsername());

			Log.d(TAG,"logged pw: " + localUser.getPasswordEncrypted());
			Log.d(TAG,"entered pw: " + user.getPasswordEncrypted());
			
			if (SessionManager.isUserApiKeyValid(user.getUsername()) &&
                    localUser.getPasswordEncrypted().equals(user.getPasswordEncrypted())){
				result.setSuccess(true);
				result.setResultMessage(ctx.getString(R.string.login_complete));
				return result;
			}
		} catch (UserNotFoundException unfe) {
			// Just ignore - means that user isn't already registered on the device
		}

        try {
			// update progress dialog
			publishProgress(ctx.getString(R.string.login_process));
            JSONObject json = new JSONObject();
            json.put("username", user.getUsername());
            json.put("password", user.getPassword());

            OkHttpClient client = HTTPClientUtils.getClient(ctx);
            Request request = new Request.Builder()
                    .url(apiEndpoint.getFullURL(ctx, Paths.LOGIN_PATH))
                    .post(RequestBody.create(json.toString(), HTTPClientUtils.MEDIA_TYPE_JSON))
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                JSONObject jsonResp = new JSONObject(response.body().string());
                setUserFields(jsonResp, user);
                setCustomFields(jsonResp, user);
                setPointsAndBadges(jsonResp, user);
                setPointsAndBadgesEnabled(jsonResp, user);
                setMetaData(jsonResp);
                DbHelper.getInstance(ctx).addOrUpdateUser(user);
                result.setSuccess(true);
                result.setResultMessage(ctx.getString(R.string.login_complete));
            }
            else{
                if (response.code() == 400 || response.code() == 401){
                    result.setSuccess(false);
                    result.setResultMessage(ctx.getString(R.string.error_login));
                }
                else{
                    result.setSuccess(false);
                    result.setResultMessage(ctx.getString(R.string.error_connection));
                }
            }

		} catch(javax.net.ssl.SSLHandshakeException e) {
            Log.d(TAG, "SSLHandshakeException: ", e);
            result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.error_connection_ssl));
        }catch (UnsupportedEncodingException e) {
            result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.error_connection));
		} catch (IOException e) {
            result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.error_connection_required));
		} catch (JSONException e) {
			Analytics.logException(e);
            Log.d(TAG, "JSONException: ", e);
            result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.error_processing_response));
		} 
		
		return result;
	}

	private void setUserFields(JSONObject json, User u) throws JSONException {
        u.setApiKey(json.getString("api_key"));
        u.setFirstname(json.getString("first_name"));
        u.setLastname(json.getString("last_name"));
        if (json.has("email")){
            u.setEmail(json.getString("email"));
        }
        if (json.has("organisation")){
            u.setOrganisation(json.getString("organisation"));
        }
        if (json.has("job_title")){
            u.setJobTitle(json.getString("job_title"));
        }
    }

    private void setCustomFields(JSONObject json, User u) throws JSONException {
        List<CustomField> cFields = DbHelper.getInstance(ctx).getCustomFields();
        for (CustomField field : cFields){
            String key = field.getKey();
            if (json.has(key)){
                if (field.isString()){
                    String value = json.getString(key);
                    u.putCustomField(key, new CustomValue<>(value));
                }
                else if (field.isBoolean()){
                    boolean value = json.getBoolean(key);
                    u.putCustomField(key, new CustomValue<>(value));
                }
                else if (field.isInteger()){
                    int value = json.getInt(key);
                    u.putCustomField(key, new CustomValue<>(value));
                }
                else if (field.isFloat()){
                    float value = (float) json.getDouble(key);
                    u.putCustomField(key, new CustomValue<>(value));
                }
            }
        }
    }

	private void setPointsAndBadges(JSONObject jsonResp, User u){
        try {
            u.setPoints(jsonResp.getInt("points"));
            u.setBadges(jsonResp.getInt("badges"));
        } catch (JSONException e){
            u.setPoints(0);
            u.setBadges(0);
        }
    }

    private void setPointsAndBadgesEnabled(JSONObject jsonResp, User u){
        try {
            u.setScoringEnabled(jsonResp.getBoolean("scoring"));
            u.setBadgingEnabled(jsonResp.getBoolean("badging"));
        } catch (JSONException e){
            u.setScoringEnabled(true);
            u.setBadgingEnabled(true);
        }
    }

    private void setMetaData(JSONObject jsonResp){
        try {
            JSONObject metadata = jsonResp.getJSONObject("metadata");
            MetaDataUtils mu = new MetaDataUtils(ctx);
            mu.saveMetaData(metadata, prefs);
        } catch (JSONException e) {
            Analytics.logException(e);
            Log.d(TAG, "JSONException: ", e);
        }
    }

	@Override
	protected void onPostExecute(EntityResult<User> result) {
		synchronized (this) {
            if (mStateListener != null) {
               mStateListener.submitComplete(result);
            }
        }
	}
	
	public void setLoginListener(SubmitEntityListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }

}
