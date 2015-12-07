package org.digitalcampus.oppia.utils.storage;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.StatFs;
import android.util.Log;

import org.digitalcampus.oppia.application.MobileLearning;

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

    private static StorageAccessStrategy storageStrategy;
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

    public static boolean createFolderStructure(Context ctx) {

        if (!storageStrategy.isStorageAvailable(ctx)){
            Log.d(TAG, "Storage not available");
            return false;
        }

        //BUFFER_SIZE_CONFIG = 21;

        String[] dirs = { Storage.getCoursesPath(ctx), Storage.getMediaPath(ctx), Storage.getDownloadPath(ctx) };

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
                e.printStackTrace();
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
            return "file:///android_asset/" + filePath;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

        String localeFilePath = "www" + File.separator + Locale.getDefault().getLanguage() + File.separator + fileName;
        try {
            InputStream stream = act.getAssets().open(localeFilePath);
            stream.close();
            return "file:///android_asset/" + localeFilePath;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

        String defaultFilePath = "www" + File.separator + MobileLearning.DEFAULT_LANG + File.separator + fileName;
        try {
            InputStream stream = act.getAssets().open(defaultFilePath);
            stream.close();
            return "file:///android_asset/" + defaultFilePath;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
        return "";

    }
}
