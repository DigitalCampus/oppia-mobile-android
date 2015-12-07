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
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.params.CoreProtocolPNames;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.HTTPConnectionUtils;
import org.digitalcampus.oppia.utils.storage.Storage;

import com.splunk.mint.Mint;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

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

			DbHelper db = new DbHelper(ctx);
        	User user = db.getUser(SessionManager.getUsername(ctx));
			DatabaseManager.getInstance().closeDatabase();
			
			String url =  client.createUrlWithCredentials(dm.getDownloadUrl(),user.getUsername(),user.getApiKey());
			
			String v = "0";
			try {
				v = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			
			URL u = new URL(url);
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty(CoreProtocolPNames.USER_AGENT, MobileLearning.USER_AGENT + v);
            Log.d(TAG,CoreProtocolPNames.USER_AGENT);
            Log.d(TAG,MobileLearning.USER_AGENT + v);
            c.setDoOutput(true);
            c.connect();
            c.setConnectTimeout(Integer.parseInt(prefs.getString(PrefsActivity.PREF_SERVER_TIMEOUT_CONN,
							ctx.getString(R.string.prefServerTimeoutConnection))));
            c.setReadTimeout(Integer.parseInt(prefs.getString(PrefsActivity.PREF_SERVER_TIMEOUT_RESP,
							ctx.getString(R.string.prefServerTimeoutResponse))));
            
			
			long fileLength = c.getContentLength();
            long availableStorage = Storage.getAvailableStorageSize(ctx);

            if (fileLength >= availableStorage){
                payload.setResult(false);
                payload.setResultResponse(ctx.getString(R.string.error_insufficient_storage_available));
            } else {
                String localFileName = dm.getShortname()+"-"+String.format("%.0f",dm.getVersionId())+".zip";

                dp.setMessage(localFileName);
                dp.setProgress(0);
                publishProgress(dp);

                FileOutputStream f = new FileOutputStream(new File(Storage.getDownloadPath(ctx),localFileName));
                InputStream in = c.getInputStream();

                byte[] buffer = new byte[1024];
                int len1 = 0;
                long total = 0;
                int progress, lastProgress = 0;
                while ((len1 = in.read(buffer)) > 0) {
                    total += len1;
                    progress = (int)((total*100)/fileLength);
                    if ((progress > 0) && (progress > lastProgress)){
                        dp.setProgress(progress);
                        lastProgress = progress;
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
            }

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
