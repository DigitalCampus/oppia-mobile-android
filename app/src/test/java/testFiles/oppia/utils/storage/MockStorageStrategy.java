package testFiles.oppia.utils.storage;

import android.content.Context;

import org.digitalcampus.oppia.utils.storage.StorageAccessStrategy;

import testFiles.UnitTests.StorageTest;


public class MockStorageStrategy implements StorageAccessStrategy {


    @Override
    public String getStorageLocation(Context ctx) {
        return StorageTest.tempFolder.getRoot().getAbsolutePath();
    }

    @Override
    public boolean isStorageAvailable(Context context) {
        return true;
    }

    @Override
    public String getStorageType() {
        return "";
    }
}
