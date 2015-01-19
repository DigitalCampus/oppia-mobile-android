/* 
 * This file is part of OppiaMobile - http://oppia-mobile.org/
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

package org.digitalcampus.oppia.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.MobileLearning;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.bugsense.trace.BugSenseHandler;

public class FileUtils {

	public static final String TAG = FileUtils.class.getSimpleName();
	public static final int BUFFER_SIZE = 1024;
	
	public static final String APP_ROOT_DIR_NAME = "digitalcampus";
	
	public static final String APP_COURSES_DIR_NAME = "modules";
	public static final String APP_DOWNLOAD_DIR_NAME = "download";
	public static final String APP_MEDIA_DIR_NAME = "media";

    public static int BUFFER_SIZE_CONFIG = 1024;
	
	public static boolean createDirs(Context ctx) {
		String cardstatus = Environment.getExternalStorageState();
		if (cardstatus.equals(Environment.MEDIA_REMOVED)
				|| cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
				|| cardstatus.equals(Environment.MEDIA_UNMOUNTED)
				|| cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)
				|| cardstatus.equals(Environment.MEDIA_SHARED)) {
			Log.d(TAG, "card status: " + cardstatus);
			return false;
		}
        BUFFER_SIZE_CONFIG = 21;

		String[] dirs = { FileUtils.getCoursesPath(ctx), FileUtils.getMediaPath(ctx), FileUtils.getDownloadPath(ctx) };

		for (String dirName : dirs) {
			File dir = new File(dirName);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
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
		
		return true;
	}
	
	public static String getStorageLocationRoot(Context ctx){
		File[] dirs = ContextCompat.getExternalFilesDirs(ctx,null);
		
		//get from prefs
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		String location = prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, "");
		// if location not set - then set it to first of dirs
		if (location.equals("") && dirs.length > 0){
			location = dirs[dirs.length-1].toString();
			Editor editor = prefs.edit();
			editor.putString(PrefsActivity.PREF_STORAGE_LOCATION, location);
			editor.commit();
		}

		return location;
	}
	
	public static String getCoursesPath(Context ctx){
		return getStorageLocationRoot(ctx) + File.separator + "modules" + File.separator;
	}
	
	public static String getDownloadPath(Context ctx){
		return getStorageLocationRoot(ctx) + File.separator + "download" + File.separator;
	}
	
	public static String getMediaPath(Context ctx){
		return getStorageLocationRoot(ctx) + File.separator + "media" + File.separator;
	}
	
	// This function converts the zip file into uncompressed files which are
	// placed in the
	// destination directory
	// destination directory should be created first
	public static boolean unzipFiles(String srcDirectory, String srcFile, String destDirectory) {
		try {
			// first make sure that all the arguments are valid and not null
			if (srcDirectory == null) {
				return false;
			}
			if (srcFile == null) {
				return false;
			}
			if (destDirectory == null) {
				return false;
			}
			if (srcDirectory.equals("")) {
				return false;
			}
			if (srcFile.equals("")) {
				return false;
			}
			if (destDirectory.equals("")) {
				return false;
			}
			// now make sure that these directories exist
			File sourceDirectory = new File(srcDirectory);
			File sourceFile = new File(srcDirectory + File.separator + srcFile);
			File destinationDirectory = new File(destDirectory);

			if (!sourceDirectory.exists()) {
				return false;
			}
			if (!sourceFile.exists()) {
				return false;
			}
			if (!destinationDirectory.exists()) {
				return false;
			}

			// now start with unzip process
			BufferedOutputStream dest = null;

			FileInputStream fis = new FileInputStream(sourceFile);
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));

			ZipEntry entry = null;

			while ((entry = zis.getNextEntry()) != null) {
				String outputFilename = destDirectory + File.separator + entry.getName();

				createDirIfNeeded(destDirectory, entry);

				int count;

				byte data[] = new byte[BUFFER_SIZE];

				File f = new File(outputFilename);

				// write the file to the disk
				if (!f.isDirectory()) {
					FileOutputStream fos = new FileOutputStream(f);
					dest = new BufferedOutputStream(fos, BUFFER_SIZE);

					// this counter is a hack to prevent getting stuck when
					// installing corrupted or not fully downloaded course
					// packages
					// it will prevent any course being installed with files
					// larger than around 500kb
					int counter = 0;
					while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
						dest.write(data, 0, count);
						counter++;
						if (counter > 5000) {
							dest.flush();
							dest.close();
							return false;
						}
					}
					// close the output streams
					dest.flush();
					dest.close();
				}
			}

			// we are done with all the files
			// close the zip file
			zis.close();

		} catch (Exception e) {
			if(!MobileLearning.DEVELOPER_MODE){
				BugSenseHandler.sendException(e);
			} else {
				e.printStackTrace();
			}
			return false;
		}

		return true;
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

    private static boolean cleanDir(File dir){
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                File delFile = new File(dir, children[i]);
                boolean success = deleteDir(delFile);
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
        if (cleanDir(dir)){
            // The directory is now empty so delete it
            return dir.delete();
        }
        else {
            return false;
        }
	}

	public static boolean mediaFileExists(Context ctx, String filename) {
		File media = new File(FileUtils.getMediaPath(ctx) + filename);
		if (media.exists()) {
			return true;
		} else {
			return false;
		}
	}

    private static long dirSize(File dir){
        if (dir.exists() && dir.isDirectory()) {
            long result = 0;
            File[] fileList = dir.listFiles();
            for(int i = 0; i < fileList.length; i++) {
                if(fileList[i].isDirectory()) {
                    result += dirSize(fileList [i]);
                } else {
                    result += fileList[i].length();
                }
            }
            return result;
        }
        return 0;
    }

    public static int getAvailableStorageSize(Context ctx){
        StatFs stat = new StatFs(getStorageLocationRoot(ctx));
        int bytesAvailable;
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bytesAvailable = (int)(stat.getBlockSizeLong() * stat.getAvailableBlocksLong());
        }
        else{
            bytesAvailable = stat.getBlockSize() * stat.getAvailableBlocks();
        }
        return bytesAvailable;
    }

    public static long totalStorageUsed(Context ctx){
        File dir = new File(getStorageLocationRoot(ctx));
        return dirSize(dir);
    }

	public static void cleanUp(File tempDir, String path) {
		FileUtils.deleteDir(tempDir);

		// delete zip file from download dir
		File zip = new File(path);
		zip.delete();
	}

	public static String readFile(String file) throws IOException {
		FileInputStream fstream = new FileInputStream(file);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		StringBuilder stringBuilder = new StringBuilder();
		while ((strLine = br.readLine()) != null) {
			stringBuilder.append(strLine);
		}
		in.close();
		return stringBuilder.toString();
	}

	public static String readFile(InputStream fileStream) throws IOException {
		DataInputStream in = new DataInputStream(fileStream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		StringBuilder stringBuilder = new StringBuilder();
		while ((strLine = br.readLine()) != null) {
			stringBuilder.append(strLine);
		}
		in.close();
		return stringBuilder.toString();
	}

	public static String getMimeType(String url) {
		String type = null;
		String extension = MimeTypeMap.getFileExtensionFromUrl(url);
		if (extension != null) {
			MimeTypeMap mime = MimeTypeMap.getSingleton();
			type = mime.getMimeTypeFromExtension(extension);
		}
		return type;
	}

	public static boolean supportedMediafileType(String mimeType) {
		if (mimeType == null) {
			return false;
		} else if (mimeType.equals("video/m4v")) {
			return true;
		} else if (mimeType.equals("video/mp4")) {
			return true;
		} else {
			return false;
		}
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
