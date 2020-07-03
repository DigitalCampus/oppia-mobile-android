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
import android.content.SharedPreferences;
import android.os.AsyncTask;
import androidx.preference.PreferenceManager;
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.listener.MoveStorageListener;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategy;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategyFactory;

import java.io.File;
import java.io.IOException;

public class ChangeStorageOptionTask extends AsyncTask<Payload, DownloadProgress, Payload> {

    public static final String TAG = ChangeStorageOptionTask.class.getSimpleName();

    private Context ctx;
    private SharedPreferences prefs;
    private MoveStorageListener mStateListener;

    public ChangeStorageOptionTask(Context ctx) {
        this.ctx = ctx;
        prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    @Override
    protected Payload doInBackground(Payload... params) {

        Payload payload = params[0];
        String storageType = (String)payload.getData().get(0);
        String location = (payload.getData().size() > 1) ? (String)payload.getData().get(1) : null;

        String previousLocation = prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, "");
        StorageAccessStrategy previousStrategy = Storage.getStorageStrategy();
        String sourcePath = previousStrategy.getStorageLocation(ctx);
        StorageAccessStrategy newStrategy = StorageAccessStrategyFactory.createStrategy(storageType);

        Log.d(TAG, "Checking if storage is available...");
        if (!newStrategy.isStorageAvailable()){
            resetStrategy(previousStrategy, previousLocation);
            payload.setResult(false);
            payload.setResultResponse(ctx.getString(R.string.error_sdcard));
            return payload;
        }

        Log.d(TAG, "Getting storage sizes...");
        long currentSize = Storage.getTotalStorageUsed(ctx);

        newStrategy.updateStorageLocation(ctx, location);
        Log.d(TAG,"newStrategy.updateStorageLocation");
        String destPath = newStrategy.getStorageLocation(ctx);
        Log.d(TAG,destPath);
        Storage.setStorageStrategy(newStrategy);
        Log.d(TAG,"FileUtils.setStorageStrategy");

        long availableDestSize;
        try{
            File destDir = new File(destPath);
            Log.d(TAG,"destDir created: " + destDir.getAbsolutePath());
            if (destDir.exists()){
                Log.d(TAG,"cleaning courses dir" );
                File coursesDir = new File(Storage.getCoursesPath(ctx));
                FileUtils.cleanDir(coursesDir);
                Log.d(TAG,"courses dir cleaned" );
            }
            else{
                boolean makeDirs = destDir.mkdirs();
                if (!makeDirs){
                    boolean canWrite = destDir.canWrite();
                    Log.d(TAG, "Error creating destination dir " + destPath + ": canWrite=" + canWrite);
                    throw new IOException("No file created!"); }
            }
            Storage.createNoMediaFile(ctx);

            availableDestSize = Storage.getAvailableStorageSize(ctx);
            Log.d(TAG, "Needed (source):" + currentSize + " - Available(destination): " + availableDestSize);
        }
        catch (Exception e){
            resetStrategy(previousStrategy, previousLocation);
            payload.setResult(false);
            payload.setResultResponse(ctx.getString(R.string.error));
            return payload;
        }

        if (availableDestSize<currentSize){
            resetStrategy(previousStrategy, previousLocation);
            payload.setResult(false);
            payload.setResultResponse(ctx.getString(R.string.error_insufficient_storage_available));
        }
        else{
            if (moveStorageDirs(sourcePath, destPath)){
                //Delete the files from source
                FileUtils.deleteDir(new File(sourcePath));
                Log.d(TAG, "Update storage location succeeded!");
                payload.setResult(true);
            }
            else{
                //Delete the files that were actually copied
                File destDir = new File(destPath);
                FileUtils.deleteDir(destDir);
                resetStrategy(previousStrategy, previousLocation);
                payload.setResult(false);
                payload.setResultResponse(ctx.getString(R.string.error));
            }

        }

        prefs.edit().putString(PrefsActivity.PREF_STORAGE_OPTION, storageType).commit();


        return payload;
    }

    private boolean copyDirectory(String sourcePath, String destPath, boolean optional){

        try {
            File source = new File(sourcePath);
            if (!source.exists()){
                return optional;
            }
            File dest = new File(destPath);
            org.apache.commons.io.FileUtils.copyDirectoryToDirectory(source, dest);
            FileUtils.deleteDir(source);
            Log.d(TAG,"Copying " + sourcePath + " completed");
        } catch (IOException e) {
            Mint.logException(e);
            Log.d(TAG, "Copying " + sourcePath + " to " + destPath + " failed", e);
            return false;
        }
        return true;
    }

    private boolean moveStorageDirs(String sourcePath, String destinationPath){

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

    private void resetStrategy(StorageAccessStrategy previousStrategy, String previousLocation){
        // If it fails, we reset the strategy to the previous one
        Storage.setStorageStrategy(previousStrategy);

        // And revert the storage option to the previos one
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PrefsActivity.PREF_STORAGE_OPTION, previousStrategy.getStorageType());
        editor.putString(PrefsActivity.PREF_STORAGE_LOCATION, previousLocation);
        editor.apply();

    }

    @Override
    protected void onPostExecute(Payload results) {
        synchronized (this) {
            if (mStateListener != null) {
                mStateListener.moveStorageComplete(results);
            }
        }
    }

    public void setMoveStorageListener(MoveStorageListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }
}
