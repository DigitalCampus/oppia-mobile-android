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
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.application.DbHelper;
import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.model.TrackerLog;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

public class SubmitMQuizTask extends AsyncTask<Payload, Object, Payload> {

	public final static String TAG = SubmitMQuizTask.class.getSimpleName();
	public final static int SUBMIT_MQUIZ_TASK = 1002;

	private Context ctx;
	private SharedPreferences prefs;

	public SubmitMQuizTask(Context c) {
		this.ctx = c;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	@Override
	protected Payload doInBackground(Payload... params) {
		Payload payload = params[0];
		for (Object l : payload.data) {
			TrackerLog tl = (TrackerLog) l;
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(
					httpParameters,
					Integer.parseInt(prefs.getString("prefServerTimeoutConnection",
							ctx.getString(R.string.prefServerTimeoutConnection))));
			HttpConnectionParams.setSoTimeout(
					httpParameters,
					Integer.parseInt(prefs.getString("prefServerTimeoutResponse",
							ctx.getString(R.string.prefServerTimeoutResponseDefault))));
			DefaultHttpClient client = new DefaultHttpClient(httpParameters);
			try {
				String url = prefs.getString("prefServer", ctx.getString(R.string.prefServerDefault))
						+ MobileLearning.MQUIZ_SUBMIT_PATH;
				
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
			
				// update progress dialog
				//Log.d(TAG, "Sending log...." + l.id);
				//Log.d(TAG, "Sending content...." + l.content);
				
				StringEntity se = new StringEntity(tl.content);
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httpPost.setEntity(se);

                // add user agent 
                String v = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
                client.getParams().setParameter(CoreProtocolPNames.USER_AGENT, MobileLearning.USER_AGENT + v);
                
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
						db.markMQuizSubmitted(tl.id);
						db.close();
						payload.result = true;
						// update points
						JSONObject jsonResp = new JSONObject(responseStr);
						Editor editor = prefs.edit();
						editor.putInt("prefPoints", jsonResp.getInt("points"));
						editor.putInt("prefBadges", jsonResp.getInt("badges"));
				    	editor.commit();
						break;
					default:
						payload.result = false;
				}

			} catch (UnsupportedEncodingException e) {
				
				e.printStackTrace();
				publishProgress(ctx.getString(R.string.error_connection));
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				publishProgress(ctx.getString(R.string.error_connection));
			} catch (IOException e) {
				e.printStackTrace();
				publishProgress(ctx.getString(R.string.error_connection));
			} catch (NameNotFoundException e) {
				BugSenseHandler.sendException(e);
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

}
