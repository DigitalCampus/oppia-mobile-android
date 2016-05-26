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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.ScheduleReminders;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.fragments.PasswordDialogFragment;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CourseMetaPage;

public class AppActivity extends AppCompatActivity {
	
	public static final String TAG = AppActivity.class.getSimpleName();

    /**
	 * @param activities: list of activities to show on the ScheduleReminders section
	 */
	public void drawReminders(ArrayList<org.digitalcampus.oppia.model.Activity> activities){
        ScheduleReminders reminders = (ScheduleReminders) findViewById(R.id.schedule_reminders);
        if (reminders != null){
            reminders.initSheduleReminders(activities);
        }
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case android.R.id.home:
				this.finish();
				return true;
		}
		return true;
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            //actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);

            //If we are in a course-related activity, we show its title
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            Bundle bundle = this.getIntent().getExtras();
            if (bundle != null) {
                Course course = (Course) bundle.getSerializable(Course.TAG);
                if (course == null ) return;
                String title = course.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));
                setTitle(title);
                actionBar.setTitle(title);
            }
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        //We check if the user session time has expired to log him out
        if (MobileLearning.SESSION_EXPIRATION_ENABLED){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            long now = System.currentTimeMillis()/1000;
            long lastTimeActive = prefs.getLong(PrefsActivity.LAST_ACTIVE_TIME, now);
            long timePassed = now - lastTimeActive;

            prefs.edit().putLong(PrefsActivity.LAST_ACTIVE_TIME, now).apply();
            if (timePassed > MobileLearning.SESSION_EXPIRATION_TIMEOUT){
                Log.d(TAG, "Session timeout (passed " + timePassed + " seconds), logging out");
                logoutAndRestartApp();
            }
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if (MobileLearning.SESSION_EXPIRATION_ENABLED){
            long now = System.currentTimeMillis()/1000;
            PreferenceManager
                .getDefaultSharedPreferences(this).edit()
                .putLong(PrefsActivity.LAST_ACTIVE_TIME, now).apply();
        }
    }

    public void logoutAndRestartApp(){

        SessionManager.logoutCurrentUser(this);

        Intent restartIntent = new Intent(this, StartUpActivity.class);
        restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(restartIntent);
        this.finish();
    }

    public void checkAdminPermission(int actionId, AdminSecurityManager.AuthListener authListener){

        boolean adminPasswordRequired = AdminSecurityManager.isActionProtected(this, actionId);
        if (adminPasswordRequired) {
            PasswordDialogFragment passDialog = new PasswordDialogFragment();
            passDialog.setListener(authListener);
            passDialog.show(this.getFragmentManager(), TAG);
        }
        else{
            //If the admin password is not needed, we simply call the listener method
            authListener.onPermissionGranted();
        }
    }

}
