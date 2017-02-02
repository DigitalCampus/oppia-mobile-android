package org.digitalcampus.oppia.utils.storage;

import android.os.Environment;

public class ExternalStorageState {

    private static ExternalStorageState _instance = new ExternalStorageState();

    public static String getExternalStorageState(){ return _instance.getState();  }

    protected String getState() { return Environment.getExternalStorageState();}

    public static void setExternalStorageState(ExternalStorageState state){
        _instance = state;
    }
}
