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

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
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

public class SubmitMQuizTask extends AsyncTask<Payload, Object, Payload> {

	public final static String TAG = SubmitMQuizTask.class.getSimpleName();
	private Context ctx;
	private SharedPreferences prefs;

	public SubmitMQuizTask(Context c) {
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
				
				String url = HTTPConnectionUtils.createUrlWithCredentials(ctx, prefs, MobileLearning.MQUIZ_SUBMIT_PATH,true);
				HttpPost httpPost = new HttpPost(url);
				
				StringEntity se = new StringEntity(tl.getContent());
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
						DbHelper db = new DbHelper(ctx);
						db.markMQuizSubmitted(tl.getId());
						db.close();
						payload.setResult(true);
						// update points
						JSONObject jsonResp = new JSONObject(responseStr);
						Editor editor = prefs.edit();
						editor.putInt("prefPoints", jsonResp.getInt("points"));
						editor.putInt("prefBadges", jsonResp.getInt("badges"));
				    	editor.commit();
						break;
					default:
						payload.setResult(false);
				}

			} catch (UnsupportedEncodingException e) {
				payload.setResult(false);
				e.printStackTrace();
				publishProgress(ctx.getString(R.string.error_connection));
			} catch (ClientProtocolException e) {
				payload.setResult(false);
				e.printStackTrace();
				publishProgress(ctx.getString(R.string.error_connection));
			} catch (IOException e) {
				payload.setResult(false);
				e.printStackTrace();
				publishProgress(ctx.getString(R.string.error_connection));
			} catch (JSONException e) {
				payload.setResult(false);
				BugSenseHandler.sendException(e);
				e.printStackTrace();
			} 
			
		}
		
		return payload;
	}
	
	protected void onProgressUpdate(String... obj) {
		Log.d(TAG, obj[0]);
	}

}
