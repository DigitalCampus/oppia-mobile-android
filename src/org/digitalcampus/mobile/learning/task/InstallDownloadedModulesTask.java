package org.digitalcampus.mobile.learning.task;

import java.io.File;
import java.util.HashMap;

import org.digitalcampus.mobile.learning.application.DbHelper;
import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.listener.InstallModuleListener;
import org.digitalcampus.mobile.learning.utils.FileUtils;
import org.digitalcampus.mobile.learning.utils.ModuleXMLReader;
import org.digitalcampus.mobile.learning.R;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class InstallDownloadedModulesTask extends AsyncTask<Payload, String, Payload>

{
	private final static String TAG = InstallDownloadedModulesTask.class.getSimpleName();
	private Context ctx;
	private InstallModuleListener mStateListener;

	public InstallDownloadedModulesTask(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	protected Payload doInBackground(Payload... params) {

		// get folder
		File dir = new File(MobileLearning.DOWNLOAD_PATH);

		String[] children = dir.list();
		if (children != null) {
			Log.d(TAG, "Installing new modules");
			publishProgress(ctx.getString(R.string.installing));
			for (int i = 0; i < children.length; i++) {

				// extract to temp dir and check it's a valid package file
				File tempdir = new File(MobileLearning.MLEARN_ROOT + "temp/");
				tempdir.mkdirs();
				boolean unzipResult = FileUtils.unzipFiles(MobileLearning.DOWNLOAD_PATH, children[i], tempdir.getAbsolutePath());
				
				if (!unzipResult){
					//then was invalid zip file and should be removed
					FileUtils.cleanUp(tempdir, MobileLearning.DOWNLOAD_PATH + children[i]);
					break;
				}
				String[] moddirs = tempdir.list(); // use this to get the module
													// name
				
				String moduleXMLPath = "";
				// check that it's unzipped etc correctly
				try {
					moduleXMLPath = tempdir + "/" + moddirs[0] + "/" + MobileLearning.MODULE_XML;
				} catch (ArrayIndexOutOfBoundsException aioobe){
					FileUtils.cleanUp(tempdir, MobileLearning.DOWNLOAD_PATH + children[i]);
					break;
				}
				
				// check a module.xml file exists and is a readable XML file
				ModuleXMLReader mxr = new ModuleXMLReader(moduleXMLPath);
				
				HashMap<String, String> hm = mxr.getMeta();

				String versionid = hm.get("versionid");
				String title = hm.get("title");
				String location = MobileLearning.MODULES_PATH + moddirs[0];
				publishProgress(ctx.getString(R.string.installing_module, title));
				
				DbHelper db = new DbHelper(ctx);
				long added = db.addOrUpdateModule(versionid, title, location, moddirs[0]);

				if (added != -1) {
					File src = new File(tempdir + "/" + moddirs[0]);
					File dest = new File(MobileLearning.MODULES_PATH);
					mxr.setTempFilePath(tempdir + "/" + moddirs[0]);

					db.insertActivities(mxr.getActivities(added));

					// Delete old module
					File oldMod = new File(MobileLearning.MODULES_PATH + moddirs[0]);
					FileUtils.deleteDir(oldMod);

					// move from temp to modules dir
					boolean success = src.renameTo(new File(dest, src.getName()));

					if (success) {
						Log.v(TAG, "File was successfully moved");
						publishProgress(ctx.getString(R.string.install_module_complete, title));
					} else {
						Log.v(TAG, "File was not successfully moved");
						publishProgress(ctx.getString(R.string.error_installing_module, title));
					}
				}  else {
					publishProgress(ctx.getString(R.string.error_latest_already_installed, title));
				}
				db.close();
				// delete temp directory
				FileUtils.deleteDir(tempdir);
				Log.d(TAG, "Temp directory deleted");

				// delete zip file from download dir
				File zip = new File(MobileLearning.DOWNLOAD_PATH + children[i]);
				zip.delete();
				Log.d(TAG, "Zip file deleted");

			}
		}
		return null;
	}

	@Override
	protected void onProgressUpdate(String... obj) {
		synchronized (this) {
            if (mStateListener != null) {
                // update progress and total
                mStateListener.installProgressUpdate(obj[0]);
            }
        }
	}

	@Override
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
