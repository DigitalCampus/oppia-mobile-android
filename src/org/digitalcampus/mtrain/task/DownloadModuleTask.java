package org.digitalcampus.mtrain.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.application.MTrain;
import org.digitalcampus.mtrain.listener.DownloadModuleListener;
import org.digitalcampus.mtrain.model.Module;

import com.bugsense.trace.BugSenseHandler;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class DownloadModuleTask extends AsyncTask<Payload, Object, Payload>{

	private final static String TAG = "DownloadModuleTask";
	private DownloadModuleListener mStateListener;
	private Context ctx;
	
	public DownloadModuleTask(Context ctx) {
		this.ctx = ctx;
	}
	
	@Override
	protected Payload doInBackground(Payload... params) {
		// TODO what to do when there is an error connecting - how to flag back to user
		for (Payload payload : params) {
			Module dm = (Module) payload.data[0];
			Log.d(TAG,"Downloading:" + dm.getDownloadUrl());
			try { 
				String localFileName = dm.getShortname()+"-"+String.format("%.0f",dm.getVersionId())+".zip";
				Log.d(TAG,"saving to: "+localFileName);
				new DefaultHttpClient().execute(new HttpGet(dm.getDownloadUrl())).getEntity().writeTo(new FileOutputStream(new File(MTrain.DOWNLOAD_PATH,localFileName)));
				publishProgress(ctx.getString(R.string.download_complete));
			} catch (ClientProtocolException e1) { 
				e1.printStackTrace(); 
				BugSenseHandler.log(TAG, e1);
			} catch (IOException e1) { 
				e1.printStackTrace();
				BugSenseHandler.log(TAG, e1);
			}
			 
		}
		return null;
	}
	
	protected void onProgressUpdate(String... obj) {
		synchronized (this) {
            if (mStateListener != null) {
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
