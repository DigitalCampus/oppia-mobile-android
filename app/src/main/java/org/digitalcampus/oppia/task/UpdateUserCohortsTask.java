package org.digitalcampus.oppia.task;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpdateUserCohortsTask {
    public static final String TAG = UpdateUserCohortsTask.class.getSimpleName();

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private UpdateUserCohortsListener listener;

    public interface UpdateUserCohortsListener{
        void onComplete();
    }

    public void setListener(UpdateUserCohortsListener listener){
        this.listener = listener;
    }

    public void updateLoggedUserCohorts(Context ctx, ApiEndpoint apiEndpoint, User localUser){
        executor.execute(() -> {
            try {
                OkHttpClient client = HTTPClientUtils.getClient(ctx);
                String url = apiEndpoint.getFullURL(ctx, Paths.USER_COHORTS_PATH);
                Request request = new Request.Builder()
                        .url(HTTPClientUtils.getUrlWithCredentials(url, localUser.getUsername(), localUser.getApiKey()))
                        .build();
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    JSONArray cohortsJson = new JSONArray(response.body().string());
                    localUser.setCohortsFromJSONArray(cohortsJson);
                    DbHelper.getInstance(ctx).addOrUpdateUser(localUser);
                }

                handler.post(() -> {
                    listener.onComplete();
                });
            } catch (JSONException | IOException e) {
                Log.w(TAG, "Unable to update user cohorts: ", e);
            }
        });
    }
}
