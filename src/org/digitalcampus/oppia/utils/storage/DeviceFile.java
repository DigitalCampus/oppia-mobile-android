package org.digitalcampus.oppia.utils.storage;

import java.io.File;
import java.lang.ref.WeakReference;

public class DeviceFile{

    private File mFile;
    private WeakReference<DeviceFile[]> mChildren = null;
    private Integer mChildCount = null;
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

    public Boolean isDirectory() {
        return mFile.isDirectory();
    }

    public Boolean canRead() {
        return mFile.canRead();
    }
    public Boolean canWrite() {
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
        File[] realFiles = null;
        try {
            realFiles = mFile.listFiles();
        } catch(Throwable e) { }
        DeviceFile[] mChildren2 = getOpenPaths(realFiles);

        if ((mChildren2 == null || mChildren2.length == 0) && !isDirectory()
                && mFile.getParentFile() != null)
            mChildren2 = getParent().listFiles(grandPeek);

        if (mChildren2 == null)
            return new DeviceFile[0];

        if (grandPeek && !bGrandPeeked && mChildren2 != null && mChildren2.length > 0) {
            for (int i = 0; i < mChildren2.length; i++) {
                try {
                    if (!mChildren2[i].isDirectory())
                        continue;
                    mChildren2[i].list();
                } catch (ArrayIndexOutOfBoundsException e) {
                }
            }
            bGrandPeeked = true;
        }

        mChildCount = mChildren2.length;
        this.mChildren = new WeakReference<DeviceFile[]>(mChildren2);

        return mChildren2;
    }
}