package org.digitalcampus.oppia.service.bluetooth;

import android.content.Context;
import android.content.Intent;

import org.digitalcampus.oppia.model.CourseTransferableFile;

public class BluetoothTransferServiceDelegate {

    private Context ctx;

    public BluetoothTransferServiceDelegate(Context ctx){
        this.ctx = ctx;
    }

    public void startReceiving(){
        Intent i = new Intent(ctx, BluetoothTransferService.class);
        i.putExtra(BluetoothTransferService.SERVICE_ACTION, BluetoothTransferService.ACTION_RECEIVE);
        ctx.startService(i);
    }

    public void sendFile(CourseTransferableFile file){
        Intent i = new Intent(ctx, BluetoothTransferService.class);
        i.putExtra(BluetoothTransferService.SERVICE_ACTION, BluetoothTransferService.ACTION_SENDFILE);
        i.putExtra(BluetoothTransferService.SERVICE_FILE, file);
        ctx.startService(i);
    }

    public void disconnect(){
        Intent i = new Intent(ctx, BluetoothTransferService.class);
        i.putExtra(BluetoothTransferService.SERVICE_ACTION, BluetoothTransferService.ACTION_DISCONNECT);
        ctx.startService(i);
    }
}
