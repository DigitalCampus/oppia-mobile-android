package org.digitalcampus.oppia.service.bluetooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


import org.digitalcampus.oppia.model.CourseTransferableFile;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothTransferService {
    // Debugging
    private static final String TAG = BluetoothTransferService.class.getSimpleName();;


    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public static final int UI_MESSAGE_STATE_CHANGE = 1;
    public static final int UI_MESSAGE_DEVICE_NAME = 2;
    public static final int UI_MESSAGE_TOAST = 3;
    public static final int UI_MESSAGE_COURSE_BACKUP = 4;
    public static final int UI_MESSAGE_COURSE_START_TRANSFER = 5;
    public static final int UI_MESSAGE_COURSE_TRANSFERRING = 6;
    public static final int UI_MESSAGE_TRANSFER_COMPLETE = 7;
    public static final int UI_MESSAGE_TRANSFER_PROGRESS = 8;
    public static final int UI_MESSAGE_COURSE_COMPLETE = 9;
    public static final int UI_MESSAGE_TRANSFER_ERROR = 10;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final String COURSE_BACKUP = "course_backup";


    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothTransferSecure";
    private static final String NAME_INSECURE = "BluetoothTransferInsecure";

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("f03ecf10-1ed0-49e6-96a6-d9b198148f81");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("b7940299-f991-4207-810d-8d3a9f3ac71d");

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler uiHandler;
    private final Context ctx;
    private AcceptThread secureAcceptThread;
    private AcceptThread insecureAcceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private int state;
    private int newState;


    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param context The UI Activity Context
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BluetoothTransferService(Context context, Handler handler) {
        ctx = context;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        state = STATE_NONE;
        newState = state;
        uiHandler = handler;
    }

    /**
     * Update UI title according to the current state of the chat connection
     */
    private synchronized void updateUserInterfaceTitle() {
        state = getState();
        newState = state;
        // Give the new state to the Handler so the UI Activity can update
        uiHandler.obtainMessage(UI_MESSAGE_STATE_CHANGE, newState, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return state;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        // Start the thread to listen on a BluetoothServerSocket
        if (secureAcceptThread != null) {
            secureAcceptThread.cancel();
        }
        secureAcceptThread = new AcceptThread(true);
        secureAcceptThread.start();
        if (insecureAcceptThread != null) {
            insecureAcceptThread.cancel();
        }
        insecureAcceptThread = new AcceptThread(false);
        insecureAcceptThread.start();

        // Update UI title
        updateUserInterfaceTitle();
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (state == STATE_CONNECTING) {
            if (connectThread != null) {
                connectThread.cancel();
                connectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        // Start the thread to connect with the given device
        connectThread = new ConnectThread(device, secure);
        connectThread.start();
        // Update UI title
        updateUserInterfaceTitle();
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        Log.d(TAG, "connected, Socket Type:" + socketType);

        // Cancel the thread that completed the connection
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (secureAcceptThread != null) {
            secureAcceptThread.cancel();
            secureAcceptThread = null;
        }
        if (insecureAcceptThread != null) {
            insecureAcceptThread.cancel();
            insecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        connectedThread = new ConnectedThread(socket, socketType);
        connectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = uiHandler.obtainMessage(UI_MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_NAME, device.getName());
        msg.setData(bundle);
        uiHandler.sendMessage(msg);
        // Update UI title
        updateUserInterfaceTitle();
    }


    public synchronized void disconnect(boolean notifyHandler) {
        Log.d(TAG, "stop");

        synchronized (BluetoothTransferService.this) {
            state = STATE_NONE;
        }

        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
        }

        if (secureAcceptThread != null) {
            secureAcceptThread.cancel();
            secureAcceptThread = null;
        }

        if (insecureAcceptThread != null) {
            insecureAcceptThread.cancel();
            insecureAcceptThread = null;
        }

        if (notifyHandler){
            updateUserInterfaceTitle();
        }

    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param trFile The file to transfer
     * @see ConnectedThread#sendFile(CourseTransferableFile)
     */
    public void sendFile(CourseTransferableFile trFile) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (state != STATE_CONNECTED) return;
            r = connectedThread;
        }
        // Perform the write unsynchronized
        r.sendFile(trFile);
    }

    private void resetState(){
        synchronized (BluetoothTransferService.this) {
            if (state != STATE_NONE) {
                state = STATE_NONE;
                updateUserInterfaceTitle();
                // Start the service over to restart listening mode
                BluetoothTransferService.this.start();
            } else {
                updateUserInterfaceTitle();
            }
        }
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
        Message msg = uiHandler.obtainMessage(UI_MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(TOAST, "Unable to connect device");
        msg.setData(bundle);
        uiHandler.sendMessage(msg);
        resetState();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = uiHandler.obtainMessage(UI_MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(TOAST, "Device connection was lost");
        msg.setData(bundle);
        uiHandler.sendMessage(msg);
        resetState();
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure) {
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Create a new listening server socket
            try {
                if (secure) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, MY_UUID_SECURE);
                } else {
                    tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE, MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "listen() failed", e);
            }
            mmServerSocket = tmp;
            state = STATE_LISTEN;
        }

        public void run() {
            Log.d(TAG, "Socket Type: " + mSocketType +
                    " BEGIN mAcceptThread " + this);
            setName("AcceptThread" + mSocketType);

            BluetoothSocket socket = null;
            // Listen to the server socket if we're not connected
            while (state != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket Type: " + mSocketType + "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothTransferService.this) {
                        switch (state) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice(),
                                        mSocketType);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

        }

        public void cancel() {
            Log.d(TAG, "Socket Type" + mSocketType + "cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket Type" + mSocketType + "close() of server failed", e);
            }
        }
    }


    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
            state = STATE_CONNECTING;
        }

        public void run() {
            Log.i(TAG, "BEGIN connectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothTransferService.this) {
                connectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            state = STATE_CONNECTED;
        }

        public void run() {
            Log.i(TAG, "BEGIN connectedThread");
            int bytes;

            // Keep listening to the InputStream while connected
            while (state == STATE_CONNECTED) {
                try {
                    // Read from the InputStream
                    BufferedInputStream in = new BufferedInputStream(mmInStream);
                    DataInputStream d = new DataInputStream(in);

                    String fileName = d.readUTF();
                    String type = d.readUTF();
                    long fileSize = d.readLong();
                    Log.d(TAG, fileName + ": " + fileSize);

                    // Notify the UI that a course is transferring
                    uiHandler.obtainMessage(UI_MESSAGE_COURSE_TRANSFERRING, (int) fileSize, -1, null)
                            .sendToTarget();

                    File destinationDir = (CourseTransferableFile
                                    .TYPE_COURSE_BACKUP.equals(type)) ?
                                    new File(Storage.getDownloadPath(ctx)) :
                                    new File(Storage.getMediaPath(ctx));
                    if (!destinationDir.exists()){
                        destinationDir.mkdirs();
                    }
                    File file = new File(destinationDir, fileName);
                    FileOutputStream output = new FileOutputStream(file);

                    int totalBytes = 0;
                    try {
                        byte[] buf = new byte[4096];
                        int bytesRead;

                        while((bytesRead = d.read(buf)) != -1) {
                            totalBytes += bytesRead;
                            output.write(buf, 0, bytesRead);
                            if (totalBytes >= fileSize) break;
                            Log.d(TAG, "progress: " + totalBytes);
                            uiHandler.obtainMessage(UI_MESSAGE_TRANSFER_PROGRESS, totalBytes, -1, null)
                                    .sendToTarget();
                        }
                    } finally {
                        output.close();
                    }

                    // Notify the UI that a course is transferring
                    uiHandler.obtainMessage(
                            CourseTransferableFile
                                    .TYPE_COURSE_BACKUP.equals(type) ?
                                    UI_MESSAGE_COURSE_COMPLETE
                                    : UI_MESSAGE_TRANSFER_COMPLETE, -1, -1, null)
                            .sendToTarget();

                    Message msg = uiHandler.obtainMessage(UI_MESSAGE_COURSE_BACKUP);
                    Bundle bundle = new Bundle();
                    bundle.putString(COURSE_BACKUP, fileName);
                    msg.setData(bundle);
                    uiHandler.sendMessage(msg);

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        public void sendFile(CourseTransferableFile trFile){

            try {
                DataOutputStream d = new DataOutputStream(mmOutStream);

                File file = trFile.getFile();
                d.writeUTF(file.getName());
                d.writeUTF(trFile.getType());
                d.writeLong(file.length());

                Log.d(TAG, "Sending " + file.getName() + " over bluetooth...");
                uiHandler.obtainMessage(UI_MESSAGE_COURSE_START_TRANSFER, newState, -1).sendToTarget();

                int totalBytes = 0;
                FileInputStream fis = new FileInputStream(file);

                try {
                    byte[] buf = new byte[4096];
                    int bytesRead = 0;

                    while((bytesRead = fis.read(buf)) != -1) {
                        totalBytes += bytesRead;
                        d.write(buf, 0, bytesRead);
                    }
                } finally {
                    fis.close();
                }

                Log.d(TAG, totalBytes + " bytes sent via bluetooth.");

                // Notify the UI that a course is transferring
                uiHandler.obtainMessage(UI_MESSAGE_TRANSFER_COMPLETE, -1, -1, null)
                        .sendToTarget();

            } catch (IOException e) {
                e.printStackTrace();
            }


        }


        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
