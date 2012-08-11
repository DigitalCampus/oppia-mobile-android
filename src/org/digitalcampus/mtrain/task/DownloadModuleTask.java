package org.digitalcampus.mtrain.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.digitalcampus.mtrain.activity.DownloadActivity.DownloadModule;
import org.digitalcampus.mtrain.application.MTrain;
import org.digitalcampus.mtrain.listener.DownloadModuleListener;

import android.os.AsyncTask;
import android.util.Log;

public class DownloadModuleTask extends AsyncTask<Payload, Object, Payload>{

	private final static String TAG = "DownloadModuleTask";
	private DownloadModuleListener mStateListener;
	
	@Override
	protected Payload doInBackground(Payload... params) {
		// TODO what to do when there is an error connecting - how to flag back to user
		for (Payload payload : params) {
			DownloadModule dm = (DownloadModule) payload.data[0];
			Log.d(TAG,"Downloading:" + dm.downloadUrl);
			try { 
				String localFileName = dm.shortname+"-"+String.format("%.0f",dm.version)+".zip";
				Log.d(TAG,"saving to: "+localFileName);
				new DefaultHttpClient().execute(new HttpGet(dm.downloadUrl)).getEntity().writeTo(new FileOutputStream(new File(MTrain.DOWNLOAD_PATH,localFileName)));
				publishProgress("Downloaded file");
			} catch (ClientProtocolException e1) { 
				e1.printStackTrace(); 
			} catch (IOException e1) { 
				e1.printStackTrace();
			}
			 
		}
		return null;
	}
	
	protected void onProgressUpdate(String... obj) {
		synchronized (this) {
            if (mStateListener != null) {
                // update progress and total
                mStateListener.downloadProgressUpdate(obj[0]);
            }
        }
	}

	protected void onPostExecute(Payload results) {
		synchronized (this) {
            if (mStateListener != null) {
               mStateListener.downloadComplete();
            }
        }
	}

	public void setInstallerListener(DownloadModuleListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }

}
