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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.listener.UpgradeListener;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.CourseScheduleXMLReader;
import org.digitalcampus.oppia.utils.CourseTrackerXMLReader;
import org.digitalcampus.oppia.utils.CourseXMLReader;
import org.digitalcampus.oppia.utils.FileUtils;

import android.content.ContentValues;
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
		
		if(!prefs.getBoolean("upgradeV29",false)){
			Editor editor = prefs.edit();
			editor.putBoolean("upgradeV29", true);
			editor.commit();
			publishProgress("Upgraded to v29");
			payload.setResult(true);
		}
		
		if(!prefs.getBoolean("upgradeV43",false)){
			upgradeV43();
			Editor editor = prefs.edit();
			editor.putBoolean("upgradeV43", true);
			editor.commit();
			publishProgress("Upgraded to v43");
			payload.setResult(true);
		}
		
		if(!prefs.getBoolean("upgradeV43a",false)){
			upgradeV43a();
			Editor editor = prefs.edit();
			editor.putBoolean("upgradeV43a", true);
			editor.commit();
			publishProgress("Upgraded to v43a");
			payload.setResult(true);
		}
		
		return payload;
	}
	
	/* rescans all the installed courses and reinstalls them, to ensure that 
	 * the new titles etc are picked up
	 */
	protected void upgradeV17(){
		File dir = new File(MobileLearning.COURSES_PATH);
		String[] children = dir.list();
		if (children != null) {
			for (int i = 0; i < children.length; i++) {
				publishProgress("checking: " + children[i]);
				String courseXMLPath = "";
				String courseScheduleXMLPath = "";
				String courseTrackerXMLPath = "";
				// check that it's unzipped etc correctly
				try {
					courseXMLPath = dir + "/" + children[i] + "/" + MobileLearning.COURSE_XML;
					courseScheduleXMLPath = dir + "/" + children[i] + "/" + MobileLearning.COURSE_SCHEDULE_XML;
					courseTrackerXMLPath = dir + "/" + children[i] + "/" + MobileLearning.COURSE_TRACKER_XML;
				} catch (ArrayIndexOutOfBoundsException aioobe){
					FileUtils.cleanUp(dir, MobileLearning.DOWNLOAD_PATH + children[i]);
					break;
				}
				
				// check a module.xml file exists and is a readable XML file
				CourseXMLReader cxr;
				CourseScheduleXMLReader csxr;
				CourseTrackerXMLReader ctxr;
				try {
					cxr = new CourseXMLReader(courseXMLPath,ctx);
					csxr = new CourseScheduleXMLReader(courseScheduleXMLPath);
					ctxr = new CourseTrackerXMLReader(courseTrackerXMLPath);
				} catch (InvalidXMLException e) {
					e.printStackTrace();
					break;
				}
				
				//HashMap<String, String> hm = mxr.getMeta();
				Course c = new Course();
				c.setVersionId(cxr.getVersionId());
				c.setTitles(cxr.getTitles());
				c.setLocation(MobileLearning.COURSES_PATH + children[i]);
				c.setShortname(children[i]);
				c.setImageFile(MobileLearning.COURSES_PATH + children[i] + "/" + cxr.getCourseImage());
				c.setLangs(cxr.getLangs());
				
				
				DbHelper db = new DbHelper(ctx);
				long modId = db.refreshCourse(c);
				
				if (modId != -1) {
					db.insertActivities(cxr.getActivities(modId));
					db.insertTrackers(ctxr.getTrackers(),modId);
				} 
				
				// add schedule
				// put this here so even if the course content isn't updated the schedule will be
				db.insertSchedule(csxr.getSchedule());
				db.updateScheduleVersion(modId, csxr.getScheduleVersion());
				
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
	
	/* go through and add html content to tables
	 */
	protected void upgradeV43(){
		
		SearchReIndex task = new SearchReIndex(ctx);
		Payload p = new Payload();
		task.execute(p);
	}
	
	/* add current user to user table and update all tracklogs to reflect this
	 */
	protected void upgradeV43a(){
		//add user
		if(MobileLearning.isLoggedIn(ctx)){
			User user = new User();
			user.setUsername(prefs.getString(ctx.getString(R.string.prefs_username), ""));
			user.setApiKey(prefs.getString(ctx.getString(R.string.prefs_api_key), "") );
			DbHelper db = new DbHelper(ctx);
			long userId = db.addOrUpdateUser(user);
			
			// update existing trackers
			ContentValues values = new ContentValues();
			values.put(DbHelper.TRACKER_LOG_C_USERID, userId);
			
			DbHelper.db.update(DbHelper.TRACKER_LOG_TABLE, values, "1=1", null);
			
			db.close();
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
