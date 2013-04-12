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

package org.digitalcampus.mobile.learning.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.digitalcampus.mobile.learning.application.MobileLearning;

import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

public class FileUtils {
	
	public static final String TAG = FileUtils.class.getSimpleName();
	public static final int BUFFER_SIZE = 1024;

	// This function converts the zip file into uncompressed files which are
	// placed in the
	// destination directory
	// destination directory should be created first
	public static boolean unzipFiles(String srcDirectory, String srcFile,
			String destDirectory) {
		try {
			// first make sure that all the arguments are valid and not null
			if (srcDirectory == null) {
				Log.v(TAG,"1");
				return false;
			}
			if (srcFile == null) {
				Log.v(TAG,"2");
				return false;
			}
			if (destDirectory == null) {
				Log.v(TAG,"3");
				return false;
			}
			if (srcDirectory.equals("")) {
				Log.v(TAG,"4");
				return false;
			}
			if (srcFile.equals("")) {
				Log.v(TAG,"5");
				return false;
			}
			if (destDirectory.equals("")) {
				Log.v(TAG,"6");
				return false;
			}
			// now make sure that these directories exist
			File sourceDirectory = new File(srcDirectory);
			File sourceFile = new File(srcDirectory + File.separator + srcFile);
			File destinationDirectory = new File(destDirectory);

			if (!sourceDirectory.exists()) {
				Log.v(TAG,"7");
				return false;
			}
			if (!sourceFile.exists()) {
				Log.v(TAG,sourceFile.getName());
				return false;
			}
			if (!destinationDirectory.exists()) {
				Log.v(TAG,"9");
				return false;
			}

			// now start with unzip process
			BufferedOutputStream dest = null;

			FileInputStream fis = new FileInputStream(sourceFile);
			ZipInputStream zis = new ZipInputStream(
					new BufferedInputStream(fis));

			ZipEntry entry = null;

			while ((entry = zis.getNextEntry()) != null) {
				String outputFilename = destDirectory + File.separator
						+ entry.getName();

				createDirIfNeeded(destDirectory, entry);

				int count;

				byte data[] = new byte[BUFFER_SIZE];

				File f = new File(outputFilename);

				// write the file to the disk
				if (!f.isDirectory()) {
					FileOutputStream fos = new FileOutputStream(f);
					dest = new BufferedOutputStream(fos, BUFFER_SIZE);

					// this counter is a hack to prevent getting stuck when installing corrupted or not fully downloaded module packages
					// it will prevent any module being installed with files larger than around 500kb
					int counter = 0;
					while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
						dest.write(data, 0, count);
						counter++;
						if (counter > 5000){
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
			e.printStackTrace();
			BugSenseHandler.sendException(e);
			return false;
		}

		return true;
	}

	private static void createDirIfNeeded(String destDirectory, ZipEntry entry) {
		String name = entry.getName();

		if (name.contains("/")) {

			int index = name.lastIndexOf("/");
			String dirSequence = name.substring(0, index);

			File newDirs = new File(destDirectory + File.separator
					+ dirSequence);

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
	
	public static boolean mediaFileExists(String filename){
		File media = new File(MobileLearning.MEDIA_PATH + filename);
		if(media.exists()){
			return true;
		} else {
			return false;
		}
		
	}
	
	public static void cleanUp(File tempDir, String path){
		FileUtils.deleteDir(tempDir);
		Log.d(TAG, "Temp directory deleted");

		// delete zip file from download dir
		File zip = new File(path);
		zip.delete();
		Log.d(TAG, "Zip file deleted");
	}
	
	public static String readFile( String file ) throws IOException {
		 FileInputStream fstream = new FileInputStream(file);
		  DataInputStream in = new DataInputStream(fstream);
		  BufferedReader br = new BufferedReader(new InputStreamReader(in));
		  String strLine;
		  StringBuilder stringBuilder = new StringBuilder();
		  while ((strLine = br.readLine()) != null)   {
			  stringBuilder.append(strLine);
		  }
		  in.close();
		  return stringBuilder.toString();
	}
}
