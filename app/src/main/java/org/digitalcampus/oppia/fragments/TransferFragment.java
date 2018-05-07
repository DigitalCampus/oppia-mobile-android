package org.digitalcampus.oppia.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
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
import org.digitalcampus.oppia.activity.OppiaMobileActivity;
import org.digitalcampus.oppia.adapter.TransferCourseListAdapter;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;
import org.digitalcampus.oppia.model.CourseBackup;
import org.digitalcampus.oppia.service.bluetooth.BluetoothTransferService;
import org.digitalcampus.oppia.service.courseinstall.CourseInstall;
import org.digitalcampus.oppia.task.FetchCourseBackupsTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


public class TransferFragment extends Fragment {

    public static final String TAG = TransferFragment.class.getSimpleName();

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private RecyclerView coursesRecyclerView;
    private RecyclerView.Adapter coursesAdapter;
    private ImageButton bluetoothBtn;
    private ImageButton discoverBtn;
    private ArrayList<CourseBackup> courses = new ArrayList<>();

    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothTransferService bluetoothService = null;
    private Handler uiHandler;
    private ProgressDialog progressDialog;

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
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (bluetoothService == null) {
            setupBluetoothConnection();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (bluetoothService != null) {
            bluetoothService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (bluetoothService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (bluetoothService.getState() == BluetoothTransferService.STATE_NONE) {
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
                Context ctx = TransferFragment.this.getActivity().getApplicationContext();
                CourseBackup toShare = courses.get(position);
                if (bluetoothService.getState() == BluetoothTransferService.STATE_CONNECTED){
                    final File backup = CourseInstall.savedBackupCourse(ctx, toShare.getShortname());
                    progressDialog = new ProgressDialog(TransferFragment.this.getActivity(), R.style.Oppia_AlertDialogStyle);
                    progressDialog.setMessage(getString(R.string.course_transferring));
                    progressDialog.setCancelable(false);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    new Thread(new Runnable() {
                        public void run() {
                            bluetoothService.sendFile(backup);
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
                scanForDevices();
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
        uiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                FragmentActivity activity = getActivity();
                switch (msg.what) {
                    case BluetoothTransferService.UI_MESSAGE_STATE_CHANGE:
                        switch (msg.arg1) {
                            case BluetoothTransferService.STATE_CONNECTED:
                                setStatus(R.string.bluetooth_title_connected_to, connectedDeviceName);
                                break;
                            case BluetoothTransferService.STATE_CONNECTING:
                                setStatus(R.string.bluetooth_title_connecting, null);
                                break;
                            case BluetoothTransferService.STATE_LISTEN:
                            case BluetoothTransferService.STATE_NONE:
                                setStatus(R.string.bluetooth_title_not_connected, null);
                                break;
                        }
                        break;

                    case BluetoothTransferService.UI_MESSAGE_DEVICE_NAME:
                        // save the connected device's name
                        connectedDeviceName = msg.getData().getString(BluetoothTransferService.DEVICE_NAME);
                        if (null != activity) {
                            Toast.makeText(activity, "Connected to "
                                    + connectedDeviceName, Toast.LENGTH_SHORT).show();
                            setStatus(R.string.bluetooth_title_connected_to, connectedDeviceName);
                        }
                        break;

                    case BluetoothTransferService.UI_MESSAGE_TOAST:
                        if (null != activity) {
                            Toast.makeText(activity, msg.getData().getString(BluetoothTransferService.TOAST),
                                    Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case BluetoothTransferService.UI_MESSAGE_COURSE_BACKUP:
                        String courseShortname = msg.getData().getString(BluetoothTransferService.COURSE_BACKUP);
                        Log.d(TAG, "Course backup! " + courseShortname);
                        break;

                    case BluetoothTransferService.UI_MESSAGE_COURSE_TRANSFERRING:
                        int total = msg.arg1;
                        Log.d(TAG, "Course transferring! ");
                        progressDialog = new ProgressDialog(TransferFragment.this.getActivity(), R.style.Oppia_AlertDialogStyle);
                        progressDialog.setMessage(getString(R.string.course_transferring));
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        progressDialog.setMax(total);
                        progressDialog.setProgress(0);
                        progressDialog.setIndeterminate(false);
                        progressDialog.setCancelable(false);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();
                        break;

                    case BluetoothTransferService.UI_MESSAGE_TRANSFER_PROGRESS:
                        int progress = msg.arg1;
                        if (progressDialog != null){
                            progressDialog.setProgress(progress);

                            Log.d(TAG, "progress");
                        }
                        break;

                    case BluetoothTransferService.UI_MESSAGE_TRANSFER_COMPLETE:

                        Toast.makeText(getActivity(), "Transfer complete",
                                Toast.LENGTH_SHORT).show();

                        Log.d(TAG, "Course transferring! ");
                        if (progressDialog != null){
                            progressDialog.hide();
                            progressDialog = null;
                        }
                        break;
                }
            }
        };
        bluetoothService = new BluetoothTransferService(getActivity(), uiHandler);
    }

    private void setStatus(int status_title, String connectedDevice) {
        statusTitle.setText(status_title);
        if (connectedDevice == null){
            statusSubtitle.setText(R.string.bluetooth_no_device_connected);
        }
        else{
            statusSubtitle.setText(connectedDevice);
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

    private void scanForDevices(){
        // Launch the DeviceListActivity to see devices and do scan
        Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
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
        FetchCourseBackupsTask task = new FetchCourseBackupsTask(this.getActivity());
        task.setListener(new FetchCourseBackupsTask.FetchBackupsListener() {
            @Override
            public void onFetchComplete(List<CourseBackup> backups) {
                courses.clear();
                courses.addAll(backups);
                coursesAdapter.notifyDataSetChanged();
            }
        });
        task.execute();

    }


}
