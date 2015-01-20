package org.digitalcampus.oppia.utils.storage;


import android.content.Context;

public interface StorageAccessStrategy {
    public void updateStorageLocation(Context ctx);
    public String  getStorageLocation(Context ctx);
    public boolean isStorageAvailable(Context ctx);
    public String getStorageType();
}
