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

package org.digitalcampus.oppia.activity;


import java.io.File;
import java.util.ArrayList;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.listener.PostInstallListener;
import org.digitalcampus.oppia.listener.UpgradeListener;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.task.InstallDownloadedCoursesTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.PostInstallTask;
import org.digitalcampus.oppia.task.UpgradeManagerTask;

import com.bugsense.trace.BugSenseHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.TextView;

public class StartUpActivity extends Activity implements UpgradeListener, PostInstallListener, InstallCourseListener{

	public final static String TAG = StartUpActivity.class.getSimpleName();
	private TextView tvProgress;
	private SharedPreferences prefs;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BugSenseHandler.initAndStartSession(this, MobileLearning.BUGSENSE_API_KEY);
        setContentView(R.layout.start_up);
        tvProgress = (TextView) this.findViewById(R.id.start_up_progress);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        // set up local dirs
 		if(!MobileLearning.createDirs()){
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setCancelable(false);
 			builder.setTitle(R.string.error);
 			builder.setMessage(R.string.error_sdcard);
 			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					StartUpActivity.this.finish();
 				}
 			});
 			builder.show();
 			return;
 		}
 		
 		UpgradeManagerTask umt = new UpgradeManagerTask(this);
		umt.setUpgradeListener(this);
		ArrayList<Object> data = new ArrayList<Object>();
 		Payload p = new Payload(data);
		umt.execute(p);
	}
	
	
    private void updateProgress(String text){
    	if(tvProgress != null){
    		tvProgress.setText(text);
    	}
    }
	
	private void endStartUpScreen() {
        // launch new activity and close splash screen
		if (!MobileLearning.isLoggedIn(this)) {
			startActivity(new Intent(StartUpActivity.this, LoginActivity.class));
			finish();
		} else {
			startActivity(new Intent(StartUpActivity.this, OppiaMobileActivity.class));
			finish();
		}
    }

	private void installCourses(){
		File dir = new File(MobileLearning.DOWNLOAD_PATH);
		String[] children = dir.list();
		if (children != null) {
			ArrayList<Object> data = new ArrayList<Object>();
     		Payload payload = new Payload(data);
			InstallDownloadedCoursesTask imTask = new InstallDownloadedCoursesTask(this);
			imTask.setInstallerListener(this);
			imTask.execute(payload);
		} else {
			endStartUpScreen();
		}
	}
	
	public void upgradeComplete(Payload p) {
		if(p.isResult()){
			Payload payload = new Payload();
			PostInstallTask piTask = new PostInstallTask(this);
			piTask.setPostInstallListener(this);
			piTask.execute(payload);
		} else {
			// now install any new courses
			this.installCourses();
		}
		
	}

	public void upgradeProgressUpdate(String s) {
		this.updateProgress(s);
	}

	public void postInstallComplete(Payload response) {
		this.installCourses();
	}

	public void downloadComplete(Payload p) {
		// do nothing
		
	}

	public void downloadProgressUpdate(DownloadProgress dp) {
		// do nothing
		
	}

	public void installComplete(Payload p) {
		if(p.getResponseData().size()>0){
			Editor e = prefs.edit();
			e.putLong(getString(R.string.prefs_last_media_scan), 0);
			e.commit();
		}
		endStartUpScreen();	
	}

	public void installProgressUpdate(DownloadProgress dp) {
		this.updateProgress(dp.getMessage());
	}
}
