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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.HTTPConnectionUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

public class DownloadCourseTask extends AsyncTask<Payload, DownloadProgress, Payload>{

	public final static String TAG = DownloadCourseTask.class.getSimpleName();
	private InstallCourseListener mStateListener;
	
	private Context ctx;
	private SharedPreferences prefs;
	
	public DownloadCourseTask(Context ctx) {
		this.ctx = ctx;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}
	
	@Override
	protected Payload doInBackground(Payload... params) {
		Payload payload = params[0];
		
		Course dm = (Course) payload.getData().get(0);
		DownloadProgress dp = new DownloadProgress();
		try { 
			HTTPConnectionUtils client = new HTTPConnectionUtils(ctx);

			String url =  client.createUrlWithCredentials(dm.getDownloadUrl());
			
			Log.d(TAG,"Downloading:" + url);
			
			URL u = new URL(url);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect();
            c.setConnectTimeout(Integer.parseInt(prefs.getString(ctx.getString(R.string.prefs_server_timeout_connection),
							ctx.getString(R.string.prefServerTimeoutConnection))));
            c.setReadTimeout(Integer.parseInt(prefs.getString(ctx.getString(R.string.prefs_server_timeout_response),
							ctx.getString(R.string.prefServerTimeoutResponse))));
			
			int fileLength = c.getContentLength();
			
			String localFileName = dm.getShortname()+"-"+String.format("%.0f",dm.getVersionId())+".zip";
            
			dp.setMessage(localFileName);
			dp.setProgress(0);
			publishProgress(dp);
				
			
			Log.d(TAG,"saving to: "+localFileName);
			
			FileOutputStream f = new FileOutputStream(new File(MobileLearning.DOWNLOAD_PATH,localFileName));
			InputStream in = c.getInputStream();
			
            byte[] buffer = new byte[1024];
            int len1 = 0;
            long total = 0;
            int progress = 0;
            while ((len1 = in.read(buffer)) > 0) {
                total += len1; 
                progress = (int)(total*100)/fileLength;
                if(progress > 0){
                    dp.setProgress(progress);
                    publishProgress(dp);
                }
                f.write(buffer, 0, len1);
            }
            f.close();
			
			dp.setProgress(100);
			publishProgress(dp);
			dp.setMessage(ctx.getString(R.string.download_complete));
			publishProgress(dp);
			payload.setResult(true);
		} catch (ClientProtocolException cpe) { 
			if(!MobileLearning.DEVELOPER_MODE){
				BugSenseHandler.sendException(cpe);
			} else {
				cpe.printStackTrace();
			}
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (SocketTimeoutException ste){
			if(!MobileLearning.DEVELOPER_MODE){
				BugSenseHandler.sendException(ste);
			} else {
				ste.printStackTrace();
			}
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		} catch (IOException ioe) { 
			if(!MobileLearning.DEVELOPER_MODE){
				BugSenseHandler.sendException(ioe);
			} else {
				ioe.printStackTrace();
			}
			payload.setResult(false);
			payload.setResultResponse(ctx.getString(R.string.error_connection));
		}
		
		return payload;
	}
	
	@Override
	protected void onProgressUpdate(DownloadProgress... obj) {
		synchronized (this) {
            if (mStateListener != null) {
                mStateListener.downloadProgressUpdate(obj[0]);
            }
        }
	}

	@Override
	protected void onPostExecute(Payload results) {
		synchronized (this) {
            if (mStateListener != null) {
               mStateListener.downloadComplete(results);
            }
        }
	}

	public void setInstallerListener(InstallCourseListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }

}
