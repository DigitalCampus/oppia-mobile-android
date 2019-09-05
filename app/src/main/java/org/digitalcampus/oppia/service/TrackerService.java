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

package org.digitalcampus.oppia.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.DownloadActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.APIRequestFinishListener;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.FetchServerInfoTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.SubmitQuizAttemptsTask;
import org.digitalcampus.oppia.task.SubmitTrackerMultipleTask;
import org.digitalcampus.oppia.utils.ConnectionUtils;
import org.digitalcampus.oppia.utils.ui.OppiaNotificationUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class TrackerService extends Service implements APIRequestListener, APIRequestFinishListener {

	public static final String TAG = TrackerService.class.getSimpleName();

	private final IBinder mBinder = new MyBinder();
	private SubmitTrackerMultipleTask omSubmitTrackerMultipleTask;
	private SubmitQuizAttemptsTask omSubmitQuizAttemptsTask;

	@Override
	public void onCreate() {
		super.onCreate();
		Mint.initAndStartSession(this,MobileLearning.MINT_API_KEY);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (ConnectionUtils.isNetworkConnected(getApplicationContext())) {

			boolean backgroundData = true;
			Bundle b = intent.getExtras();
			if (b != null) {
				backgroundData = b.getBoolean("backgroundData");
			}

			final boolean finalBackgroundData = backgroundData;
			Thread thread = new Thread(new Runnable(){
				@Override
				public void run() {
					if (finalBackgroundData){
						updateTracking();
					}
				}
			});
			thread.start();

		}
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

    @Override
    public void apiKeyInvalidated() {
        SessionManager.logoutCurrentUser(this);
    }

	@Override
	public void onRequestFinish(String idRequest) {

	}

	public class MyBinder extends Binder {
		public TrackerService getService() {
			return TrackerService.this;
		}
	}

	public void updateTracking(){

		Payload p;
		// Update server info
		FetchServerInfoTask fetchServerInfoTask = new FetchServerInfoTask(this);
		fetchServerInfoTask.setAPIRequestFinishListener(this, null);
		fetchServerInfoTask.execute();

		// check for updated courses
		// should only do this once a day or so....
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		long lastRun = prefs.getLong("lastCourseUpdateCheck", 0);
		long now = System.currentTimeMillis()/1000;
		if((lastRun + (3600*12)) < now){
			APIUserRequestTask task = new APIUserRequestTask(this);
			p = new Payload(MobileLearning.SERVER_COURSES_PATH);
			task.setAPIRequestListener(this);
			task.execute(p);

			Editor editor = prefs.edit();
			editor.putLong("lastCourseUpdateCheck", now);
			editor.apply();
		}

		// send activity trackers
//		MobileLearning app = (MobileLearning) this.getApplication();
		if(omSubmitTrackerMultipleTask == null){
			Log.d(TAG,"Submitting trackers multiple task");
			omSubmitTrackerMultipleTask = new SubmitTrackerMultipleTask(this);
			omSubmitTrackerMultipleTask.execute();
		}

		// send quiz results
		if(omSubmitQuizAttemptsTask == null){
			Log.d(TAG,"Submitting quiz task");
			DbHelper db = DbHelper.getInstance(this);
			List<QuizAttempt> unsent = db.getUnsentQuizAttempts();

			if (unsent.size() > 0){
				p = new Payload(unsent);
				omSubmitQuizAttemptsTask = new SubmitQuizAttemptsTask(this);
				omSubmitQuizAttemptsTask.execute(p);
			}
		}

	}

	public void apiRequestComplete(Payload response) {
		boolean updateAvailable = false;
		try {
			
			JSONObject json = new JSONObject(response.getResultResponse());
			Log.d(TAG,json.toString(4));
			DbHelper db = DbHelper.getInstance(this);
			for (int i = 0; i < (json.getJSONArray("courses").length()); i++) {
				JSONObject json_obj = (JSONObject) json.getJSONArray("courses").get(i);
				String shortName = json_obj.getString("shortname");
				Double version = json_obj.getDouble("version");
				
				if(db.toUpdate(shortName,version)){
					updateAvailable = true;
				}
				if(json_obj.has("schedule")){
					Double scheduleVersion = json_obj.getDouble("schedule");
					if(db.toUpdateSchedule(shortName, scheduleVersion)){
						updateAvailable = true;
					}
				}
			}
			
		} catch (JSONException e) {
			Mint.logException(e);
			Log.d(TAG, "JSON error: ", e);
		} 
		
		if(updateAvailable){
            Intent resultIntent = new Intent(this, DownloadActivity.class);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder mBuilder = OppiaNotificationUtils.getBaseBuilder(this, true);
			mBuilder
					.setContentTitle(getString(R.string.notification_course_update_title))
					.setContentText(getString(R.string.notification_course_update_text))
					.setContentIntent(resultPendingIntent);
			int mId = 001;

			OppiaNotificationUtils.sendNotification(this, mId, mBuilder.build());
		}
	}


}
