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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.params.CoreProtocolPNames;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.listener.UpdateActivityListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.HTTPConnectionUtils;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.xmlreaders.CourseTrackerXMLReader;

import com.splunk.mint.Mint;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class UpdateCourseActivityTask extends AsyncTask<Payload, DownloadProgress, Payload> {

	public final static String TAG = UpdateCourseActivityTask.class.getSimpleName();
	private UpdateActivityListener mStateListener;
	
	private Context ctx;
	private SharedPreferences prefs;
	private long userId;
	
	public UpdateCourseActivityTask(Context ctx, long userId) {
		this.ctx = ctx;
		this.userId = userId;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}
	
	@Override
	protected Payload doInBackground(Payload... params) {
		Payload payload = params[0];
		
		Course course = (Course) payload.getData().get(0);
		DownloadProgress dp = new DownloadProgress();
		String responseStr = "";
		
		
		
		try {
			DbHelper db = new DbHelper(this.ctx);
			User u = db.getUser(userId);
			
			HTTPConnectionUtils client = new HTTPConnectionUtils(ctx);
			String url = client.getFullURL(course.getTrackerLogUrl());
			HttpGet httpGet = new HttpGet(url);
			httpGet.addHeader(client.getAuthHeader(u.getUsername(),u.getApiKey()));
			Log.d(TAG,url);
			
			// make request
			HttpResponse response = client.execute(httpGet);
		
			// read response
			InputStream content = response.getEntity().getContent();
			BufferedReader buffer = new BufferedReader(new InputStreamReader(content), 8098);
			String s = "";
			while ((s = buffer.readLine()) != null) {
				responseStr += s;
				Log.d(TAG,s);
			}		

			
			CourseTrackerXMLReader ctxr;
			try {
				ctxr = new CourseTrackerXMLReader(responseStr);
				db.resetCourse(course.getCourseId(), userId);
	            db.insertTrackers(ctxr.getTrackers(course.getCourseId(), userId));
	            db.insertQuizAttempts(ctxr.getQuizAttempts(course.getCourseId(), userId));
			} catch (InvalidXMLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			DatabaseManager.getInstance().closeDatabase();
			
            dp.setProgress(100);
            publishProgress(dp);
            dp.setMessage(ctx.getString(R.string.download_complete));
            publishProgress(dp);
            payload.setResult(true);
       

		} catch (ClientProtocolException cpe) {
			Mint.logException(cpe);
			cpe.printStackTrace();
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (SocketTimeoutException ste){
			Mint.logException(ste);
			ste.printStackTrace();
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (IOException ioe) { 
			Mint.logException(ioe);
			ioe.printStackTrace();
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (UserNotFoundException unfe) {
			unfe.printStackTrace();
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		}
		
		return payload;
	}
	
	@Override
	protected void onProgressUpdate(DownloadProgress... obj) {
		synchronized (this) {
            if (mStateListener != null) {
                mStateListener.updateActivityProgressUpdate(obj[0]);
            }
        }
	}

	@Override
	protected void onPostExecute(Payload results) {
		synchronized (this) {
            if (mStateListener != null) {
               mStateListener.updateActivityComplete(results);
            }
        }
	}

	public void setUpdateActivityListener(UpdateActivityListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }
}
