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
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SubmitQuizAttemptsTask extends APIRequestTask<Payload, Object, Payload> {

	public SubmitQuizAttemptsTask(Context ctx) { super(ctx); }
	public SubmitQuizAttemptsTask(Context ctx, ApiEndpoint api) { super(ctx, api); }

	@Override
	protected Payload doInBackground(Payload... params) {
		Payload payload = params[0];
        DbHelper db = DbHelper.getInstance(ctx);

		for (Object l : payload.getData()) {
			QuizAttempt qa = (QuizAttempt) l;
			try {
				Log.d(TAG, qa.getData());
                OkHttpClient client = HTTPClientUtils.getClient(ctx);
                Request request = new Request.Builder()
                        .url(apiEndpoint.getFullURL(ctx, Paths.QUIZ_SUBMIT_PATH))
                        .addHeader(HTTPClientUtils.HEADER_AUTH,
                                HTTPClientUtils.getAuthHeaderValue(qa.getUser().getUsername(), qa.getUser().getApiKey()))
                        .post(RequestBody.create(qa.getData(), HTTPClientUtils.MEDIA_TYPE_JSON))
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()){
                    JSONObject jsonResp = new JSONObject(response.body().string());

                    db.markQuizSubmitted(qa.getId());
                    db.updateUserBadges(qa.getUser().getUsername(), jsonResp.getInt("badges"));
                    payload.setResult(true);
                }
                else {
                    payload.setResult(false);
                    switch (response.code()) {
                        case 400: // bad request - so to prevent re-submitting over and
                            // over just mark as submitted
                            db.markQuizSubmitted(qa.getId());
                            break;
                        case 401:
                            SessionManager.setUserApiKeyValid(qa.getUser(), false);
                            break;
                        case 500: // server error - so to prevent re-submitting over and
                            // over just mark as submitted
                            db.markQuizSubmitted(qa.getId());
                            break;
						default:
							// do nothing
                    }
                }

			} catch (IOException e) {
				payload.setResult(false);
				publishProgress(ctx.getString(R.string.error_connection));
			} catch (JSONException e) {
				payload.setResult(false);
				Mint.logException(e);
				Log.d(TAG, "JSONException:", e);
			} 
		}
		Editor editor = prefs.edit();
		long now = System.currentTimeMillis()/1000;
		editor.putLong(PrefsActivity.PREF_TRIGGER_POINTS_REFRESH, now).apply();

		return payload;
	}

	protected void onProgressUpdate(String... obj) {
		// do nothing
	}

}
