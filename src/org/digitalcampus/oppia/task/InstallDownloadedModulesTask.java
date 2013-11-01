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

package org.digitalcampus.oppia.task;

import java.io.File;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.listener.InstallModuleListener;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.Module;
import org.digitalcampus.oppia.utils.FileUtils;
import org.digitalcampus.oppia.utils.ModuleScheduleXMLReader;
import org.digitalcampus.oppia.utils.ModuleTrackerXMLReader;
import org.digitalcampus.oppia.utils.ModuleXMLReader;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class InstallDownloadedModulesTask extends AsyncTask<Payload, DownloadProgress, Payload>{
	
	public final static String TAG = InstallDownloadedModulesTask.class.getSimpleName();
	private Context ctx;
	private InstallModuleListener mStateListener;
	private SharedPreferences prefs;
	
	public InstallDownloadedModulesTask(Context ctx) {
		this.ctx = ctx;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
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
				File tempdir = new File(MobileLearning.OPPIAMOBILE_ROOT + "temp/");
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
				String moduleTrackerXMLPath = "";
				// check that it's unzipped etc correctly
				try {
					moduleXMLPath = tempdir + "/" + moddirs[0] + "/" + MobileLearning.MODULE_XML;
					moduleScheduleXMLPath = tempdir + "/" + moddirs[0] + "/" + MobileLearning.MODULE_SCHEDULE_XML;
					moduleTrackerXMLPath = tempdir + "/" + moddirs[0] + "/" + MobileLearning.MODULE_TRACKER_XML;
				} catch (ArrayIndexOutOfBoundsException aioobe){
					FileUtils.cleanUp(tempdir, MobileLearning.DOWNLOAD_PATH + children[i]);
					break;
				}
				
				// check a module.xml file exists and is a readable XML file
				ModuleXMLReader mxr;
				ModuleScheduleXMLReader msxr;
				ModuleTrackerXMLReader mtxr;
				try {
					mxr = new ModuleXMLReader(moduleXMLPath);
					msxr = new ModuleScheduleXMLReader(moduleScheduleXMLPath);
					mtxr = new ModuleTrackerXMLReader(moduleTrackerXMLPath);
				} catch (InvalidXMLException e) {
					payload.setResult(false);
					return payload;
				}
				
				
				//HashMap<String, String> hm = mxr.getMeta();
				Module m = new Module();
				m.setVersionId(mxr.getVersionId());
				m.setTitles(mxr.getTitles());
				m.setLocation(MobileLearning.MODULES_PATH + moddirs[0]);
				m.setShortname(moddirs[0]);
				m.setImageFile(MobileLearning.MODULES_PATH + moddirs[0] + "/" + mxr.getModuleImage());
				m.setLangs(mxr.getLangs());
				String title = m.getTitle(prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage()));
				
				dp.setProgress(ctx.getString(R.string.installing_module, title));
				publishProgress(dp);
				
				DbHelper db = new DbHelper(ctx);
				long added = db.addOrUpdateModule(m);
				
				if (added != -1) {
					payload.addResponseData(m);
					File src = new File(tempdir + "/" + moddirs[0]);
					File dest = new File(MobileLearning.MODULES_PATH);

					db.insertActivities(mxr.getActivities(added));
					db.insertTrackers(mtxr.getTrackers(),added);
					// Delete old module
					File oldMod = new File(MobileLearning.MODULES_PATH + moddirs[0]);
					FileUtils.deleteDir(oldMod);

					// move from temp to modules dir
					boolean success = src.renameTo(new File(dest, src.getName()));

					if (success) {
						payload.setResult(true);
						payload.setResultResponse(ctx.getString(R.string.install_module_complete, title));
					} else {
						payload.setResult(false);
						payload.setResultResponse(ctx.getString(R.string.error_installing_module, title));
					}
				}  else {
					payload.setResult(false);
					payload.setResultResponse(ctx.getString(R.string.error_latest_already_installed, title));
				}
				
				// add schedule
				// put this here so even if the module content isn't updated the schedule will be
				db.insertSchedule(msxr.getSchedule());
				db.updateScheduleVersion(added, msxr.getScheduleVersion());
				
				
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
