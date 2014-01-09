/* 
 * This file is part of OppiaMobile - http://oppia-mobile.org/
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
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.model.TrackerLog;
import org.digitalcampus.oppia.utils.HTTPConnectionUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

public class SubmitQuizTask extends AsyncTask<Payload, Object, Payload> {

	public final static String TAG = SubmitQuizTask.class.getSimpleName();
	private Context ctx;
	private SharedPreferences prefs;

	public SubmitQuizTask(Context c) {
		this.ctx = c;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	@Override
	protected Payload doInBackground(Payload... params) {
		Payload payload = params[0];
		for (Object l : payload.getData()) {
			TrackerLog tl = (TrackerLog) l;
			HTTPConnectionUtils client = new HTTPConnectionUtils(ctx);
			try {

				String url = client.getFullURL(MobileLearning.QUIZ_SUBMIT_PATH);
				HttpPost httpPost = new HttpPost(url);
				Log.d(TAG, url);
				StringEntity se = new StringEntity(tl.getContent(), "utf8");
				Log.d(TAG, tl.getContent());
				se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
				httpPost.setEntity(se);
				httpPost.addHeader(client.getAuthHeader());
				// make request
				HttpResponse response = client.execute(httpPost);
				InputStream content = response.getEntity().getContent();
				BufferedReader buffer = new BufferedReader(new InputStreamReader(content), 4096);
				String responseStr = "";
				String s = "";

				while ((s = buffer.readLine()) != null) {
					responseStr += s;
				}
				Log.d(TAG, responseStr);

				switch (response.getStatusLine().getStatusCode()) {
				case 201: // submitted
					DbHelper db = new DbHelper(ctx);
					db.markQuizSubmitted(tl.getId());
					db.close();
					payload.setResult(true);
					// update points
					JSONObject jsonResp = new JSONObject(responseStr);
					Editor editor = prefs.edit();
					editor.putInt(ctx.getString(R.string.prefs_points), jsonResp.getInt("points"));
					editor.putInt(ctx.getString(R.string.prefs_badges), jsonResp.getInt("badges"));
					editor.commit();
					break;
				case 400: // bad request - so to prevent re-submitting over and
							// over
							// just mark as submitted
					DbHelper dba = new DbHelper(ctx);
					dba.markQuizSubmitted(tl.getId());
					dba.close();
					payload.setResult(false);
					break;
				case 500: // bad request - so to prevent re-submitting over and
							// over
					// just mark as submitted
					DbHelper dbb = new DbHelper(ctx);
					dbb.markQuizSubmitted(tl.getId());
					dbb.close();
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
				if (!MobileLearning.DEVELOPER_MODE) {
					BugSenseHandler.sendException(e);
				} else {
					e.printStackTrace();
				}
			}

		}

		return payload;
	}

	protected void onProgressUpdate(String... obj) {
		Log.d(TAG, obj[0]);
	}

	@Override
	protected void onPostExecute(Payload p) {
		// reset submittask back to null after completion - so next call can run
		// properly
		MobileLearning app = (MobileLearning) ctx.getApplicationContext();
		app.omSubmitQuizTask = null;
	}

}
