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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.client.ClientProtocolException;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.listener.DownloadMediaListener;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.utils.storage.FileUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.bugsense.trace.BugSenseHandler;


public class DownloadMediaTask extends AsyncTask<Payload, DownloadProgress, Payload>{

	public final static String TAG = DownloadMediaTask.class.getSimpleName();
	private DownloadMediaListener mStateListener;
	private Context ctx;
	private SharedPreferences prefs;
	
	public DownloadMediaTask(Context ctx) {
		this.ctx = ctx;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}
	
	@Override
	protected Payload doInBackground(Payload... params) {
		Payload payload = params[0];
		for (Object o: payload.getData()){
			Media m = (Media) o;
			File file = new File(FileUtils.getMediaPath(ctx),m.getFilename());
			try { 
				
				URL u = new URL(m.getDownloadUrl());
                HttpURLConnection c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("GET");
                c.setDoOutput(true);
                c.connect();
                c.setConnectTimeout(Integer.parseInt(prefs.getString(PrefsActivity.PREF_SERVER_TIMEOUT_CONN,
								ctx.getString(R.string.prefServerTimeoutConnection))));
                c.setReadTimeout(Integer.parseInt(prefs.getString(PrefsActivity.PREF_SERVER_TIMEOUT_RESP,
								ctx.getString(R.string.prefServerTimeoutResponse))));
                
                int fileLength = c.getContentLength();
                int availableStorage = FileUtils.getAvailableStorageSize(ctx);

                if (fileLength >= availableStorage){
                    payload.setResult(false);
                    payload.setResultResponse(ctx.getString(R.string.error_insufficient_storage_available));
                }
                else{

                    DownloadProgress dp = new DownloadProgress();
                    dp.setMessage(m.getFilename());
                    dp.setProgress(0);
                    publishProgress(dp);

                    FileOutputStream f = new FileOutputStream(file);
                    InputStream in = c.getInputStream();

                    MessageDigest md = MessageDigest.getInstance("MD5");
                    in = new DigestInputStream(in, md);

                    byte[] buffer = new byte[8192];
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

                    // check the file digest matches, otherwise delete the file
                    // (it's either been a corrupted download or it's the wrong file)
                    byte[] digest = md.digest();
                    String resultMD5 = "";

                    for (int i=0; i < digest.length; i++) {
                        resultMD5 += Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
                    }

                    if(!resultMD5.contains(m.getDigest())){
                        this.deleteFile(file);
                        payload.setResult(false);
                        payload.setResultResponse(ctx.getString(R.string.error_media_download));
                    } else {
                        payload.setResult(true);
                        payload.setResultResponse(ctx.getString(R.string.success_media_download,m.getFilename()));
                    }
                }

			} catch (ClientProtocolException e1) { 
				e1.printStackTrace(); 
				payload.setResult(false);
				payload.setResultResponse(ctx.getString(R.string.error_media_download));
			} catch (IOException e1) { 
				e1.printStackTrace();
				this.deleteFile(file);
				payload.setResult(false);
				payload.setResultResponse(ctx.getString(R.string.error_media_download));
			} catch (NoSuchAlgorithmException e) {
				BugSenseHandler.sendException(e);
				e.printStackTrace();
				payload.setResult(false);
				payload.setResultResponse(ctx.getString(R.string.error_media_download));
			}
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
	protected void onPostExecute(Payload response) {
		synchronized (this) {
            if (mStateListener != null) {
               mStateListener.downloadComplete(response);
            }
        }
	}
	
	public void setDownloadListener(DownloadMediaListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }
	
	private void deleteFile(File file){
		if (file.exists() && !file.isDirectory()){
	        file.delete();
	    }
	}

}
