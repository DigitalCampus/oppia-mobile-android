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

package org.digitalcampus.oppia.service.courseinstall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.digitalcampus.oppia.listener.CourseInstallerListener;

public class InstallerBroadcastReceiver extends BroadcastReceiver {

    private CourseInstallerListener cListener;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (!intent.hasExtra(CourseInstallerService.SERVICE_URL) || !intent.hasExtra(CourseInstallerService.SERVICE_ACTION)){
            //If the file URL and the action are not present, we can't identify it
            return;
        }

        String fileUrl = intent.getStringExtra(CourseInstallerService.SERVICE_URL);
        String action = intent.getStringExtra(CourseInstallerService.SERVICE_ACTION);

        if(cListener != null){
            switch (action) {
                case CourseInstallerService.ACTION_COMPLETE:
                    cListener.onInstallComplete(fileUrl);
                    break;
                case CourseInstallerService.ACTION_FAILED:
                    String message = intent.getStringExtra(CourseInstallerService.SERVICE_MESSAGE);
                    cListener.onInstallFailed(fileUrl, message);
                    break;
                case CourseInstallerService.ACTION_DOWNLOAD:
                    int progressDownload = Integer.parseInt(intent.getStringExtra(CourseInstallerService.SERVICE_MESSAGE));
                    cListener.onDownloadProgress(fileUrl, progressDownload);
                    break;
                case CourseInstallerService.ACTION_INSTALL:
                    int progressInstall = Integer.parseInt(intent.getStringExtra(CourseInstallerService.SERVICE_MESSAGE));
                    cListener.onInstallProgress(fileUrl, progressInstall);
                    break;
                default:
                    // do nothing
            }
        }

        abortBroadcast();
    }

    public void setCourseInstallerListener(CourseInstallerListener listener){
        cListener = listener;
    }
}
