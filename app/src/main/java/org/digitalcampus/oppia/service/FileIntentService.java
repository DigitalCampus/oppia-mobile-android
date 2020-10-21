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

package org.digitalcampus.oppia.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import org.digitalcampus.mobile.learning.R;

import java.util.ArrayList;

public abstract class FileIntentService extends IntentService {

    public static final String TAG = DownloadService.class.getSimpleName();

    public static final String SERVICE_ACTION = "action"; //field for providing action
    public static final String SERVICE_URL = "fileurl"; //field for providing file URL
    public static final String SERVICE_MESSAGE = "message";

    public static final String ACTION_UPDATE = "update";
    public static final String ACTION_CANCEL = "cancel";
    public static final String ACTION_DOWNLOAD = "download";
    public static final String ACTION_COMPLETE = "complete";
    public static final String ACTION_FAILED = "failed";

    protected ArrayList<String> tasksCancelled;
    protected ArrayList<String> tasksDownloading;


    protected FileIntentService(String tag) { super(tag); }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.hasExtra(SERVICE_ACTION) && intent.hasExtra(SERVICE_URL)) {
            // Set the canceling flag to that file
            if (intent.getStringExtra(SERVICE_ACTION).equals(ACTION_CANCEL)){
                Log.d(TAG, "CANCEL commmand received");
                addCancelledTask(intent.getStringExtra(SERVICE_URL));
            }
            else if (intent.getStringExtra(SERVICE_ACTION).equals(ACTION_DOWNLOAD) ||
                    intent.getStringExtra(SERVICE_ACTION).equals(ACTION_UPDATE)) {
                addDownloadingTask(intent.getStringExtra(SERVICE_URL));
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    protected void logAndNotifyError(String fileUrl, Exception e){
        Log.d(TAG, "Error: " + e.getMessage());
        sendBroadcast(fileUrl, ACTION_FAILED, this.getString(R.string.error_media_download));
        removeDownloading(fileUrl);
    }

    protected abstract String getBroadcastAction();

    /*
     * Sends a new Broadcast with the results of the action
     */
    protected void sendBroadcast(String fileUrl, String result, String message){

        Intent localIntent = new Intent(getBroadcastAction());
        localIntent.putExtra(SERVICE_ACTION, result);
        localIntent.putExtra(SERVICE_URL, fileUrl);
        if (message != null){
            localIntent.putExtra(SERVICE_MESSAGE, message);
        }
        // Broadcasts the Intent to receivers in this app.
        Log.d(TAG, fileUrl + "=" + result + ":" + message);
        sendOrderedBroadcast(localIntent, null);

    }

    protected void addCancelledTask(String fileUrl){
        if (tasksCancelled == null){
            tasksCancelled = new ArrayList<>();
        }
        if (!tasksCancelled.contains(fileUrl)){
            tasksCancelled.add(fileUrl);
        }
    }

    protected boolean isCancelled(String fileUrl){
        return (tasksCancelled != null) && (tasksCancelled.contains(fileUrl));
    }

    protected void removeCancelled(String fileUrl) {
        if (tasksCancelled != null){
            tasksCancelled.remove(fileUrl);
        }
    }

    protected void addDownloadingTask(String fileUrl){
        if (tasksDownloading == null){
            tasksDownloading = new ArrayList<>();
        }
        if (!tasksDownloading.contains(fileUrl)){
            synchronized (this){
                tasksDownloading.add(fileUrl);
            }
        }
    }

    protected void removeDownloading(String fileUrl){
        if (tasksDownloading != null){
            synchronized (this){
                tasksDownloading.remove(fileUrl);
            }
        }
    }

}