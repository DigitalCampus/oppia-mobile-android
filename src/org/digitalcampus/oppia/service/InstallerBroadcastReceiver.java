package org.digitalcampus.oppia.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.digitalcampus.oppia.listener.CourseInstallerListener;

public class InstallerBroadcastReceiver extends BroadcastReceiver {

    private CourseInstallerListener cListener;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!intent.hasExtra(CourseIntallerService.SERVICE_URL) || !intent.hasExtra(CourseIntallerService.SERVICE_ACTION)){
            //If the file URL and the action are not present, we can't identify it
            return;
        }

        String fileUrl = intent.getStringExtra(CourseIntallerService.SERVICE_URL);
        String action = intent.getStringExtra(CourseIntallerService.SERVICE_ACTION);

        if(cListener != null){
            if (action.equals(CourseIntallerService.ACTION_COMPLETE)){
                cListener.onInstallComplete(fileUrl);
            }
            else if(action.equals(CourseIntallerService.ACTION_FAILED)){
                String message = intent.getStringExtra(CourseIntallerService.SERVICE_MESSAGE);
                cListener.onInstallFailed(fileUrl, message);
            }
            else if(action.equals(CourseIntallerService.ACTION_DOWNLOAD)){
                int progress = Integer.parseInt(intent.getStringExtra(CourseIntallerService.SERVICE_MESSAGE));
                cListener.onDownloadProgress(fileUrl, progress);
            }
            else if(action.equals(CourseIntallerService.ACTION_INSTALL)){
                int progress = Integer.parseInt(intent.getStringExtra(CourseIntallerService.SERVICE_MESSAGE));
                cListener.onInstallProgress(fileUrl, progress);
            }
        }

        abortBroadcast();
    }

    public void setCourseInstallerListener(CourseInstallerListener listener){
        cListener = listener;
    }
}
