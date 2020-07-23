package org.digitalcampus.oppia.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.listener.ExportActivityListener;
import org.digitalcampus.oppia.model.CustomField;
import org.digitalcampus.oppia.model.CustomValue;
import org.digitalcampus.oppia.model.QuizAttempt;
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
import java.util.List;

public class ExportActivityTask extends AsyncTask<Payload, Integer, String> {

    private static final String TAG = ExportActivityTask.class.getSimpleName();
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

        DbHelper db = DbHelper.getInstance(ctx);
        List<User> users = db.getAllUsers();

        int trackersCount = 0;
        int offlineUsersCount = 0;
        ArrayList<String> userResults = new ArrayList<>();
        for (User u: users) {
            Collection<TrackerLog> userTrackers = db.getUnexportedTrackers(u.getUserId());
            Collection<QuizAttempt> userQuizzes = db.getUnexportedQuizAttempts(u.getUserId());

            trackersCount+= userTrackers.size();
            trackersCount+= userQuizzes.size();

            String userJSON = "{ \"username\":\"" + u.getUsername() + "\",";
            userJSON += "\"email\":\"" + u.getEmail() + "\", ";
            if (u.isOfflineRegister()){
                offlineUsersCount++;
                userJSON += "\"password\":\"" + u.getPasswordHashed() + "\", ";
            }
            userJSON += "\"firstname\":\"" + u.getFirstname() + "\", ";
            userJSON += "\"lastname\":\"" + u.getLastname() + "\", ";
            if (!TextUtils.isEmpty(u.getOrganisation())){
                userJSON += "\"organisation\":\"" + u.getOrganisation() + "\", ";
            }
            userJSON += "\"jobtitle\":\"" + u.getJobTitle() + "\", ";
            userJSON += "\"phoneno\":\"" + u.getPhoneNo() + "\", ";

            addCustomFields(userJSON, u);

            userJSON += "\"trackers\":" + TrackerLog.asJSONCollectionString(userTrackers) + ", ";
            userJSON += "\"quizresponses\":" + QuizAttempt.asJSONCollectionString(userQuizzes) + ", ";
            userJSON += "\"points\":[]";
            userJSON += "}";
            userResults.add(userJSON);
        }

        if ((trackersCount <= 0) && (offlineUsersCount <=0)){
            //We didn't have any new tracker!
            Log.d(TAG, "There are no new trackers to export...");
            return null;
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

        String filename = (users.size() > 1) ? "activity" : users.get(0).getUsername();
        filename += "_" + new SimpleDateFormat("yyyyMMddhhmm").format(new Date()) + ".json";

        File file = new File(destDir, filename);
        FileOutputStream f = null;
        Writer out = null;
        try {
            f = new FileOutputStream(file);
            out = new OutputStreamWriter(f);
            out.write(json);
        } catch (FileNotFoundException e) {
            Mint.logException(e);
            Log.d(TAG, "FileNotFoundException: ", e);
        } catch (IOException e) {
            Mint.logException(e);
            Log.d(TAG, "IO exception: ", e);
        } finally {
            if (out != null){
                try {
                    out.close();
                } catch (IOException ioe) {
                    Log.d(TAG, "couldn't close OutputStreamWriter object", ioe);
                }
            }
            if (f != null){
                try {
                    f.close();
                } catch (IOException ioe) {
                    Log.d(TAG, "couldn't close FileOutputStream object", ioe);
                }
            }
        }

        db.markLogsAndQuizzesExported();

        return filename;
    }

    @Override
    protected void onPostExecute(String filename) {
        if (listener != null){
            listener.onExportComplete(filename);
        }

    }

    private void addCustomFields(String json, User u){

        List<CustomField> customFields = DbHelper.getInstance(ctx).getCustomFields();

        for (CustomField field : customFields){
            CustomValue value = u.getCustomField(field.getKey());
            if (value != null){
                json += "\"" + field.getKey() + "\":"
                        + (field.isExportedAsString() ? "\"" : "")
                        + value.getValue().toString()
                        + (field.isExportedAsString() ? "\"" : "")
                        + ", ";
            }
        }
    }
}

