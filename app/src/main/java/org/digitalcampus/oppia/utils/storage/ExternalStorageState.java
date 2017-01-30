package org.digitalcampus.oppia.utils.storage;

import android.os.Environment;

public class ExternalStorageState {

    public String getExternalStorageState(){
        return Environment.getExternalStorageState();
    }
}
