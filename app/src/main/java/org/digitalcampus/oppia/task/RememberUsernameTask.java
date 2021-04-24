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
import org.digitalcampus.oppia.listener.SubmitEntityListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.result.EntityResult;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RememberUsernameTask extends APIRequestTask<User, Object, EntityResult<User>> {

	private SubmitEntityListener mStateListener;

	public RememberUsernameTask(Context ctx, ApiEndpoint api) { super(ctx, api); }

    @Override
	protected EntityResult<User> doInBackground(User... params) {

		User user = params[0];

		EntityResult<User> result = new EntityResult<>();
		result.setEntity(user);

		try {

			JSONObject json = new JSONObject();
			json.put("email", user.getEmail());

            OkHttpClient client = HTTPClientUtils.getClient(ctx);
            Request request = new Request.Builder()
                    .url(apiEndpoint.getFullURL(ctx, Paths.REMEMBER_USERNAME_PATH))
                    .post(RequestBody.create(json.toString(), HTTPClientUtils.MEDIA_TYPE_JSON))
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                new JSONObject(response.body().string()); //Added to check that the response is well formed
				result.setSuccess(true);
				result.setResultMessage(ctx.getString(R.string.remember_username_complete));
            }
            else{
                result.setSuccess(false);
                if (response.code() == 400){
					result.setResultMessage(ctx.getString(R.string.error_remember_username));
                }
                else{
					result.setResultMessage(ctx.getString(R.string.error_connection));
                }
            }

		} catch (IOException  e) {
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

	@Override
	protected void onPostExecute(EntityResult<User> result) {
		synchronized (this) {
			if (mStateListener != null) {
				mStateListener.submitComplete(result);
			}
		}
	}

	public void setListener(SubmitEntityListener srl) {
		synchronized (this) {
			mStateListener = srl;
		}
	}

}
