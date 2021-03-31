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
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SubmitQuizAttemptsTask extends APIRequestTask<List<QuizAttempt>, Object, BasicResult> {

	public SubmitQuizAttemptsTask(Context ctx) { super(ctx); }
	public SubmitQuizAttemptsTask(Context ctx, ApiEndpoint api) { super(ctx, api); }

	@Override
	protected BasicResult doInBackground(List<QuizAttempt>... params) {
		
        DbHelper db = DbHelper.getInstance(ctx);
        
		List<QuizAttempt> quizAttempts = params[0];
		BasicResult result = new BasicResult();
		
		for (QuizAttempt quizAttempt : quizAttempts) {
			
			try {
				Log.d(TAG, quizAttempt.getData());
                OkHttpClient client = HTTPClientUtils.getClient(ctx);
                Request request = new Request.Builder()
                        .url(apiEndpoint.getFullURL(ctx, Paths.QUIZ_SUBMIT_PATH))
                        .addHeader(HTTPClientUtils.HEADER_AUTH,
                                HTTPClientUtils.getAuthHeaderValue(quizAttempt.getUser().getUsername(), quizAttempt.getUser().getApiKey()))
                        .post(RequestBody.create(quizAttempt.getData(), HTTPClientUtils.MEDIA_TYPE_JSON))
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()){
                    JSONObject jsonResp = new JSONObject(response.body().string());

                    db.markQuizSubmitted(quizAttempt.getId());
                    db.updateUserBadges(quizAttempt.getUser().getUsername(), jsonResp.getInt("badges"));
                    result.setSuccess(true);
                }
                else {
                    result.setSuccess(false);
                    switch (response.code()) {
                        case 400: // bad request - so to prevent re-submitting over and
                            // over just mark as submitted
                            db.markQuizSubmitted(quizAttempt.getId());
                            break;
                        case 401:
                            SessionManager.setUserApiKeyValid(quizAttempt.getUser(), false);
                            break;
                        case 500: // server error - so to prevent re-submitting over and
                            // over just mark as submitted
                            db.markQuizSubmitted(quizAttempt.getId());
                            break;
						default:
							// do nothing
                    }
                }

			} catch (IOException e) {
				result.setSuccess(false);
				publishProgress(ctx.getString(R.string.error_connection));
			} catch (JSONException e) {
				result.setSuccess(false);
				Mint.logException(e);
				Log.d(TAG, "JSONException:", e);
			} 
		}
		Editor editor = prefs.edit();
		long now = System.currentTimeMillis()/1000;
		editor.putLong(PrefsActivity.PREF_TRIGGER_POINTS_REFRESH, now).apply();

		return result;
	}

	protected void onProgressUpdate(String... obj) {
		// do nothing
	}

}
