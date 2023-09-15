package org.digitalcampus.oppia.model

import android.content.Context
import org.digitalcampus.oppia.utils.storage.Storage
import java.io.File

class ActivityLogRepository {
    fun getExportedActivityLogs(ctx: Context): List<File> {
        val files = ArrayList<File>()
        val activityFolder = File(Storage.getActivityPath(ctx))
        if (activityFolder.exists()) {
            val children = activityFolder.listFiles()
            for (dirFile in children!!) {
                if (dirFile.isFile) {
                    files.add(dirFile)
                }
            }
        }
        return files
    }

    fun getArchivedActivityLogs(ctx: Context): List<File> {
        val files = ArrayList<File>()
        val archivedFolder = File(Storage.getActivityArchivePath(ctx))
        if (archivedFolder.exists()) {
            val children = archivedFolder.list()
            for (dirFiles in children!!) {
                val exportedActivity = File(archivedFolder, dirFiles)
                if (exportedActivity.isFile) {
                    files.add(exportedActivity)
                }
            }
        }
        return files
    }
}