package org.digitalcampus.mtrain.application;

import java.io.File;
import java.util.HashMap;

import org.digitalcampus.mtrain.utils.FileUtils;
import org.digitalcampus.mtrain.utils.ModuleXMLReader;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class MTrain extends Application {

	public static final String TAG = "MTrain";

	public static final String MTRAIN_ROOT = Environment
			.getExternalStorageDirectory() + "/mtrain/";
	public static final String MODULES_PATH = MTRAIN_ROOT + "modules/";
	public static final String MEDIA_PATH = MTRAIN_ROOT + "media/";
	public static final String DOWNLOAD_PATH = MTRAIN_ROOT + "download/";
	public static final String MODULE_XML = "module.xml";

	private Context ctx;
	
	public MTrain(Context context){
		this.ctx = context;
	}
	
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
	public boolean installNewDownloads() {
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
				HashMap<String, String> hm = mxr.getMeta();
				
				String versionid = hm.get("versionid");
				String title = hm.get("title");
				String location = MTrain.MODULES_PATH + moddirs[0];
				
				DbHelper db = new DbHelper(ctx);
				long added = db.addOrUpdateModule(versionid, title, location);

				if(added != -1){
					db.insertActivities(mxr.getActivities(added));
					// Delete old module 
					File oldMod = new File(MTrain.MODULES_PATH + moddirs[0]);
					FileUtils.deleteDir(oldMod);
					
					// move from temp to modules dir
					File src = new File(tempdir + "/" + moddirs[0]);
					File dest = new File(MTrain.MODULES_PATH);
					boolean success = src.renameTo(new File(dest, src.getName()));
	
			        if (success) {
			            Log.v(TAG,"File was successfully moved");
			        } else {
			        	Log.v(TAG,"File was not successfully moved");
			        }
			        // Show user that modules has been installed
			        // TODO change to use string param 
			        Toast.makeText(ctx, "Module: '"+ title + "' successfully installed", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(ctx, "Error installing module", Toast.LENGTH_SHORT).show();
				}
				db.close();
				// delete temp directory
				FileUtils.deleteDir(tempdir);
				Log.d(TAG,"Temp directory deleted");
				
				// TODO delete zip file from download dir 
				File zip = new File(MTrain.DOWNLOAD_PATH + children[i]);
				zip.delete();
				Log.d(TAG,"Zip file deleted");

			}
		}

		return true;
	}
}
