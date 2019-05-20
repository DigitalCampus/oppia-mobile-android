package org.digitalcampus.oppia.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.APIKeyRequestListener;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.utils.ConnectionUtils;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FetchServerInfoTask extends APIRequestTask<Payload, Object, Payload> {


    private APIRequestListener listener;

    public FetchServerInfoTask(Context ctx) {
        super(ctx);
    }

    public FetchServerInfoTask(Context ctx, ApiEndpoint api) {
        super(ctx, api);
    }

    @Override
    protected Payload doInBackground(Payload... serverUrl) {
        Payload payload = serverUrl[0];

        if (!ConnectionUtils.isNetworkConnected(ctx)){
            // If there is no connection available right now, we don't try to fetch info (to avoid setting a server as invalid)
            payload.setResult(true);
            return payload;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        OkHttpClient client = HTTPClientUtils.getClient(ctx);
        boolean validServer = false;
        Request request = createRequestWithUserAuth(apiEndpoint.getFullURL(ctx, MobileLearning.SERVER_INFO_PATH));
        try {

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                payload.setResult(true);
                String serverInfo = response.body().string();
                JSONObject json = new JSONObject(serverInfo);
                String serverVersion = json.getString("version");
                String serverName = json.getString("name");

                prefs.edit()
                    .putBoolean(PrefsActivity.PREF_SERVER_CHECKED, true)
                    .putBoolean(PrefsActivity.PREF_SERVER_VALID, true)
                    .putString(PrefsActivity.PREF_SERVER_NAME, serverName)
                    .putString(PrefsActivity.PREF_SERVER_VERSION, serverVersion)
                    .apply();
                validServer = true;
            }
            else{
                switch (response.code()) {
                    case 401:
                    case 403: // unauthorised
                        payload.setResult(false);
                        payload.setResultResponse(ctx.getString(R.string.error_login));
                        break;

                    default:
                        payload.setResult(false);
                        payload.setResultResponse(ctx.getString(R.string.error_connection));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!validServer){
            prefs.edit()
                .putBoolean(PrefsActivity.PREF_SERVER_CHECKED, true)
                .putBoolean(PrefsActivity.PREF_SERVER_VALID, false)
                .apply();
        }

        return payload;
    }

    @Override
    protected void onPostExecute(Payload payload) {
        super.onPostExecute(payload);
        if (listener != null){
            listener.apiRequestComplete(payload);
        }
    }

    public void setListener(APIRequestListener listener) {
        this.listener = listener;
    }
}
