package org.digitalcampus.oppia.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.service.courseinstall.CourseInstall;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImportLeaderboardsTask extends AsyncTask<Payload, DownloadProgress, Payload> {


    public interface ImportLeaderboardListener {
        void onComplete(Boolean success, String message);
    }


    public final static String TAG = ImportLeaderboardsTask.class.getSimpleName();
    private Context ctx;



    private ImportLeaderboardListener listener;

    public ImportLeaderboardsTask(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected Payload doInBackground(Payload... params) {

        final Payload payload = params[0] == null ? new Payload() : params[0];

        File dir = new File(Storage.getLeaderboardImportPath(ctx));
        String[] children = dir.list();
        DbHelper db = DbHelper.getInstance(ctx);

        int updatedPositions = 0;
        if (children != null) {
            for (final String leaderboard_file : children) {

                File json_file = new File(dir, leaderboard_file);
                if (json_file.exists()){
                    try {
                        String json = FileUtils.readFile(json_file);
                        JSONObject leaderboard = new JSONObject(json);
                        String lastUpdateStr = leaderboard.getString("generated_date");
                        Date sdt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(lastUpdateStr);
                        JSONArray positions = leaderboard.getJSONArray("leaderboard");
                        DateTime lastUpdate = new DateTime(sdt);
                        for (int i=0; i<positions.length(); i++){
                            JSONObject pos = positions.getJSONObject(i);
                            boolean updated = db.insertOrUpdateUserLeaderboard(
                                    pos.getString("username"),
                                    pos.getString("first_name") + " " + pos.getString("last_name"),
                                    pos.getInt("points"),
                                    lastUpdate
                            );
                            if (updated){
                                updatedPositions++;
                            }
                        }
                        Log.d(TAG, "pos added:" + updatedPositions);
                        Log.d(TAG, sdt.toString());
                        payload.setResult(true);

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        return payload;
    }


    @Override
    protected void onPostExecute(Payload p) {
        synchronized (this) {
            if (listener != null) {
                listener.onComplete(p.isResult(), p.getResultResponse());
            }
        }
    }

    public void setListener(ImportLeaderboardListener listener) {
        this.listener = listener;
    }
}
