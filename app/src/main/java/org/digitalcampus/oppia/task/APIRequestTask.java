package org.digitalcampus.oppia.task;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.RemoteApiEndpoint;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.HTTPClientUtils;

import okhttp3.Request;

public abstract class APIRequestTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    public static final String TAG = APIRequestTask.class.getSimpleName();

    protected Context ctx;
    protected SharedPreferences prefs;
    protected ApiEndpoint apiEndpoint;

    protected APIRequestTask(Context ctx) {
        this(ctx, new RemoteApiEndpoint());
    }

    protected APIRequestTask(Context ctx, ApiEndpoint api){
        this.ctx = ctx;
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        apiEndpoint = api;
    }

    protected Request createRequestWithUserAuth(String url){
        DbHelper db = DbHelper.getInstance(ctx);
        Request request = null;
        try {
            User u = db.getUser(SessionManager.getUsername(ctx));
            request = new Request.Builder()
                    .url(url)
                    .addHeader(HTTPClientUtils.HEADER_AUTH,
                            HTTPClientUtils.getAuthHeaderValue(u.getUsername(), u.getApiKey()))
                    .build();

        } catch (UserNotFoundException e) {
            Mint.logException(e);
            Log.d(TAG, "User not found: ", e);
        }

        if (request == null){
            //the user was not found, we create it without the header
            request = new Request.Builder().url(url).build();
        }

        return request;
    }
}
