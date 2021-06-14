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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivityStartUpBinding;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.application.PermissionsManager;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.listener.UpgradeListener;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.task.ImportLeaderboardsTask;
import org.digitalcampus.oppia.task.InstallDownloadedCoursesTask;
import org.digitalcampus.oppia.task.PostInstallTask;
import org.digitalcampus.oppia.task.UpgradeManagerTask;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.File;

public class StartUpActivity extends Activity implements UpgradeListener, InstallCourseListener {

    public static final String TAG = StartUpActivity.class.getSimpleName();
    private SharedPreferences prefs;
    private ActivityStartUpBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStartUpBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Analytics.startTrackingIfEnabled(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean shouldContinue = PermissionsManager.checkPermissionsAndInform(this,
                PermissionsManager.STARTUP_PERMISSIONS);
        if (!shouldContinue) return;

        UpgradeManagerTask umt = new UpgradeManagerTask(this);
        umt.setUpgradeListener(this);
        umt.execute();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsManager.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void updateProgress(String text) {
        if (binding.startUpProgress != null) {
            binding.startUpProgress.setText(text);
        }
    }

    private void endStartUpScreen() {
        // launch new activity and close splash screen

        if (SessionManager.isLoggedIn(this)) {

            startActivity(new Intent(this, MainActivity.class));

            if (Analytics.shouldShowOptOutRationale(this)) {
                overridePendingTransition(0, 0);
                startActivity(new Intent(this, AnalyticsOptinActivity.class));
            }

        } else {
            startActivity(new Intent(this, WelcomeActivity.class));
        }

        finish();
    }

    private void installCourses() {
        File dir = new File(Storage.getDownloadPath(this));
        String[] children = dir.list();
        if (children != null) {
            InstallDownloadedCoursesTask imTask = new InstallDownloadedCoursesTask(this);
            imTask.setInstallerListener(this);
            imTask.execute();
        } else {
            preloadAccounts();
        }
    }

    private void preloadAccounts() {
        SessionManager.preloadUserAccounts(this, result -> {
            if (result != null && result.isSuccess()) {
                Toast.makeText(StartUpActivity.this, result.getResultMessage(), Toast.LENGTH_LONG).show();
            }
            importLeaderboard();
        });
    }

    public void upgradeComplete(BasicResult result) {

        afterUpgrade(result);

    }

    private void afterUpgrade(BasicResult result) {
        // set up local dirs
        if (!Storage.createFolderStructure(this)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Oppia_AlertDialogStyle);
            builder.setCancelable(false);
            builder.setTitle(R.string.error);
            builder.setMessage(R.string.error_sdcard);
            builder.setPositiveButton(R.string.ok, (dialog, which) -> StartUpActivity.this.finish());
            builder.show();
            return;
        }

        if (result.isSuccess()) {
            PostInstallTask piTask = new PostInstallTask(this);
            piTask.setPostInstallListener(this::installCourses);
            piTask.execute();
        } else {
            // now install any new courses
            this.installCourses();
        }
    }

    @Override
    public void upgradeProgressUpdate(String s) {
        this.updateProgress(s);
    }

    public void installComplete(BasicResult result) {
        preloadAccounts();
    }

    public void installProgressUpdate(DownloadProgress dp) {
        this.updateProgress(dp.getMessage());
    }

    private void importLeaderboard() {
        ImportLeaderboardsTask imTask = new ImportLeaderboardsTask(StartUpActivity.this);
        imTask.setListener((success, message) -> endStartUpScreen());
        imTask.execute();
    }
}
