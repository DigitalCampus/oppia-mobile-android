package org.digitalcampus.oppia.utils.storage;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.StatFs;
import android.util.Log;

import com.splunk.mint.Mint;

import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.Tracker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class Storage {

    public static final String TAG = Storage.class.getSimpleName();

    public static final String APP_ROOT_DIR_NAME = "digitalcampus";
    public static final String APP_COURSES_DIR_NAME = "modules";
    public static final String APP_DOWNLOAD_DIR_NAME = "download";
    public static final String APP_MEDIA_DIR_NAME = "media";
    public static final String APP_ACTIVITY_DIR_NAME = "activity";
    public static final String APP_ACTIVITY_ARCHIVE_DIR_NAME = "archived_activity";
    public static final String APP_BACKUP_DIR_NAME = "backup";
    public static final String APP_TMP_TRANSFER_DIR_NAME = "tmpbt";
    public static final String APP_LEADERBOARD_DIR_NAME = "leaderboard";

    private static final String FILE_ASSETS_ROOT = "file:///android_asset/";
    private static final String FILE_NOT_FOUND = "file not found for:";
    private static final String FILE_READ_ERROR = "Error reading file: ";

    private static StorageAccessStrategy storageStrategy;

    private Storage() {
        throw new IllegalStateException("Utility class");
    }

    public static void setStorageStrategy(StorageAccessStrategy strategy){
        storageStrategy = strategy;
    }
    public static StorageAccessStrategy getStorageStrategy() {
        return storageStrategy;
    }

    public static String getStorageLocationRoot(Context ctx){
        return storageStrategy.getStorageLocation(ctx);
    }

    public static String getCoursesPath(Context ctx){
        return getStorageLocationRoot(ctx) + File.separator + APP_COURSES_DIR_NAME + File.separator;
    }

    public static String getDownloadPath(Context ctx){
        return getStorageLocationRoot(ctx) + File.separator + APP_DOWNLOAD_DIR_NAME + File.separator;
    }

    public static String getMediaPath(Context ctx){
        return getStorageLocationRoot(ctx) + File.separator + APP_MEDIA_DIR_NAME + File.separator;
    }

    public static String getActivityPath(Context ctx){
        return getStorageLocationRoot(ctx) + File.separator + APP_ACTIVITY_DIR_NAME + File.separator;
    }

    public static String getActivityArchivePath(Context ctx){
        return getStorageLocationRoot(ctx) + File.separator + APP_ACTIVITY_ARCHIVE_DIR_NAME + File.separator;
    }

    public static String getCourseBackupPath(Context ctx){
        return getStorageLocationRoot(ctx) + File.separator + APP_BACKUP_DIR_NAME + File.separator;
    }

    public static String getBluetoothTransferTempPath(Context ctx){
        return getStorageLocationRoot(ctx) + File.separator + APP_TMP_TRANSFER_DIR_NAME + File.separator;
    }

    public static String getLeaderboardImportPath(Context ctx){
        return getStorageLocationRoot(ctx) + File.separator + APP_LEADERBOARD_DIR_NAME + File.separator;
    }

    public static boolean createFolderStructure(Context ctx) {

        if (!storageStrategy.isStorageAvailable()){
            Log.d(TAG, "Storage not available");
            return false;
        }

        String[] dirs = {
                Storage.getCoursesPath(ctx),
                Storage.getMediaPath(ctx),
                Storage.getDownloadPath(ctx),
                Storage.getLeaderboardImportPath(ctx) };

        for (String dirName : dirs) {
            File dir = new File(dirName);
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    Log.d(TAG, dirName);
                    Log.d(TAG, "can't mkdirs");
                    return false;
                }
            } else {
                if (!dir.isDirectory()) {
                    Log.d(TAG, "not a directory");
                    return false;
                }
            }
        }
        //After creating the necessary folders, we create the .nomedia file
        createNoMediaFile(ctx);

        return true;
    }

    public static boolean mediaFileExists(Context ctx, String filename) {
        File media = new File(Storage.getMediaPath(ctx) + filename);
        Log.d(TAG, "Looking for: " + Storage.getMediaPath(ctx) + filename);

        if (!media.exists()){
            //Save the missing media tracker
            new Tracker(ctx).saveMissingMediaTracker(filename);
        }

        return media.exists();
    }

    public static void createNoMediaFile(Context ctx){
        String storagePath = storageStrategy.getStorageLocation(ctx);
        File dir = new File(storagePath);
        File nomedia = new File(dir, ".nomedia");
        if (!nomedia.exists()){
            boolean fileCreated = false;
            try {
                fileCreated = nomedia.createNewFile();
            } catch (IOException e) {
                Mint.logException(e);
                Log.d(TAG, "IOException", e);
            }
            Log.d(TAG, (fileCreated ? "File .nomedia created in " : "Failed creating .nomedia file in ") + dir.getAbsolutePath());
        }
    }

    @SuppressWarnings("deprecation")
    public static long getAvailableStorageSize(Context ctx){
        String path = getStorageLocationRoot(ctx);
        StatFs stat = new StatFs(path);
        long bytesAvailable;
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        }
        else{
            bytesAvailable = (long)stat.getBlockSize() * (long)stat.getAvailableBlocks();
        }
        return bytesAvailable;
    }

    public static long getTotalStorageUsed(Context ctx){
        File dir = new File(getStorageLocationRoot(ctx));
        return FileUtils.dirSize(dir);
    }

    public static String getLocalizedFilePath(Activity act, String currentLang, String fileName) {
        String filePath = "www" + File.separator + currentLang + File.separator + fileName;
        try {
            InputStream stream = act.getAssets().open(filePath);
            stream.close();
            return FILE_ASSETS_ROOT + filePath;
        } catch (FileNotFoundException fnfe) {
            Log.d(TAG,FILE_NOT_FOUND+ filePath, fnfe);
        } catch (IOException ioe) {
            Log.d(TAG,FILE_READ_ERROR+ filePath, ioe);
        }

        String localeFilePath = "www" + File.separator + Locale.getDefault().getLanguage() + File.separator + fileName;
        try {
            InputStream stream = act.getAssets().open(localeFilePath);
            stream.close();
            return FILE_ASSETS_ROOT + localeFilePath;
        } catch (FileNotFoundException fnfe) {
            Log.d(TAG,FILE_NOT_FOUND+ localeFilePath, fnfe);
        } catch (IOException ioe) {
            Log.d(TAG,FILE_READ_ERROR+ localeFilePath, ioe);
        }

        String defaultFilePath = "www" + File.separator + App.DEFAULT_LANG + File.separator + fileName;
        try {
            InputStream stream = act.getAssets().open(defaultFilePath);
            stream.close();
            return FILE_ASSETS_ROOT + defaultFilePath;
        } catch (FileNotFoundException fnfe) {
            Log.d(TAG,FILE_NOT_FOUND+ defaultFilePath, fnfe);
        } catch (IOException ioe) {
            Log.d(TAG,FILE_READ_ERROR+ defaultFilePath, ioe);
        }
        return "";

    }
}
