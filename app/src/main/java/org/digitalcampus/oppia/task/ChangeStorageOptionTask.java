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

package org.digitalcampus.oppia.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.listener.MoveStorageListener;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategy;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategyFactory;
import org.digitalcampus.oppia.utils.storage.StorageUtils;

import java.io.File;
import java.io.IOException;

public class ChangeStorageOptionTask extends AsyncTask<String, Void, BasicResult> {

    public static final String TAG = ChangeStorageOptionTask.class.getSimpleName();

    private Context ctx;

    private MoveStorageListener mStateListener;

    public ChangeStorageOptionTask(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected BasicResult doInBackground(String... params) {

        String storageType = params[0];
        BasicResult result = new BasicResult();

        StorageAccessStrategy previousStrategy = Storage.getStorageStrategy();
        String sourcePath = previousStrategy.getStorageLocation(ctx);
        StorageAccessStrategy newStrategy = StorageAccessStrategyFactory.createStrategy(storageType);

        try {
            Log.d(TAG, "Checking if storage is available...");
            if (!newStrategy.isStorageAvailable(ctx)) {
                throw new ChangeStorageException(ctx.getString(R.string.error_sdcard));
            }

            Log.d(TAG, "Getting storage sizes...");
            long currentSize = Storage.getTotalStorageUsed(ctx);

            String destPath = newStrategy.getStorageLocation(ctx);
            Log.d(TAG, destPath);
            Storage.setStorageStrategy(newStrategy);
            Log.d(TAG, "FileUtils.setStorageStrategy");

            long availableDestSize;
            File destDir = new File(destPath);
            Log.d(TAG, "destDir created: " + destDir.getAbsolutePath());
            if (destDir.exists()) {
                Log.d(TAG, "cleaning courses dir");
                File coursesDir = new File(Storage.getCoursesPath(ctx));
                FileUtils.cleanDir(coursesDir);
                Log.d(TAG, "courses dir cleaned");
            } else {
                boolean makeDirs = destDir.mkdirs();
                if (!makeDirs) {
                    boolean canWrite = destDir.canWrite();
                    Log.d(TAG, "Error creating destination dir " + destPath + ": canWrite=" + canWrite);
                    throw new IOException("No file created!");
                }
            }

            Storage.createNoMediaFile(ctx);

            availableDestSize = Storage.getAvailableStorageSize(ctx);
            Log.d(TAG, "Needed (source):" + currentSize + " - Available(destination): " + availableDestSize);

            if (availableDestSize < currentSize) {
                throw new ChangeStorageException(ctx.getString(R.string.error_insufficient_storage_available));
            } else {
                if (moveStorageDirs(sourcePath, destPath)) {
                    //Delete the files from source
                    FileUtils.deleteDir(new File(sourcePath));
                    Log.d(TAG, "Update storage location succeeded!");
                    result.setSuccess(true);
                } else {
                    //Delete the files that were actually copied
                    File destDirDelete = new File(destPath);
                    FileUtils.deleteDir(destDirDelete);

                    throw new ChangeStorageException(ctx.getString(R.string.error));

                }

            }

        } catch (Exception e) {
            resetStrategy(previousStrategy);
            result.setSuccess(false);
            String errorMessage = e instanceof ChangeStorageException ? e.getMessage() : ctx.getString(R.string.error);
            result.setResultMessage(errorMessage);
            return result;
        }

        StorageUtils.saveStorageData(ctx, storageType);
        return result;

    }

    public static boolean copyDirectory(String sourcePath, String destPath, boolean optional) {

        try {
            File source = new File(sourcePath);
            if (!source.exists()) {
                return optional;
            }
            File dest = new File(destPath);
            org.apache.commons.io.FileUtils.copyDirectoryToDirectory(source, dest);
            FileUtils.deleteDir(source);
            Log.d(TAG, "Copying " + sourcePath + " completed");
        } catch (IOException e) {
            Analytics.logException(e);
            Log.d(TAG, "Copying " + sourcePath + " to " + destPath + " failed", e);
            return false;
        }
        return true;
    }

    public static boolean moveStorageDirs(String sourcePath, String destinationPath) {

        String downloadPath = sourcePath + File.separator + Storage.APP_DOWNLOAD_DIR_NAME;
        String mediaPath = sourcePath + File.separator + Storage.APP_MEDIA_DIR_NAME;
        String coursePath = sourcePath + File.separator + Storage.APP_COURSES_DIR_NAME;
        String backupPath = sourcePath + File.separator + Storage.APP_BACKUP_DIR_NAME;
        String logsPath = sourcePath + File.separator + Storage.APP_ACTIVITY_DIR_NAME;


        return (copyDirectory(downloadPath, destinationPath, false) &&
                copyDirectory(mediaPath, destinationPath, false) &&
                copyDirectory(backupPath, destinationPath, true) &&
                copyDirectory(logsPath, destinationPath, true) &&
                copyDirectory(coursePath, destinationPath, false));
    }

    private void resetStrategy(StorageAccessStrategy previousStrategy) {
        // If it fails, we reset the strategy to the previous one
        Storage.setStorageStrategy(previousStrategy);

        // And revert the storage option to the previos one
        StorageUtils.saveStorageData(ctx, previousStrategy.getStorageType());


    }

    @Override
    protected void onPostExecute(BasicResult result) {
        synchronized (this) {
            if (mStateListener != null) {
                mStateListener.moveStorageComplete(result);
            }
        }
    }

    public void setMoveStorageListener(MoveStorageListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }

    public class ChangeStorageException extends Exception {
        public ChangeStorageException(String message) {
            super(message);
        }
    }
}
