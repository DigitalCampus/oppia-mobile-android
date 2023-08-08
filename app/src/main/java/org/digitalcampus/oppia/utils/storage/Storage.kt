package org.digitalcampus.oppia.utils.storage

import android.app.Activity
import android.content.Context
import android.os.StatFs
import android.util.Log
import org.digitalcampus.oppia.analytics.Analytics
import org.digitalcampus.oppia.application.App
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.util.Locale

object Storage {
    val TAG = Storage::class.simpleName
    const val APP_ROOT_DIR_NAME = "digitalcampus"
    const val APP_COURSES_DIR_NAME = "modules"
    const val APP_DOWNLOAD_DIR_NAME = "download"
    const val APP_MEDIA_DIR_NAME = "media"
    const val APP_ACTIVITY_DIR_NAME = "activity"
    const val APP_ACTIVITY_ARCHIVE_DIR_NAME = "archived_activity"
    const val APP_ACTIVITY_FULL_EXPORT_DIR_NAME = "activity_full_export"
    const val APP_BACKUP_DIR_NAME = "backup"
    const val APP_TMP_TRANSFER_DIR_NAME = "tmpbt"
    const val APP_LEADERBOARD_DIR_NAME = "leaderboard"
    private const val FILE_ASSETS_ROOT = "file:///android_asset/"
    private const val FILE_NOT_FOUND = "file not found for:"
    private const val FILE_READ_ERROR = "Error reading file: "

    @JvmStatic
    var storageStrategy: StorageAccessStrategy? = null

    @JvmStatic
    fun getStorageLocationRoot(ctx: Context): String? {
        return storageStrategy?.getStorageLocation(ctx)
    }

    @JvmStatic
    fun getCoursesPath(ctx: Context): String {
        return getStorageLocationRoot(ctx) + File.separator + APP_COURSES_DIR_NAME + File.separator
    }

    @JvmStatic
    fun getDownloadPath(ctx: Context): String {
        return getStorageLocationRoot(ctx) + File.separator + APP_DOWNLOAD_DIR_NAME + File.separator
    }

    @JvmStatic
    fun getMediaPath(ctx: Context): String {
        return getStorageLocationRoot(ctx) + File.separator + APP_MEDIA_DIR_NAME + File.separator
    }

    @JvmStatic
    fun getActivityPath(ctx: Context): String {
        return getStorageLocationRoot(ctx) + File.separator + APP_ACTIVITY_DIR_NAME + File.separator
    }

    @JvmStatic
    fun getActivityArchivePath(ctx: Context): String {
        return getStorageLocationRoot(ctx) + File.separator + APP_ACTIVITY_ARCHIVE_DIR_NAME + File.separator
    }

    @JvmStatic
    fun getActivityFullExportPath(ctx: Context): String {
        return getStorageLocationRoot(ctx) + File.separator + APP_ACTIVITY_FULL_EXPORT_DIR_NAME + File.separator
    }

    @JvmStatic
    fun getCourseBackupPath(ctx: Context): String {
        return getStorageLocationRoot(ctx) + File.separator + APP_BACKUP_DIR_NAME + File.separator
    }

    @JvmStatic
    fun getBluetoothTransferTempPath(ctx: Context): String {
        return getStorageLocationRoot(ctx) + File.separator + APP_TMP_TRANSFER_DIR_NAME + File.separator
    }

    @JvmStatic
    fun getLeaderboardImportPath(ctx: Context): String {
        return getStorageLocationRoot(ctx) + File.separator + APP_LEADERBOARD_DIR_NAME + File.separator
    }

    @JvmStatic
    fun createFolderStructure(ctx: Context): Boolean {
        if (storageStrategy?.isStorageAvailable(ctx) == false) {
            Log.d(TAG, "Storage not available")
            return false
        }
        val dirs = arrayOf(
                getCoursesPath(ctx),
                getMediaPath(ctx),
                getDownloadPath(ctx),
                getLeaderboardImportPath(ctx)
        )
        for (dirName in dirs) {
            val dir = File(dirName)
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.d(TAG, dirName)
                    Log.d(TAG, "can't mkdirs")
                    return false
                }
            } else {
                if (!dir.isDirectory) {
                    Log.d(TAG, "not a directory")
                    return false
                }
            }
        }
        //After creating the necessary folders, we create the .nomedia file
        createNoMediaFile(ctx)
        return true
    }

    @JvmStatic
    fun mediaFileExists(ctx: Context, filename: String): Boolean {
        val media = File(getMediaPath(ctx) + filename)
        Log.d(TAG, "Looking for: " + getMediaPath(ctx) + filename)
        return media.exists()
    }

    @JvmStatic
    fun createNoMediaFile(ctx: Context) {
        val storagePath = storageStrategy?.getStorageLocation(ctx)
        val dir = File(storagePath)
        val nomedia = File(dir, ".nomedia")
        if (!nomedia.exists()) {
            var fileCreated = false
            try {
                fileCreated = nomedia.createNewFile()
            } catch (e: IOException) {
                Analytics.logException(e)
                Log.d(TAG, "IOException", e)
            }
            Log.d(TAG, (if (fileCreated) "File .nomedia created in " else "Failed creating .nomedia file in ") + dir.absolutePath)
        }
    }

    @JvmStatic
    fun getAvailableStorageSize(ctx: Context): Long {
        val path = getStorageLocationRoot(ctx)
        val stat = StatFs(path)
        return stat.blockSizeLong * stat.availableBlocksLong
    }

    @JvmStatic
    fun getTotalStorageUsed(ctx: Context): Long {
        val dir = File(getStorageLocationRoot(ctx))
        return FileUtils.dirSize(dir)
    }

    @JvmStatic
    fun getLocalizedFilePath(act: Activity, currentLang: String, fileName: String): String {
        val filePath = "www" + File.separator + currentLang + File.separator + fileName
        try {
            val stream = act.assets.open(filePath)
            stream.close()
            return FILE_ASSETS_ROOT + filePath
        } catch (fnfe: FileNotFoundException) {
            Log.d(TAG, FILE_NOT_FOUND + filePath, fnfe)
        } catch (ioe: IOException) {
            Log.d(TAG, FILE_READ_ERROR + filePath, ioe)
        }
        val localeFilePath = "www" + File.separator + Locale.getDefault().language + File.separator + fileName
        try {
            val stream = act.assets.open(localeFilePath)
            stream.close()
            return FILE_ASSETS_ROOT + localeFilePath
        } catch (fnfe: FileNotFoundException) {
            Log.d(TAG, FILE_NOT_FOUND + localeFilePath, fnfe)
        } catch (ioe: IOException) {
            Log.d(TAG, FILE_READ_ERROR + localeFilePath, ioe)
        }
        val defaultFilePath = "www" + File.separator + App.DEFAULT_LANG + File.separator + fileName
        try {
            val stream = act.assets.open(defaultFilePath)
            stream.close()
            return FILE_ASSETS_ROOT + defaultFilePath
        } catch (fnfe: FileNotFoundException) {
            Log.d(TAG, FILE_NOT_FOUND + defaultFilePath, fnfe)
        } catch (ioe: IOException) {
            Log.d(TAG, FILE_READ_ERROR + defaultFilePath, ioe)
        }
        return ""
    }

}