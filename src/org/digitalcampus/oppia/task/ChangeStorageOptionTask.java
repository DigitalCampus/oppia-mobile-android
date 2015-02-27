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
import android.preference.PreferenceManager;
import android.util.Log;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.listener.MoveStorageListener;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategy;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategyFactory;

import java.io.File;
import java.io.IOException;

public class ChangeStorageOptionTask extends AsyncTask<Payload, DownloadProgress, Payload> {

    public static final String TAG = MoveStorageLocationTask.class.getSimpleName();

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

        String previousLocation = PreferenceManager.getDefaultSharedPreferences(ctx).getString(PrefsActivity.PREF_STORAGE_LOCATION, "");
        StorageAccessStrategy previousStrategy = FileUtils.getStorageStrategy();
        String sourcePath = previousStrategy.getStorageLocation(ctx);
        StorageAccessStrategy newStrategy = StorageAccessStrategyFactory.createStrategy(storageType);


        Log.d(TAG, "Checking if storage is available...");
        if (!newStrategy.isStorageAvailable(ctx)){
            resetStrategy(previousStrategy, previousLocation);
            payload.setResult(false);
            payload.setResultResponse(ctx.getString(R.string.error_sdcard));
        }
        else{
            Log.d(TAG, "Getting storage sizes...");
            long currentSize = FileUtils.getTotalStorageUsed(ctx);

            newStrategy.updateStorageLocation(ctx, location);
            String destPath = newStrategy.getStorageLocation(ctx);
            FileUtils.setStorageStrategy(newStrategy);

            long availableDestSize;
            try{
                File destDir = new File(destPath);
                if (destDir.exists()){
                    FileUtils.cleanDir(destDir);
                }
                else{
                    boolean makeDirs = destDir.mkdirs();
                    if (!makeDirs){ throw new Exception("No file created!"); }
                }
                availableDestSize = FileUtils.getAvailableStorageSize(ctx);
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
        }

        return payload;
    }

    private boolean moveStorageDirs(String sourcePath, String destinationPath){

        File destination = new File(destinationPath);
        String downloadPath = sourcePath + File.separator + FileUtils.APP_DOWNLOAD_DIR_NAME;
        String mediaPath = sourcePath + File.separator + FileUtils.APP_MEDIA_DIR_NAME;
        String coursePath = sourcePath + File.separator + FileUtils.APP_COURSES_DIR_NAME;

        try {
            File downloadSource = new File(downloadPath);
            org.apache.commons.io.FileUtils.moveDirectoryToDirectory(downloadSource,destination,true);
            Log.d(TAG,"Copying " + downloadPath + " completed");
        } catch (IOException e) {
            Log.d(TAG,"Copying " + downloadPath + " to " + destination + " failed");
            e.printStackTrace();
            return false;
        }

        try {
            File mediaSource = new File(mediaPath);
            org.apache.commons.io.FileUtils.moveDirectoryToDirectory(mediaSource,destination,true);
            Log.d(TAG,"Copying " + mediaPath + " completed");
        } catch (IOException e) {
            Log.d(TAG,"Copying " + mediaPath + " to " + destination + " failed");
            e.printStackTrace();
            return false;
        }

        try {
            File courseSource = new File(coursePath);
            org.apache.commons.io.FileUtils.moveDirectoryToDirectory(courseSource,destination,true);
            Log.d(TAG,"Copying " + coursePath + " completed");
        } catch (IOException e) {
            Log.d(TAG,"Copying " + coursePath + " to " + destination + " failed");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void resetStrategy(StorageAccessStrategy previousStrategy, String previousLocation){
        // If it fails, we reset the strategy to the previous one
        FileUtils.setStorageStrategy(previousStrategy);

        // And revert the storage option to the previos one
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PrefsActivity.PREF_STORAGE_OPTION, previousStrategy.getStorageType());
        editor.putString(PrefsActivity.PREF_STORAGE_LOCATION, previousLocation);
        editor.commit();

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
