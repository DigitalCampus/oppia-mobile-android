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

package org.digitalcampus.mobile.learning.task;

import java.io.File;
import java.util.HashMap;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.application.DbHelper;
import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.listener.InstallModuleListener;
import org.digitalcampus.mobile.learning.model.DownloadProgress;
import org.digitalcampus.mobile.learning.utils.FileUtils;
import org.digitalcampus.mobile.learning.utils.ModuleScheduleXMLReader;
import org.digitalcampus.mobile.learning.utils.ModuleXMLReader;

import android.content.Context;
import android.os.AsyncTask;

public class InstallDownloadedModulesTask extends AsyncTask<Payload, DownloadProgress, Payload>

{
	public final static String TAG = InstallDownloadedModulesTask.class.getSimpleName();
	private Context ctx;
	private InstallModuleListener mStateListener;

	public InstallDownloadedModulesTask(Context ctx) {
		this.ctx = ctx;
	}

	@Override
	protected Payload doInBackground(Payload... params) {

		Payload payload = params[0];
		
		// get folder
		File dir = new File(MobileLearning.DOWNLOAD_PATH);
		DownloadProgress dp = new DownloadProgress();
		String[] children = dir.list();
		if (children != null) {

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
				String moduleScheduleXMLPath = "";
				// check that it's unzipped etc correctly
				try {
					moduleXMLPath = tempdir + "/" + moddirs[0] + "/" + MobileLearning.MODULE_XML;
					moduleScheduleXMLPath = tempdir + "/" + moddirs[0] + "/" + MobileLearning.MODULE_SCHEDULE_XML;
				} catch (ArrayIndexOutOfBoundsException aioobe){
					FileUtils.cleanUp(tempdir, MobileLearning.DOWNLOAD_PATH + children[i]);
					break;
				}
				
				// check a module.xml file exists and is a readable XML file
				ModuleXMLReader mxr = new ModuleXMLReader(moduleXMLPath);
				ModuleScheduleXMLReader msxr = new ModuleScheduleXMLReader(moduleScheduleXMLPath);
				
				HashMap<String, String> hm = mxr.getMeta();

				String versionid = hm.get("versionid");
				String title = hm.get("title");
				String location = MobileLearning.MODULES_PATH + moddirs[0];
				dp.setProgress(ctx.getString(R.string.installing_module, title));
				publishProgress(dp);
				
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
						payload.result = true;
						payload.resultResponse = ctx.getString(R.string.install_module_complete, title);
					} else {
						payload.result = false;
						payload.resultResponse = ctx.getString(R.string.error_installing_module, title);
					}
				}  else {
					payload.result = false;
					payload.resultResponse = ctx.getString(R.string.error_latest_already_installed, title);
				}
				
				// add schedule
				// put this here so even if the module content isn;t updated the schedule will be
				db.insertSchedule(msxr.getSchedule());
				
				db.close();
				// delete temp directory
				FileUtils.deleteDir(tempdir);

				// delete zip file from download dir
				File zip = new File(MobileLearning.DOWNLOAD_PATH + children[i]);
				zip.delete();
			}
		}
		return payload;
	}

	@Override
	protected void onProgressUpdate(DownloadProgress... obj) {
		synchronized (this) {
            if (mStateListener != null) {
                // update progress and total
                mStateListener.installProgressUpdate(obj[0]);
            }
        }
	}

	@Override
	protected void onPostExecute(Payload p) {
		synchronized (this) {
            if (mStateListener != null) {
               mStateListener.installComplete(p);
            }
        }
	}

	public void setInstallerListener(InstallModuleListener srl) {
        synchronized (this) {
            mStateListener = srl;
        }
    }
	
}
