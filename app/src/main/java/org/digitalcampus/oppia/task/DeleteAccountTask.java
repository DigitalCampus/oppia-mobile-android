package org.digitalcampus.oppia.task;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.HTTPClientUtils;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DeleteAccountTask extends APIUserRequestTask{
    public DeleteAccountTask(Context ctx) {
        super(ctx);
    }

    public DeleteAccountTask(Context ctx, ApiEndpoint api) {
        super(ctx, api);
    }

    @Override
    protected BasicResult doInBackground(String... params){
        String password = params[0];

        String passwordJSON = "{\"password\":\"" + password + "\"}";
        BasicResult result = new BasicResult();
        OkHttpClient client = HTTPClientUtils.getClient(ctx);
        Request request = createRequestBuilderWithUserAuth(apiEndpoint.getFullURL(ctx, Paths.DELETE_ACCOUNT_PATH))
                .post(RequestBody.create(passwordJSON, HTTPClientUtils.MEDIA_TYPE_JSON)).build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                result.setSuccess(true);
                result.setResultMessage(response.body().string());
                String currentUser = SessionManager.getUsername(ctx);
                DbHelper.getInstance(ctx).deleteUser(currentUser);
            }
            else{
                switch (response.code()) {
                    case 401:
                        invalidateApiKey(result);
                        apiKeyInvalidated = true;
                        break;

                    case 400:
                    case 403: // unauthorised
                        result.setSuccess(false);
                        String message = response.body().string();
                        if (TextUtils.isEmpty(message)){
                            result.setResultMessage(message);
                        }
                        else{
                            result.setResultMessage(ctx.getString(R.string.error_login));
                        }
                        break;

                    default:
                        result.setSuccess(false);
                        result.setResultMessage(ctx.getString(R.string.error_connection));
                }
            }
        }
        catch (IOException e) {
            Analytics.logException(e);
            Log.d(TAG, "IO exception", e);
            result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.error_connection));
        }

        return result;
    }

}
