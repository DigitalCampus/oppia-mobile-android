package org.digitalcampus.oppia.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.listener.ExportActivityListener;
import org.digitalcampus.oppia.model.CustomField;
import org.digitalcampus.oppia.model.CustomValue;
import org.digitalcampus.oppia.model.QuizAttempt;
import org.digitalcampus.oppia.model.TrackerLog;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import androidx.preference.PreferenceManager;

public class ExportActivityTask extends AsyncTask<Integer, Integer, BasicResult> {

    private static final String TAG = ExportActivityTask.class.getSimpleName();
    protected Context ctx;
    private SharedPreferences prefs;
    private ExportActivityListener listener;

    public static final int UNEXPORTED_ACTIVITY = 0;
    public static final int FULL_EXPORT_ACTIVTY = 1;

    public static String activityTimestampFormat = "yyyyMMddHHmmss";

    public ExportActivityTask(Context ctx) {
        this.ctx = ctx;
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public void setListener(ExportActivityListener l) {
        listener = l;
    }


    @Override
    protected BasicResult doInBackground(Integer... params) {

        DbHelper db = DbHelper.getInstance(ctx);
        List<User> users = db.getAllUsers();

        BasicResult result = new BasicResult();

        int typeActivity = params[0];

        int trackersCount = 0;
        int offlineUsersCount = 0;
        ArrayList<String> userResults = new ArrayList<>();
        for (User u: users) {

            Collection<TrackerLog> userTrackers;
            Collection<QuizAttempt> userQuizzes;

            switch (typeActivity) {
                case UNEXPORTED_ACTIVITY:
                    userTrackers = db.getUnexportedTrackers(u.getUserId());
                    userQuizzes = db.getUnexportedQuizAttempts(u.getUserId());
                    break;

                case FULL_EXPORT_ACTIVTY:
                    userTrackers = db.getTrackers(u.getUserId(), false);
                    userQuizzes = db.getQuizAttempts(u.getUserId(), false);
                    break;

                default:
                    throw new IllegalArgumentException("Missing activity type");
            }

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
            result.setSuccess(false);
            result.setResultMessage(ctx.getString(R.string.export_task_no_activities));
            return result;
        }

        String json = "{\"export_date\":\"" + new Date().toString() + "\", ";
        json += "\"server\":\"" + prefs.getString(PrefsActivity.PREF_SERVER, "") + "\", ";
        json += "\"users\":[" + TextUtils.join(",", userResults) + "]}";
        Log.d(TAG, json);
        String destPath = typeActivity == UNEXPORTED_ACTIVITY ? Storage.getActivityPath(ctx)
                : Storage.getActivityFullExportPath(ctx);
        File destDir = new File(destPath);
        if (!destDir.exists() && !destDir.mkdirs()) {
            boolean canWrite = destDir.canWrite();
            Log.d(TAG, "Error creating destination dir: canWrite=" + canWrite);
            result.setSuccess(false);
            result.setResultMessage("Error creating destination dir: canWrite=" + canWrite);
            return result;
        }

        String filename = (users.size() > 1) ? "activity" : users.get(0).getUsername();
        filename += "_" + new SimpleDateFormat(activityTimestampFormat).format(new Date()) + ".json";

        if (typeActivity == FULL_EXPORT_ACTIVTY) {
            filename = "full-export-" + filename;
        }

        File file = new File(destDir, filename);
        FileOutputStream f = null;
        Writer out = null;
        try {
            f = new FileOutputStream(file);
            out = new OutputStreamWriter(f);
            out.write(json);
        } catch (FileNotFoundException e) {
            Analytics.logException(e);
            Log.d(TAG, "FileNotFoundException: ", e);
            result.setSuccess(false);
            result.setResultMessage("FileNotFoundException");
            return result;
        } catch (IOException e) {
            Analytics.logException(e);
            Log.d(TAG, "IO exception: ", e);
            result.setSuccess(false);
            result.setResultMessage("IO exception");
            return result;
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

        if (typeActivity == UNEXPORTED_ACTIVITY) {
            db.markLogsAndQuizzesExported();
        }

        result.setSuccess(true);
        result.setResultMessage(filename);
        return result;
    }

    @Override
    protected void onPostExecute(BasicResult result) {
        if (listener != null){
            listener.onExportComplete(result);
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

