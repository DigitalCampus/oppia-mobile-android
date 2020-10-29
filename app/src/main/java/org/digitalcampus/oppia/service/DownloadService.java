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

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.utils.HTTPClientUtils;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.ui.OppiaNotificationUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import androidx.core.app.NotificationCompat;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadService extends FileIntentService {

    public static final String TAG = DownloadService.class.getSimpleName();
    public static final String BROADCAST_ACTION = "com.digitalcampus.oppia.DOWNLOADSERVICE";

    public static final String SERVICE_MESSAGE = "message";
    public static final String SERVICE_FILENAME = "filename";
    public static final String SERVICE_DIGEST = "digest";

    private static DownloadService currentInstance;
    private BroadcastReceiver alternativeNotifier;

    private static void setInstance(DownloadService instance){
        currentInstance = instance;
    }

    public static List<String> getTasksDownloading(){
        if (currentInstance != null){
            synchronized (currentInstance){
                return currentInstance.tasksDownloading;
            }
        }
        return new ArrayList<>();
    }

    public DownloadService() { super(TAG); }

    protected String getBroadcastAction(){
        return BROADCAST_ACTION;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        DownloadService.setInstance(this);

        alternativeNotifier = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!intent.hasExtra(DownloadService.SERVICE_URL) || !intent.hasExtra(DownloadService.SERVICE_ACTION)){
                    //If the file URL and the action are not present, we can't identify it
                    return;
                }
                String action = intent.getStringExtra(DownloadService.SERVICE_ACTION);
                DownloadService.this.notifyDownloads(action);
            }
        };

        //We register the alternative notifier for sending notifications when no other BroadcasReceiver is set
        IntentFilter broadcastFilter = new IntentFilter(DownloadService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_LOW_PRIORITY);
        registerReceiver(alternativeNotifier, broadcastFilter);

    }

    private void notifyDownloads(String action) {
        //If there are no more pending downloads after the completion of this one, send a Notification
        if (action.equals(FileIntentService.ACTION_COMPLETE) && (tasksDownloading==null || tasksDownloading.isEmpty())){
            Log.d(TAG, "Sending notification from Service for the completion of all pending media downloads");

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(), PendingIntent.FLAG_CANCEL_CURRENT);
            NotificationCompat.Builder mBuilder  = OppiaNotificationUtils.getBaseBuilder(this, true);
            mBuilder.setContentTitle(getString(R.string.app_name))
                    .setContentText(getString(R.string.notification_media_subject))
                    .setContentIntent(contentIntent)
                    .build();

            OppiaNotificationUtils.sendNotification(this, 0, mBuilder.build());
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.hasExtra(SERVICE_ACTION) && intent.hasExtra(SERVICE_URL)) {
            // Set the canceling flag to that file
            if (intent.getStringExtra(SERVICE_ACTION).equals(ACTION_CANCEL)){
                Log.d(TAG, "CANCEL commmand received");
                addCancelledTask(intent.getStringExtra(SERVICE_URL));
            }
            else if (intent.getStringExtra(SERVICE_ACTION).equals(ACTION_DOWNLOAD)) {
                addDownloadingTask(intent.getStringExtra(SERVICE_URL));
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent.hasExtra (SERVICE_ACTION)) {
            boolean cancel = intent.getStringExtra(SERVICE_ACTION).equals(ACTION_CANCEL);
            //We have nothing more to do with a 'cancel' action than what is done in onStartCommand()
            if (cancel) { return; }
        }

        if (!intent.hasExtra(SERVICE_URL)){
            Log.d(TAG, "No Media passed to the service. Invalid task");
            return;
        }

        String fileUrl = intent.getStringExtra(SERVICE_URL);
        String filename = intent.getStringExtra(SERVICE_FILENAME);
        String fileDigest = intent.getStringExtra(SERVICE_DIGEST);

        if (isCancelled(fileUrl)) {
            //If it was cancelled before starting, we do nothing
            Log.d(TAG, "Media " + fileUrl + " cancelled before started.");
            removeCancelled(fileUrl);
            removeDownloading(fileUrl);
            return;
        }

        downloadFile(fileUrl, filename, fileDigest);

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        DownloadService.setInstance(null);
        unregisterReceiver(alternativeNotifier);
    }

    private void downloadFile(String fileUrl, String filename, String fileDigest){

        File downloadedFile = null;
        FileOutputStream f = null;

        try {
            URL url = new URL(fileUrl);
            //If no filename was passed, we set the filename based on the URL
            if (filename == null){ filename = url.getPath().substring(url.getPath().lastIndexOf('/')+1); }
            downloadedFile = new File(Storage.getMediaPath(this), filename);

            OkHttpClient client = HTTPClientUtils.getClient(this);
            Request request = new Request.Builder().url(fileUrl).build();
            Response response = client.newCall(request).execute();
            long fileLength = response.body().contentLength();
            long availableStorage = Storage.getAvailableStorageSize(this);

            if (fileLength >= availableStorage){
                sendBroadcast(fileUrl, ACTION_FAILED, this.getString(R.string.error_insufficient_storage_available));
                removeDownloading(fileUrl);
                return;
            }

            f = new FileOutputStream(downloadedFile);
            InputStream in = response.body().byteStream();

            MessageDigest mDigest = MessageDigest.getInstance("MD5");
            in = new DigestInputStream(in, mDigest);

            byte[] buffer = new byte[8192];
            int len1;
            long total = 0;
            int previousProgress = 0;
            int progress;
            while ((len1 = in.read(buffer)) > 0) {
                //If received a cancel action while downloading, stop it
                if (isCancelled(fileUrl)) {
                    Log.d(TAG, "Media " + filename + " cancelled while downloading. Deleting temp file...");
                    f.close();
                    in.close();
                    deleteFile(downloadedFile);
                    removeCancelled(fileUrl);
                    removeDownloading(fileUrl);
                    return;
                }

                total += len1;
                progress = (int)((total*100)/fileLength);
                if (progress > previousProgress){
                    sendBroadcast(fileUrl, ACTION_DOWNLOAD, ""+progress);
                    previousProgress = progress;
                }
                f.write(buffer, 0, len1);
            }
            in.close();
            if (fileDigest != null){
                // check the file digest matches, otherwise delete the file
                // (it's either been a corrupted download or it's the wrong file)
                String md5Digest = FileUtils.getDigestFromMessage(mDigest);
                if(!md5Digest.contains(fileDigest)){
                    this.deleteFile(downloadedFile);
                    sendBroadcast(fileUrl, ACTION_FAILED, this.getString(R.string.error_media_download));
                    removeDownloading(fileUrl);
                }
            }

        } catch (MalformedURLException|NoSuchAlgorithmException e) {
            Mint.logException(e);
            logAndNotifyError(fileUrl, e);
        } catch (IOException e) {
            Mint.logException(e);
            this.deleteFile(downloadedFile);
            logAndNotifyError(fileUrl, e);
        } finally {
            if (f != null){
               try {
                    f.close();
               } catch (IOException ioe) {
                   Log.d(TAG, "couldn't close FileOutputStream object", ioe);
               }
            }
        }

        Log.d(TAG, fileUrl + " successfully downloaded");
        removeDownloading(fileUrl);
        sendBroadcast(fileUrl, ACTION_COMPLETE, null);
    }


    private void deleteFile(File file){
        if ((file != null) && file.exists() && !file.isDirectory()){
            Log.e(TAG, "Removing file: " + file.getAbsolutePath());
            if (!file.delete()){
                Log.e(TAG, "deleteFile: File could not be deleted: " + file.getAbsolutePath());
            }
        }
    }

}