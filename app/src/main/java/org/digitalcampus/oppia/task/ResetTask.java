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
import android.os.AsyncTask;

import com.splunk.mint.Mint;

import org.apache.http.client.ClientProtocolException;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.listener.TaskCompleteListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResetTask extends AsyncTask<Payload, Object, Payload> {

	public static final String TAG = ResetTask.class.getSimpleName();

	private Context ctx;
	private SubmitListener mStateListener;
	private TaskCompleteListener _taskCompleteListener = null;

	public ResetTask(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	protected Payload doInBackground(Payload... params) {

		Payload payload = params[0];
		User u = (User) payload.getData().get(0);

		try {
			// update progress dialog
			publishProgress(ctx.getString(R.string.reset_process));

			JSONObject json = new JSONObject();
			json.put("username", u.getUsername());

            OkHttpClient client = HTTPClientUtils.getClient(ctx);
            Request request = new Request.Builder()
                    .url(HTTPClientUtils.getFullURL(ctx, MobileLearning.RESET_PATH))
                    .post(RequestBody.create(HTTPClientUtils.MEDIA_TYPE_JSON, json.toString()))
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                payload.setResult(true);
                payload.setResultResponse(ctx.getString(R.string.reset_complete));
            }
            else{
                payload.setResult(false);
                if (response.code() == 400){
                    payload.setResultResponse(response.toString());
                }
                else{
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

		if (this._taskCompleteListener != null) {
			this._taskCompleteListener.onComplete(response);
		}
	}

	public void setResetListener(SubmitListener srl) {
		synchronized (this) {
			mStateListener = srl;
		}
	}

	public ResetTask setTaskCompleteListener(TaskCompleteListener listener){
		this._taskCompleteListener = listener;
		return this;
	}
}
