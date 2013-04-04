package org.digitalcampus.mobile.learning.task;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.listener.InstallModuleListener;
import org.digitalcampus.mobile.learning.model.Module;

import com.bugsense.trace.BugSenseHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class DownloadModuleTask extends AsyncTask<Payload, String, Payload>{

	public final static String TAG = DownloadModuleTask.class.getSimpleName();
	private InstallModuleListener mStateListener;
	private Context ctx;
	private SharedPreferences prefs;
	
	public DownloadModuleTask(Context ctx) {
		this.ctx = ctx;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}
	
	@Override
	protected Payload doInBackground(Payload... params) {
		// TODO what to do when there is an error connecting - how to flag back to user
		for (Payload payload : params) {
			Module dm = (Module) payload.data.get(0);

			try { 
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(
						httpParameters,
						Integer.parseInt(prefs.getString("prefServerTimeoutConnection",
								ctx.getString(R.string.prefServerTimeoutConnection))));
				HttpConnectionParams.setSoTimeout(
						httpParameters,
						Integer.parseInt(prefs.getString("prefServerTimeoutResponse",
								ctx.getString(R.string.prefServerTimeoutResponseDefault))));
				DefaultHttpClient client = new DefaultHttpClient(httpParameters);

				// add api_key/username params
				List<NameValuePair> pairs = new LinkedList<NameValuePair>();
				pairs.add(new BasicNameValuePair("username", prefs.getString("prefUsername", "")));
				pairs.add(new BasicNameValuePair("api_key", prefs.getString("prefApiKey", "")));
				String paramString = URLEncodedUtils.format(pairs, "utf-8");
				
				String url = dm.getDownloadUrl();
				
				if(!url.endsWith("?"))
			        url += "?";
				url += paramString;
				
				Log.d(TAG,"Downloading:" + url);
				
				HttpGet httpGet = new HttpGet(url);
				
				String localFileName = dm.getShortname()+"-"+String.format("%.0f",dm.getVersionId())+".zip";
				Log.d(TAG,"saving to: "+localFileName);
				
				FileOutputStream fos = new FileOutputStream(new File(MobileLearning.DOWNLOAD_PATH,localFileName));
				client.execute(httpGet).getEntity().writeTo(fos);
				fos.close();
				publishProgress(ctx.getString(R.string.download_complete));
			} catch (ClientProtocolException e1) { 
				e1.printStackTrace(); 
				BugSenseHandler.sendException(e1);
			} catch (SocketTimeoutException ste){
				ste.printStackTrace();
				publishProgress(ctx.getString(R.string.error_connection));
			} catch (IOException e1) { 
				e1.printStackTrace();
				publishProgress(ctx.getString(R.string.error_connection));
			}
		}
		return null;
	}
	
	@Override
	protected void onProgressUpdate(String... obj) {
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
               mStateListener.downloadComplete();
            }
        }
	}

	public void setInstallerListener(InstallModuleListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }

}
