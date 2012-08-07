package org.digitalcampus.mtrain.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.util.Log;

public class FileUtils {
	
	public static final String TAG = "FileUtils";
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

				Log.v(TAG,"Extracting file: " + entry.getName());

				createDirIfNeeded(destDirectory, entry);

				int count;

				byte data[] = new byte[BUFFER_SIZE];

				File f = new File(outputFilename);

				// write the file to the disk
				if (!f.isDirectory()) {
					FileOutputStream fos = new FileOutputStream(f);
					dest = new BufferedOutputStream(fos, BUFFER_SIZE);

					while ((count = zis.read(data, 0, BUFFER_SIZE)) != -1) {
						dest.write(data, 0, count);
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
				} else {
					//Log.v(TAG,delFile.getName() + " deleted");
				}
			}
		}
		
		// The directory is now empty so delete it
		return dir.delete();
	}
	
	public static boolean mediaFileExists(String filename){
		
		return true;
	}

}
