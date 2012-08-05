package org.digitalcampus.mtrain.application;

import java.io.File;

import org.digitalcampus.mtrain.utils.FileUtils;
import org.digitalcampus.mtrain.utils.ModuleXMLReader;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

public class MTrain extends Application {

	public static final String TAG = "MTrain";

	public static final String MTRAIN_ROOT = Environment
			.getExternalStorageDirectory() + "/mtrain/";
	public static final String MODULES_PATH = MTRAIN_ROOT + "modules/";
	public static final String MEDIA_PATH = MTRAIN_ROOT + "media/";
	public static final String DOWNLOAD_PATH = MTRAIN_ROOT + "download/";
	public static final String MODULE_XML = "module.xml";

	public static void createMTrainDirs() throws RuntimeException {
		String cardstatus = Environment.getExternalStorageState();
		if (cardstatus.equals(Environment.MEDIA_REMOVED)
				|| cardstatus.equals(Environment.MEDIA_UNMOUNTABLE)
				|| cardstatus.equals(Environment.MEDIA_UNMOUNTED)
				|| cardstatus.equals(Environment.MEDIA_MOUNTED_READ_ONLY)
				|| cardstatus.equals(Environment.MEDIA_SHARED)) {
			RuntimeException e = new RuntimeException(
					"mTrain reports :: SDCard error: "
							+ Environment.getExternalStorageState());
			throw e;
		}

		String[] dirs = { MTRAIN_ROOT, MODULES_PATH, MEDIA_PATH, DOWNLOAD_PATH };

		for (String dirName : dirs) {
			File dir = new File(dirName);
			if (!dir.exists()) {
				if (!dir.mkdirs()) {
					RuntimeException e = new RuntimeException(
							"mTrain reports :: Cannot create directory: "
									+ dirName);
					throw e;
				}
			} else {
				if (!dir.isDirectory()) {
					RuntimeException e = new RuntimeException(
							"mTrain reports :: " + dirName
									+ " exists, but is not a directory");
					throw e;
				}
			}
		}
	}

	// Scan for any newly downloaded modules
	public static boolean installNewDownloads() {
		// get folder
		File dir = new File(MTrain.DOWNLOAD_PATH);

		String[] children = dir.list();
		if (children == null) {
			// Either dir does not exist or is not a directory
		} else {
			for (int i = 0; i < children.length; i++) {
				// Get filename of file or directory
				Log.v(TAG, children[i]);

				// extract to temp dir and check it's a valid package file
				File tempdir = new File(MTrain.MTRAIN_ROOT + "temp/");
				tempdir.mkdirs();
				FileUtils.unzipFiles(MTrain.DOWNLOAD_PATH, children[i],
						tempdir.getAbsolutePath());

				String[] moddirs = tempdir.list(); // use this to get the module
													// name
				// check a module.xml file exists and is a readable XML file
				String moduleXMLPath = tempdir + "/" + moddirs[0] + "/"
						+ MTrain.MODULE_XML;
				Log.v(TAG, moduleXMLPath);

				ModuleXMLReader mxr = new ModuleXMLReader(moduleXMLPath);
				mxr.getMeta();
				
				// finally delete temp directory
				FileUtils.deleteDir(tempdir);

			}
		}

		return true;
	}
}
