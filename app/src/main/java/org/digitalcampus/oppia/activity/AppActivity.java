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

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.ScheduleReminders;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.gamification.GamificationBroadcastReceiver;
import org.digitalcampus.oppia.gamification.GamificationService;
import org.digitalcampus.oppia.listener.APIKeyRequestListener;
import org.digitalcampus.oppia.listener.GamificationEventListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.UIUtils;

import java.util.List;
import java.util.concurrent.Callable;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;


public class AppActivity extends AppCompatActivity implements APIKeyRequestListener, GamificationEventListener {
	
	public static final String TAG = AppActivity.class.getSimpleName();

	GamificationBroadcastReceiver gamificationReceiver;


    /**
	 * @param activities: list of activities to show on the ScheduleReminders section
	 */
	public void drawReminders(List<org.digitalcampus.oppia.model.Activity> activities){
        ScheduleReminders reminders = findViewById(R.id.schedule_reminders);
        if (reminders != null){
            reminders.initSheduleReminders(activities);
        }
	}

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case android.R.id.home:
				this.finish();
				return true;
            default:
			    return false;
		}
	}

    protected void onStart(boolean overrideTitle, boolean configureActionBar){
        super.onStart();

        if (!configureActionBar)
            return;

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null){
            setSupportActionBar( toolbar );
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);

            //If we are in a course-related activity, we show its title
            if (overrideTitle){
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                Bundle bundle = this.getIntent().getExtras();
                if (bundle != null) {
                    Course course = (Course) bundle.getSerializable(Course.TAG);
                    if (course == null ) return;
                    String title = course.getTitle(prefs);
                    setTitle(title);
                    actionBar.setTitle(title);
                }
            }

        }
    }

    protected void onStart(boolean overrideTitle) {
        onStart(overrideTitle, true);
    }

    @Override
    protected void onStart() {
        onStart(true, true);
    }

    @Override
    public void onResume(){
        super.onResume();
        //Check if the apiKey of the current user is valid
        boolean apiKeyValid = SessionManager.isUserApiKeyValid(this);
        if (!apiKeyValid){
            apiKeyInvalidated();
        }

        // Register the receiver for gamification events
        gamificationReceiver = new GamificationBroadcastReceiver();
        gamificationReceiver.setGamificationEventListener(this);
        IntentFilter broadcastFilter = new IntentFilter(GamificationService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(gamificationReceiver, broadcastFilter);

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

        unregisterReceiver(gamificationReceiver);
    }

    public void logoutAndRestartApp(){
        SessionManager.logoutCurrentUser(this);

        Intent restartIntent = new Intent(this, StartUpActivity.class);
        restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(restartIntent);
        this.finish();
    }

    @Override
    public void apiKeyInvalidated() {
        UIUtils.showAlert(this, R.string.error, R.string.error_apikey_expired, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                logoutAndRestartApp();
                return true;
            }
        });
    }

    @Override
    public void onGamificationEvent(String message, int points) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notifEnabled = prefs.getBoolean(PrefsActivity.PREF_SHOW_GAMIFICATION_EVENTS, true);
        if(notifEnabled) {
            final View rootView =  ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
            Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
        }
    }
}
