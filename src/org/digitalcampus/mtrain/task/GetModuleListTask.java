package org.digitalcampus.mtrain.task;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.listener.GetModuleListListener;

import com.bugsense.trace.BugSenseHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class GetModuleListTask extends AsyncTask<String, String, String>{
	
	public static final String TAG = "GetModuleListTask";
	protected Context ctx;
	private SharedPreferences prefs;
	private GetModuleListListener mStateListener;
	
	public GetModuleListTask(Context c) {
		this.ctx = c;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}
	
	@Override
	protected String doInBackground(String... urls){
		
		String toRet = "";
		for (String url : urls) {
			String response = "";
			
			// TODO change to be like the other tasks
			HttpParams httpParameters = new BasicHttpParams();
			int timeoutConnection = 10000;
			try {
				timeoutConnection = Integer.parseInt(prefs.getString("prefServerTimeoutConnection", "10000"));
			} catch (NumberFormatException e){
				// do nothing - will remain as default as above
				e.printStackTrace();
				BugSenseHandler.log(TAG, e);
			}
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 10000;
			try {
				timeoutSocket= Integer.parseInt(prefs.getString("prefServerTimeoutConnection", "10000"));
			} catch (NumberFormatException e){
				// do nothing - will remain as default as above
				e.printStackTrace();
				BugSenseHandler.log(TAG, e);
			}
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			DefaultHttpClient client = new DefaultHttpClient(httpParameters);
			HttpPost httpPost = new HttpPost(url);
			Log.d(TAG,"connecting to: "+url);
			try {
				// add post params
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("username", prefs.getString("prefLanguage", Locale.getDefault().getLanguage())));
				//nameValuePairs.add(new BasicNameValuePair("username", prefs.getString("prefUsername", "")));
				//nameValuePairs.add(new BasicNameValuePair("password", prefs.getString("prefPassword", "")));
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				
				// make request
				HttpResponse execute = client.execute(httpPost);
			
				// read response
				InputStream content = execute.getEntity().getContent();
				BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
				String s = "";
				while ((s = buffer.readLine()) != null) {
					response += s;
				}
				
				toRet = response;
				
			} catch (Exception e) {
				e.printStackTrace();
				toRet = ctx.getString(R.string.error_connection);
			}
		}
		return toRet;
	}
	
	@Override
	protected void onPostExecute(String response) {
		
		synchronized (this) {
            if (mStateListener != null) {
               mStateListener.moduleListComplete(response);
            }
        }
		
		
		
	}
	
	public void setGetModuleListListener(GetModuleListListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }
}