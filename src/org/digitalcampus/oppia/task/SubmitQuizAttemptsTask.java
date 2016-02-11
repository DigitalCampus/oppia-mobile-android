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
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.splunk.mint.Mint;

import org.apache.http.client.ClientProtocolException;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SubmitQuizAttemptsTask extends AsyncTask<Payload, Object, Payload> {

	public final static String TAG = SubmitQuizAttemptsTask.class.getSimpleName();
	private Context ctx;
	private SharedPreferences prefs;
	
	public SubmitQuizAttemptsTask(Context c) {
		this.ctx = c;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	@Override
	protected Payload doInBackground(Payload... params) {
		Payload payload = params[0];
		for (Object l : payload.getData()) {
			QuizAttempt qa = (QuizAttempt) l;
			try {
                OkHttpClient client = HTTPClientUtils.getClient(ctx);
                Request request = new Request.Builder()
                        .url(HTTPClientUtils.getFullURL(ctx, MobileLearning.QUIZ_SUBMIT_PATH))
                        .addHeader(HTTPClientUtils.HEADER_AUTH,
                                HTTPClientUtils.getAuthHeaderValue(qa.getUser().getUsername(), qa.getUser().getApiKey()))
                        .post(RequestBody.create(HTTPClientUtils.MEDIA_TYPE_JSON, qa.getData()))
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()){
                    JSONObject jsonResp = new JSONObject(response.body().string());
                    DbHelper db = new DbHelper(ctx);
                    db.markQuizSubmitted(qa.getId());
                    db.updateUserPoints(qa.getUser().getUsername(), jsonResp.getInt("points"));
                    db.updateUserBadges(qa.getUser().getUsername(), jsonResp.getInt("badges"));
                    DatabaseManager.getInstance().closeDatabase();
                    payload.setResult(true);
                }
                else {
                    switch (response.code()) {
                        case 400: // bad request - so to prevent re-submitting over and
                        case 401: // over just mark as submitted
                            DbHelper db2 = new DbHelper(ctx);
                            db2.markQuizSubmitted(qa.getId());
                            DatabaseManager.getInstance().closeDatabase();
                            payload.setResult(false);
                            break;

                        case 500: // server error - so to prevent re-submitting over and
                            // over just mark as submitted
                            DbHelper db3 = new DbHelper(ctx);
                            db3.markQuizSubmitted(qa.getId());
                            DatabaseManager.getInstance().closeDatabase();
                            payload.setResult(false);
                            break;
                        default:
                            payload.setResult(false);
                    }
                }

			} catch (UnsupportedEncodingException e) {
				payload.setResult(false);
				publishProgress(ctx.getString(R.string.error_connection));
			} catch (ClientProtocolException e) {
				payload.setResult(false);
				publishProgress(ctx.getString(R.string.error_connection));
			} catch (IOException e) {
				payload.setResult(false);
				publishProgress(ctx.getString(R.string.error_connection));
			} catch (JSONException e) {
				payload.setResult(false);
				Mint.logException(e);
				e.printStackTrace();
			} 
		}
		Editor editor = prefs.edit();
		long now = System.currentTimeMillis()/1000;
		editor.putLong(PrefsActivity.PREF_TRIGGER_POINTS_REFRESH, now);
		editor.commit();
		return payload;
	}

	protected void onProgressUpdate(String... obj) {
		
	}

	@Override
	protected void onPostExecute(Payload p) {
		// reset submittask back to null after completion - so next call can run
		// properly
		MobileLearning app = (MobileLearning) ctx.getApplicationContext();
		app.omSubmitQuizAttemptsTask = null;
	}

}
