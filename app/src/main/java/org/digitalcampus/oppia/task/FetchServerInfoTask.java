package org.digitalcampus.oppia.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.application.MobileLearning;
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


    private final static String SERVER_NAME = "name";
    private final static String SERVER_VERSION = "version";
    private final static String ERROR_MESSAGE = "errorMessage";

    public interface FetchServerInfoListener{
        void onError(String message);
        void onValidServer(String version, String name);
        void onUnchecked();
    }

    private FetchServerInfoListener listener;

    public FetchServerInfoTask(Context ctx) {
        super(ctx);
    }

    public FetchServerInfoTask(Context ctx, ApiEndpoint api) {
        super(ctx, api);
    }

    @Override
    protected HashMap<String, String> doInBackground(Void... params) {

        HashMap<String, String> result = new HashMap<>();

        if (!ConnectionUtils.isNetworkConnected(ctx)){
            // If there is no connection available right now, we don't try to fetch info (to avoid setting a server as invalid)
            result.put("result", "noInternet");
            return result;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        OkHttpClient client = HTTPClientUtils.getClient(ctx);
        boolean validServer = false;
        Request request = createRequestWithUserAuth(apiEndpoint.getFullURL(ctx, MobileLearning.SERVER_INFO_PATH));
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                String serverInfo = response.body().string();
                JSONObject json = new JSONObject(serverInfo);
                String serverVersion = json.getString(SERVER_VERSION);
                String serverName = json.getString(SERVER_NAME);

                result.put("result", "success");
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
                result.put("result", "error");
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
            result.put("result", "error");
            result.put(ERROR_MESSAGE, e.getMessage());
            Log.e(TAG, "doInBackground: ", e);
            return result;

        } catch (JSONException e) {
            result.put("result", "error");
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
            String result = r.get("result");
            Log.d(TAG, result);
            if ("noInternet".equals(result)){
                listener.onUnchecked();
            }
            if ("success".equals(result)){
                listener.onValidServer(r.get(SERVER_VERSION), r.get(SERVER_NAME));
            }
            else if (("error".equals(result))){
                listener.onError(r.get(ERROR_MESSAGE));
            }
        }
    }

    public void setListener(FetchServerInfoListener listener) {
        this.listener = listener;
    }
}
