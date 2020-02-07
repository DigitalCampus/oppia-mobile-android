package org.digitalcampus.oppia.utils.storage;

import android.os.Environment;

public class ExternalStorageState {

    private static ExternalStorageState externalStorageState = new ExternalStorageState();

    public static String getExternalStorageState(){ return externalStorageState.getState();  }

    protected String getState() { return Environment.getExternalStorageState();}

    public static void setExternalStorageState(ExternalStorageState state){
        externalStorageState = state;
    }
}
