package org.digitalcampus.oppia.utils.storage;

import android.os.Environment;

import java.io.File;

public class ExternalStorageState {

    public static final String STATE_NOT_WRITABLE = "STATE_NOT_WRITABLE";

    private static ExternalStorageState externalStorageState = new ExternalStorageState();

    public static String getExternalStorageState(File file){ return externalStorageState.getState(file);  }

    protected String getState(File file) { return Environment.getExternalStorageState(file);}

    public static void setExternalStorageState(ExternalStorageState state){
        externalStorageState = state;
    }
}
