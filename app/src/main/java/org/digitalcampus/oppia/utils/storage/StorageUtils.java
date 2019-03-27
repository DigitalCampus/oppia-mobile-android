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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// Code from StackOverflow: http://stackoverflow.com/questions/9340332/how-can-i-get-the-list-of-mounted-external-storage-of-android-device/19982338#19982338

public class StorageUtils {

    public static final String TAG = StorageUtils.class.getSimpleName();
    private static DeviceFile mExternalDrive;
    private static DeviceFile mInternalDrive;

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
                    if (kid.getName().toLowerCase().contains("sd"))
                        if (kid.canWrite()){
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

    public static DeviceFile getExternalMemoryDrive(Context ctx)
    {
        if (mExternalDrive != null){
            if (mExternalDrive.exists()){
                return mExternalDrive;
            }
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

        DeviceFile mDaddy = getInternalMemoryDrive().getParent();
        while(mDaddy.getDepth() > 2)
            mDaddy = mDaddy.getParent();
        Log.d(TAG, "traversing root path " + mDaddy.getPath());
        for (DeviceFile kid : mDaddy.listFiles()) {
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

}
