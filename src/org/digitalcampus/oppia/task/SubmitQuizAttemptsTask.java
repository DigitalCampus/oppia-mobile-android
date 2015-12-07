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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.utils.HTTPConnectionUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.splunk.mint.Mint;

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
			HTTPConnectionUtils client = new HTTPConnectionUtils(ctx);
			
			try {

				String url = client.getFullURL(MobileLearning.QUIZ_SUBMIT_PATH);
				HttpPost httpPost = new HttpPost(url);
				StringEntity se = new StringEntity(qa.getData(), "utf8");
				se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				httpPost.setEntity(se);
				httpPost.addHeader(client.getAuthHeader(qa.getUser().getUsername(),qa.getUser().getApiKey()));
				// make request
				HttpResponse response = client.execute(httpPost);
				InputStream content = response.getEntity().getContent();
				BufferedReader buffer = new BufferedReader(new InputStreamReader(content), 4096);
				String responseStr = "";
				String s = "";

				while ((s = buffer.readLine()) != null) {
					responseStr += s;
				}
				
				switch (response.getStatusLine().getStatusCode()) {
					case 201: // submitted
						JSONObject jsonResp = new JSONObject(responseStr);
						DbHelper db = new DbHelper(ctx);
						db.markQuizSubmitted(qa.getId());
						db.updateUserPoints(qa.getUser().getUsername(), jsonResp.getInt("points"));
						db.updateUserBadges(qa.getUser().getUsername(), jsonResp.getInt("badges"));
						DatabaseManager.getInstance().closeDatabase();
						payload.setResult(true);
						break;
					case 400: // bad request - so to prevent re-submitting over and
								// over just mark as submitted
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
