package org.digitalcampus.oppia.service;

import android.content.Context;
import android.util.Log;

import androidx.work.Data;
import androidx.work.ListenableWorker;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.listener.APIRequestFinishListener;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.APIUserRequestTask;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.digitalcampus.oppia.utils.TextUtilsJava;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;

public class UpdateUserProfileWorkerManager implements APIRequestFinishListener, APIRequestListener {

    public static final String TAG = UpdateUserProfileWorkerManager.class.getSimpleName();
    public static final String RESULT_MESSAGE = "message";

    private final Context context;
    private int pendingChecks;
    private OnFinishListener onFinishListener;

    @Inject
    User user;

    public UpdateUserProfileWorkerManager(Context context) {
        this.context = context;
        initializeDaggerBase();
    }

    private void initializeDaggerBase() {
        App app = (App) context.getApplicationContext();
        app.getComponent().inject(this);
    }

    public interface OnFinishListener {
        void onFinish(ListenableWorker.Result result);
    }

    public void setOnFinishListener(OnFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }

    private boolean isUserLoggedIn() {
        return user != null && !TextUtilsJava.isEmpty(user.getUsername());
    }

    public void startChecks() {
        if (isUserLoggedIn()) {
            pendingChecks = 1;
            fetchUserProfile();

        } else {
            if (onFinishListener != null) {
                Data data = new Data.Builder().putString(RESULT_MESSAGE, getString(R.string.user_not_logged_in)).build();
                onFinishListener.onFinish(ListenableWorker.Result.failure(data));
            }
        }

    }

    public void fetchUserProfile() {
        APIUserRequestTask task = new APIUserRequestTask(context);
        String url = Paths.USER_PROFILE_PATH;
        task.setAPIRequestListener(this);
        task.setAPIRequestFinishListener(this, "APIUserRequestTask");
        task.execute(url);
    }


    @Override
    public void apiRequestComplete(BasicResult result) {

        if (result.isSuccess()) {
            try {
                JSONObject response = new JSONObject(result.getResultMessage());
                user.updateFromJSON(context, response);
                DbHelper.getInstance(context).addOrUpdateUser(user);
                new MetaDataUtils(context).saveMetaData(response);
            } catch (JSONException e) {
                Analytics.logException(e);
                Log.d(TAG, "JSON error: ", e);
            }
        }
    }

    private String getString(int stringId) {
        return context.getString(stringId);
    }

    @Override
    public void onRequestFinish(String idRequest) {

        pendingChecks--;
        Log.i(TAG, "onRequestFinish: pendingChecks: " + pendingChecks);

        if ((pendingChecks == 0) && (onFinishListener != null)) {
            onFinishListener.onFinish(ListenableWorker.Result.success());
        }
    }

    @Override
    public void apiKeyInvalidated() {
        SessionManager.logoutCurrentUser(context);
    }
}
