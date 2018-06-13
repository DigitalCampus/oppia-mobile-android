package org.digitalcampus.oppia.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.DeviceListActivity;
import org.digitalcampus.oppia.adapter.TransferCourseListAdapter;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;
import org.digitalcampus.oppia.model.CourseTransferableFile;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.service.bluetooth.BluetoothTransferService;
import org.digitalcampus.oppia.task.FetchCourseTransferableFilesTask;
import org.digitalcampus.oppia.task.InstallDownloadedCoursesTask;
import org.digitalcampus.oppia.task.Payload;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class TransferFragment extends Fragment implements InstallCourseListener {

    public static final String TAG = TransferFragment.class.getSimpleName();

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private RecyclerView coursesRecyclerView;
    private RecyclerView.Adapter coursesAdapter;
    private ImageButton bluetoothBtn;
    private ImageButton discoverBtn;
    private ArrayList<CourseTransferableFile> courses = new ArrayList<>();

    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothTransferService bluetoothService = null;
    private ProgressDialog progressDialog;
    private View notConnectedInfo;

    private TextView statusTitle;
    private TextView statusSubtitle;
    private String connectedDeviceName = null;


    public TransferFragment() {
        // Required empty public constructor
    }

    public static TransferFragment newInstance() {
        return new TransferFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (bluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if ((bluetoothAdapter != null) && !bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (bluetoothService == null) {
            setupBluetoothConnection();
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
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothService != null) {
            bluetoothService.disconnect(false);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
       startBluetooth();
    }

    private void startBluetooth(){
        if ((bluetoothAdapter != null) && (bluetoothService != null)) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (bluetoothService.getState() == BluetoothTransferService.STATE_NONE) {
                Log.d(TAG, "Starting Bluetooth service");
                bluetoothService.start();
            }
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
        return vv;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        coursesRecyclerView.setHasFixedSize(true);
        coursesRecyclerView.setLayoutManager( new LinearLayoutManager(this.getContext()));
        coursesAdapter = new TransferCourseListAdapter(courses, new ListInnerBtnOnClickListener() {
            @Override
            public void onClick(int position) {
                final CourseTransferableFile toShare = courses.get(position);
                if (bluetoothService.getState() == BluetoothTransferService.STATE_CONNECTED){
                    new Thread(new Runnable() {
                        public void run() {
                            bluetoothService.sendFile(toShare);
                        }
                    }).start();
                }
            }
        });

        coursesRecyclerView.setAdapter(coursesAdapter);
        coursesRecyclerView.addItemDecoration(
                new DividerItemDecoration(this.getContext(), DividerItemDecoration.VERTICAL));
        refreshFileList();

        bluetoothBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manageBluetoothConnection();
            }
        });
        discoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ensureDiscoverable();
            }
        });

    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        bluetoothService.connect(device, secure);
    }

    private void setupBluetoothConnection() {
        Log.d(TAG, "setting up connection");
        bluetoothService = new BluetoothTransferService(getActivity(), uiHandler);
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
        Log.d(TAG, "state: " + bluetoothService.getState());
        //If we are not connected, we attempt to make new connection
        if (bluetoothService.getState() == BluetoothTransferService.STATE_CONNECTED){
            //If we are currently connected, stop the connection
            bluetoothService.disconnect(true);
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
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
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


    private void refreshFileList(){
        FetchCourseTransferableFilesTask task = new FetchCourseTransferableFilesTask(this.getActivity());
        task.setListener(new FetchCourseTransferableFilesTask.FetchBackupsListener() {
            @Override
            public void onFetchComplete(List<CourseTransferableFile> backups) {
                courses.clear();
                courses.addAll(backups);
                coursesAdapter.notifyDataSetChanged();
            }
        });
        task.execute();

    }


    private final BluetoothTransferHandler uiHandler = new BluetoothTransferHandler(this);

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
        refreshFileList();
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
            ProgressDialog pd;
            if (self == null) return;
            FragmentActivity ctx = self.getActivity();
            if (ctx == null) return;

            switch (msg.what) {
                case BluetoothTransferService.UI_MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothTransferService.STATE_CONNECTED:
                            self.setStatus(R.string.bluetooth_title_connected_to, self.connectedDeviceName);
                            break;
                        case BluetoothTransferService.STATE_CONNECTING:
                            self.setStatus(R.string.bluetooth_title_connecting, null);
                            break;
                        case BluetoothTransferService.STATE_LISTEN:
                        case BluetoothTransferService.STATE_NONE:
                            self.setStatus(R.string.bluetooth_title_not_connected, null);
                            if (self.progressDialog != null){
                                self.progressDialog.dismiss();
                                self.progressDialog = null;
                            }
                            self.startBluetooth();
                            break;
                    }
                    break;

                case BluetoothTransferService.UI_MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    self.connectedDeviceName = msg.getData().getString(BluetoothTransferService.DEVICE_NAME);
                    Toast.makeText(ctx, "Connected to "
                            + self.connectedDeviceName, Toast.LENGTH_SHORT).show();
                    self.setStatus(R.string.bluetooth_title_connected_to, self.connectedDeviceName);
                    break;

                case BluetoothTransferService.UI_MESSAGE_TOAST:
                    Toast.makeText(ctx, msg.getData().getString(BluetoothTransferService.TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;

                case BluetoothTransferService.UI_MESSAGE_COURSE_BACKUP:
                    String courseShortname = msg.getData().getString(BluetoothTransferService.COURSE_BACKUP);
                    Log.d(TAG, "Course backup! " + courseShortname);
                    break;

                case BluetoothTransferService.UI_MESSAGE_COURSE_START_TRANSFER:
                    Log.d(TAG, "Course transferring! ");
                    if (self.progressDialog != null){
                        self.progressDialog.dismiss();
                        self.progressDialog = null;
                    }
                    pd = new ProgressDialog(ctx, R.style.Oppia_AlertDialogStyle);
                    self.progressDialog = pd;
                    pd.setMessage(self.getString(R.string.course_transferring));
                    pd.setIndeterminate(true);
                    pd.setCancelable(false);
                    pd.setCanceledOnTouchOutside(false);
                    pd.show();
                    break;

                case BluetoothTransferService.UI_MESSAGE_COURSE_TRANSFERRING:
                    int total = msg.arg1;
                    Log.d(TAG, "Course transferring! ");
                    pd = new ProgressDialog(ctx, R.style.Oppia_AlertDialogStyle);
                    self.progressDialog = pd;
                    pd.setMessage(self.getString(R.string.course_transferring));
                    pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    pd.setMax(total);
                    pd.setProgress(0);
                    pd.setIndeterminate(false);
                    pd.setCancelable(false);
                    pd.setCanceledOnTouchOutside(false);
                    pd.show();

                    break;

                case BluetoothTransferService.UI_MESSAGE_TRANSFER_PROGRESS:
                    int progress = msg.arg1;
                    if (self.progressDialog != null) {
                        self.progressDialog.setProgress(progress);
                        Log.d(TAG, "progress");
                    }
                    break;

                case BluetoothTransferService.UI_MESSAGE_TRANSFER_COMPLETE:

                    Toast.makeText(ctx, "Transfer complete", Toast.LENGTH_SHORT).show();
                    if (self.progressDialog != null) {
                        self.progressDialog.dismiss();
                        self.progressDialog = null;
                    }
                    self.refreshFileList();
                    break;


                case BluetoothTransferService.UI_MESSAGE_COURSE_COMPLETE:
                    if (self.progressDialog != null) {
                        self.progressDialog.dismiss();
                        self.progressDialog = null;
                    }
                    Toast.makeText(ctx, "Transfer complete", Toast.LENGTH_SHORT).show();
                    InstallDownloadedCoursesTask imTask = new InstallDownloadedCoursesTask(ctx);
                    imTask.setInstallerListener(self);
                    imTask.execute(new Payload());
                    break;

            }

        }
    }



}
