package org.digitalcampus.mobile.learning.task;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.listener.GetModuleListListener;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class GetModuleListTask extends AsyncTask<Payload, Object, Payload>{
	
	public static final String TAG = "GetModuleListTask";
	protected Context ctx;
	private SharedPreferences prefs;
	private GetModuleListListener mStateListener;
	
	public GetModuleListTask(Context c) {
		this.ctx = c;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}
	
	@Override
	protected Payload doInBackground(Payload... params){
		
		Payload payload = params[0];
		String responseStr = "";
		
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

		// add post params
		List<NameValuePair> pairs = new LinkedList<NameValuePair>();
		pairs.add(new BasicNameValuePair("username", prefs.getString("prefUsername", "")));
		pairs.add(new BasicNameValuePair("api_key", prefs.getString("prefApiKey", "")));
		
		String url = prefs.getString("prefServer", ctx.getString(R.string.prefServerDefault)) + MobileLearning.SERVER_MODULES_PATH;
		String paramString = URLEncodedUtils.format(pairs, "utf-8");
		if(!url.endsWith("?"))
	        url += "?";
		url += paramString;
		
		HttpGet httpGet = new HttpGet(url);
		Log.d(TAG,"connecting to: "+url);
		try {
			
			// make request
			HttpResponse response = client.execute(httpGet);
		
			// read response
			InputStream content = response.getEntity().getContent();
			BufferedReader buffer = new BufferedReader(new InputStreamReader(content));
			String s = "";
			while ((s = buffer.readLine()) != null) {
				responseStr += s;
			}
			
			Log.d(TAG,String.valueOf(response.getStatusLine().getStatusCode()));
			
			switch (response.getStatusLine().getStatusCode()){
				case 400: // unauthorised
					payload.result = false;
					payload.resultResponse = ctx.getString(R.string.error_login);
					break;
				case 200: 
					payload.result = true;
					payload.resultResponse = responseStr;
					break;
				default:
					payload.result = false;
					payload.resultResponse = ctx.getString(R.string.error_connection);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			payload.resultResponse = ctx.getString(R.string.error_connection);
		}
		return payload;
	}
	
	@Override
	protected void onPostExecute(Payload response) {
		
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