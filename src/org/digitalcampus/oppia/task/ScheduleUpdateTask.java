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
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.UpdateScheduleListener;
import org.digitalcampus.oppia.model.ActivitySchedule;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.utils.HTTPConnectionUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

import com.bugsense.trace.BugSenseHandler;

public class ScheduleUpdateTask extends AsyncTask<Payload, DownloadProgress, Payload>{
	
	public final static String TAG = ScheduleUpdateTask.class.getSimpleName();
	private Context ctx;
	private UpdateScheduleListener uStateListener;
	
	public ScheduleUpdateTask(Context ctx) {
		this.ctx = ctx;
	}
	
	@Override
	protected Payload doInBackground(Payload... params) {
		Payload payload = params[0];
		
		Course dm = (Course) payload.getData().get(0);
		DownloadProgress dp = new DownloadProgress();
		
		dp.setProgress(0);
		dp.setMessage(ctx.getString(R.string.updating));
		publishProgress(dp);
		
		HTTPConnectionUtils client = new HTTPConnectionUtils(ctx);
		String url = client.getFullURL(dm.getScheduleURI());
		
		String responseStr = "";
		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader(client.getAuthHeader());
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
					int modId = db.getCourseID(dm.getShortname());
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
			if(!MobileLearning.DEVELOPER_MODE){
				BugSenseHandler.sendException(e);
			} else {
				e.printStackTrace();
			}
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_processing_response));
		} catch (ClientProtocolException e) {
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (IOException e) {
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		}
		
		dp.setProgress(100);
		dp.setMessage(ctx.getString(R.string.update_complete));
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
