package org.digitalcampus.oppia.service;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.work.Data;
import androidx.work.ListenableWorker;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

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
import org.digitalcampus.oppia.utils.TextUtilsJava;

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;

public class UserCohortsChecksWorkerManager implements APIRequestFinishListener, APIRequestListener {

    public static final String TAG = UserCohortsChecksWorkerManager.class.getSimpleName();
    public static final String RESULT_MESSAGE = "message";

    private Context context;
    private int pendingChecks;
    private OnFinishListener onFinishListener;

    @Inject
    User user;

    public UserCohortsChecksWorkerManager(Context context) {
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

            checkUserCohortsUpdates();

        } else {
            if (onFinishListener != null) {
                Data data = new Data.Builder().putString(RESULT_MESSAGE, getString(R.string.user_not_logged_in)).build();
                onFinishListener.onFinish(ListenableWorker.Result.failure(data));
            }
        }

    }

    public void checkUserCohortsUpdates() {
        APIUserRequestTask task = new APIUserRequestTask(context);
        String url = Paths.USER_COHORTS_PATH;
        task.setAPIRequestListener(this);
        task.setAPIRequestFinishListener(this, "APIUserRequestTask");
        task.execute(url);
    }


    @Override
    public void apiRequestComplete(BasicResult result) {

        if (result.isSuccess()) {

            try {

                Type listType = new TypeToken<List<Integer>>(){}.getType();
                List<Integer> userCohorts = new Gson().fromJson(result.getResultMessage(), listType);

                user.setCohorts(userCohorts);
                DbHelper.getInstance(context).addOrUpdateUser(user);

                onFinishListener.onFinish(ListenableWorker.Result.success());

            } catch (JsonSyntaxException e) {
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
