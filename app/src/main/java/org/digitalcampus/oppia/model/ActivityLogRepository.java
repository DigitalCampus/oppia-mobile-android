package org.digitalcampus.oppia.model;

import android.content.Context;

import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogRepository {

    public List<File> getExportedActivityLogs(Context ctx){
        List<File> files = new ArrayList<>();
        File activityFolder = new File(Storage.getActivityPath(ctx));
        if (activityFolder.exists()) {
            String[] children = activityFolder.list();
            for (String dirFiles : children) {
                File exportedActivity = new File(activityFolder, dirFiles);
                files.add(exportedActivity);
            }
        }
        return files;
    }

    public List<File> getArchivedActivityLogs(Context ctx){
        List<File> files = new ArrayList<>();
        File archivedFolder = new File(Storage.getActivityArchivePath(ctx));
        if (archivedFolder.exists()) {
            String[] children = archivedFolder.list();
            for (String dirFiles : children) {
                File exportedActivity = new File(archivedFolder, dirFiles);
                files.add(exportedActivity);
            }
        }
        return files;
    }
}
