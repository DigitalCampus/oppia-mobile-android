package org.digitalcampus.oppia.task;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.HTTPClientUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
        String filename = data + ".html";

        final ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "text/html");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        final ContentResolver resolver = ctx.getContentResolver();
        Uri uri = null;
        OutputStream f = null;

        try {
            final Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            uri = resolver.insert(contentUri, values);

            if (uri == null)
                throw new IOException("Failed to create new MediaStore record.");

            f = resolver.openOutputStream(uri);
            if (f == null)
                throw new IOException("Failed to open output stream.");

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()){
                InputStream in = response.body().byteStream();

                byte[] buffer = new byte[8192];
                int len1;
                while ((len1 = in.read(buffer)) > 0) {
                    f.write(buffer, 0, len1);
                }
                in.close();
                result.setSuccess(true);
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

        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            Analytics.logException(e);
            result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.error_download_failure));
        } finally {
            if (uri != null) {
                // Don't leave an orphan entry in the MediaStore
                resolver.delete(uri, null, null);
            }

            if (f != null){
                try {
                    f.close();
                } catch (IOException ioe) {
                    Log.d(TAG, "couldn't close FileOutputStream object", ioe);
                }
            }
        }

        return result;
    }

}
