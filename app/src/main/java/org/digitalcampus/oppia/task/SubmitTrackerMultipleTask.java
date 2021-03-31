/*
 * This file is part of OppiaMobile - https://digital-campus.org/
 *
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.task;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.Paths;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.listener.TrackerServiceListener;
import org.digitalcampus.oppia.model.TrackerLog;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.task.result.EntityListResult;
import org.digitalcampus.oppia.task.result.EntityResult;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SubmitTrackerMultipleTask extends APIRequestTask<Void, Integer, EntityListResult<String>> {


    private static final String JSON_EXCEPTION_MESSAGE = "JSON Exception: ";

    private TrackerServiceListener trackerServiceListener;

    public SubmitTrackerMultipleTask(Context ctx) { super(ctx); }
    public SubmitTrackerMultipleTask(Context ctx, ApiEndpoint api) { super(ctx, api); }

    @Override
    protected EntityListResult<String> doInBackground(Void... params) {

        EntityListResult<String> result = new EntityListResult<>();

        boolean submitAttempted = false;

        try {
            DbHelper db = DbHelper.getInstance(ctx);
            List<User> users = db.getAllUsers();

            for (User u: users) {

                boolean offlineUser = u.isOfflineRegister();
                //We try to send the new user to register
                if (offlineUser){
                    Log.d(TAG, "Trying to send user " + u.getUsername() + " to registration...");
                    EntityResult<User> resultRegisterUser = new EntityResult<>();
                    RegisterTask rt = new RegisterTask(ctx);
                    boolean success = rt.submitUserToServer(u, resultRegisterUser, false);
                    Log.d(TAG, "User " + u.getUsername() + " " + (success?"succeeded":"failed"));

                    if (success){
                        db.addOrUpdateUser(u);
                        offlineUser = false;
                    }
                }

                if (!offlineUser){
                    // If we don't get the user registered, then avoid sending the trackers as he would not have an apiKey
                    List<TrackerLog> trackerLogList = db.getUnsentTrackers(u.getUserId());
                    submitAttempted = true;

                    @SuppressWarnings("unchecked")
                    Collection<Collection<TrackerLog>> userTrackers = split(trackerLogList, App.MAX_TRACKER_SUBMIT);
                    sendTrackerBatch(userTrackers, u, result);
                }
            }

            List<File> unsentLogs = getActivityLogsToSend();
            if (!unsentLogs.isEmpty()){
                for (File activityLog : unsentLogs){
                    sendTrackerLog(activityLog, result);
                }
            }

        } catch (IllegalStateException ise) {
            Log.d(TAG, "IllegalStateException:", ise);
            result.setSuccess(false);
        }

        Editor editor = prefs.edit();
        long now = System.currentTimeMillis()/1000;
        editor.putLong(PrefsActivity.PREF_TRIGGER_POINTS_REFRESH, now).apply();

        if (!submitAttempted){
            result.setResultMessage("Trackers from offline registered users could not be sent");
        }
        return result;
    }



    private void sendTrackerLog(File activityLog, EntityListResult<String> result){
        try {
            DbHelper db = DbHelper.getInstance(ctx);
            String dataToSend = org.apache.commons.io.FileUtils.readFileToString(activityLog);

            //We don't need the current user to send this, just some with a valid apiKey
            User user = db.getOneRegisteredUser();

            if (!user.isOfflineRegister()){
                boolean success = sendTrackers(user, dataToSend, true, result);
                result.setSuccess(success);
                if (!success){
                    result.getEntityList().add(activityLog.getName());
                }
                if (success){
                    Log.d(TAG, "Success sending " + activityLog.getName());
                    // If the logs were sent successfully, we can delete the file
                    FileUtils.deleteFile(activityLog);
                }
            }

        } catch (IOException | UserNotFoundException e) {
            Mint.logException(e);
            result.setSuccess(false);
        }

    }

    private boolean sendTrackers(User user, String dataToSend, boolean isRaw, BasicResult result){
        Log.d(TAG, dataToSend);
        DbHelper db = DbHelper.getInstance(ctx);

        try {
            OkHttpClient client = HTTPClientUtils.getClient(ctx);
            Request request = new Request.Builder()
                    .url(apiEndpoint.getFullURL(ctx, isRaw ? Paths.ACTIVITYLOG_PATH : Paths.TRACKER_PATH))
                    .addHeader(HTTPClientUtils.HEADER_AUTH,
                            HTTPClientUtils.getAuthHeaderValue(user.getUsername(), user.getApiKey()))
                    .patch(RequestBody.create(dataToSend, HTTPClientUtils.MEDIA_TYPE_JSON))
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {

                if (!isRaw){
                    JSONObject jsonResp = new JSONObject(response.body().string());
                    db.updateUserBadges(user.getUserId(), jsonResp.getInt("badges"));

                    Editor editor = prefs.edit();
                    try {
                        editor.putBoolean(PrefsActivity.PREF_SCORING_ENABLED, jsonResp.getBoolean("scoring"));
                        editor.putBoolean(PrefsActivity.PREF_BADGING_ENABLED, jsonResp.getBoolean("badging"));
                    } catch (JSONException e) {
                        Log.d(TAG, JSON_EXCEPTION_MESSAGE, e);
                    }
                    editor.apply();

                    saveMetadata(jsonResp);
                }

                return true;

            } else {
                if (response.code() == 400) {
                    // submitted but invalid digest - returned 400 Bad Request -
                    // so record as submitted so doesn't keep trying
                    return true; // OPPIA-217
                } else{
                    Log.d(TAG, "Error sending trackers:" + response.code());
                    Log.d(TAG, "Msg:" + response.body().string());
                    if (response.code() == 401) {
                        //The apiKey of this user is invalid
                        SessionManager.setUserApiKeyValid(user, false);
                        //We don't process more of this user trackers
                    }
                    return false;
                }
            }
        } catch (UnsupportedEncodingException e) {
            Mint.logException(e);
            return false;
        } catch (IOException e) {
            Mint.logException(e);
            result.setResultMessage(ctx.getString(R.string.error_connection));
            return false;
        } catch (JSONException e) {
            Log.d(TAG, JSON_EXCEPTION_MESSAGE, e);
            Mint.logException(e);
            return false;
        }
    }

    private void saveMetadata(JSONObject jsonResp) {
        try {
            JSONObject metadata = jsonResp.getJSONObject("metadata");
            MetaDataUtils mu = new MetaDataUtils(ctx);
            mu.saveMetaData(metadata, prefs);
        } catch (JSONException e) {
            Log.d(TAG, JSON_EXCEPTION_MESSAGE, e);
        }
    }

    private List<File> getActivityLogsToSend(){
        ArrayList<File> files = new ArrayList<>();
        File activityFolder = new File(Storage.getActivityPath(ctx));
        if (activityFolder.exists()){

            String[] children = activityFolder.list();
            for (String dirFile : children) {
                File exportedActivity = new File(activityFolder, dirFile);
                files.add(exportedActivity);
            }
        }
        return files;
    }

    private void sendTrackerBatch(Collection<Collection<TrackerLog>> trackers, User user, BasicResult result) {

        DbHelper db = DbHelper.getInstance(ctx);
        result.setSuccess(true);
        for (Collection<TrackerLog> trackerBatch : trackers) {
            String dataToSend = createDataString(trackerBatch);
            boolean success = sendTrackers(user, dataToSend, false, result);
            result.setSuccess(success);

            if (success){
                for (TrackerLog tl : trackerBatch) {
                    db.markLogSubmitted(tl.getId());
                }
            }
            publishProgress(0);
        }
    }

    @Override
    protected void onProgressUpdate(Integer... obj) {
        synchronized (this) {
            if (trackerServiceListener != null) {
                trackerServiceListener.trackerProgressUpdate();
            }
        }
    }

    @Override
    protected void onPostExecute(EntityListResult<String> result) {
        super.onPostExecute(result);
        synchronized (this) {
            if (trackerServiceListener != null) {
                List<String> failures = result.getEntityList();
                trackerServiceListener.trackerComplete(result.isSuccess(), result.getResultMessage(), failures);
            }
        }
    }

    public void setTrackerServiceListener(TrackerServiceListener tsl) {
        trackerServiceListener = tsl;
    }

    private static Collection<Collection<TrackerLog>> split(Collection<TrackerLog> bigCollection, int maxBatchSize) {
        Collection<Collection<TrackerLog>> result = new ArrayList<>();

        ArrayList<TrackerLog> currentBatch = null;
        for (Object obj : bigCollection) {
            TrackerLog tl = (TrackerLog) obj;
            if (currentBatch == null) {
                currentBatch = new ArrayList<>();
            } else if (currentBatch.size() >= maxBatchSize) {
                result.add(currentBatch);
                currentBatch = new ArrayList<>();
            }

            currentBatch.add(tl);
        }

        if (currentBatch != null) {
            result.add(currentBatch);
        }

        return result;
    }

    private String createDataString(Collection<TrackerLog> collection){
        StringBuilder jsonString = new StringBuilder("{\"objects\":[");
        int counter = 0;
        int collectionSize = collection.size();
        for(TrackerLog tl: collection){
            counter++;
            jsonString.append(tl.getContent());
            if(counter != collectionSize){
                jsonString.append(",");
            }
        }
        jsonString.append("]}");
        return jsonString.toString();
    }

}
