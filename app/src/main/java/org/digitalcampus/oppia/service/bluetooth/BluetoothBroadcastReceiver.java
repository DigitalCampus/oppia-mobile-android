package org.digitalcampus.oppia.service.bluetooth;

/*
 * This file is part of OppiaMobile - https://digital-campus.org/
 *
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.digitalcampus.oppia.model.CourseTransferableFile;

public class BluetoothBroadcastReceiver extends BroadcastReceiver {

    public interface BluetoothTransferListener{
        void onFail(CourseTransferableFile file, String error);
        void onStartTransfer(CourseTransferableFile file);
        void onSendProgress(CourseTransferableFile file, int progress);
        void onReceiveProgress(CourseTransferableFile file, int progress);
        void onTransferComplete(CourseTransferableFile file);
        void onCommunicationClosed(String error);
    }

    private BluetoothTransferListener cListener;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!intent.hasExtra(BluetoothTransferService.SERVICE_MESSAGE)){
            //If the file URL and the action are not present, we can't identify it
            return;
        }

        String message = intent.getStringExtra(BluetoothTransferService.SERVICE_MESSAGE);

        if(cListener != null){
            if (BluetoothTransferService.MESSAGE_DISCONNECT.equals(message)){
                String error = intent.getStringExtra(BluetoothTransferService.SERVICE_ERROR);
                cListener.onCommunicationClosed(error);
            }
            else if (BluetoothTransferService.MESSAGE_START_TRANSFER.equals(message)){
                CourseTransferableFile file = (CourseTransferableFile) intent.getSerializableExtra(BluetoothTransferService.SERVICE_FILE);
                cListener.onStartTransfer(file);
            }
            else if (BluetoothTransferService.MESSAGE_RECEIVE_PROGRESS.equals(message)){
                CourseTransferableFile file = (CourseTransferableFile) intent.getSerializableExtra(BluetoothTransferService.SERVICE_FILE);
                int progress = intent.getIntExtra(BluetoothTransferService.SERVICE_PROGRESS, 0);
                cListener.onReceiveProgress(file, progress);
            }
            else if (BluetoothTransferService.MESSAGE_SEND_PROGRESS.equals(message)){
                CourseTransferableFile file = (CourseTransferableFile) intent.getSerializableExtra(BluetoothTransferService.SERVICE_FILE);
                int progress = intent.getIntExtra(BluetoothTransferService.SERVICE_PROGRESS, 0);
                cListener.onSendProgress(file, progress);
            }
            else if (BluetoothTransferService.MESSAGE_TRANSFER_FAIL.equals(message)){
                CourseTransferableFile file = (CourseTransferableFile) intent.getSerializableExtra(BluetoothTransferService.SERVICE_FILE);
                String error = intent.getStringExtra(BluetoothTransferService.SERVICE_ERROR);
                cListener.onFail(file, error);
            }
            else if (BluetoothTransferService.MESSAGE_TRANSFER_COMPLETE.equals(message)){
                CourseTransferableFile file = (CourseTransferableFile) intent.getSerializableExtra(BluetoothTransferService.SERVICE_FILE);
                cListener.onTransferComplete(file);
            }
        }

        abortBroadcast();
    }

    public void setListener(BluetoothTransferListener listener){
        cListener = listener;
    }
}
