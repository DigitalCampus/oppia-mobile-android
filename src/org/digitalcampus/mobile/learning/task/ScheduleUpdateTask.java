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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.application.DbHelper;
import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.listener.UpdateScheduleListener;
import org.digitalcampus.mobile.learning.model.ActivitySchedule;
import org.digitalcampus.mobile.learning.model.DownloadProgress;
import org.digitalcampus.mobile.learning.model.Module;
import org.digitalcampus.mobile.learning.utils.HTTPConnectionUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.bugsense.trace.BugSenseHandler;

public class ScheduleUpdateTask extends AsyncTask<Payload, DownloadProgress, Payload>{
	
	public final static String TAG = ScheduleUpdateTask.class.getSimpleName();
	private Context ctx;
	private SharedPreferences prefs;
	private UpdateScheduleListener uStateListener;
	
	public ScheduleUpdateTask(Context ctx) {
		this.ctx = ctx;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}
	
	@Override
	protected Payload doInBackground(Payload... params) {
		Payload payload = params[0];
		
		Module dm = (Module) payload.getData().get(0);
		DownloadProgress dp = new DownloadProgress();
		// add api_key/username params
		List<NameValuePair> pairs = new LinkedList<NameValuePair>();
		pairs.add(new BasicNameValuePair("username", prefs.getString("prefUsername", "")));
		pairs.add(new BasicNameValuePair("api_key", prefs.getString("prefApiKey", "")));
		pairs.add(new BasicNameValuePair("format", "json"));
		String paramString = URLEncodedUtils.format(pairs, "utf-8");
		
		String url = prefs.getString("prefServer",
				ctx.getString(R.string.prefServerDefault)) + dm.getScheduleURI();

		
		if(!url.endsWith("?"))
		    url += "?";
		url += paramString;
		
		dp.setProgress(0);
		dp.setMessage(ctx.getString(R.string.updating));
		publishProgress(dp);
		
		HTTPConnectionUtils client = new HTTPConnectionUtils(ctx);
		
		String responseStr = "";
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
					payload.setResult(false);
					payload.setResultResponse(ctx.getString(R.string.error_login));
					break;
				case 200: 
					payload.setResult(true);
					payload.setResultResponse("");
					JSONObject jsonObj = new JSONObject(responseStr);
					long scheduleVersion = jsonObj.getLong("version");
					DbHelper db = new DbHelper(this.ctx);
					JSONArray schedule = jsonObj.getJSONArray("activityschedule");
					ArrayList<ActivitySchedule> activitySchedule = new ArrayList<ActivitySchedule>();
					for (int i = 0; i < (schedule.length()); i++) {
						dp.setProgress((i+1)*100/schedule.length());
						publishProgress(dp);
						JSONObject acts = (JSONObject) schedule.get(i);
						ActivitySchedule as = new ActivitySchedule();
						as.setDigest(acts.getString("digest"));
						DateTime sdt = MobileLearning.DATETIME_FORMAT.parseDateTime(acts.getString("start_date"));
						DateTime edt = MobileLearning.DATETIME_FORMAT.parseDateTime(acts.getString("end_date"));
						as.setStartTime(sdt);
						as.setEndTime(edt);
						activitySchedule.add(as);
					}
					int modId = db.getModuleID(dm.getShortname());
					db.resetSchedule(modId);
					db.insertSchedule(activitySchedule);
					db.updateScheduleVersion(modId, scheduleVersion);
					db.close();
					break;
				default:
					payload.setResult(false);
					payload.setResultResponse(ctx.getString(R.string.error_connection));
			}
		
		} catch (JSONException e) {
			BugSenseHandler.sendException(e);
			e.printStackTrace();
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_processing_response));
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (IOException e) {
			e.printStackTrace();
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		}
		
		dp.setProgress(100);
		dp.setProgress(ctx.getString(R.string.update_complete));
		publishProgress(dp);
		payload.setResult(true);
		
		return payload;
	}
	
	@Override
	protected void onProgressUpdate(DownloadProgress... obj) {
		synchronized (this) {
            if (uStateListener != null) {
                uStateListener.updateProgressUpdate(obj[0]);
            }
        }
	}

	@Override
	protected void onPostExecute(Payload results) {
		synchronized (this) {
            if (uStateListener != null) {
               uStateListener.updateComplete(results);
            }
        }
	}
	
	public void setUpdateListener(UpdateScheduleListener usl) {
        synchronized (this) {
            uStateListener = usl;
        }
    }

}
