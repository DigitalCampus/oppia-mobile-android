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

package org.digitalcampus.oppia.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.digitalcampus.oppia.listener.DownloadMediaListener;


public class DownloadBroadcastReceiver extends BroadcastReceiver {

    private DownloadMediaListener mediaListener;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!intent.hasExtra(DownloadService.SERVICE_URL) || !intent.hasExtra(DownloadService.SERVICE_ACTION)){
            //If the file URL and the action are not present, we can't identify it
            return;
        }

        String fileUrl = intent.getStringExtra(DownloadService.SERVICE_URL);
        String action = intent.getStringExtra(DownloadService.SERVICE_ACTION);

        if(mediaListener != null){
            switch (action) {
                case DownloadService.ACTION_COMPLETE:
                    mediaListener.onDownloadComplete(fileUrl);
                    break;
                case DownloadService.ACTION_FAILED:
                    String message = intent.getStringExtra(DownloadService.SERVICE_MESSAGE);
                    mediaListener.onDownloadFailed(fileUrl, message);
                    break;
                case DownloadService.ACTION_DOWNLOAD:
                    int progress = Integer.parseInt(intent.getStringExtra(DownloadService.SERVICE_MESSAGE));
                    mediaListener.onDownloadProgress(fileUrl, progress);
                    break;
                default:
                    // do nothing
            }
        }

        abortBroadcast();
    }

    public void setMediaListener(DownloadMediaListener listener){
        mediaListener = listener;
    }
}
