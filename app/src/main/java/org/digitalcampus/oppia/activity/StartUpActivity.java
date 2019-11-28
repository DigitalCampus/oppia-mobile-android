/* 
 * This file is part of OppiaMobile - https://digital-campus.org/
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

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.PermissionsManager;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.listener.PostInstallListener;
import org.digitalcampus.oppia.listener.PreloadAccountsListener;
import org.digitalcampus.oppia.listener.StorageAccessListener;
import org.digitalcampus.oppia.listener.UpgradeListener;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.task.ImportLeaderboardsTask;
import org.digitalcampus.oppia.task.InstallDownloadedCoursesTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.PostInstallTask;
import org.digitalcampus.oppia.task.UpgradeManagerTask;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;
import java.util.ArrayList;

public class StartUpActivity extends Activity implements UpgradeListener, PostInstallListener, InstallCourseListener, PreloadAccountsListener, ImportLeaderboardsTask.ImportLeaderboardListener {

	public static final String TAG = StartUpActivity.class.getSimpleName();
	private TextView tvProgress;
	private SharedPreferences prefs;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mint.disableNetworkMonitoring();
        Mint.initAndStartSession(this, MobileLearning.MINT_API_KEY);
        setContentView(R.layout.start_up);

        tvProgress = this.findViewById(R.id.start_up_progress);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = SessionManager.getUsername(this);
        Mint.setUserIdentifier( username.equals("") ? "anon" : username);
	}

    @Override
    public void onResume(){
        super.onResume();

        boolean shouldContinue = PermissionsManager.CheckPermissionsAndInform(this);
        if (!shouldContinue) return;

        UpgradeManagerTask umt = new UpgradeManagerTask(this);
        umt.setUpgradeListener(this);
        ArrayList<Object> data = new ArrayList<>();
        Payload p = new Payload(data);
        umt.execute(p);
 		
	}

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionsManager.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
	
	
    private void updateProgress(String text){
    	if(tvProgress != null){
    		tvProgress.setText(text);
    	}
    }
	
	private void endStartUpScreen() {
        // launch new activity and close splash screen

        startActivity(new Intent(StartUpActivity.this,
                SessionManager.isLoggedIn(StartUpActivity.this)
                        ? MainActivity.class
                        : WelcomeActivity.class));

        finish();
    }

	private void installCourses(){
		File dir = new File(Storage.getDownloadPath(this));
		String[] children = dir.list();
		if (children != null) {
			ArrayList<Object> data = new ArrayList<>();
     		Payload payload = new Payload(data);
			InstallDownloadedCoursesTask imTask = new InstallDownloadedCoursesTask(this);
			imTask.setInstallerListener(this);
			imTask.execute(payload);
		} else {
            preloadAccounts();

		}
	}

    private void preloadAccounts(){
        SessionManager.preloadUserAccounts(this, this);
    }
	
	public void upgradeComplete(final Payload p) {

        if (Storage.getStorageStrategy().needsUserPermissions(this)){
            Log.d(TAG, "Asking user for storage permissions");
            Storage.getStorageStrategy().askUserPermissions(this, new StorageAccessListener() {
                @Override
                public void onAccessGranted(boolean isGranted) {
                    Log.d(TAG, "Access granted for storage: " + isGranted);
                    if (!isGranted) {
                        Toast.makeText(StartUpActivity.this, getString(R.string.storageAccessNotGranted), Toast.LENGTH_LONG).show();
                    }
                    afterUpgrade(p);
                }
            });
        }
        else{
            afterUpgrade(p);
        }
	}

    private void afterUpgrade(Payload p){
        // set up local dirs
        if(!Storage.createFolderStructure(this)){
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Oppia_AlertDialogStyle);
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

	public void upgradeProgressUpdate(String s) { this.updateProgress(s); }
	public void postInstallComplete(Payload response) {
		this.installCourses();
	}

	public void downloadComplete(Payload p) {
        // no need to show download complete in this activity
    }

	public void downloadProgressUpdate(DownloadProgress dp) {
        // no need to show download progress in this activity
    }

	public void installComplete(Payload p) {
		if(!p.getResponseData().isEmpty()){
            Media.resetMediaScan(prefs);
		}
		preloadAccounts();
	}

	public void installProgressUpdate(DownloadProgress dp) {
		this.updateProgress(dp.getMessage());
	}

    @Override
    public void onPreloadAccountsComplete(Payload payload) {
        if ((payload!=null) && payload.isResult()){
            Toast.makeText(this, payload.getResultResponse(), Toast.LENGTH_LONG).show();
        }
        ImportLeaderboardsTask imTask = new ImportLeaderboardsTask(this);
        imTask.setListener(this);
        imTask.execute(payload);

    }

    @Override
    public void onLeaderboardImportProgress(String message) {

    }

    @Override
    public void onLeaderboardImportComplete(Boolean success, String message) {
        endStartUpScreen();
    }
}
