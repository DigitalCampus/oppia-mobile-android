package org.digitalcampus.oppia.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import androidx.preference.PreferenceManager;
import android.util.Log;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.utils.ConnectionUtils;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FetchServerInfoTask extends APIRequestTask<Void, Object, HashMap<String, String>> {


    private static final String SERVER_NAME = "name";
    private static final String SERVER_VERSION = "version";
    private static final String ERROR_MESSAGE = "errorMessage";

    private static final String RESULT_TAG = "result";
    private static final String RESULT_NOINTERNET = "noInternet";
    private static final String RESULT_SUCCESS = "success";
    private static final String RESULT_ERROR = "error";

    public interface FetchServerInfoListener{
        void onError(String message);
        void onValidServer(String version, String name);
        void onUnchecked();
    }

    private FetchServerInfoListener listener;
    private ConnectivityManager connectivityManager;

    public FetchServerInfoTask(Context ctx) {
        super(ctx);
        connectivityManager = ConnectionUtils.getConnectivityManager(ctx);
    }

    public FetchServerInfoTask(Context ctx, ApiEndpoint api) {
        super(ctx, api);
        connectivityManager = ConnectionUtils.getConnectivityManager(ctx);
    }

    public FetchServerInfoTask(Context ctx, ApiEndpoint api, ConnectivityManager connectivityManager){
        super(ctx, api);
        this.connectivityManager = connectivityManager != null ? connectivityManager: ConnectionUtils.getConnectivityManager(ctx);
    }

    @Override
    protected HashMap<String, String> doInBackground(Void... params) {

        HashMap<String, String> result = new HashMap<>();

        if (!ConnectionUtils.isNetworkConnected(connectivityManager)){
            // If there is no connection available right now, we don't try to fetch info (to avoid setting a server as invalid)
            result.put(RESULT_TAG, RESULT_NOINTERNET);
            return result;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        OkHttpClient client = HTTPClientUtils.getClient(ctx);
        boolean validServer = false;
        Request request = createRequestBuilderWithUserAuth(apiEndpoint.getFullURL(ctx, Paths.SERVER_INFO_PATH)).build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                String serverInfo = response.body().string();
                JSONObject json = new JSONObject(serverInfo);
                String serverVersion = json.getString(SERVER_VERSION);
                String serverName = json.getString(SERVER_NAME);

                result.put(RESULT_TAG, RESULT_SUCCESS);
                result.put(SERVER_VERSION, serverVersion);
                result.put(SERVER_NAME, serverName);

                prefs.edit()
                    .putBoolean(PrefsActivity.PREF_SERVER_CHECKED, true)
                    .putBoolean(PrefsActivity.PREF_SERVER_VALID, true)
                    .putString(PrefsActivity.PREF_SERVER_NAME, serverName)
                    .putString(PrefsActivity.PREF_SERVER_VERSION, serverVersion)
                    .apply();
                validServer = true;
            }
            else{
                result.put(RESULT_TAG, RESULT_ERROR);
                switch (response.code()) {
                    case 401:
                    case 403: // unauthorised
                        result.put(ERROR_MESSAGE, ctx.getString(R.string.error_login));
                        break;
                    default:
                        result.put(ERROR_MESSAGE, ctx.getString(R.string.error_processing_response));
                }
            }
        } catch (IOException e) {
            result.put(RESULT_TAG, RESULT_ERROR);
            result.put(ERROR_MESSAGE, e.getMessage());
            Log.e(TAG, "doInBackground: ", e);

        } catch (JSONException e) {
            result.put(RESULT_TAG, RESULT_ERROR);
            result.put(ERROR_MESSAGE, ctx.getString(R.string.error_processing_response));
            Log.e(TAG, "doInBackground: ", e);
        }

        if (!validServer){
            prefs.edit()
                .putBoolean(PrefsActivity.PREF_SERVER_CHECKED, true)
                .putBoolean(PrefsActivity.PREF_SERVER_VALID, false)
                .apply();
        }

        return result;
    }

    @Override
    protected void onPostExecute(HashMap<String, String> r) {
        super.onPostExecute(r);
        if (listener != null){
            String result = r.get(RESULT_TAG);
            Log.d(TAG, result);
            if (RESULT_NOINTERNET.equals(result)){
                listener.onUnchecked();
            }
            if (RESULT_SUCCESS.equals(result)){
                listener.onValidServer(r.get(SERVER_VERSION), r.get(SERVER_NAME));
            }
            else if ((RESULT_ERROR.equals(result))){
                listener.onError(r.get(ERROR_MESSAGE));
            }
        }
    }

    public void setListener(FetchServerInfoListener listener) {
        this.listener = listener;
    }
}
