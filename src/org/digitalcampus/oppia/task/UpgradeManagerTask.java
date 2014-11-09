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

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.listener.MoveStorageListener;
import org.digitalcampus.oppia.listener.UpgradeListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.CourseScheduleXMLReader;
import org.digitalcampus.oppia.utils.CourseTrackerXMLReader;
import org.digitalcampus.oppia.utils.CourseXMLReader;
import org.digitalcampus.oppia.utils.FileUtils;
import org.digitalcampus.oppia.utils.SearchUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
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
			publishProgress(this.ctx.getString(R.string.info_upgrading,"v17"));
			payload.setResult(true);
		}
		
		if(!prefs.getBoolean("upgradeV20",false)){
			upgradeV20();
			Editor editor = prefs.edit();
			editor.putBoolean("upgradeV20", true);
			editor.commit();
			publishProgress(this.ctx.getString(R.string.info_upgrading,"v20"));
			payload.setResult(true);
		}
		
		if(!prefs.getBoolean("upgradeV29",false)){
			Editor editor = prefs.edit();
			editor.putBoolean("upgradeV29", true);
			editor.commit();
			publishProgress(this.ctx.getString(R.string.info_upgrading,"v29"));
			payload.setResult(true);
		}
		
		if(!prefs.getBoolean("upgradeV43",false)){
			upgradeV43();
			Editor editor = prefs.edit();
			editor.putBoolean("upgradeV43", true);
			editor.commit();
			publishProgress(this.ctx.getString(R.string.info_upgrading,"v43"));
			payload.setResult(true);
		}
		
		if(!prefs.getBoolean("upgradeV46",false)){
			Editor editor = prefs.edit();
			editor.putBoolean("upgradeV46", true);
			editor.putString("prefServer", ctx.getString(R.string.prefServerDefault));
			editor.commit();
			publishProgress(this.ctx.getString(R.string.info_upgrading,"v46"));
			payload.setResult(true);
		}
		
		if(!prefs.getBoolean("upgradeV49a",false)){
			upgradeV49();
			Editor editor = prefs.edit();
			editor.putBoolean("upgradeV49a", true);
			editor.commit();
			publishProgress(this.ctx.getString(R.string.info_upgrading,"v49a"));
			payload.setResult(true);
		}
		
		return payload;
	}
	
	/* rescans all the installed courses and reinstalls them, to ensure that 
	 * the new titles etc are picked up
	 */
	protected void upgradeV17(){
		File dir = new File(FileUtils.getCoursesPath(ctx));
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
					FileUtils.cleanUp(dir, FileUtils.getDownloadPath(ctx) + children[i]);
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

				Course c = new Course(prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, ""));
				c.setVersionId(cxr.getVersionId());
				c.setTitles(cxr.getTitles());
				c.setShortname(children[i]);
				c.setImageFile(children[i] + "/" + cxr.getCourseImage());
				c.setLangs(cxr.getLangs());
				c.setPriority(cxr.getPriority());
				
				DbHelper db = new DbHelper(ctx);
				long courseId = db.addOrUpdateCourse(c);
				
				if (courseId != -1) {
					db.insertActivities(cxr.getActivities(courseId));
					db.insertTrackers(ctxr.getTrackers(),courseId);
				} 
				
				// add schedule
				// put this here so even if the course content isn't updated the schedule will be
				db.insertSchedule(csxr.getSchedule());
				db.updateScheduleVersion(courseId, csxr.getScheduleVersion());
				
				DatabaseManager.getInstance().closeDatabase();
			}
		}
	}
	
	/* switch to using demo.oppia-mobile.org
	 */
	protected void upgradeV20(){
		Editor editor = prefs.edit();
		editor.putString("prefServer", ctx.getString(R.string.prefServerDefault));
		editor.commit();
	}
	
	/* go through and add html content to tables
	 */
	protected void upgradeV43(){
		SearchUtils.reindexAll(ctx);
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		User user = new User();
		user.setUsername(prefs.getString("prefUsername", ""));
		user.setApiKey(prefs.getString("prefApiKey", "") );
		DbHelper db = new DbHelper(ctx);
		long userId = db.addOrUpdateUser(user);
		db.updateV43(userId);
		DatabaseManager.getInstance().closeDatabase();
		
	}
	
	/*
	 * Move files from current location into new one
	 */
	protected void upgradeV49(){
		
		String source = Environment.getExternalStorageDirectory() + "/" + FileUtils.APP_ROOT_DIR_NAME  +"/";
    	
    	File[] dirs = ContextCompat.getExternalFilesDirs(ctx,null);
    	if(dirs.length > 0){
	    	
		
	    	String destination = dirs[0].getAbsolutePath();
	    	File downloadSource = new File(source + FileUtils.APP_DOWNLOAD_DIR_NAME);
			File mediaSource = new File(source +  FileUtils.APP_MEDIA_DIR_NAME);
			File courseSource = new File(source +  FileUtils.APP_COURSES_DIR_NAME);
			
			boolean success = true;
	    	try {
				org.apache.commons.io.FileUtils.forceDelete(new File (destination + "/" + FileUtils.APP_DOWNLOAD_DIR_NAME ));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d(TAG,"failed to delete: " + destination + "/" + FileUtils.APP_DOWNLOAD_DIR_NAME );
				e.printStackTrace();
				success = false;
			}
			
			try {
				org.apache.commons.io.FileUtils.forceDelete(new File (destination + "/" + FileUtils.APP_MEDIA_DIR_NAME ));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d(TAG,"failed to delete: " + destination + "/" + FileUtils.APP_MEDIA_DIR_NAME );
				e.printStackTrace();
				success = false;
			}
			
			try {
				org.apache.commons.io.FileUtils.forceDelete(new File (destination + "/" + FileUtils.APP_COURSES_DIR_NAME ));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d(TAG,"failed to delete: " + destination + "/" + FileUtils.APP_COURSES_DIR_NAME );
				e.printStackTrace();
				success = false;
			}
			
			
			// now copy over 
			try {
				
				org.apache.commons.io.FileUtils.moveDirectoryToDirectory(downloadSource,new File(destination),true);
				Log.d(TAG,"completed");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d(TAG,"failed");
				e.printStackTrace();
				success = false;
			}

			try {
				org.apache.commons.io.FileUtils.moveDirectoryToDirectory(mediaSource,new File(destination),true);
				Log.d(TAG,"completed");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d(TAG,"failed");
				e.printStackTrace();
				success = false;
			}
			
			try {
				org.apache.commons.io.FileUtils.moveDirectoryToDirectory(courseSource,new File(destination),true);
				Log.d(TAG,"completed");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d(TAG,"failed");
				e.printStackTrace();
				success = false;
			}
			
			Editor editor = prefs.edit();
			editor.putString(PrefsActivity.PREF_STORAGE_LOCATION, destination);
			editor.commit();
			
			// delete original dir
			try {
				org.apache.commons.io.FileUtils.forceDelete(new File(source));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d(TAG,"failed to delete original file");
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
