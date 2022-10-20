package testFiles.utils;

import android.content.Context;

import org.digitalcampus.oppia.utils.storage.StorageAccessStrategy;

import testFiles.features.StorageTest;


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
