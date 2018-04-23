package org.digitalcampus.oppia.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.listener.ExportActivityListener;
import org.digitalcampus.oppia.model.TrackerLog;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class ExportActivityTask extends AsyncTask<Payload, Integer, String> {

    private static final String TAG = ExportActivityTask.class.getSimpleName();;
    protected Context ctx;
    private SharedPreferences prefs;
    private ExportActivityListener listener;

    public ExportActivityTask(Context ctx) {
        this.ctx = ctx;
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public void setListener(ExportActivityListener l) {
        listener = l;
    }


    @Override
    protected String doInBackground(Payload... payloads) {

        String filename = "activity_" +  new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".json";

        DbHelper db = DbHelper.getInstance(ctx);
        ArrayList<User> users = db.getAllUsers();

        ArrayList<String> userResults = new ArrayList<>();
        for (User u: users) {
            Collection<TrackerLog> userTrackers = db.getUnexportedTrackers(u.getUserId());

            String userJSON = "{ \"username\":\"" + u.getUsername() + "\",";
            userJSON += "\"trackers\":" + TrackerLog.asJSONCollectionString(userTrackers);
            userJSON += "}";
            userResults.add(userJSON);
        }

        String json = "{\"export_date\":\"" + new Date().toString() + "\", ";
        json += "\"server\":\"" + prefs.getString(PrefsActivity.PREF_SERVER, "") + "\", ";
        json += "\"users\":[" + TextUtils.join(",", userResults) + "]}";
        Log.d(TAG, json);
        File destDir = new File(Storage.getActivityPath(ctx));
        if (!destDir.exists() && !destDir.mkdirs()) {
            boolean canWrite = destDir.canWrite();
            Log.d(TAG, "Error creating destination dir: canWrite=" + canWrite);
        }

        File file = new File(destDir, filename);
        try {
            FileOutputStream f = new FileOutputStream(file);
            Writer out = new OutputStreamWriter(f);
            out.write(json);
            out.close();
            f.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        db.markLogsExported();

        return filename;
    }

    @Override
    protected void onPostExecute(String filename) {
        if (listener != null){
            listener.onExportComplete(filename);
        }

    }
}

