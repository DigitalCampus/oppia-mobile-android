package org.digitalcampus.oppia.task;

import android.content.Context;
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.exception.WrongServerException;
import org.digitalcampus.oppia.gamification.LeaderboardUtils;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateLeaderboardFromServerTask extends APIRequestTask<Payload, Object, Payload> {

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
            Request request = createRequestBuilderWithUserAuth(apiEndpoint.getFullURL(ctx, Paths.LEADERBOARD_PATH)).build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                String json = response.body().string();
                int updatedPositions = 0;
                try {
                    updatedPositions += LeaderboardUtils.importLeaderboardJSON(ctx, json);
                    payload.setResult(true);
                    payload.setResultResponse(updatedPositions + " updated.");
                } catch (ParseException e) {
                    Mint.logException(e);
                    Log.d(TAG, "ParseException:", e);
                    payload.setResult(false);
                } catch (JSONException e) {
                    Mint.logException(e);
                    Log.d(TAG, "JSONException:", e);
                    payload.setResult(false);
                } catch (WrongServerException e) {
                    Mint.logException(e);
                    Log.d(TAG, "WrongServerException:", e);
                    payload.setResult(false);
                }
            }
            else{
                if (response.code() == 404){
                    payload.setResultResponse("Your server version is old and does not support leaderboard export.");
                }
                payload.setResult(false);
                response.body().close();
            }

        } catch (IOException e) {
            Mint.logException(e);
            Log.d(TAG, "IOException:", e);
            payload.setResult(false);
            payload.setResultResponse(ctx.getString(R.string.error_connection_required));
        }

        return payload;

    }

    @Override
    protected void onPostExecute(Payload response) {

        LeaderboardUtils.updateLeaderboardFetchTime(prefs);
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
