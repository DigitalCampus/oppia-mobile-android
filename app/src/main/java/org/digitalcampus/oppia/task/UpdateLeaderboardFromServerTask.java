package org.digitalcampus.oppia.task;

import android.content.Context;
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateLeaderboardFromServerTask extends APIRequestTask<Payload, Object, Payload> {

    public static final String TAG = UpdateLeaderboardFromServerTask.class.getSimpleName();

    private SubmitListener listener;
    public UpdateLeaderboardFromServerTask(Context ctx) {
        super(ctx);
    }

    public UpdateLeaderboardFromServerTask(Context ctx, ApiEndpoint api) {
        super(ctx, api);
    }

    @Override
    protected Payload doInBackground(Payload... params) {

        Payload payload = params[0];

        try {
            OkHttpClient client = HTTPClientUtils.getClient(ctx);
            Request request = new Request.Builder()
                    .url(apiEndpoint.getFullURL(ctx, MobileLearning.LEADERBOARD_PATH))
                    .get()
                    .build();

            Response response = null;
            response = client.newCall(request).execute();
            if (response.isSuccessful()){
                JSONObject jsonResp = new JSONObject(response.body().string());
                String lastUpdateStr = jsonResp.getString("generated_date");
                String server = jsonResp.getString("server");

                if (!prefs.getString(PrefsActivity.PREF_SERVER, "").equals(server)){
                    payload.setResult(false);
                    payload.setResultResponse("Not matching server");
                    Log.d(TAG, "Leaderboard server doesn't match with current one");

                }
                Log.d(TAG, lastUpdateStr);
                payload.setResult(true);
            }
            else{
                if (response.code() == 404){
                    payload.setResultResponse("Your server version is old and does not support leaderboard export.");
                }
                payload.setResult(false);
            }


        } catch (IOException e) {
            e.printStackTrace();
            payload.setResult(false);
            payload.setResultResponse(ctx.getString(R.string.error_connection_required));
        } catch (JSONException e) {
            Mint.logException(e);
            e.printStackTrace();
            payload.setResult(false);
            payload.setResultResponse(ctx.getString(R.string.error_processing_response));
        }

        return payload;

    }

    @Override
    protected void onPostExecute(Payload response) {
        synchronized (this) {
            if (listener != null) {
                listener.submitComplete(response);
            }
        }
    }


    public void seListener(SubmitListener srl) {
        synchronized (this) {
            listener = srl;
        }
    }
}
