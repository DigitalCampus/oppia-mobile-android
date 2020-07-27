package org.digitalcampus.oppia.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.oppia.exception.WrongServerException;
import org.digitalcampus.oppia.gamification.LeaderboardUtils;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

public class ImportLeaderboardsTask extends AsyncTask<Payload, DownloadProgress, Payload> {


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
    protected Payload doInBackground(Payload... params) {

        final Payload payload = params[0] == null ? new Payload() : params[0];
        payload.setResult(true);

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
                        Mint.logException(e);
                        Log.d(TAG, "Error: ", e);
                        payload.setResult(false);
                    }

                    FileUtils.deleteFile(jsonFile);
                }
            }
        }
        payload.setResult( payload.isResult() || (updatedPositions > 0) );
        return payload;
    }


    @Override
    protected void onPostExecute(Payload p) {
        synchronized (this) {
            if (listener != null) {
                listener.onLeaderboardImportComplete(p.isResult(), p.getResultResponse());
            }
        }
    }

    public void setListener(ImportLeaderboardListener listener) {
        this.listener = listener;
    }
}
