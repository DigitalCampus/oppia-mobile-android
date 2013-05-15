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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.listener.GetPointsListener;
import org.digitalcampus.mobile.learning.utils.HTTPConnectionUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class GetPointsTask extends AsyncTask<Payload, Object, Payload>{
	
	public static final String TAG = GetPointsTask.class.getSimpleName();
	protected Context ctx;
	private SharedPreferences prefs;
	private GetPointsListener mStateListener;
	
	public GetPointsTask(Context ctx) {
		this.ctx = ctx;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}
	
	@Override
	protected Payload doInBackground(Payload... params){
		
		Payload payload = params[0];
		String responseStr = "";
		
		HTTPConnectionUtils client = new HTTPConnectionUtils(ctx);

		// add post params
		List<NameValuePair> pairs = new LinkedList<NameValuePair>();
		pairs.add(new BasicNameValuePair("username", prefs.getString("prefUsername", "")));
		pairs.add(new BasicNameValuePair("api_key", prefs.getString("prefApiKey", "")));
		pairs.add(new BasicNameValuePair("format", "json"));
		
		String url = prefs.getString("prefServer", ctx.getString(R.string.prefServerDefault)) + MobileLearning.SERVER_POINTS_PATH;
		String paramString = URLEncodedUtils.format(pairs, "utf-8");
		if(!url.endsWith("?"))
	        url += "?";
		url += paramString;
		
		HttpGet httpGet = new HttpGet(url);
		try {
			
			// make request
			HttpResponse response = client.execute(httpGet);
		
			// read response
			InputStream content = response.getEntity().getContent();
			BufferedReader buffer = new BufferedReader(new InputStreamReader(content), 1024);
			String s = "";
			while ((s = buffer.readLine()) != null) {
				responseStr += s;
			}
			
			switch (response.getStatusLine().getStatusCode()){
				case 400: // unauthorised
					payload.result = false;
					payload.resultResponse = ctx.getString(R.string.error_login);
					break;
				case 200: 
					payload.result = true;
					payload.resultResponse = responseStr;
					break;
				default:
					payload.result = false;
					payload.resultResponse = ctx.getString(R.string.error_connection);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			payload.resultResponse = ctx.getString(R.string.error_connection);
		}
		return payload;
	}
	
	@Override
	protected void onPostExecute(Payload response) {
		synchronized (this) {
            if (mStateListener != null) {
               mStateListener.pointsComplete(response);
            }
        }
	}
	
	public void setGetPointsListener(GetPointsListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }

}
