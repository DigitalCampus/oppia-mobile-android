package org.digitalcampus.oppia.utils.storage;

import org.digitalcampus.oppia.activity.PrefsActivity;

public class StorageAccessStrategyFactory {
    public static StorageAccessStrategy createStrategy(String type){
        if ((type!=null)&&(type.equals(PrefsActivity.STORAGE_OPTION_EXTERNAL))){
            return new ExternalStorageStrategy();
        }
        else{
            return new InternalStorageStrategy();
        }
    }
}
