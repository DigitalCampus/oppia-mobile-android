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
import android.text.TextUtils;
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

import com.google.android.material.tabs.TabLayout;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.TransferableFileListAdapter;
import org.digitalcampus.oppia.application.PermissionsManager;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.model.CourseTransferableFile;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.service.bluetooth.BluetoothBroadcastReceiver;
import org.digitalcampus.oppia.service.bluetooth.BluetoothConnectionManager;
import org.digitalcampus.oppia.service.bluetooth.BluetoothTransferService;
import org.digitalcampus.oppia.service.bluetooth.BluetoothTransferServiceDelegate;
import org.digitalcampus.oppia.task.ExportActivityTask;
import org.digitalcampus.oppia.task.FetchCourseTransferableFilesTask;
import org.digitalcampus.oppia.task.InstallDownloadedCoursesTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.storage.FileUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SyncActivity extends AppActivity implements InstallCourseListener, BluetoothBroadcastReceiver.BluetoothTransferListener, TabLayout.OnTabSelectedListener {

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final int REQUEST_DISCOVERABLE_MODE = 4;
    private static final int BLUETOOTH_DISCOVERABLE_TIME = 300;

    private static final int TAB_ACTIVITYLOGS = 0;
    private static final int TAB_COURSES = 1;

    private RecyclerView coursesRecyclerView;
    private RecyclerView.Adapter coursesAdapter;
    private ArrayList<CourseTransferableFile> transferableFiles = new ArrayList<>();

    private RecyclerView activitylogsRecyclerView;
    private RecyclerView.Adapter activitylogsAdapter;
    private ArrayList<CourseTransferableFile> activityLogs = new ArrayList<>();

    private ProgressDialog progressDialog;
    private View notConnectedInfo;
    private TextView statusTitle;
    private TextView statusSubtitle;
    private TextView pendingFiles;
    private TextView pendingSize;
    private ProgressBar sendTransferProgress;
    private Button connectBtn;
    private Button sendAllButton;
    private ImageButton tetherBtn;
    private View receivingCover;
    private View connectedPanel;

    private int currentSelectedTab = TAB_ACTIVITYLOGS;
    private TabLayout tabsFilter;

    private final BluetoothTransferHandler uiHandler = new BluetoothTransferHandler(this);
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothConnectionManager bluetoothManager = null;
    private BluetoothTransferServiceDelegate btServiceDelegate = null;
    private BluetoothBroadcastReceiver receiver;
    private boolean isReceiving = false;
    private boolean hasPermissions = true;
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
        activitylogsRecyclerView = findViewById(R.id.activitylogs_list);

        statusTitle = findViewById(R.id.status_title);
        statusSubtitle = findViewById(R.id.status_subtitle);
        notConnectedInfo = findViewById(R.id.not_connected_info);
        connectBtn = findViewById(R.id.connect_btn);
        tetherBtn = findViewById(R.id.tethering_btn);
        sendTransferProgress = findViewById(R.id.send_transfer_progress);
        pendingFiles = findViewById(R.id.transfer_pending_files);
        pendingSize = findViewById(R.id.transfer_pending_size);
        receivingCover = findViewById(R.id.receiving_progress);
        tvDeviceName = findViewById(R.id.tv_device_name);
        tabsFilter = findViewById(R.id.tabs_filter);
        connectedPanel = findViewById(R.id.connected_panel);
        sendAllButton = findViewById(R.id.send_all_button);

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
        coursesAdapter = new TransferableFileListAdapter(transferableFiles, position -> {
            CourseTransferableFile toShare = transferableFiles.get(position);
            sendFile(toShare);
        }, true);
        tabsFilter.addOnTabSelectedListener(this);

        activitylogsRecyclerView.setHasFixedSize(true);
        activitylogsRecyclerView.setLayoutManager( new LinearLayoutManager(this));
        activitylogsAdapter = new TransferableFileListAdapter(activityLogs, position -> {
            if (BluetoothConnectionManager.getState() == BluetoothConnectionManager.STATE_CONNECTED){
                btServiceDelegate.sendFile(activityLogs.get(position));
            }
        });

        activitylogsRecyclerView.setAdapter(activitylogsAdapter);

        DividerItemDecoration divider = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        coursesRecyclerView.setAdapter(coursesAdapter);
        coursesRecyclerView.addItemDecoration(divider);
        activitylogsRecyclerView.addItemDecoration(divider);
        refreshFileList(false);

        connectBtn.setOnClickListener(v -> manageBluetoothConnection());
        tetherBtn.setOnClickListener(v -> ensureDiscoverable());
        sendAllButton.setOnClickListener(v -> sendAll());

        ExportActivityTask task = new ExportActivityTask(this);
        task.setListener(filename -> {
            updateStatus(true);
            refreshFileList(false);
        });
        task.execute();
        sendTransferProgress.setVisibility(View.VISIBLE);

    }

    @Override
    public void onResume() {
        super.onResume();

        hasPermissions = PermissionsManager.checkPermissionsAndInform(this,
                PermissionsManager.BLUETOOTH_PERMISSIONS);
        if (!hasPermissions){
            return;
        }

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
        if (receiver != null){
            unregisterReceiver(receiver);
        }

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
        Bundle extras = data.getExtras();
        if (extras != null){
            String address = extras.getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            bluetoothManager.connect(device);
        }
    }

    private void setupBluetoothConnection() {
        Log.d(TAG, "setting up connection");
        bluetoothManager = new BluetoothConnectionManager(this, uiHandler);
    }

    private void sendFile(CourseTransferableFile toShare){
        if (BluetoothConnectionManager.getState() == BluetoothConnectionManager.STATE_CONNECTED){
            for (CourseTransferableFile file : transferableFiles){
                if (toShare.getRelatedMedia().contains(file.getFilename())){
                    btServiceDelegate.sendFile(file);
                }
            }
            btServiceDelegate.sendFile(toShare);
        }
    }

    private void sendAll(List<CourseTransferableFile> files){
        if (BluetoothConnectionManager.getState() == BluetoothConnectionManager.STATE_CONNECTED){
            for (CourseTransferableFile file : files){
                btServiceDelegate.sendFile(file);
            }
        }
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
            default:
                // do nothing
        }

    }

    private void setStatus(int statusTitle, String connectedDevice) {
        this.statusTitle.setText(statusTitle);
        if (connectedDevice == null){
            statusSubtitle.setText(R.string.bluetooth_no_device_connected);
            notConnectedInfo.setVisibility(View.VISIBLE);
            connectedPanel.setVisibility(View.GONE);
            sendAllButton.setVisibility(View.GONE);
            updateTabs();

            if (connectMenuItem != null){
                connectMenuItem.setVisible(hasPermissions);
                discoverMenuItem.setVisible(hasPermissions);
                disconnectMenuItem.setVisible(false);
            }
        }
        else{
            statusSubtitle.setText(connectedDevice);
            notConnectedInfo.setVisibility(View.GONE);
            connectedPanel.setVisibility(View.VISIBLE);
            updateTabs();

            if (connectMenuItem != null){
                connectMenuItem.setVisible(false);
                discoverMenuItem.setVisible(false);
                disconnectMenuItem.setVisible(true);
            }
        }
    }

    private void ensureDiscoverable() {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BLUETOOTH_DISCOVERABLE_TIME);
            startActivityForResult(discoverableIntent, REQUEST_DISCOVERABLE_MODE);
        }
        else{
            Toast.makeText(this, R.string.bluetooth_discovery_already_enabled, Toast.LENGTH_SHORT).show();
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

    private void sendAll(){
        if(currentSelectedTab == TAB_ACTIVITYLOGS) {
            sendAll(activityLogs);
        } else if (currentSelectedTab == TAB_COURSES) {
            sendAll(transferableFiles);
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


    private void refreshFileList(final boolean isAfterTransfer){
        FetchCourseTransferableFilesTask task = new FetchCourseTransferableFilesTask(this);
        task.setListener(new FetchCourseTransferableFilesTask.FetchBackupsListener() {
            @Override
            public void coursesPendingToInstall(boolean pending) {
                if (pending && isAfterTransfer && !isReceiving){
                    final Handler handler = new Handler();
                    Log.e(TAG, "Installing pending courses!");
                    handler.postDelayed(() -> {
                        Log.e(TAG, "launch delayed task");
                        installTransferredCourses();
                    }, 250);
                }
                else{
                    Log.d(TAG, "There are courses left to install!");
                }
            }

            @Override
            public void onFetchComplete(List<CourseTransferableFile> backups, List<CourseTransferableFile> logs) {
                transferableFiles.clear();
                transferableFiles.addAll(backups);
                coursesAdapter.notifyDataSetChanged();

                activityLogs.clear();
                activityLogs.addAll(logs);
                activitylogsAdapter.notifyDataSetChanged();
                updateStatus(false);
            }
        });
        task.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsManager.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        if (isBluetoothAvailable()){
            bluetoothManager.disconnect(false);
        }
        super.onBackPressed();

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

        if (BluetoothTransferService.getTasksTransferring().isEmpty()){
            sendTransferProgress.setVisibility(View.GONE);
            pendingSize.setVisibility(View.GONE);
            pendingFiles.setVisibility(View.GONE);
            sendAllButton.setVisibility(View.VISIBLE);
        }

        refreshFileList(true);
        updateStatus(false);

        if ((isReceiving) || (file.getType().equals(CourseTransferableFile.TYPE_ACTIVITY_LOG))){
            Log.d(TAG, "Complete!");
            String title = file.getNotificationName();
            if ((file.getFile() == null) && !TextUtils.isEmpty(title)){
                Toast.makeText(this, getString(R.string.bluetooth_transfer_complete, title), Toast.LENGTH_LONG).show();
            }
        }
        isReceiving = false;

        Log.e(TAG, "File complete!");
        if (file.getType().equals(CourseTransferableFile.TYPE_COURSE_BACKUP) ){
            Log.e(TAG, "Installing new course!");
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
    public void installComplete(Payload p) {
        Log.d(TAG, "Course completed installing!");
        if (progressDialog != null){
            progressDialog.dismiss();
            progressDialog = null;
        }
        if (!p.isResult()){
            Toast.makeText(this, p.getResultResponse(), Toast.LENGTH_SHORT).show();
        }
        refreshFileList(false);
        Toast.makeText(this, R.string.install_complete, Toast.LENGTH_SHORT).show();

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
        if (pending.isEmpty() || pendingProgress == 0){
            sendTransferProgress.setVisibility(View.GONE);
            pendingFiles.setVisibility(View.GONE);
            pendingSize.setVisibility(View.GONE);
            sendAllButton.setVisibility(View.VISIBLE);
            refreshFileList(true);
        }
        else{
            sendTransferProgress.setVisibility(View.VISIBLE);
            pendingFiles.setVisibility(View.VISIBLE);
            pendingSize.setVisibility(View.VISIBLE);
            sendAllButton.setVisibility(View.GONE);
            pendingFiles.setText(getString(R.string.bluetooth_files_pending, pending.size()));
            pendingSize.setText(FileUtils.readableFileSize(pendingProgress));
        }

        if (pending.isEmpty() && pendingProgress == 0){
            //If we are sending and there are no transfers left, show the complete toast
            Toast.makeText(this, R.string.bluetooth_all_transfers_complete, Toast.LENGTH_LONG).show();
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
            case REQUEST_DISCOVERABLE_MODE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_CANCELED) {
                    Toast.makeText(this, R.string.bluetooth_discovery_fail, Toast.LENGTH_SHORT).show();
                }
                else{
                    //In case os success, resultCode is the discoverability time
                    Toast.makeText(this, getString(R.string.bluetooth_discovery_success, resultCode), Toast.LENGTH_SHORT).show();
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
                break;
            default:
                // do nothing
        }
    }

    private void updateTabs(){
        boolean connected = connectedPanel.getVisibility() == View.VISIBLE;
        if(currentSelectedTab == TAB_ACTIVITYLOGS) {
            activitylogsRecyclerView.setVisibility(View.VISIBLE);
            coursesRecyclerView.setVisibility(View.GONE);
            if (connected) {
                boolean hide = activityLogs.isEmpty() || pendingFiles.getVisibility() == View.VISIBLE;
                sendAllButton.setVisibility(hide ? View.GONE : View.VISIBLE);
            }
        } else if (currentSelectedTab == TAB_COURSES) {
            activitylogsRecyclerView.setVisibility(View.GONE);
            coursesRecyclerView.setVisibility(View.VISIBLE);
            if (connected){
                boolean hide = transferableFiles.isEmpty() || pendingFiles.getVisibility() == View.VISIBLE;
                sendAllButton.setVisibility( hide ? View.GONE : View.VISIBLE);
            }
        }
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        currentSelectedTab = tab.getPosition();
        updateTabs();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        // do nothing
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        // do nothing
    }



    //static inner class doesn't hold an implicit reference to the outer class
    private class BluetoothTransferHandler extends Handler {
        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<SyncActivity> activity;

        BluetoothTransferHandler(SyncActivity activityInstance) {
            activity = new WeakReference<>(activityInstance);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {

            SyncActivity self = activity.get();
            if (self == null) return;

            Log.d(TAG, "Handle message");
            switch (msg.what) {
                case BluetoothConnectionManager.UI_MESSAGE_STATE_CHANGE:
                case BluetoothConnectionManager.UI_MESSAGE_DEVICE_NAME:
                    self.updateStatus(true);
                    break;

                case BluetoothConnectionManager.UI_MESSAGE_TOAST:
                    Toast.makeText(self, msg.getData().getString(BluetoothConnectionManager.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    // do nothing
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

            case android.R.id.home:
                this.onBackPressed();
                break;
            default:
                // do nothing
        }

        return super.onOptionsItemSelected(item);
    }

}
