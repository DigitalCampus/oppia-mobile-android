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

import org.digitalcampus.oppia.application.MobileLearning;

import android.app.Activity;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.bugsense.trace.BugSenseHandler;

public class FileUtils {

	public static final String TAG = FileUtils.class.getSimpleName();
	public static final int BUFFER_SIZE = 1024;

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

		if (name.contains("/")) {

			int index = name.lastIndexOf("/");
			String dirSequence = name.substring(0, index);

			File newDirs = new File(destDirectory + File.separator + dirSequence);

			// create the directory
			newDirs.mkdirs();
		}
	}

	// Deletes all files and subdirectories under dir.
	// Returns true if all deletions were successful.
	// If a deletion fails, the method stops attempting to delete and returns
	// false.
	public static boolean deleteDir(File dir) {
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

		// The directory is now empty so delete it
		return dir.delete();
	}

	public static boolean mediaFileExists(String filename) {
		File media = new File(MobileLearning.MEDIA_PATH + filename);
		if (media.exists()) {
			return true;
		} else {
			return false;
		}

	}

	public static void cleanUp(File tempDir, String path) {
		FileUtils.deleteDir(tempDir);
		Log.d(TAG, "Temp directory deleted");

		// delete zip file from download dir
		File zip = new File(path);
		zip.delete();
		Log.d(TAG, "Zip file deleted");
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
		String filePath = "www/" + currentLang + "/" + fileName;
		try {
			InputStream stream = act.getAssets().open(filePath);
			stream.close();
			Log.d(TAG, "assetExists exists: " + filePath);
			return filePath;
		} catch (FileNotFoundException e) {
			Log.d(TAG, "assetExists failed: " + e.toString());
		} catch (IOException e) {
			Log.w(TAG, "assetExists failed: " + e.toString());
		}

		String localeFilePath = "www/" + Locale.getDefault().getLanguage() + "/" + fileName;
		try {
			InputStream stream = act.getAssets().open(localeFilePath);
			stream.close();
			Log.d(TAG, "assetExists exists: " + localeFilePath);
			return "file:///android_asset/" + localeFilePath;
		} catch (FileNotFoundException e) {
			Log.d(TAG, "assetExists failed: " + e.toString());
		} catch (IOException e) {
			Log.w(TAG, "assetExists failed: " + e.toString());
		}

		String defaultFilePath = "www/" + MobileLearning.DEFAULT_LANG + "/" + fileName;
		try {
			InputStream stream = act.getAssets().open(defaultFilePath);
			stream.close();
			Log.d(TAG, "assetExists exists: " + defaultFilePath);
			return "file:///android_asset/" + defaultFilePath;
		} catch (FileNotFoundException e) {
			Log.d(TAG, "assetExists failed: " + e.toString());
		} catch (IOException e) {
			Log.w(TAG, "assetExists failed: " + e.toString());
		}
		return "";

	}
}
