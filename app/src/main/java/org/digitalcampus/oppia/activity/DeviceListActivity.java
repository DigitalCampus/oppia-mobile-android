/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.digitalcampus.oppia.activity;


import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.DevicesBTAdapter;
import org.digitalcampus.oppia.application.PermissionsManager;
import org.digitalcampus.oppia.model.CourseTransferableFile;
import org.digitalcampus.oppia.service.bluetooth.BluetoothBroadcastReceiver;
import org.digitalcampus.oppia.service.bluetooth.BluetoothTransferService;
import org.digitalcampus.oppia.utils.UIUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class DeviceListActivity extends Activity implements BluetoothBroadcastReceiver.BluetoothTransferListener {

    private static final String TAG = "DeviceListActivity";

    public static final String EXTRA_DEVICE_ADDRESS = "device_address";
    private static final List<String> BLUETOOTH_PERMISSIONS = Arrays.asList(
            //Remember to update this when the Manifest permissions change!
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    );

    private View scanningMessage;

    private BluetoothBroadcastReceiver receiver;
    private BluetoothAdapter mBtAdapter;
    private DevicesBTAdapter adapterNewDevices;
    private List<String> newDevicesNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_device_list);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        scanningMessage = findViewById(R.id.scanning_message);
        // Initialize the button to perform device discovery
        Button scanButton = findViewById(R.id.button_scan);
        scanButton.setOnClickListener(v -> {
            final List<String> notGrantedPerms = PermissionsManager.filterNotGrantedPermissions(DeviceListActivity.this, BLUETOOTH_PERMISSIONS);
            if (!notGrantedPerms.isEmpty()) {
                if (PermissionsManager.canAskForAllPermissions(DeviceListActivity.this, notGrantedPerms)) {
                    UIUtils.showAlert(
                            DeviceListActivity.this,
                            R.string.permissions_simple_title,
                            R.string.permissions_bluetooth_message,
                            R.string.permissions_allow_btn_text,
                            () -> {
                                PermissionsManager.requestPermissions(
                                        DeviceListActivity.this, notGrantedPerms);
                                return true;
                            }
                    );
                } else {
                    UIUtils.showAlert(
                            DeviceListActivity.this,
                            R.string.permissions_simple_title,
                            R.string.permissions_not_askable_message);
                }
            } else {
                scanButton.setVisibility(View.GONE);
                doDiscovery();
            }

        });


        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        List<String> pairedDevicesNames = new ArrayList<>();

        // If there are paired devices, add each one to the ArrayAdapter
        if (!pairedDevices.isEmpty()) {
            findViewById(R.id.paired_devices_title).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                pairedDevicesNames.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.bluetooth_none_paired).toString();
            pairedDevicesNames.add(noDevices);
        }

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        DevicesBTAdapter adapterPairedDevices = new DevicesBTAdapter(this, pairedDevicesNames);

        // Find and set up the RecyclerView for paired devices
        RecyclerView recyclerPairedDevices = findViewById(R.id.recycler_paired_devices);
        adapterPairedDevices.setOnItemClickListener((v, position) -> selectDevice(v));
        recyclerPairedDevices.setAdapter(adapterPairedDevices);

        // Find and set up the RecyclerView for newly discovered devices

        newDevicesNames.clear();
        adapterNewDevices = new DevicesBTAdapter(this, newDevicesNames);

        RecyclerView recyclerNewDevices = findViewById(R.id.recycler_new_devices);
        adapterNewDevices.setOnItemClickListener((v, position) -> selectDevice(v));
        recyclerNewDevices.setAdapter(adapterNewDevices);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null && mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new BluetoothBroadcastReceiver();
        receiver.setListener(this);
        IntentFilter broadcastFilter = new IntentFilter(BluetoothTransferService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(receiver, broadcastFilter);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.bluetooth_scanning);
        scanningMessage.setVisibility(View.VISIBLE);

        // Turn on sub-title for new devices
        findViewById(R.id.new_devices_title).setVisibility(View.VISIBLE);
        findViewById(R.id.recycler_new_devices).setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    private String getAddress(String device) {
        // Get the device MAC address, which is the last 17 chars in the View
        return device.substring(device.length() - 17);
    }


    private void selectDevice(View v){
        String info = ((TextView) v).getText().toString();

        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        // Get the device MAC address, which is the last 17 chars in the View
        String address = getAddress(info);
        // Create the result Intent and include the MAC address
        Intent intent = new Intent();
        intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
        // Set result and finish this Activity
        setResult(Activity.RESULT_OK, intent);
        finish();
    }


    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    //Check if the device is already added
                    boolean added = false;
                    for (int i = 0; i < newDevicesNames.size(); i++) {
                        String item = newDevicesNames.get(i);
                        if (device.getAddress().equals(getAddress(item))) {
                            added = true;
                        }
                    }
                    if (!added) {
                        String deviceName = device.getName();
                        newDevicesNames.add((deviceName == null ? "Unknown" : deviceName) + "\n" + device.getAddress());
                    }

                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.bluetooth_select_device);
                if (newDevicesNames.isEmpty()) {
                    String noDevices = getResources().getText(R.string.bluetooth_no_devices_found).toString();
                    newDevicesNames.add(noDevices);
                }
                scanningMessage.setVisibility(View.GONE);
            }

            adapterNewDevices.notifyDataSetChanged();

        }
    };

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (PermissionsManager.onRequestPermissionsResult(this, requestCode, permissions, grantResults)) {
            doDiscovery();
        }
    }


    // We subscribe to the Bluetooth broadcast in case the two devices try to connect at
    // the same time. Once the connection is established, the best strategy is to close the
    // select device dialog so we avoid the double connection.
    
    @Override
    public void onCommunicationStarted() {
        this.finish();
    }

    @Override
    public void onFail(CourseTransferableFile file, String error) {
        this.finish();
    }

    @Override
    public void onStartTransfer(CourseTransferableFile file) {
        this.finish();
    }

    @Override
    public void onSendProgress(CourseTransferableFile file, int progress) {
        this.finish();
    }

    @Override
    public void onReceiveProgress(CourseTransferableFile file, int progress) {
        this.finish();
    }

    @Override
    public void onTransferComplete(CourseTransferableFile file) {
        this.finish();
    }

    @Override
    public void onCommunicationClosed(String error) {
        this.finish();
    }
}
