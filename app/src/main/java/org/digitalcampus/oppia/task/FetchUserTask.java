package org.digitalcampus.oppia.task;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FetchUserTask {
    public static final String TAG = FetchUserTask.class.getSimpleName();

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private FetchUserListener listener;

    public interface FetchUserListener {
        void onComplete();
    }

    public void setListener(FetchUserListener listener){
        this.listener = listener;
    }

    public void updateLoggedUserProfile(Context ctx, ApiEndpoint apiEndpoint, User localUser){
        executor.execute(() -> {
            try {
                OkHttpClient client = HTTPClientUtils.getClient(ctx);
                String url = apiEndpoint.getFullURL(ctx, Paths.USER_PROFILE_PATH);
                Request request = new Request.Builder()
                        .url(HTTPClientUtils.getUrlWithCredentials(url, localUser.getUsername(), localUser.getApiKey()))
                        .build();
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        localUser.updateFromJSON(ctx, json);
                        DbHelper.getInstance(ctx).addOrUpdateUser(localUser);
                        new MetaDataUtils(ctx).saveMetaData(json);
                    } catch (JSONException e) {
                        Analytics.logException(e);
                        Log.d(TAG, "JSON error: ", e);
                    }
                }

            } catch (IOException e) {
                Log.w(TAG, "Unable to update user profile: ", e);
            }

            handler.post(() -> {
                if (listener != null) {
                    listener.onComplete();
                }
            });
        });
    }
}
