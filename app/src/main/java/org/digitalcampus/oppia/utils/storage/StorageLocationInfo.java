package org.digitalcampus.oppia.utils.storage;

import android.content.Context;

import org.digitalcampus.mobile.learning.R;

public class StorageLocationInfo {

    public final String path;
    public final boolean readonly;
    public final boolean removable;
    public final int number;

    StorageLocationInfo(String path, boolean readonly, boolean removable, int number) {
        this.path = path;
        this.readonly = readonly;
        this.removable = removable;
        this.number = number;
    }

    public String getDisplayName(Context ctx) {
        StringBuilder res = new StringBuilder();
        if (!removable) {
            res.append(ctx.getString(R.string.storageNonRemovable));
        } else {
            res.append(ctx.getString(R.string.storageRemovable));
            if (number > 1) {
                res.append(" ").append(number);
            }
        }
        if (readonly) {
            res.append(" (Read only)");
        }
        return res.toString();
    }
}