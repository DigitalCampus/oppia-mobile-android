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
            File[] children = activityFolder.listFiles();
            for (File dirFile : children) {
                if (dirFile.isFile()) {
                    files.add(dirFile);
                }
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
                if (exportedActivity.isFile()) {
                    files.add(exportedActivity);
                }
            }
        }
        return files;
    }
}
