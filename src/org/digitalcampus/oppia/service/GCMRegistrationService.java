package org.digitalcampus.oppia.service;

import android.app.IntentService;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.splunk.mint.Mint;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.RegisterDeviceRemoteAdminTask;
import org.digitalcampus.oppia.utils.HTTPConnectionUtils;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class GCMRegistrationService extends IntentService {

    private static final String TAG = GCMRegistrationService.class.getSimpleName();


    public GCMRegistrationService() { super(TAG); }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!MobileLearning.DEVICEADMIN_ENABLED) return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        try {
            // Initially this call goes out to the network to retrieve the token, subsequent calls are local.
            InstanceID instanceID = InstanceID.getInstance(this);
            // R.string.gcm_defaultSenderId (the Sender ID) is typically derived from google-services.json.
            // See https://developers.google.com/cloud-messaging/android/start for details on this file.
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            String previousToken = prefs.getString(PrefsActivity.GCM_TOKEN_ID, "");
            boolean tokenSent = prefs.getBoolean(PrefsActivity.GCM_TOKEN_SENT, false);

            if (!previousToken.equals(token)){
                Log.i(TAG, "New GCM Registration Token: " + token);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(PrefsActivity.GCM_TOKEN_SENT, false);
                editor.putString(PrefsActivity.GCM_TOKEN_ID, token).apply();
                tokenSent = false;
            }

            if (!tokenSent){
                RegisterDeviceRemoteAdminTask.registerDevice(this, prefs);
            }

        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            prefs.edit().putBoolean(PrefsActivity.GCM_TOKEN_SENT, false).apply();
        }
    }

}