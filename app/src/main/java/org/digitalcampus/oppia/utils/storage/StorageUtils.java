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

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

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
        Log.d(TAG, "Internal Storage: " + ret);
        if (ret == null || !ret.exists()) {
            DeviceFile mnt = new DeviceFile("/mnt");
            if (mnt != null && mnt.exists())
                for (DeviceFile kid : mnt.listFiles())
                    if (kid.getName().toLowerCase().indexOf("sd") > -1)
                        if (kid.canWrite()){
                            mInternalDrive = kid;
                            return mInternalDrive;
                        }

        } else if (ret.getName().endsWith("1")) {
            DeviceFile sdcard0 = new DeviceFile(ret.getPath().substring(0, ret.getPath().length() - 1) + "0");
            if (sdcard0 != null && sdcard0.exists()) ret = sdcard0;
        }
        mInternalDrive = ret;
        return mInternalDrive;
    }

    public static DeviceFile getExternalMemoryDrive()
    {
        if (mExternalDrive != null){
            if (mExternalDrive.exists()){
                return mExternalDrive;
            }
        }

        DeviceFile mDaddy = getInternalMemoryDrive().getParent();
        while(mDaddy.getDepth() > 2)
            mDaddy = mDaddy.getParent();
        for (DeviceFile kid : mDaddy.listFiles())
            if ((kid.getName().toLowerCase().indexOf("ext") > -1 || kid.getName().toLowerCase()
                    .indexOf("sdcard1") > -1)
                    && !kid.getPath().equals(getInternalMemoryDrive())
                    && kid.canRead()
                    && kid.canWrite()) {
                mExternalDrive = kid;
                return mExternalDrive;
            }
        if (new File("/Removable").exists())
            for (DeviceFile kid : new DeviceFile("/Removable").listFiles())
                if (kid.getName().toLowerCase().indexOf("ext") > -1 && kid.canRead()
                        && !kid.getPath().equals(getInternalMemoryDrive().getPath())
                        && kid.list().length > 0) {
                    mExternalDrive = kid;
                    return mExternalDrive;
                }
        return null;
    }


    public static List<StorageLocationInfo> getStorageList() {

        List<StorageLocationInfo> list = new ArrayList<StorageLocationInfo>();
        DeviceFile internalStorage = getInternalMemoryDrive();
        DeviceFile externalStorage = getExternalMemoryDrive();

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
