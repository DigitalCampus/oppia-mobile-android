package org.digitalcampus.oppia.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.gcm.Task;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.utils.HTTPConnectionUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class RegisterDeviceRemoteAdminTask extends AsyncTask<Payload, Void, Payload> {

    public static final String TAG = RegisterDeviceRemoteAdminTask.class.getSimpleName();

    private Context ctx;
    private SharedPreferences prefs;

    public RegisterDeviceRemoteAdminTask(Context appContext, SharedPreferences sharedPrefs){
        this.ctx = appContext;
        prefs = sharedPrefs;
    }

    @Override
    protected Payload doInBackground(Payload... args) {

        Payload payload = new Payload();
        boolean success = registerDevice(ctx, prefs);
        payload.setResult(success);
        return payload;
    }

    public static boolean registerDevice(Context ctx, SharedPreferences prefs){

        Log.d(TAG, "Checking if is needed to send the token");
        String username = prefs.getString(PrefsActivity.PREF_USER_NAME, "");
        boolean tokenSent = prefs.getBoolean(PrefsActivity.GCM_TOKEN_SENT, false);
        //If there is no user logged in or the token has already been sent, we exit the task
        if (tokenSent || username.equals("")){
            return false;
        }

        String token = prefs.getString(PrefsActivity.GCM_TOKEN_ID, "");
        String deviceModel = android.os.Build.BRAND + " " + android.os.Build.MODEL;
        String deviceID = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);

        Log.d(TAG, "Registering device in remote admin list");
        HTTPConnectionUtils client = new HTTPConnectionUtils(ctx);
        String url = "http://www.chaotic-kingdoms.com/oppia/new.php";
        JSONObject json = new JSONObject();

        HttpPost httpPost = new HttpPost(url);
        try {
            // Request parameters and other properties.
            List<NameValuePair> params = new ArrayList<>(2);
            params.add(new BasicNameValuePair("model_name", deviceModel));
            params.add(new BasicNameValuePair("device_id", deviceID));
            params.add(new BasicNameValuePair("sender_id", token));
            params.add(new BasicNameValuePair("username", username));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

            // make request
            HttpResponse response = client.execute(httpPost);

            // check status code
            switch (response.getStatusLine().getStatusCode()){
                case 400: // unauthorised
                    Log.d(TAG, "Bad request");
                    return false;
                case 200: // logged in
                    Log.d(TAG, "Successful registration!");
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean(PrefsActivity.GCM_TOKEN_SENT, true);
                    editor.commit();
                    break;
            }

        } catch (UnsupportedEncodingException | ClientProtocolException e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
