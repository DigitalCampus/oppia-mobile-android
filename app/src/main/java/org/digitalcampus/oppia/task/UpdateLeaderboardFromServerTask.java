package org.digitalcampus.oppia.task;

import android.content.Context;
import android.util.Log;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.exception.WrongServerException;
import org.digitalcampus.oppia.gamification.LeaderboardUtils;
import org.digitalcampus.oppia.listener.SubmitListener;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateLeaderboardFromServerTask extends APIRequestTask<Void, Object, BasicResult> {

    private SubmitListener listener;

    public UpdateLeaderboardFromServerTask(Context ctx, ApiEndpoint api) {
        super(ctx, api);
    }

    @Override
    protected BasicResult doInBackground(Void... params) {

        BasicResult result = new BasicResult();
        
        try {
            OkHttpClient client = HTTPClientUtils.getClient(ctx);
            Request request = createRequestBuilderWithUserAuth(apiEndpoint.getFullURL(ctx, Paths.LEADERBOARD_PATH)).build();
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                String json = response.body().string();
                int updatedPositions = 0;
                try {
                    updatedPositions += LeaderboardUtils.importLeaderboardJSON(ctx, json);
                    result.setSuccess(true);
                    result.setResultMessage(updatedPositions + " updated.");
                } catch (ParseException e) {
                    Analytics.logException(e);
                    Log.d(TAG, "ParseException:", e);
                    result.setSuccess(false);
                } catch (JSONException e) {
                    Analytics.logException(e);
                    Log.d(TAG, "JSONException:", e);
                    result.setSuccess(false);
                } catch (WrongServerException e) {
                    Analytics.logException(e);
                    Log.d(TAG, "WrongServerException:", e);
                    result.setSuccess(false);
                }
            }
            else{
                if (response.code() == 401){
                    invalidateApiKey(result);
                }
                if (response.code() == 404){
                    result.setResultMessage("Your server version is old and does not support leaderboard export.");
                }
                else{

                }
                result.setSuccess(false);
                response.body().close();
            }

        } catch (IOException e) {
            Analytics.logException(e);
            Log.d(TAG, "IOException:", e);
            result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.error_connection_required));
        }

        return result;

    }

    @Override
    protected void onPostExecute(BasicResult result) {

        LeaderboardUtils.updateLeaderboardFetchTime(prefs);
        synchronized (this) {
            if (listener != null) {
                listener.submitComplete(result);
            }
        }
    }


    public void seListener(SubmitListener srl) {
        synchronized (this) {
            listener = srl;
        }
    }
}
