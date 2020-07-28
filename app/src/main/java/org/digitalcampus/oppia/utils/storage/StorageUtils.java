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

package org.digitalcampus.oppia.utils.storage;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.splunk.mint.Mint;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class StorageUtils {

    public static final String TAG = StorageUtils.class.getSimpleName();
    private static DeviceFile mExternalDrive;
    private static DeviceFile mInternalDrive;

    private StorageUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static DeviceFile getInternalMemoryDrive() {
        DeviceFile ret = null;
        if (mInternalDrive != null)
            return mInternalDrive;
        ret = new DeviceFile(Environment.getExternalStorageDirectory());
        Log.d(TAG, "Storage: " + ret.getPath());
        if (!ret.exists()) {
            DeviceFile mnt = new DeviceFile("/mnt");
            if (mnt.exists())
                for (DeviceFile kid : mnt.listFiles())
                    if (kid.getName().toLowerCase().contains("sd") && kid.canWrite()){
                        mInternalDrive = kid;
                        return mInternalDrive;
                    }

        } else if (ret.getName().endsWith("1")) {
            DeviceFile sdcard0 = new DeviceFile(ret.getPath().substring(0, ret.getPath().length() - 1) + "0");
            if (sdcard0.exists()) ret = sdcard0;
        }
        mInternalDrive = ret;
        return mInternalDrive;
    }

    // Code from StackOverflow: http://stackoverflow.com/questions/9340332/how-can-i-get-the-list-of-mounted-external-storage-of-android-device/19982338#19982338
    public static DeviceFile getExternalMemoryDrive(Context ctx)
    {
        if (mExternalDrive != null && mExternalDrive.exists()){
            return mExternalDrive;
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            File[] dirs = ctx.getExternalFilesDirs(null);
            if (dirs.length > 1){

                DeviceFile externalDrive = null;
                for (int i=1; i<dirs.length; i++){
                    if (dirs[i] != null){
                        Log.d(TAG, "Filedirs: " + dirs[i].getPath() + ": " + (dirs[i].canWrite()?"writable":"not writable!"));
                        if (dirs[i].canWrite() && externalDrive == null) externalDrive = new DeviceFile(dirs[i]);
                    }
                }

                if ((externalDrive!=null) && externalDrive.canRead() && externalDrive.canWrite()){
                    mExternalDrive = externalDrive;
                    return externalDrive;
                }
            }

        }

        DeviceFile parent = getInternalMemoryDrive().getParent();
        while(parent.getDepth() > 2)
            parent = parent.getParent();
        Log.d(TAG, "traversing root path " + parent.getPath());
        for (DeviceFile kid : parent.listFiles()) {
            Log.d(TAG, "  > " + kid.getPath() + " : " + (kid.canWrite()?"writable":"not writable!"));
            if ((kid.getName().toLowerCase().contains("ext") || kid.getName().toLowerCase().contains("sdcard1"))
                    && !kid.getPath().equals(getInternalMemoryDrive().getPath())
                    && kid.canRead()
                    && kid.canWrite()) {
                mExternalDrive = kid;
                return mExternalDrive;
            }
        }
        if (new File("/Removable").exists())
            for (DeviceFile kid : new DeviceFile("/Removable").listFiles())
                if (kid.getName().toLowerCase().contains("ext") && kid.canRead()
                        && !kid.getPath().equals(getInternalMemoryDrive().getPath())
                        && kid.list().length > 0) {
                    mExternalDrive = kid;
                    return mExternalDrive;
                }
        return null;
    }


    public static List<StorageLocationInfo> getStorageList(Context ctx) {

        List<StorageLocationInfo> list = new ArrayList<>();
        DeviceFile internalStorage = getInternalMemoryDrive();
        DeviceFile externalStorage = getExternalMemoryDrive(ctx);

        if ((internalStorage != null) && internalStorage.canWrite()){
            StorageLocationInfo internal = new StorageLocationInfo(internalStorage.getPath(), false, false, 1);
            list.add(internal);
        }
        if ((externalStorage != null) && externalStorage.canWrite()){
            StorageLocationInfo external = new StorageLocationInfo(externalStorage.getPath(), false, true, 1);
            list.add(external);
        }

        return list;
    }


    public static String readFileFromAssets(Context ctx, String filename) {
        String content = null;
        InputStream is = null;
        try {
            is = ctx.getResources().getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            content = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Mint.logException(e);
            return null;
        }
        finally {
            try {
                if (is != null) { is.close(); }
            } catch (IOException e) { /* Pass */ }
        }
        return content;
    }

}
