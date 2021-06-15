package org.digitalcampus.oppia.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.exception.WrongServerException;
import org.digitalcampus.oppia.gamification.LeaderboardUtils;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

public class ImportLeaderboardsTask extends AsyncTask<Void, DownloadProgress, BasicResult> {


    private static final String TAG = ImportLeaderboardsTask.class.getSimpleName();

    public interface ImportLeaderboardListener {
        void onLeaderboardImportComplete(Boolean success, String message);
    }

    private Context ctx;
    private ImportLeaderboardListener listener;

    public ImportLeaderboardsTask(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected BasicResult doInBackground(Void... params) {

        BasicResult result = new BasicResult();
        result.setSuccess(true);

        File dir = new File(Storage.getLeaderboardImportPath(ctx));
        String[] children = dir.list();

        int updatedPositions = 0;
        if (children != null) {
            for (final String leaderboardFile : children) {

                File jsonFile = new File(dir, leaderboardFile);
                if (jsonFile.exists()){
                    try {
                        String json = FileUtils.readFile(jsonFile);
                        updatedPositions += LeaderboardUtils.importLeaderboardJSON(ctx, json);

                    } catch (IOException | WrongServerException | ParseException | JSONException e) {
                        Analytics.logException(e);
                        Log.d(TAG, "Error: ", e);
                        result.setSuccess(false);
                    }

                    FileUtils.deleteFile(jsonFile);
                }
            }
        }
        result.setSuccess( result.isSuccess() || (updatedPositions > 0) );
        return result;
    }


    @Override
    protected void onPostExecute(BasicResult result) {
        synchronized (this) {
            if (listener != null) {
                listener.onLeaderboardImportComplete(result.isSuccess(), result.getResultMessage());
            }
        }
    }

    public void setListener(ImportLeaderboardListener listener) {
        this.listener = listener;
    }
}
