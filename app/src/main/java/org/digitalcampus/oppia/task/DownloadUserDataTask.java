package org.digitalcampus.oppia.task;

import android.content.Context;
import android.util.Log;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.HTTPClientUtils;

import java.io.File;
import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadUserDataTask extends APIUserRequestTask{
    public DownloadUserDataTask(Context ctx) {
        super(ctx);
    }
    public DownloadUserDataTask(Context ctx, ApiEndpoint api) {
        super(ctx, api);
    }

    @Override
    protected BasicResult doInBackground(String... params){
        String data = params[0];
        String url = Paths.DOWNLOAD_ACCOUNT_DATA_PATH + data + File.separator;
        BasicResult result = new BasicResult();
        OkHttpClient client = HTTPClientUtils.getClient(ctx);
        Request request = createRequestBuilderWithUserAuth(apiEndpoint.getFullURL(ctx, url)).build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                result.setSuccess(true);
                result.setResultMessage(response.body().string());
            }
            else{
                if (response.code() == 401) {
                    invalidateApiKey(result);
                    apiKeyInvalidated = true;
                } else {
                    result.setSuccess(false);
                    result.setResultMessage(ctx.getString(R.string.error_connection));
                }
            }

        }  catch (IOException e) {
            Analytics.logException(e);
            Log.d(TAG, "IO exception", e);
            result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.error_connection));
        }

        return result;
    }

}
