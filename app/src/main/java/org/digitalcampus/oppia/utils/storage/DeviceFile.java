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

import android.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;

public class DeviceFile{

	public static final String TAG = DeviceFile.class.getSimpleName();
	
    private File mFile;
    private WeakReference<DeviceFile[]> mChildren = null;
    private boolean bGrandPeeked = false;

    public DeviceFile(String path) {
        mFile = new File(path);
    }

    public DeviceFile(File file) {
        mFile = file;
    }

    public DeviceFile getParent() {
        String parent = mFile.getParent();
        if (parent == null)
            return null;
        return new DeviceFile(parent);
    }

    public String getPath() {
        return mFile.getPath();
    }

    public int getDepth() {
        if (getParent() == null || getParent().getPath().equals(getPath()))
            return 1;
        return 1 + getParent().getDepth();
    }

    private DeviceFile[] getOpenPaths(File[] files) {
        if (files == null)
            return new DeviceFile[0];
        DeviceFile[] ret = new DeviceFile[files.length];
        for (int i = 0; i < files.length; i++)
            ret[i] = new DeviceFile(files[i]);
        return ret;
    }

    public boolean exists() {
        return mFile.exists();
    }

    public String getName() {
        return mFile.getName();
    }

    public boolean isDirectory() {
        return mFile.isDirectory();
    }

    public boolean canRead() {
        return mFile.canRead();
    }
    public boolean canWrite() {
        return mFile.canWrite();
    }

    public DeviceFile[] list() {
        if (mChildren != null)
            return mChildren.get();
        else
            return listFiles();
    }

    public DeviceFile[] listFiles() {
        return listFiles(false);
    }

    public DeviceFile[] listFiles(boolean grandPeek) {
        if(mChildren != null && mChildren.get() != null && mChildren.get().length > 0)
            return mChildren.get();
        File[] realFiles = mFile.listFiles();

        DeviceFile[] mChildren2 = getOpenPaths(realFiles);

        if ((mChildren2 == null || mChildren2.length == 0) && !isDirectory()
                && mFile.getParentFile() != null)
            mChildren2 = getParent().listFiles(grandPeek);

        if (mChildren2 == null)
            return new DeviceFile[0];

        if (grandPeek && !bGrandPeeked && mChildren2.length > 0) {
            for (int i = 0; i < mChildren2.length; i++) {
                try {
                    if (!mChildren2[i].isDirectory())
                        continue;
                    mChildren2[i].list();
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    Log.d(TAG, "mChildren2 cannot find item in index no", aioobe);
                }
            }
            bGrandPeeked = true;
        }

        this.mChildren = new WeakReference<>(mChildren2);

        return mChildren2;
    }
}