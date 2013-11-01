package org.digitalcampus.oppia.task;

import java.io.File;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.listener.UpgradeListener;
import org.digitalcampus.oppia.model.Module;
import org.digitalcampus.oppia.utils.FileUtils;
import org.digitalcampus.oppia.utils.ModuleScheduleXMLReader;
import org.digitalcampus.oppia.utils.ModuleTrackerXMLReader;
import org.digitalcampus.oppia.utils.ModuleXMLReader;

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
		
		payload.setResult(false);
		if(!prefs.getBoolean("upgradeV17",false)){
			upgradeV17();
			Editor editor = prefs.edit();
			editor.putBoolean("upgradeV17", true);
			editor.commit();
			publishProgress("Upgraded to v17");
			payload.setResult(true);
		}
		
		if(!prefs.getBoolean("upgradeV20",false)){
			upgradeV20();
			Editor editor = prefs.edit();
			editor.putBoolean("upgradeV20", true);
			editor.commit();
			publishProgress("Upgraded to v20");
			payload.setResult(true);
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
				ModuleXMLReader mxr;
				ModuleScheduleXMLReader msxr;
				ModuleTrackerXMLReader mtxr;
				try {
					mxr = new ModuleXMLReader(moduleXMLPath);
					msxr = new ModuleScheduleXMLReader(moduleScheduleXMLPath);
					mtxr = new ModuleTrackerXMLReader(moduleTrackerXMLPath);
				} catch (InvalidXMLException e) {
					e.printStackTrace();
					break;
				}
				
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
	
	/* switch to using demo.oppia-mobile.org
	 */
	protected void upgradeV20(){
		Editor editor = prefs.edit();
		editor.putString(ctx.getString(R.string.prefs_server), ctx.getString(R.string.prefServerDefault));
		editor.commit();
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
