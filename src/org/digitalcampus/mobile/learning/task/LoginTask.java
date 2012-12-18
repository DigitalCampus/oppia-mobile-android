package org.digitalcampus.mobile.learning.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.listener.SubmitListener;
import org.digitalcampus.mobile.learning.model.User;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

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
		User u = (User) payload.data.get(0);
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

		String url = prefs.getString("prefServer", ctx.getString(R.string.prefServerDefault)) + MobileLearning.LOGIN_PATH;
		Log.d(TAG,url);
		JSONObject json = new JSONObject();
		
		HttpPost httpPost = new HttpPost(url);
		try {
			// update progress dialog
			publishProgress(ctx.getString(R.string.login_process));
			Log.d(TAG,"logging in ...." + u.getUsername());
			// add post params
			json.put("username", u.getUsername());
            json.put("password", u.getPassword());
            StringEntity se = new StringEntity( json.toString());
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            httpPost.setEntity(se);

			// make request
			HttpResponse response = client.execute(httpPost);

			// read response
			InputStream content = response.getEntity().getContent();
			BufferedReader buffer = new BufferedReader(new InputStreamReader(content), 4096);
			String responseStr = "";
			String s = "";

			while ((s = buffer.readLine()) != null) {
				responseStr += s;
			}
			
			// check status code
			switch (response.getStatusLine().getStatusCode()){
				case 400: // unauthorised
					payload.result = false;
					payload.resultResponse = ctx.getString(R.string.error_login);
					break;
				case 201: // logged in
					JSONObject jsonResp = new JSONObject(responseStr);
					u.setApi_key(jsonResp.getString("api_key"));
					u.setFirstname(jsonResp.getString("first_name"));
					u.setLastname(jsonResp.getString("last_name"));
					u.setPoints(jsonResp.getInt("points"));
					payload.result = true;
					payload.resultResponse = ctx.getString(R.string.login_complete);
					break;
				default:
					payload.result = false;
					payload.resultResponse = ctx.getString(R.string.error_connection);
			}
			

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			payload.result = false;
			payload.resultResponse = ctx.getString(R.string.error_connection);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			payload.result = false;
			payload.resultResponse = ctx.getString(R.string.error_connection);
		} catch (IOException e) {
			e.printStackTrace();
			payload.result = false;
			payload.resultResponse = ctx.getString(R.string.error_connection);
		} catch (JSONException e) {
			BugSenseHandler.sendException(e);
			e.printStackTrace();
			payload.result = false;
			payload.resultResponse = ctx.getString(R.string.error_processing_response);
		} finally {

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
