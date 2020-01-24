package org.digitalcampus.oppia.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.TransferCourseListAdapter;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;
import org.digitalcampus.oppia.model.CourseTransferableFile;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.service.bluetooth.BluetoothBroadcastReceiver;
import org.digitalcampus.oppia.service.bluetooth.BluetoothConnectionManager;
import org.digitalcampus.oppia.service.bluetooth.BluetoothTransferService;
import org.digitalcampus.oppia.service.bluetooth.BluetoothTransferServiceDelegate;
import org.digitalcampus.oppia.task.FetchCourseTransferableFilesTask;
import org.digitalcampus.oppia.task.InstallDownloadedCoursesTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class SyncActivity extends AppActivity implements InstallCourseListener, BluetoothBroadcastReceiver.BluetoothTransferListener{

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final int BLUETOOTH_DISCOVERABLE_TIME = 300;

    private RecyclerView coursesRecyclerView;
    private RecyclerView.Adapter coursesAdapter;
    private ArrayList<CourseTransferableFile> transferableFiles = new ArrayList<>();

    private ProgressDialog progressDialog;
    private View notConnectedInfo;
    private TextView statusTitle;
    private TextView statusSubtitle;
    private TextView pendingFiles;
    private TextView pendingSize;
    private ImageButton bluetoothBtn;
    private ImageButton discoverBtn;
    private ProgressBar sendTransferProgress;
    private View pendingLogsMessage;
    private Button connectBtn;
    private ImageButton tetherBtn;
    private View receivingCover;

    private final BluetoothTransferHandler uiHandler = new BluetoothTransferHandler(this);
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothConnectionManager bluetoothManager = null;
    private BluetoothTransferServiceDelegate btServiceDelegate = null;
    private BluetoothBroadcastReceiver receiver;
    private boolean isReceiving = false;
    private TextView tvDeviceName;

    private MenuItem connectMenuItem;
    private MenuItem disconnectMenuItem;
    private MenuItem discoverMenuItem;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        // Prevent activity from going to sleep
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        coursesRecyclerView = findViewById(R.id.course_backups_list);
        bluetoothBtn = findViewById(R.id.bluetooth_btn);
        discoverBtn = findViewById(R.id.discover_btn);
        statusTitle = findViewById(R.id.status_title);
        statusSubtitle = findViewById(R.id.status_subtitle);
        notConnectedInfo = findViewById(R.id.not_connected_info);
        pendingLogsMessage = findViewById(R.id.home_messages);
        connectBtn = findViewById(R.id.connect_btn);
        tetherBtn = findViewById(R.id.tethering_btn);
        sendTransferProgress = findViewById(R.id.send_transfer_progress);
        pendingFiles = findViewById(R.id.transfer_pending_files);
        pendingSize = findViewById(R.id.transfer_pending_size);
        receivingCover = findViewById(R.id.receiving_progress);
        tvDeviceName = findViewById(R.id.tv_device_name);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            this.finish();
        }
        btServiceDelegate = new BluetoothTransferServiceDelegate(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();

        coursesRecyclerView.setHasFixedSize(true);
        coursesRecyclerView.setLayoutManager( new LinearLayoutManager(this));
        coursesAdapter = new TransferCourseListAdapter(transferableFiles, new ListInnerBtnOnClickListener() {
            @Override
            public void onClick(int position) {
                final CourseTransferableFile toShare = transferableFiles.get(position);
                if (BluetoothConnectionManager.getState() == BluetoothConnectionManager.STATE_CONNECTED){
                    for (CourseTransferableFile file : transferableFiles){
                        if (toShare.getRelatedMedia().contains(file.getFilename())){
                            btServiceDelegate.sendFile(file);
                        }
                    }
                    btServiceDelegate.sendFile(toShare);
                }
            }
        });

        coursesRecyclerView.setAdapter(coursesAdapter);
        coursesRecyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        refreshFileList(false);

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { manageBluetoothConnection(); }
        });
        tetherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { ensureDiscoverable(); }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // If BT is not on, request that it be enabled.
        if ((bluetoothAdapter != null) && !bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (bluetoothManager == null) {
            setupBluetoothConnection();
            updateStatus(true);
        }

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        startBluetooth();

        receiver = new BluetoothBroadcastReceiver();
        receiver.setListener(this);
        IntentFilter broadcastFilter = new IntentFilter(BluetoothTransferService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(receiver, broadcastFilter);

        updateStatus(true);

        if (bluetoothAdapter != null) {
            tvDeviceName.setText(bluetoothAdapter.getName());
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        isReceiving = false;
        unregisterReceiver(receiver);
    }

    private void startBluetooth(){
        // Only if the state is STATE_NONE, do we know that we haven't started already
        if ((bluetoothAdapter != null) && (bluetoothManager != null) &&
                (BluetoothConnectionManager.getState() == BluetoothConnectionManager.STATE_NONE)) {
            Log.d(TAG, "Starting Bluetooth service");
            bluetoothManager.start();
        }
    }

    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        bluetoothManager.connect(device);
    }

    private void setupBluetoothConnection() {
        Log.d(TAG, "setting up connection");
        bluetoothManager = new BluetoothConnectionManager(this, uiHandler);
    }

    private void updateStatus(boolean updateTransferProgress){
        switch (BluetoothConnectionManager.getState()){
            case BluetoothConnectionManager.STATE_CONNECTED:
                String deviceName = BluetoothConnectionManager.getDeviceName();
                setStatus(R.string.bluetooth_title_connected_to, deviceName);
                if (updateTransferProgress)
                    sendTransferProgress.setVisibility(View.GONE);
                break;
            case BluetoothConnectionManager.STATE_CONNECTING:
                setStatus(R.string.bluetooth_title_connecting, null);
                if (updateTransferProgress)
                    sendTransferProgress.setVisibility(View.VISIBLE);
                break;
            case BluetoothConnectionManager.STATE_LISTEN:
            case BluetoothConnectionManager.STATE_NONE:
                setStatus(R.string.bluetooth_title_not_connected, null);
                if (updateTransferProgress)
                    sendTransferProgress.setVisibility(View.GONE);
                startBluetooth();
                break;
        }

    }

    private void setStatus(int status_title, String connectedDevice) {
        statusTitle.setText(status_title);
        if (connectedDevice == null){
            statusSubtitle.setText(R.string.bluetooth_no_device_connected);
            notConnectedInfo.setVisibility(View.VISIBLE);
            tvDeviceName.setVisibility(View.VISIBLE);
            pendingLogsMessage.setVisibility(View.GONE);
            coursesRecyclerView.setVisibility(View.GONE);

            if (connectMenuItem != null){
                connectMenuItem.setVisible(true);
                discoverMenuItem.setVisible(true);
                disconnectMenuItem.setVisible(false);
            }
        }
        else{
            updateActivityTransferBar();
            statusSubtitle.setText(connectedDevice);
            notConnectedInfo.setVisibility(View.GONE);
            tvDeviceName.setVisibility(View.GONE);
            coursesRecyclerView.setVisibility(View.VISIBLE);

            if (connectMenuItem != null){
                connectMenuItem.setVisible(false);
                discoverMenuItem.setVisible(false);
                disconnectMenuItem.setVisible(true);
            }
        }
    }


    private void ensureDiscoverable() {
        if (bluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BLUETOOTH_DISCOVERABLE_TIME);
            startActivity(discoverableIntent);
        }
    }

    private void manageBluetoothConnection(){
        Log.d(TAG, "state: " + BluetoothConnectionManager.getState());
        //If we are not connected, we attempt to make new connection
        if (BluetoothConnectionManager.getState() == BluetoothConnectionManager.STATE_CONNECTED){
            //If we are currently connected, stop the connection
            bluetoothManager.disconnect(true);
        }
        else{
            // Launch the DeviceListActivity to see devices and do scan
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
        }

    }

    private void installCourses() {
        InstallDownloadedCoursesTask imTask = new InstallDownloadedCoursesTask(this);
        imTask.setInstallerListener(this);
        imTask.execute(new Payload());
    }

    private void installTransferredCourses(){
        if (isReceiving){
            Log.d(TAG, "We are receiving more files, wait until the last one");
        }
        else{
            Log.d(TAG, "Installing transferred courses!");
            installCourses();
        }
    }

    private void updateActivityTransferBar(){
        File dir = new File(Storage.getActivityPath(this));
        if (!dir.exists()){
            return;
        }
        String[] children = dir.list();
        boolean pending = children != null && children.length > 0;
        Log.d(TAG, "Activity. dir list: " + (children != null ? children.length : "null"));
        pendingLogsMessage.setVisibility(
                ( BluetoothConnectionManager.isConnected() && pending ) ? View.VISIBLE : View.GONE
        );
    }

    private void refreshFileList(final boolean isAfterTransfer){
        updateActivityTransferBar();

        FetchCourseTransferableFilesTask task = new FetchCourseTransferableFilesTask(this);
        task.setListener(new FetchCourseTransferableFilesTask.FetchBackupsListener() {
            @Override
            public void coursesPendingToInstall(boolean pending) {
                if (pending && isAfterTransfer){
                    final Handler handler = new Handler();
                    Log.e(TAG, "Installing pending courses!");
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "launch delayed task");
                            installTransferredCourses();
                        }
                    }, 250);
                }
                else{
                    Log.d(TAG, "There are courses left to install!");
                }
            }

            @Override
            public void onFetchComplete(List<CourseTransferableFile> backups) {
                transferableFiles.clear();
                transferableFiles.addAll(backups);
                coursesAdapter.notifyDataSetChanged();
            }
        });
        task.execute();
    }


    @Override
    public void onBackPressed() {
        if (isBluetoothAvailable()){
            bluetoothManager.disconnect(false);
        }
        else{
            super.onBackPressed();
        }

    }

    private boolean isBluetoothAvailable(){
        return (bluetoothAdapter != null) && (bluetoothManager != null)
                && (BluetoothConnectionManager.getState() != BluetoothConnectionManager.STATE_NONE);
    }


    @Override
    public void onReceiveProgress(CourseTransferableFile file, int progress) {
        updateStatus(false);
        receivingCover.setVisibility(View.VISIBLE);
        isReceiving = true;
    }

    @Override
    public void onTransferComplete(CourseTransferableFile file) {
        updateStatus(false);

        isReceiving = false;
        Log.d(TAG, "Complete! ");

        Log.e(TAG, "File complete!");
        if (file.getType().equals(CourseTransferableFile.TYPE_COURSE_BACKUP) ){
            Log.e(TAG, "Installing new course!");
            refreshFileList(true);
        }


        if (BluetoothTransferService.getTasksTransferring().size() == 0){
            sendTransferProgress.setVisibility(View.GONE);
            pendingSize.setVisibility(View.GONE);
            pendingFiles.setVisibility(View.GONE);
        }
        receivingCover.setVisibility(View.GONE);

    }

    @Override
    public void onCommunicationClosed(String error) {
        Log.d(TAG, "Communication lost!");
        bluetoothManager.resetState();
        pendingFiles.setVisibility(View.GONE);
        pendingSize.setVisibility(View.GONE);
        sendTransferProgress.setVisibility(View.GONE);
    }

    @Override
    public void downloadComplete(Payload p) { }
    @Override
    public void downloadProgressUpdate(DownloadProgress dp) { }

    @Override
    public void installComplete(Payload p) {
        Log.d(TAG, "Course completed installing!");
        if (progressDialog != null){
            progressDialog.dismiss();
            progressDialog = null;
        }
        refreshFileList(false);
        //Toast.makeText(this.getActivity(), R.string.install_complete, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void installProgressUpdate(DownloadProgress dp) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this, R.style.Oppia_AlertDialogStyle);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setIndeterminate(false);
            progressDialog.setMessage(dp.getMessage());
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();
            Log.d(TAG, "progress");
        }
        progressDialog.setProgress(dp.getProgress());
        progressDialog.setMessage(dp.getMessage());
    }

    @Override
    public void onCommunicationStarted() {
        Log.d(TAG, "Communication started! ");
        updateStatus(true);
    }

    @Override
    public void onFail(CourseTransferableFile file, String error) {
        sendTransferProgress.setVisibility(View.GONE);
        pendingSize.setVisibility(View.GONE);
        pendingFiles.setVisibility(View.GONE);
        receivingCover.setVisibility(View.GONE);
        isReceiving = false;
        Toast.makeText(this, "Error transferring file", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onStartTransfer(CourseTransferableFile file) {
        Log.d(TAG, "Course transferring! ");
        updateStatus(false);
    }

    @Override
    public void onSendProgress(CourseTransferableFile file, int progress) {

        updateStatus(false);
        List<CourseTransferableFile> pending = BluetoothTransferService.getTasksTransferring();
        long pendingProgress = 0;
        for (CourseTransferableFile pendingFile : pending){
            pendingProgress += pendingFile.getFileSize();
        }
        pendingProgress = Math.max(0, pendingProgress - progress);
        if ((pending.size() == 0) || (pendingProgress == 0)){
            sendTransferProgress.setVisibility(View.GONE);
            pendingFiles.setVisibility(View.GONE);
            pendingSize.setVisibility(View.GONE);
        }
        else{
            sendTransferProgress.setVisibility(View.VISIBLE);
            pendingFiles.setVisibility(View.VISIBLE);
            pendingSize.setVisibility(View.VISIBLE);
            pendingFiles.setText(getString(R.string.bluetooth_files_pending, pending.size()));
            pendingSize.setText(FileUtils.readableFileSize(pendingProgress));
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupBluetoothConnection();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    this.finish();
                }
        }
    }


    //static inner class doesn't hold an implicit reference to the outer class
    private class BluetoothTransferHandler extends Handler {
        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<SyncActivity> activity;

        BluetoothTransferHandler(SyncActivity activityInstance) {
            activity = new WeakReference<>(activityInstance);
        }

        @Override
        public void handleMessage(Message msg) {

            Log.d(TAG, "New message" + msg.what);

            SyncActivity self = activity.get();
            if (self == null) return;

            Log.d(TAG, "Handle message");
            switch (msg.what) {
                case BluetoothConnectionManager.UI_MESSAGE_STATE_CHANGE:
                    self.updateStatus(true);
                    break;

                case BluetoothConnectionManager.UI_MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    self.updateStatus(true);
                    break;

                case BluetoothConnectionManager.UI_MESSAGE_TOAST:
                    Toast.makeText(self, msg.getData().getString(BluetoothConnectionManager.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_sync, menu);
        connectMenuItem = menu.findItem(R.id.menu_connect);
        disconnectMenuItem = menu.findItem(R.id.menu_disconnect);
        discoverMenuItem = menu.findItem(R.id.menu_discover);
        updateStatus(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                manageBluetoothConnection();
                break;

            case R.id.menu_discover:
                ensureDiscoverable();
                break;

            case R.id.menu_disconnect:
                bluetoothManager.disconnect(true);
                break;
        }


        return super.onOptionsItemSelected(item);
    }

}
