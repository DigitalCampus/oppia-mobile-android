package org.digitalcampus.oppia.service.bluetooth;


import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.CourseTransferableFile;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.ui.OppiaNotificationUtils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothTransferService extends Service {
    // Debugging
    private static final String TAG = BluetoothTransferService.class.getSimpleName();
    public static final String BROADCAST_ACTION = "com.digitalcampus.oppia.BLUETOOTHSERVICE";

    public static final String SERVICE_ACTION = "action"; //field for providing action
    public static final String SERVICE_MESSAGE = "message";
    public static final String SERVICE_FILE = "file";
    public static final String SERVICE_ERROR = "error";
    public static final String SERVICE_PROGRESS = "progress";

    public static final String ACTION_RECEIVE = "receive";
    public static final String ACTION_SENDFILE = "sendfile";
    public static final String ACTION_DISCONNECT = "disconnect";

    public static final String MESSAGE_DISCONNECT = "disconnect";
    public static final String MESSAGE_START_TRANSFER = "starttransfer";
    public static final String MESSAGE_SEND_PROGRESS = "sendprogress";
    public static final String MESSAGE_RECEIVE_PROGRESS = "receiveprogress";
    public static final String MESSAGE_TRANSFER_FAIL = "transferfailed";
    public static final String MESSAGE_TRANSFER_COMPLETE = "transfercomplete";

    private static final int BUFFER_SIZE = 4096;

    private ArrayList<CourseTransferableFile> tasksDownloading;
    private static BluetoothTransferService currentInstance;

    InputStream input = null;
    OutputStream output = null;

    private static void setInstance(BluetoothTransferService instance){
        currentInstance = instance;
    }


    private static BluetoothSocket socket;
    public static synchronized void setSocket(BluetoothSocket socket){
        BluetoothTransferService.socket = socket;
    }

    private volatile HandlerThread receiveHandlerThread;
    private volatile HandlerThread sendHandlerThread;
    private Handler receiveHandler;
    private Handler sendHandler;
    private BluetoothBroadcastReceiver alternateNotifier;

    public synchronized static List<CourseTransferableFile> getTasksTransferring(){
        if (currentInstance != null){
            return currentInstance.tasksDownloading;
        }
        return new ArrayList<>();
    }

    // Fires when a service is first initialized
    public void onCreate() {
        super.onCreate();

        BluetoothTransferService.setInstance(this);
        Log.d(TAG, "Created Bluetooth service!");
        tasksDownloading = new ArrayList<>();

        alternateNotifier = new BluetoothBroadcastReceiver();
        alternateNotifier.setListener(new BluetoothBroadcastReceiver.BluetoothTransferListener() {
            @Override
            public void onFail(CourseTransferableFile file, String error) {

            }

            @Override
            public void onStartTransfer(CourseTransferableFile file) {
                //Do nothing
            }

            @Override
            public void onSendProgress(CourseTransferableFile file, int progress) {
                //Do nothing
            }

            @Override
            public void onReceiveProgress(CourseTransferableFile file, int progress) {
                //Do nothing
            }

            @Override
            public void onTransferComplete(CourseTransferableFile file) {
                if (tasksDownloading.size() == 0){
                    OppiaNotificationUtils.sendSimpleMessage(BluetoothTransferService.this, true, 0,
                            getString(R.string.bluetooth_all_transfers_complete));
                }
            }

            @Override
            public void onCommunicationClosed(String error) {
                OppiaNotificationUtils.sendSimpleMessage(BluetoothTransferService.this, true, 0,
                        getString(R.string.bluetooth_communication_closed));
            }
        });

        //We register the alternative notifier for sending notifications when no other BroadcasReceiver is set
        IntentFilter broadcastFilter = new IntentFilter(BluetoothTransferService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_LOW_PRIORITY);
        registerReceiver(alternateNotifier, broadcastFilter);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null && intent.hasExtra(SERVICE_ACTION)) {
            String action = intent.getStringExtra(SERVICE_ACTION);
            if (ACTION_RECEIVE.equals(action) && BluetoothConnectionManager.isConnected()){
                Log.d(TAG, "Start transferring commmand received");
                startTransferThreads();
            }
            else if (ACTION_DISCONNECT.equals(action)){
                Log.d(TAG, "Disconnect commmand received");
                closeConnection();
            }
            else if (ACTION_SENDFILE.equals(action) && BluetoothConnectionManager.isConnected()){
                Log.d(TAG, "File send commmand received");
                final CourseTransferableFile file = (CourseTransferableFile) intent.getSerializableExtra(SERVICE_FILE);

                if (!tasksDownloading.contains(file)){
                    tasksDownloading.add(file);
                }
                sendHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        sendFile(file);
                    }
                }, 100);
            }
        }

        // Keep service around "sticky"
        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        BluetoothTransferService.setInstance(null);
        unregisterReceiver(alternateNotifier);
    }

    private void startTransferThreads(){

        receiveHandlerThread = new HandlerThread("BluetoothTransfer.ReceiveHandlerThread");
        receiveHandlerThread.start();
        receiveHandler = new Handler(receiveHandlerThread.getLooper());

        sendHandlerThread = new HandlerThread("BluetoothTransfer.SendHandlerThread");
        sendHandlerThread.start();
        sendHandler = new Handler(sendHandlerThread.getLooper());

        InputStream tmpIn;
        OutputStream tmpOut;

        // Get the BluetoothSocket input and output streams
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "temp sockets not created", e);
            closeConnection();
            return;
        }

        input = tmpIn;
        output = tmpOut;

        Log.d(TAG, "Socket streams created, starting receive thread");
        receiveHandler.post(new Runnable() {
            @Override
            public void run() {
                listenAndReceiveFiles();
            }
        });
    }


    private void closeConnection(){
        try {
            if (BluetoothTransferService.socket != null){
                BluetoothTransferService.socket.close();
                BluetoothTransferService.socket = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
        BluetoothConnectionManager.notifySocketDisconnected();
        if (receiveHandlerThread != null){
            receiveHandlerThread.quit();
            receiveHandlerThread = null;
            receiveHandler = null;
        }
        if (sendHandlerThread != null){
            sendHandlerThread.quit();
            sendHandlerThread = null;
            sendHandler = null;
        }
        tasksDownloading.clear();

    }


    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost(Exception e) {
        // Send a failure message back to the Activity
        Intent localIntent = new Intent(BROADCAST_ACTION);
        localIntent.putExtra(SERVICE_MESSAGE, MESSAGE_DISCONNECT);
        localIntent.putExtra(SERVICE_ERROR, e.getMessage());

        // Broadcasts the Intent to receivers in this app.
        Log.d(TAG, "sending connection lost broadcast");
        sendOrderedBroadcast(localIntent, null);
    }


    public void sendFile(CourseTransferableFile trFile){
        try {
            DataOutputStream d = new DataOutputStream(output);

            File file = trFile.getFile();
            long fileLength = file.length();
            d.writeUTF(file.getName());
            d.writeUTF(trFile.getType());
            d.writeLong(fileLength);

            Log.d(TAG, "Sending " + file.getName() + " over bluetooth...");
            // Notify the UI that a course is transferring
            Intent localIntent = new Intent(BROADCAST_ACTION);
            localIntent.putExtra(SERVICE_MESSAGE, MESSAGE_START_TRANSFER);
            localIntent.putExtra(SERVICE_FILE, trFile);
            sendOrderedBroadcast(localIntent, null);

            int totalBytes = 0;
            FileInputStream fis = new FileInputStream(file);

            try {
                byte[] buf = new byte[BUFFER_SIZE];
                int bytesRead;

                while((bytesRead = fis.read(buf)) != -1) {
                    totalBytes += bytesRead;
                    d.write(buf, 0, bytesRead);

                    localIntent = new Intent(BROADCAST_ACTION);
                    localIntent.putExtra(SERVICE_MESSAGE, MESSAGE_SEND_PROGRESS);
                    localIntent.putExtra(SERVICE_FILE, trFile);
                    localIntent.putExtra(SERVICE_PROGRESS, totalBytes);
                    // Broadcasts the Intent to receivers in this app.
                    sendOrderedBroadcast(localIntent, null);
                }
                d.flush();
            }catch (IOException e){
                Log.e(TAG, e.getMessage(), e);
            }
            finally {
                fis.close();
            }

            Log.d(TAG, totalBytes + " bytes sent via bluetooth.");

            if (trFile.getType().equals(CourseTransferableFile.TYPE_ACTIVITY_LOG)){
                //If it was an activity log, remove the local one
                FileUtils.deleteFile(trFile.getFile());
            }

            localIntent = new Intent(BROADCAST_ACTION);
            localIntent.putExtra(SERVICE_MESSAGE, MESSAGE_TRANSFER_COMPLETE);
            localIntent.putExtra(SERVICE_FILE, trFile);
            // Broadcasts the Intent to receivers in this app.
            sendOrderedBroadcast(localIntent, null);

            tasksDownloading.remove(trFile);

        } catch (IOException e) {
            Mint.logException(e);
            Log.d(TAG, "IO exception: ", e);
        }
    }


    private void listenAndReceiveFiles(){
        Log.i(TAG, "BEGIN receiving thread");

        // Read from the InputStream
        BufferedInputStream in = new BufferedInputStream(input);
        DataInputStream d = new DataInputStream(in);

        // Keep listening to the InputStream while connected
        while (BluetoothConnectionManager.getState() == BluetoothConnectionManager.STATE_CONNECTED) {
            try {
                String fileName = d.readUTF();
                Log.d(TAG, "Receiving file! " + fileName);
                String type = d.readUTF();
                long fileSize = d.readLong();

                CourseTransferableFile trFile = new CourseTransferableFile();
                trFile.setType(type);
                trFile.setFileSize(fileSize);
                trFile.setFilename(fileName);

                Log.d(TAG, fileName + ": " + fileSize);

                // Notify the UI that a course is transferring
                Intent localIntent = new Intent(BROADCAST_ACTION);
                localIntent.putExtra(SERVICE_MESSAGE, MESSAGE_START_TRANSFER);
                localIntent.putExtra(SERVICE_FILE, trFile);
                sendOrderedBroadcast(localIntent, null);

                File destinationDir = (CourseTransferableFile
                        .TYPE_COURSE_BACKUP.equals(type)) ?
                        new File(Storage.getDownloadPath(BluetoothTransferService.this)) :
                        new File(Storage.getBluetoothTransferTempPath(BluetoothTransferService.this));
                if (!destinationDir.exists()){
                    destinationDir.mkdirs();
                }
                File file = new File(destinationDir, fileName);
                FileOutputStream output = new FileOutputStream(file);

                int totalBytes = 0;
                int prevProgress = 0;
                try {
                    byte[] buf = new byte[BUFFER_SIZE];
                    int bytesRead;

                    while ((bytesRead = d.read(buf, 0, Math.min(BUFFER_SIZE, (int) fileSize - totalBytes))) != -1) {
                        totalBytes += bytesRead;
                        output.write(buf, 0, bytesRead);
                        if (totalBytes >= fileSize) {
                            Log.d(TAG, "Total bytes read: " + totalBytes + "/" + fileSize);
                            break;
                        }

                        int currentProgress = totalBytes * 100 / (int)fileSize;
                        if (currentProgress > prevProgress){
                            //Only send broadcast if progress advanced
                            prevProgress = currentProgress;

                            localIntent = new Intent(BROADCAST_ACTION);
                            localIntent.putExtra(SERVICE_MESSAGE, MESSAGE_RECEIVE_PROGRESS);
                            localIntent.putExtra(SERVICE_FILE, trFile);
                            localIntent.putExtra(SERVICE_PROGRESS, totalBytes);
                            // Broadcasts the Intent to receivers in this app.
                            sendOrderedBroadcast(localIntent, null);
                        }

                    }
                } catch (IOException e){
                    Log.e(TAG,"Error receiving file", e);
                } finally {
                    output.close();
                }

                if (totalBytes < fileSize){
                    FileUtils.deleteFile(file);
                    localIntent = new Intent(BROADCAST_ACTION);
                    localIntent.putExtra(SERVICE_MESSAGE, MESSAGE_TRANSFER_FAIL);
                    localIntent.putExtra(SERVICE_FILE, trFile);
                    // Broadcasts the Intent to receivers in this app.
                    sendOrderedBroadcast(localIntent, null);
                }
                else{
                    if (type.equals(CourseTransferableFile.TYPE_COURSE_MEDIA)){
                        File mediaDir = new File(Storage.getMediaPath(BluetoothTransferService.this));
                        FileUtils.moveFileToDir(file, mediaDir, true);
                    }
                    else if (type.equals(CourseTransferableFile.TYPE_ACTIVITY_LOG)){
                        File activityLogsDir = new File(Storage.getActivityPath(BluetoothTransferService.this));
                        FileUtils.moveFileToDir(file, activityLogsDir, true);
                    }
                    localIntent = new Intent(BROADCAST_ACTION);
                    localIntent.putExtra(SERVICE_MESSAGE, MESSAGE_TRANSFER_COMPLETE);
                    localIntent.putExtra(SERVICE_FILE, trFile);
                    // Broadcasts the Intent to receivers in this app.
                    sendOrderedBroadcast(localIntent, null);
                }

            } catch (IOException e) {
                Log.e(TAG, "disconnected", e);
                closeConnection();
                connectionLost(e);
                break;
            }
        }
    }

}
