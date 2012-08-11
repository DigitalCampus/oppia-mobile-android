package org.digitalcampus.mtrain.task;

import java.io.File;
import java.util.HashMap;

import org.digitalcampus.mtrain.application.DbHelper;
import org.digitalcampus.mtrain.application.MTrain;
import org.digitalcampus.mtrain.listener.InstallModuleListener;
import org.digitalcampus.mtrain.utils.FileUtils;
import org.digitalcampus.mtrain.utils.ModuleXMLReader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class InstallModules extends AsyncTask<Payload, Object, Payload>

{
	private final static String TAG = "InstallModules";
	private Context ctx;
	private InstallModuleListener mStateListener;

	public InstallModules(Context ctx) {
		this.ctx = ctx;
	}

	protected Payload doInBackground(Payload... params) {

		
		// get folder
		File dir = new File(MTrain.DOWNLOAD_PATH);

		String[] children = dir.list();
		if (children == null) {
			// Either dir does not exist or is not a directory
		} else {
			Log.d(TAG, "Installing new modules");
			for (int i = 0; i < children.length; i++) {
				// Get filename of file or directory
				Log.v(TAG, children[i]);

				// extract to temp dir and check it's a valid package file
				File tempdir = new File(MTrain.MTRAIN_ROOT + "temp/");
				tempdir.mkdirs();
				FileUtils.unzipFiles(MTrain.DOWNLOAD_PATH, children[i], tempdir.getAbsolutePath());

				String[] moddirs = tempdir.list(); // use this to get the module
													// name
				// check a module.xml file exists and is a readable XML file
				String moduleXMLPath = tempdir + "/" + moddirs[0] + "/" + MTrain.MODULE_XML;
				Log.v(TAG, moduleXMLPath);

				ModuleXMLReader mxr = new ModuleXMLReader(moduleXMLPath);
				HashMap<String, String> hm = mxr.getMeta();

				String versionid = hm.get("versionid");
				String title = hm.get("title");
				String location = MTrain.MODULES_PATH + moddirs[0];
				publishProgress("Installing:" + title);
				
				DbHelper db = new DbHelper(ctx);
				long added = db.addOrUpdateModule(versionid, title, location);

				if (added != -1) {
					File src = new File(tempdir + "/" + moddirs[0]);
					File dest = new File(MTrain.MODULES_PATH);
					mxr.setTempFilePath(tempdir + "/" + moddirs[0]);

					db.insertActivities(mxr.getActivities(added));

					// Delete old module
					File oldMod = new File(MTrain.MODULES_PATH + moddirs[0]);
					FileUtils.deleteDir(oldMod);

					// move from temp to modules dir
					boolean success = src.renameTo(new File(dest, src.getName()));

					if (success) {
						Log.v(TAG, "File was successfully moved");
						publishProgress(title + ": installed");
					} else {
						Log.v(TAG, "File was not successfully moved");
						publishProgress(title + ": failed to install");
					}
				}  else {
					publishProgress(title + ": latest version already installed");
				}
				db.close();
				// delete temp directory
				FileUtils.deleteDir(tempdir);
				Log.d(TAG, "Temp directory deleted");

				// TODO delete zip file from download dir
				File zip = new File(MTrain.DOWNLOAD_PATH + children[i]);
				zip.delete();
				Log.d(TAG, "Zip file deleted");

			}
		}
		return null;
	}

	protected void onProgressUpdate(String... obj) {
		synchronized (this) {
            if (mStateListener != null) {
                // update progress and total
                mStateListener.progressUpdate(obj[0]);
            }
        }
		// if(notify){
		// Toast.makeText(myCtx, "Finished downloading:" + strings[0],
		// Toast.LENGTH_SHORT).show();
		// }

	}

	protected void onPostExecute(Payload results) {
		synchronized (this) {
            if (mStateListener != null) {
               mStateListener.installComplete();
            }
        }
	}

	public void setInstallerListener(InstallModuleListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }
	
}
