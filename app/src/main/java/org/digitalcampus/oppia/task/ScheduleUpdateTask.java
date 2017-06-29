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
import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.listener.UpdateScheduleListener;
import org.digitalcampus.oppia.model.ActivitySchedule;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ScheduleUpdateTask extends APIRequestTask<Payload, DownloadProgress, Payload>{
	
	public final static String TAG = ScheduleUpdateTask.class.getSimpleName();

	private UpdateScheduleListener uStateListener;

    public ScheduleUpdateTask(Context ctx) { super(ctx); }
    public ScheduleUpdateTask(Context ctx, ApiEndpoint api) { super(ctx, api); }

    @Override
	protected Payload doInBackground(Payload... params) {
		Payload payload = params[0];
		
		Course dm = (Course) payload.getData().get(0);
		DownloadProgress dp = new DownloadProgress();
		
		dp.setProgress(0);
		dp.setMessage(ctx.getString(R.string.updating));
		publishProgress(dp);

		try {
			DbHelper db = DbHelper.getInstance(ctx);
        	User user = db.getUser(SessionManager.getUsername(ctx));

            OkHttpClient client = HTTPClientUtils.getClient(ctx);
            Request request = new Request.Builder()
                    .url(apiEndpoint.getFullURL(ctx, dm.getScheduleURI()))
                    .addHeader(HTTPClientUtils.HEADER_AUTH,
                            HTTPClientUtils.getAuthHeaderValue(user.getUsername(), user.getApiKey()))
                    .build();
			
			// make request
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                payload.setResult(true);
                payload.setResultResponse("");
                JSONObject jsonObj = new JSONObject(response.body().string());
                long scheduleVersion = jsonObj.getLong("version");
                db = DbHelper.getInstance(this.ctx);
                JSONArray schedule = jsonObj.getJSONArray("activityschedule");
                ArrayList<ActivitySchedule> activitySchedule = new ArrayList<>();
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
                int courseId = db.getCourseID(dm.getShortname());
                db.resetSchedule(courseId);
                db.insertSchedule(activitySchedule);
                db.updateScheduleVersion(courseId, scheduleVersion);
            }
            else if (response.code() == 400){
                payload.setResult(false);
                payload.setResultResponse(ctx.getString(R.string.error_login));
            }
            else{
                payload.setResult(false);
                payload.setResultResponse(ctx.getString(R.string.error_connection));
            }
		
		} catch (JSONException e) {
			Mint.logException(e);
			e.printStackTrace();
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_processing_response));
		} catch (UserNotFoundException e) {
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
