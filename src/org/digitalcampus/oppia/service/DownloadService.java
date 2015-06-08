package org.digitalcampus.oppia.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.utils.storage.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class DownloadService extends IntentService {

    public static final String TAG = TrackerService.class.getSimpleName();
    private static final String BROADCAST_ACTION = "com.digitalcampus.oppia.DOWNLOADSERVICE";

    private static final String SERVICE_ACTION = "action"; //field for providing action
    private static final String SERVICE_URL = "fileurl"; //field for providing file URL
    private static final String SERVICE_MESSAGE = "message";
    private static final String SERVICE_FILENAME = "filename";
    private static final String SERVICE_DIGEST = "digest";

    private static final String ACTION_CANCEL = "cancel";
    private static final String ACTION_DOWNLOAD = "download";
    private static final String ACTION_COMPLETE = "complete";
    private static final String ACTION_FAILED = "failed";

    private static ArrayList<String> tasksCancelled;
    private SharedPreferences prefs;

    public DownloadService(String name) {
        super(name);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.hasExtra(SERVICE_ACTION) && intent.hasExtra(SERVICE_URL)) {
            // Set the canceling flag to that file
            boolean cancelDownload = intent.getStringExtra(SERVICE_ACTION).equals(ACTION_CANCEL);
            if (cancelDownload){
                addCancelledTask(intent.getStringExtra(SERVICE_URL));
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
        File downloadedFile = null;

        try {
            URL url = new URL(fileUrl);
            //If no filename was passed, we set the filename based on the URL
            if (filename == null){ filename = url.getPath().substring(url.getPath().lastIndexOf("/")+1); }
            downloadedFile = new File(FileUtils.getMediaPath(this), filename);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.connect();

            connection.setConnectTimeout(Integer.parseInt(
                    prefs.getString(PrefsActivity.PREF_SERVER_TIMEOUT_CONN,
                    this.getString(R.string.prefServerTimeoutConnection))));
            connection.setReadTimeout(Integer.parseInt(
                    prefs.getString(PrefsActivity.PREF_SERVER_TIMEOUT_RESP,
                    this.getString(R.string.prefServerTimeoutResponse))));

            long fileLength = connection.getContentLength();
            long availableStorage = FileUtils.getAvailableStorageSize(this);

            if (fileLength >= availableStorage){
                sendBroadcast(fileUrl, ACTION_FAILED, this.getString(R.string.error_insufficient_storage_available));
                return;
            }

            FileOutputStream f = new FileOutputStream(downloadedFile);
            InputStream in = connection.getInputStream();

            MessageDigest mDigest = MessageDigest.getInstance("MD5");
            in = new DigestInputStream(in, mDigest);

            byte[] buffer = new byte[8192];
            int len1;
            long total = 0;
            int previousProgress = 0, progress = 0;
            while ((len1 = in.read(buffer)) > 0) {
                //If received a cancel action while downloading, stop it
                if (isCancelled(fileUrl)) {
                    deleteFile(downloadedFile);
                    removeCancelled(fileUrl);
                    break;
                }

                total += len1;
                progress = (int)((total*100)/fileLength);
                if ( (progress > 0) && (progress > previousProgress)){
                    sendBroadcast(fileUrl, ACTION_DOWNLOAD, ""+progress);
                }
                f.write(buffer, 0, len1);
            }
            f.close();

            if (fileDigest != null){
                // check the file digest matches, otherwise delete the file
                // (it's either been a corrupted download or it's the wrong file)
                byte[] digest = mDigest.digest();
                String resultMD5 = "";

                for (int i=0; i < digest.length; i++) {
                    resultMD5 += Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
                }
                if(!resultMD5.contains(fileDigest)){
                    this.deleteFile(downloadedFile);
                    sendBroadcast(fileUrl, ACTION_FAILED, this.getString(R.string.error_media_download));
                    return;
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            sendBroadcast(fileUrl, ACTION_FAILED, this.getString(R.string.error_media_download));
            return;
        } catch (ProtocolException e) {
            this.deleteFile(downloadedFile);
            e.printStackTrace();
            sendBroadcast(fileUrl, ACTION_FAILED, this.getString(R.string.error_media_download));
            return;
        } catch (IOException e) {
            this.deleteFile(downloadedFile);
            e.printStackTrace();
            sendBroadcast(fileUrl, ACTION_FAILED, this.getString(R.string.error_media_download));
            return;
        } catch (NoSuchAlgorithmException e) {
            Mint.logException(e);
            e.printStackTrace();
            sendBroadcast(fileUrl, ACTION_FAILED, this.getString(R.string.error_media_download));
            return;
        }

        sendBroadcast(fileUrl, ACTION_COMPLETE, null);
    }

    /*
    * Sends a new Broadcast with the results of the action
    */
    private void sendBroadcast(String fileUrl, String result, String message){

        Intent localIntent = new Intent(BROADCAST_ACTION);
        localIntent.putExtra(SERVICE_ACTION, result);
        localIntent.putExtra(SERVICE_URL, fileUrl);
        if (message != null){
            localIntent.putExtra(SERVICE_MESSAGE, message);
        }
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }


    private void addCancelledTask(String fileUrl){
        if (tasksCancelled == null){
            tasksCancelled = new ArrayList<String>();
        }
        if (!tasksCancelled.contains(fileUrl)){
            tasksCancelled.add(fileUrl);
        }
    }

    private boolean isCancelled(String fileUrl){
        return (tasksCancelled != null) && (tasksCancelled.contains(fileUrl));
    }

    private boolean removeCancelled(String fileUrl){
        return (tasksCancelled != null) && (tasksCancelled.remove(fileUrl));
    }

    private void deleteFile(File file){
        if ((file != null) && file.exists() && !file.isDirectory()){
            file.delete();
        }
    }

}