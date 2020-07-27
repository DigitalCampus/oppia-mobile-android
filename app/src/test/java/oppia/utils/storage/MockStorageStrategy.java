package oppia.utils.storage;

import android.app.Activity;
import android.content.Context;

import org.digitalcampus.oppia.listener.StorageAccessListener;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategy;

import UnitTests.StorageTest;

public class MockStorageStrategy implements StorageAccessStrategy {

    @Override
    public boolean updateStorageLocation(Context ctx) {
        return true;
    }

    @Override
    public boolean updateStorageLocation(Context ctx, String mount) {
        return true;
    }

    @Override
    public String getStorageLocation(Context ctx) {
        return StorageTest.tempFolder.getRoot().getAbsolutePath();
    }

    @Override
    public boolean isStorageAvailable() {
        return true;
    }

    @Override
    public boolean needsUserPermissions(Context ctx) {
        return false;
    }

    @Override
    public void askUserPermissions(Activity activity, StorageAccessListener listener) {

    }

    @Override
    public String getStorageType() {
        return "";
    }
}
