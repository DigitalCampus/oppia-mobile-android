/*
 * This file is part of OppiaMobile - https://digital-campus.org/
 *
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.utils.storage;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.splunk.mint.Mint;

import org.digitalcampus.oppia.application.App;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtils {

    public static final String TAG = FileUtils.class.getSimpleName();
    public static final int BUFFER_SIZE = 1024;

    private FileUtils() {
        throw new IllegalStateException("Utility class");
    }

    // This function converts the zip file into uncompressed files which are
    // placed in the destination directory
    // destination directory should be created first
    public static boolean unzipFiles(String srcDirectory, String srcFile, String destDirectory) {

        // first make sure that all the arguments are valid and not null
        if (TextUtils.isEmpty(srcDirectory)
                || TextUtils.isEmpty(srcFile)
                || TextUtils.isEmpty(destDirectory)) {
            return false;
        }

        // now make sure that these directories exist
        File sourceDirectory = new File(srcDirectory);
        File sourceFile = new File(srcDirectory + File.separator + srcFile);
        File destinationDirectory = new File(destDirectory);

        if (!sourceDirectory.exists()
                || !sourceFile.exists()
                || !destinationDirectory.exists()) {
            return false;
        }

        try (FileInputStream fis = new FileInputStream(sourceFile);
             ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis))) {

            // now start with unzip process

            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {

                if (entry.getName().startsWith("..")) {
                    throw new SecurityException("Suspect file: " + entry.getName()
                            + ". Possibility of trying to access parent directory");
                }

                String outputFilename = destDirectory + File.separator + entry.getName();

                createDirIfNeeded(destDirectory, entry);

                int count;

                byte[] data = new byte[BUFFER_SIZE];

                File f = new File(outputFilename);

                // write the file to the disk
                if (!f.isDirectory()) {

                    try (FileOutputStream fos = new FileOutputStream(f);
                         BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE)) {

                        while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
                            dest.write(data, 0, count);
                        }
                    }

                }
            }

        } catch (Exception e) {
            Mint.logException(e);
            Log.d(TAG, "Exception:", e);
            return false;
        }

        return true;
    }


    public static String getDigestFromMessage(MessageDigest mDigest) {
        byte[] digest = mDigest.digest();
        StringBuilder resultMD5 = new StringBuilder();

        for (byte aDigest : digest) {
            resultMD5.append(Integer.toString((aDigest & 0xff) + 0x100, 16).substring(1));
        }
        return resultMD5.toString();
    }

    public static boolean zipFileAtPath(File sourceFile, File zipDestination) {
        final int BUFFER = 2048;
        Log.d(TAG, "Zipping " + sourceFile + " into " + zipDestination);

        try (FileOutputStream dest = new FileOutputStream(zipDestination);
             ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
             FileInputStream fi = new FileInputStream(zipDestination);
             BufferedInputStream origin = new BufferedInputStream(fi, BUFFER)) {

            if (sourceFile.isDirectory()) {
                zipSubFolder(out, sourceFile, sourceFile.getParent().length());
            } else {
                byte[] data = new byte[BUFFER];
                ZipEntry entry = new ZipEntry(getLastPathComponent(zipDestination.getPath()));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
            }
        } catch (Exception e) {
            Mint.logException(e);
            Log.d(TAG, "Exception:", e);
            return false;
        }

        return true;
    }


    private static void zipSubFolder(ZipOutputStream out, File folder,
                                     int basePathLength) throws IOException {

        final int BUFFER = 2048;

        Log.d(TAG, "Zipping folder " + folder.getPath());

        File[] fileList = folder.listFiles();

        for (File file : fileList) {
                if (file.isDirectory()) {
                    zipSubFolder(out, file, basePathLength);
                } else {
                    String unmodifiedFilePath = file.getPath();
                    try (FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                         BufferedInputStream origin = new BufferedInputStream(fi, BUFFER)) {

                    byte[] data = new byte[BUFFER];
                    String relativePath = unmodifiedFilePath
                            .substring(basePathLength);
                    ZipEntry entry = new ZipEntry(relativePath);
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                }
            }
        }
    }

    private static String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        if (segments.length == 0)
            return "";
        return segments[segments.length - 1];
    }

    private static void createDirIfNeeded(String destDirectory, ZipEntry entry) {
        String name = entry.getName();

        if (name.contains(File.separator)) {
            int index = name.lastIndexOf(File.separator);
            String dirSequence = name.substring(0, index);
            File newDirs = new File(destDirectory + File.separator + dirSequence);

            // create the directory
            newDirs.mkdirs();
        }
    }

    public static boolean cleanDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String dirFiles : children) {
                File fileToDelete = new File(dir, dirFiles);
                boolean success = deleteDir(fileToDelete);
                if (!success) {
                    return false;
                }
            }
        }
        return true;
    }

    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns
    // false.
    public static boolean deleteDir(File dir) {
        if (cleanDir(dir)) {
            // The directory is now empty so delete it
            return dir.delete(); //NOSONAR (Files.delete() is available from API 26)
        } else {
            return false;
        }
    }

    public static long dirSize(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            long result = 0;
            File[] fileList = dir.listFiles();
            for (File file : fileList) {
                if (file.isDirectory()) {
                    result += dirSize(file);
                } else {
                    result += file.length();
                }
            }
            return result;
        }
        return 0;
    }

    public static boolean cleanUp(File tempDir, String path) {
        FileUtils.deleteDir(tempDir);

        // delete zip file from download dir
        File zip = new File(path);
        return zip.delete(); //NOSONAR (Files.delete() is available from API 26)
    }

    public static String readFile(String file) throws IOException {
        FileInputStream fstream = new FileInputStream(file);
        return readFile(fstream);
    }

    public static String readFile(File file) throws IOException {
        FileInputStream fstream = new FileInputStream(file);
        return readFile(fstream);
    }

    public static String readFile(InputStream fileStream) throws IOException {

        try (DataInputStream in = new DataInputStream(fileStream);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));){

            String strLine;
            StringBuilder stringBuilder = new StringBuilder();
            while ((strLine = br.readLine()) != null) {
                stringBuilder.append(strLine);
            }
            return stringBuilder.toString();
        }
    }

    public static boolean deleteFile(File file) {
        if ((file != null) && file.exists() && !file.isDirectory()) {
            boolean deleted = file.delete(); //NOSONAR (Files.delete() is available from API 26)
            Log.d(TAG, file.getName() + (deleted ? " deleted succesfully." : " deletion failed!"));
            return deleted;
        }

        return false;
    }

    public static String getMimeType(String url) {
        String type = null;
        int lastIndex = url.lastIndexOf('.');
        if (lastIndex > 0) {
            String extension = url.substring(lastIndex + 1);
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension.toLowerCase());
        }
        return type;
    }

    public static boolean isSupportedMediafileType(String mimeType) {
        Log.d(TAG, mimeType);
        if (mimeType == null) {
            return false;
        }
        for (String s : App.SUPPORTED_MEDIA_TYPES) {
            if (mimeType.equals(s)) {
                return true;
            }
        }
        return false;
    }

    public static void moveFileToDir(File file, File mediaDir, boolean deleteOnError) {
        try {
            org.apache.commons.io.FileUtils.moveFileToDirectory(file, mediaDir, true);
        } catch (IOException e) {
            Mint.logException(e);
            Log.d(TAG, "Moving file failed", e);
            if (deleteOnError) {
                FileUtils.deleteFile(file);
            }
        }
    }

    public static String readableFileSize(long size) {
        if (size <= 0)
            return "0";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
