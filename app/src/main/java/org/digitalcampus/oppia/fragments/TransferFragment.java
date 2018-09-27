package org.digitalcampus.oppia.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.DeviceListActivity;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class TransferFragment extends Fragment implements InstallCourseListener, BluetoothBroadcastReceiver.BluetoothTransferListener {

    public static final String TAG = TransferFragment.class.getSimpleName();

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_ENABLE_BT = 3;

    private RecyclerView coursesRecyclerView;
    private RecyclerView.Adapter coursesAdapter;
    private ImageButton bluetoothBtn;
    private ImageButton discoverBtn;
    private ArrayList<CourseTransferableFile> transferableFiles = new ArrayList<>();
    private ArrayList<CourseTransferableFile> courseFiles = new ArrayList<>();

    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothConnectionManager bluetoothManager = null;
    private BluetoothTransferServiceDelegate btServiceDelegate = null;
    private BluetoothBroadcastReceiver receiver;
    private ProgressDialog progressDialog;
    private View notConnectedInfo;

    private TextView statusTitle;
    private TextView statusSubtitle;
    private TextView pendingFiles;
    private TextView pendingSize;

    private ProgressBar sendTransferProgress;
    private View pendingCoursesMessage;
    private Button installCoursesBtn;

    private View receivingCover;

    private final BluetoothTransferHandler uiHandler = new BluetoothTransferHandler(this);
    private boolean isReceiving = false;


    public TransferFragment() {
        // Required empty public constructor
    }

    public static TransferFragment newInstance() {
        return new TransferFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prevent activity
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (bluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }

        btServiceDelegate = new BluetoothTransferServiceDelegate(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        if ((bluetoothAdapter != null) && !bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (bluetoothManager == null) {
            setupBluetoothConnection();
            updateStatus(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        startBluetooth();

        receiver = new BluetoothBroadcastReceiver();
        receiver.setListener(this);
        IntentFilter broadcastFilter = new IntentFilter(BluetoothTransferService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        getActivity().registerReceiver(receiver, broadcastFilter);

        updateStatus(true);
    }

    @Override
    public void onPause(){
        super.onPause();
        isReceiving = false;
        getActivity().unregisterReceiver(receiver);
    }

    private void startBluetooth(){
        // Only if the state is STATE_NONE, do we know that we haven't started already
        if ((bluetoothAdapter != null) && (bluetoothManager != null) &&
                (BluetoothConnectionManager.getState() == BluetoothConnectionManager.STATE_NONE)) {
            Log.d(TAG, "Starting Bluetooth service");
            bluetoothManager.start();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View vv = inflater.inflate(R.layout.fragment_transfer, container, false);
        coursesRecyclerView = (RecyclerView) vv.findViewById(R.id.course_backups_list);
        bluetoothBtn = (ImageButton) vv.findViewById(R.id.bluetooth_btn);
        discoverBtn = (ImageButton) vv.findViewById(R.id.discover_btn);
        statusTitle = (TextView) vv.findViewById(R.id.status_title);
        statusSubtitle = (TextView) vv.findViewById(R.id.status_subtitle);
        notConnectedInfo = vv.findViewById(R.id.not_connected_info);
        pendingCoursesMessage = vv.findViewById(R.id.home_messages);
        installCoursesBtn = (Button) vv.findViewById(R.id.message_action_button);
        sendTransferProgress = (ProgressBar) vv.findViewById(R.id.send_transfer_progress);
        pendingFiles = (TextView) vv.findViewById(R.id.transfer_pending_files);
        pendingSize = (TextView) vv.findViewById(R.id.transfer_pending_size);
        receivingCover = vv.findViewById(R.id.receiving_progress);
        return vv;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        coursesRecyclerView.setHasFixedSize(true);
        coursesRecyclerView.setLayoutManager( new LinearLayoutManager(this.getContext()));
        coursesAdapter = new TransferCourseListAdapter(courseFiles, new ListInnerBtnOnClickListener() {
            @Override
            public void onClick(int position) {
                final CourseTransferableFile toShare = courseFiles.get(position);
                if (BluetoothConnectionManager.getState() == BluetoothConnectionManager.STATE_CONNECTED){
                    btServiceDelegate.sendFile(toShare);

                    for (CourseTransferableFile file : transferableFiles){
                        if (toShare.getRelatedMedia().contains(file.getFilename())){
                            btServiceDelegate.sendFile(file);
                        }
                    }
                }
            }
        });

        coursesRecyclerView.setAdapter(coursesAdapter);
        coursesRecyclerView.addItemDecoration(
                new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL));
        refreshFileList(false);

        bluetoothBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { manageBluetoothConnection(); }
        });
        discoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { ensureDiscoverable(); }
        });
        installCoursesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { installCourses(); }
        });
    }

    private void installCourses(){
        InstallDownloadedCoursesTask imTask = new InstallDownloadedCoursesTask(getActivity());
        imTask.setInstallerListener(this);
        imTask.execute(new Payload());
    }

    private void connectDevice(Intent data) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        bluetoothManager.connect(device);
    }

    private void setupBluetoothConnection() {
        Log.d(TAG, "setting up connection");
        bluetoothManager = new BluetoothConnectionManager(this.getActivity(), uiHandler);
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
            coursesRecyclerView.setVisibility(View.GONE);
            discoverBtn.setVisibility(View.VISIBLE);
            bluetoothBtn.setImageResource(R.drawable.ic_bluetooth);
        }
        else{
            statusSubtitle.setText(connectedDevice);
            notConnectedInfo.setVisibility(View.GONE);
            coursesRecyclerView.setVisibility(View.VISIBLE);
            discoverBtn.setVisibility(View.GONE);
            bluetoothBtn.setImageResource(R.drawable.ic_bluetooth_disabled);
        }
    }


    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (bluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
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
            Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }


    private void installTransferredCourses(){

        if (isReceiving){
            Log.d(TAG, "We are receiving more files, wait until the last one");
        }
        else{
            Log.d(TAG, "Install transferred courses!");
            installCourses();
        }

    }


    private void refreshFileList(final boolean isAfterTransfer){
        FetchCourseTransferableFilesTask task = new FetchCourseTransferableFilesTask(this.getActivity());
        task.setListener(new FetchCourseTransferableFilesTask.FetchBackupsListener() {
            @Override
            public void coursesPendingToInstall(boolean pending) {
                if (isAfterTransfer){
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            installTransferredCourses();
                        }
                    }, 150);
                }
                else{
                    Log.d(TAG, "There are courses left to install!");
                    pendingCoursesMessage.setVisibility(pending ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFetchComplete(List<CourseTransferableFile> backups) {
                transferableFiles.clear();
                transferableFiles.addAll(backups);
                filterCoursesList();
            }
        });
        task.execute();
    }

    private void filterCoursesList(){
        courseFiles.clear();
        for (CourseTransferableFile file : transferableFiles){
            if (CourseTransferableFile.TYPE_COURSE_BACKUP.equals(file.getType())){
                courseFiles.add(file);
            }
        }
        coursesAdapter.notifyDataSetChanged();
    }

    private void initializeProgressDialog(){
        ProgressDialog pd = new ProgressDialog(this.getActivity(), R.style.Oppia_AlertDialogStyle);
        progressDialog = pd;
        pd.setMessage(getString(R.string.course_transferring));
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setProgress(0);
        pd.setIndeterminate(false);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
    }

    public void onBackPressed() {
        bluetoothManager.disconnect(true);
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
        Toast.makeText(this.getActivity(), R.string.install_complete, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void installProgressUpdate(DownloadProgress dp) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this.getActivity(), R.style.Oppia_AlertDialogStyle);
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
    public void onFail(CourseTransferableFile file, String error) {
        sendTransferProgress.setVisibility(View.GONE);
        pendingSize.setVisibility(View.GONE);
        pendingFiles.setVisibility(View.GONE);
        receivingCover.setVisibility(View.GONE);
        isReceiving = false;
        Toast.makeText(getActivity(), "Error transferring file", Toast.LENGTH_SHORT).show();
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
            pendingFiles.setText(pending.size() + " files pending");
            pendingSize.setText(FileUtils.readableFileSize(pendingProgress));
        }

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
        if (BluetoothTransferService.getTasksTransferring().size() == 0){
            sendTransferProgress.setVisibility(View.GONE);
            pendingSize.setVisibility(View.GONE);
            pendingFiles.setVisibility(View.GONE);
        }
        receivingCover.setVisibility(View.GONE);
        isReceiving = false;

        Log.d(TAG, "Complete! ");

        Toast.makeText(getActivity(), "Transfer complete", Toast.LENGTH_SHORT).show();
        refreshFileList(true);
    }

    @Override
    public void onCommunicationClosed(String error) {
        Log.d(TAG, "Communication lost!");
        bluetoothManager.resetState();
        pendingFiles.setVisibility(View.GONE);
        pendingSize.setVisibility(View.GONE);
        sendTransferProgress.setVisibility(View.GONE);
    }

    //static inner class doesn't hold an implicit reference to the outer class
    private static class BluetoothTransferHandler extends Handler {
        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<TransferFragment> fragment;

        BluetoothTransferHandler(TransferFragment fragmentInstance) {
            fragment = new WeakReference<>(fragmentInstance);
        }

        @Override
        public void handleMessage(Message msg) {
            TransferFragment self = fragment.get();
            if (self == null) return;
            FragmentActivity ctx = self.getActivity();
            if (ctx == null) return;

            switch (msg.what) {
                case BluetoothConnectionManager.UI_MESSAGE_STATE_CHANGE:
                    self.updateStatus(true);
                    break;

                case BluetoothConnectionManager.UI_MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    self.updateStatus(true);
                    break;

                case BluetoothConnectionManager.UI_MESSAGE_TOAST:
                    Toast.makeText(ctx, msg.getData().getString(BluetoothConnectionManager.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    }



}
