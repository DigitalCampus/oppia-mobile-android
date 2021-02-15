package org.digitalcampus.oppia.task;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.preference.PreferenceManager;
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.RemoteApiEndpoint;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.listener.APIRequestFinishListener;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.HTTPClientUtils;

import okhttp3.Request;

public abstract class APIRequestTask<P, G, R> extends AsyncTask<P, G, R> {

    public static final String TAG = APIRequestTask.class.getSimpleName();

    private APIRequestFinishListener listener;

    protected Context ctx;
    protected SharedPreferences prefs;
    protected ApiEndpoint apiEndpoint;
    private String nameRequest;

    protected APIRequestTask(Context ctx) {
        this(ctx, new RemoteApiEndpoint());
    }

    protected APIRequestTask(Context ctx, ApiEndpoint api) {
        this.ctx = ctx;
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        apiEndpoint = api;
    }

    Request.Builder createRequestBuilderWithUserAuth(String url) {
        DbHelper db = DbHelper.getInstance(ctx);
        Request.Builder requestBuilder = null;
        try {
            User u = db.getUser(SessionManager.getUsername(ctx));
            requestBuilder = new Request.Builder()
                    .url(url)
                    .addHeader(HTTPClientUtils.HEADER_AUTH,
                            HTTPClientUtils.getAuthHeaderValue(u.getUsername(), u.getApiKey()));

        } catch (UserNotFoundException e) {
            Mint.logException(e);
            Log.d(TAG, "User not found: ", e);
        }

        if (requestBuilder == null) {
            //the user was not found, we create it without the header
            requestBuilder = new Request.Builder().url(url);
        }

        return requestBuilder;
    }

    @Override
    protected void onPostExecute(R result) {
        super.onPostExecute(result);

        if (listener != null) {
            listener.onRequestFinish(nameRequest);
        }
    }

    protected void invalidateApiKey(Payload payload){
        // If the server returns a 401 error it means an invalid APIkey
        try {
            User u = DbHelper.getInstance(ctx).getUser(SessionManager.getUsername(ctx));
            SessionManager.setUserApiKeyValid(u, false);
        } catch (UserNotFoundException e) {
            Mint.logException(e);
        }
        payload.setResult(false);
        payload.setResultResponse(ctx.getString(org.digitalcampus.mobile.learning.R.string.error_apikey_expired ));
    }

    public void setAPIRequestFinishListener(APIRequestFinishListener listener, String nameRequest) {
        this.nameRequest = nameRequest;
        this.listener = listener;

    }
}
