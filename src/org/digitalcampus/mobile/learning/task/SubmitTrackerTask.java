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

package org.digitalcampus.mobile.learning.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.application.DbHelper;
import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.model.TrackerLog;
import org.digitalcampus.mobile.learning.utils.HTTPConnectionUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

public class SubmitTrackerTask extends AsyncTask<Payload, Object, Payload> {

	public final static String TAG = SubmitTrackerTask.class.getSimpleName();
	public final static int SUBMIT_LOG_TASK = 1001;

	private Context ctx;
	private SharedPreferences prefs;

	public SubmitTrackerTask(Context c) {
		this.ctx = c;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	@Override
	protected Payload doInBackground(Payload... params) {
		DbHelper db = new DbHelper(ctx);
		Payload payload = db.getUnsentLog();
		db.close();
		
		for (Object o : payload.data) {
			TrackerLog l = (TrackerLog) o;
			HTTPConnectionUtils client = new HTTPConnectionUtils(ctx);
			try {
				String url = prefs.getString("prefServer", ctx.getString(R.string.prefServerDefault)) + MobileLearning.TRACKER_PATH;
				// add url params
				List<NameValuePair> pairs = new LinkedList<NameValuePair>();
				pairs.add(new BasicNameValuePair("username", prefs.getString("prefUsername", "")));
				pairs.add(new BasicNameValuePair("api_key", prefs.getString("prefApiKey", "")));
				pairs.add(new BasicNameValuePair("format", "json"));
				String paramString = URLEncodedUtils.format(pairs, "utf-8");
				if(!url.endsWith("?"))
			        url += "?";
				url += paramString;
				
				HttpPost httpPost = new HttpPost(url);
				
				StringEntity se = new StringEntity(l.getContent());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPost.setEntity(se);
				
                // make request
				HttpResponse response = client.execute(httpPost);				
				
				InputStream content = response.getEntity().getContent();
				BufferedReader buffer = new BufferedReader(new InputStreamReader(content), 4096);
				String responseStr = "";
				String s = "";

				while ((s = buffer.readLine()) != null) {
					responseStr += s;
				}
				
				switch (response.getStatusLine().getStatusCode()){
					case 201: // submitted
						DbHelper dbh = new DbHelper(ctx);
						dbh.markLogSubmitted(l.getId());
						dbh.close();
						payload.result = true;
						// update points
						JSONObject jsonResp = new JSONObject(responseStr);
						Editor editor = prefs.edit();
						editor.putInt("prefPoints", jsonResp.getInt("points"));
						editor.putInt("prefBadges", jsonResp.getInt("badges"));
				    	editor.commit();
						break;
					case 404: // submitted but invalid digest - so record as submitted so doesn't keep trying
						DbHelper dbh1 = new DbHelper(ctx);
						dbh1.markLogSubmitted(l.getId());
						dbh1.close();
						payload.result = true;
						break;
					default:
						payload.result = false;
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				BugSenseHandler.sendException(e);
				e.printStackTrace();
			}
		}
		
		return null;
	}

	protected void onProgressUpdate(String... obj) {
		Log.d(TAG, obj[0]);
	}
	
	@Override
    protected void onPostExecute(Payload p) {
		// reset submittask back to null after completion - so next call can run properly
		MobileLearning app = (MobileLearning) ctx.getApplicationContext();
		app.omSubmitTrackerTask = null;
    }

}
