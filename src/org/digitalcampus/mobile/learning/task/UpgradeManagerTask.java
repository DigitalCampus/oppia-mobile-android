package org.digitalcampus.mobile.learning.task;

import java.io.File;

import org.digitalcampus.mobile.learning.application.DbHelper;
import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.listener.UpgradeListener;
import org.digitalcampus.mobile.learning.model.Module;
import org.digitalcampus.mobile.learning.utils.FileUtils;
import org.digitalcampus.mobile.learning.utils.ModuleScheduleXMLReader;
import org.digitalcampus.mobile.learning.utils.ModuleTrackerXMLReader;
import org.digitalcampus.mobile.learning.utils.ModuleXMLReader;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

public class UpgradeManagerTask extends AsyncTask<Payload, String, Payload> {
	
	public static final String TAG = UpgradeManagerTask.class.getSimpleName();
	private Context ctx;
	private SharedPreferences prefs;
	private UpgradeListener mUpgradeListener;
	
	public UpgradeManagerTask(Context ctx){
		this.ctx = ctx;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}
	
	@Override
	protected Payload doInBackground(Payload... params) {
		Payload payload = params[0];
		
		payload.result = false;
		if(!prefs.getBoolean("upgradeV17",false)){
			upgradeV17();
			Editor editor = prefs.edit();
			editor.putBoolean("upgradeV17", true);
			editor.commit();
			publishProgress("Upgraded to v17");
			payload.result = true;
		}
		
		return payload;
	}
	
	/* rescans all the installed modules and reinstalls them, to ensure that 
	 * the new titles etc are picked up
	 */
	protected void upgradeV17(){
		File dir = new File(MobileLearning.MODULES_PATH);
		String[] children = dir.list();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				Log.d(TAG,"checking "+ children[i]);
				
				String moduleXMLPath = "";
				String moduleScheduleXMLPath = "";
				String moduleTrackerXMLPath = "";
				// check that it's unzipped etc correctly
				try {
					moduleXMLPath = dir + "/" + children[i] + "/" + MobileLearning.MODULE_XML;
					moduleScheduleXMLPath = dir + "/" + children[i] + "/" + MobileLearning.MODULE_SCHEDULE_XML;
					moduleTrackerXMLPath = dir + "/" + children[i] + "/" + MobileLearning.MODULE_TRACKER_XML;
				} catch (ArrayIndexOutOfBoundsException aioobe){
					FileUtils.cleanUp(dir, MobileLearning.DOWNLOAD_PATH + children[i]);
					break;
				}
				// check a module.xml file exists and is a readable XML file
				ModuleXMLReader mxr = new ModuleXMLReader(moduleXMLPath);
				ModuleScheduleXMLReader msxr = new ModuleScheduleXMLReader(moduleScheduleXMLPath);
				ModuleTrackerXMLReader mtxr = new ModuleTrackerXMLReader(moduleTrackerXMLPath);
				
				//HashMap<String, String> hm = mxr.getMeta();
				Module m = new Module();
				m.setVersionId(mxr.getVersionId());
				m.setTitles(mxr.getTitles());
				m.setLocation(MobileLearning.MODULES_PATH + children[i]);
				m.setShortname(children[i]);
				m.setImageFile(MobileLearning.MODULES_PATH + children[i] + "/" + mxr.getModuleImage());
				m.setLangs(mxr.getLangs());
				
				
				DbHelper db = new DbHelper(ctx);
				long modId = db.refreshModule(m);
				
				if (modId != -1) {
					db.insertActivities(mxr.getActivities(modId));
					db.insertTrackers(mtxr.getTrackers(),modId);
				}  
				
				// add schedule
				// put this here so even if the module content isn't updated the schedule will be
				db.insertSchedule(msxr.getSchedule());
				db.updateScheduleVersion(modId, msxr.getScheduleVersion());
				
				db.close();
			}
		}
	}
	
	
	
	@Override
	protected void onProgressUpdate(String... obj) {
		synchronized (this) {
            if (mUpgradeListener != null) {
                // update progress and total
            	mUpgradeListener.upgradeProgressUpdate(obj[0]);
            }
        }
	}

	@Override
	protected void onPostExecute(Payload p) {
		synchronized (this) {
            if (mUpgradeListener != null) {
            	mUpgradeListener.upgradeComplete(p);
            }
        }
	}

	public void setUpgradeListener(UpgradeListener srl) {
        synchronized (this) {
        	mUpgradeListener = srl;
        }
    }

}
