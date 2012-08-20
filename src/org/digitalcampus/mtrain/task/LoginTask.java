package org.digitalcampus.mtrain.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.application.MTrain;
import org.digitalcampus.mtrain.listener.SubmitListener;
import org.digitalcampus.mtrain.model.User;
import org.json.JSONException;
import org.json.JSONObject;

import com.bugsense.trace.BugSenseHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class LoginTask extends AsyncTask<Payload, Object, Payload> {

	public static final String TAG = "LoginTask";

	private Context ctx;
	private SharedPreferences prefs;
	private SubmitListener mStateListener;
	
	public LoginTask(Context c) {
		this.ctx = c;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	@Override
	protected Payload doInBackground(Payload... params) {

		Payload payload = params[0];
		for (User u : (User[]) payload.data) {
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

			String url = prefs.getString("prefServer", ctx.getString(R.string.prefServerDefault)) + MTrain.LOGIN_PATH;
			HttpPost httpPost = new HttpPost(url);
			try {
				// update progress dialog
				publishProgress(ctx.getString(R.string.login_process));
				Log.d(TAG,"logging in ...." + u.username);
				// add post params
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("username", u.username));
				nameValuePairs.add(new BasicNameValuePair("password", u.password));
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// make request
				HttpResponse execute = client.execute(httpPost);

				// read response
				InputStream content = execute.getEntity().getContent();
				BufferedReader buffer = new BufferedReader(new InputStreamReader(content), 4096);
				String response = "";
				String s = "";

				while ((s = buffer.readLine()) != null) {
					response += s;
				}
				Log.d(TAG,response);
				
				JSONObject json = new JSONObject(response);
				if (json.has("login")) {
					if (json.getBoolean("login")) {
						payload.result = true;
						payload.resultResponse = ctx.getString(R.string.login_complete);
					} else {
						payload.result = false;
						payload.resultResponse = ctx.getString(R.string.error_login);
					}
				} else if (json.has("error")){
					payload.result = false;
					payload.resultResponse = json.getString("error");
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				BugSenseHandler.log(TAG, e);
				payload.result = false;
				payload.resultResponse = ctx.getString(R.string.error_connection);
			} catch (ClientProtocolException e) {
				BugSenseHandler.log(TAG, e);
				e.printStackTrace();
				payload.result = false;
				payload.resultResponse = ctx.getString(R.string.error_connection);
			} catch (IOException e) {
				BugSenseHandler.log(TAG, e);
				e.printStackTrace();
				payload.result = false;
				payload.resultResponse = ctx.getString(R.string.error_connection);
			} catch (JSONException e) {
				BugSenseHandler.log(TAG, e);
				e.printStackTrace();
				payload.result = false;
				payload.resultResponse = ctx.getString(R.string.error_processing_response);
			} finally {

			}
		}
		return payload;
	}

	@Override
	protected void onPostExecute(Payload response) {
		synchronized (this) {
            if (mStateListener != null) {
               mStateListener.submitComplete(response);
            }
        }
	}
	
	public void setLoginListener(SubmitListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }
}
