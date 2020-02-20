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

import java.io.IOException;
import java.util.UUID;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections and a thread for connecting with a device
 */
public class BluetoothConnectionManager {
    // Debugging
    private static final String TAG = BluetoothConnectionManager.class.getSimpleName();

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    public static final int UI_MESSAGE_STATE_CHANGE = 1;
    public static final int UI_MESSAGE_DEVICE_NAME = 2;
    public static final int UI_MESSAGE_TOAST = 3;

    public static final String TOAST = "toast";

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "BluetoothTransferSecure";

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
            UUID.fromString("f03ecf10-1ed0-49e6-96a6-d9b198148f81");


    private static int state;
    private static String deviceName;

    // Member fields
    private final BluetoothAdapter mAdapter;
    private final Handler uiHandler;

    private AcceptThread secureAcceptThread;
    private ConnectThread connectThread;

    private int newState;
    private Context ctx;

    /**
     * Constructor. Prepares a new BluetoothChat session.
     *
     * @param handler A Handler to send messages back to the UI Activity
     */
    public BluetoothConnectionManager(Context ctx, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        uiHandler = handler;
        this.ctx = ctx;

        if (state != STATE_CONNECTED){
            state = STATE_NONE;
            deviceName = null;
            newState = state;
        }
    }

    /**
     * Update UI title according to the current state of the chat connection
     */
    private synchronized void updateUserInterfaceTitle() {
        state = getState();
        newState = state;
        // Give the new state to the Handler so the UI Activity can update
        Log.d(TAG, "Updating interface title" + newState);
        uiHandler.obtainMessage(UI_MESSAGE_STATE_CHANGE, newState, -1).sendToTarget();
    }

    /**
     * Return the current connection state.
     */
    public static synchronized int getState() {
        return state;
    }

    /**
     * Return the current connection state.
     */
    public static synchronized boolean isConnected() {
        return state == STATE_CONNECTED;
    }

    /**
     * Return the current connection state.
     */
    public static synchronized void notifySocketDisconnected() {
        state = STATE_NONE;
    }


    /**
     * Return the current connected device name.
     */
    public static synchronized String getDeviceName() {
        return state == STATE_CONNECTED ? deviceName : "";
    }


    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {

        if (state == STATE_CONNECTED){
            return;
        }

        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        // Start the thread to listen on a BluetoothServerSocket
        if (secureAcceptThread != null) {
            secureAcceptThread.cancel();
        }
        secureAcceptThread = new AcceptThread();
        secureAcceptThread.start();

        // Update UI title
        updateUserInterfaceTitle();
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device.getName() + ". Address: " + device.getAddress());

        // Cancel any thread attempting to make a connection
        if (state == STATE_CONNECTING) {
            Log.d(TAG, "connect: STATE_CONNECTING");
            if (connectThread != null) {
                connectThread.cancel();
                Log.d(TAG, "connectThread canceled");
                connectThread = null;
            }
        }


        // Start the thread to connect with the given device
        connectThread = new ConnectThread(device);
        connectThread.start();
        Log.d(TAG, "connectThread started");

        // Update UI title
        updateUserInterfaceTitle();
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (secureAcceptThread != null) {
            secureAcceptThread.cancel();
            secureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        deviceName = device.getName();
        state = STATE_CONNECTED;
        BluetoothTransferService.setSocket(socket);

        BluetoothTransferServiceDelegate del = new BluetoothTransferServiceDelegate(ctx);
        del.startReceiving();

        // Update UI title
        updateUserInterfaceTitle();
    }

    public synchronized void disconnect(boolean notifyHandler) {
        Log.d(TAG, "stop");

        synchronized (BluetoothConnectionManager.this) {
            state = STATE_NONE;
        }
        new BluetoothTransferServiceDelegate(ctx).disconnect();

        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }

        if (secureAcceptThread != null) {
            secureAcceptThread.cancel();
            secureAcceptThread = null;
        }

        if (notifyHandler){
            updateUserInterfaceTitle();
        }
    }


    public void resetState(){
        synchronized (BluetoothConnectionManager.this) {
            state = STATE_NONE;
            updateUserInterfaceTitle();
            // Start the service over to restart listening mode
            BluetoothConnectionManager.this.start();
        }
    }


    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;

        AcceptThread() {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try {
                tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, MY_UUID_SECURE);

            } catch (IOException e) {
                Log.e(TAG, "Socket listen() failed", e);
            }
            mmServerSocket = tmp;
            state = STATE_LISTEN;
        }

        @Override
        public void run() {
            Log.d(TAG, "Socket BEGIN mAcceptThread " + this);
            setName("AcceptThread");

            BluetoothSocket socket;
            // Listen to the server socket if we're not connected
            while (state != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    Log.e(TAG, "Socket accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothConnectionManager.this) {
                        switch (state) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                Log.d(TAG, "Already connected");
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(TAG, "Could not close unwanted socket", e);
                                }
                                break;
                            default:
                                // do nothing
                        }
                    }
                }
            }
            Log.i(TAG, "END mAcceptThread ");

        }

        public void cancel() {
            Log.d(TAG, "Socket cancel " + this);
            try {
                mmServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Socket close() of server failed", e);
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

        ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
            } catch (IOException e) {
                Log.e(TAG, "Socket create() failed", e);
            }
            mmSocket = tmp;
            state = STATE_CONNECTING;
        }

        @Override
        public void run() {
            Log.i(TAG, "BEGIN connectThread " );
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            if (mAdapter.isDiscovering()) {
                mAdapter.cancelDiscovery();
            }
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
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothConnectionManager.this) {
                connectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);

            Log.i(TAG, "END ConnectThread ");
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }

        /**
         * Indicate that the connection attempt failed and notify the UI Activity.
         */
        private void connectionFailed() {
            Log.e(TAG, "connectionFailed. Unable to connect device");
            // Send a failure message back to the Activity
            Message msg = uiHandler.obtainMessage(UI_MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(TOAST, "Unable to connect device");
            msg.setData(bundle);
            uiHandler.sendMessage(msg);
            resetState();
        }
    }

}
