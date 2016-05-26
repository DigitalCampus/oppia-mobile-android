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

package org.digitalcampus.oppia.application;


import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.task.SubmitQuizAttemptsTask;
import org.digitalcampus.oppia.task.SubmitTrackerMultipleTask;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategy;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategyFactory;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class MobileLearning extends Application {

	public static final String TAG = MobileLearning.class.getSimpleName();
	
	public static final int APP_LOGO = R.drawable.dc_logo;
	
	public static final String COURSE_XML = "module.xml";
	public static final String COURSE_SCHEDULE_XML = "schedule.xml";
	public static final String COURSE_TRACKER_XML = "tracker.xml";
	public static final String PRE_INSTALL_COURSES_DIR = "www/preload/courses"; // don't include leading or trailing slash
	public static final String PRE_INSTALL_MEDIA_DIR = "www/preload/media"; // don't include leading or trailing slash
	
	// server path vars - new version
	public static final String OPPIAMOBILE_API = "api/v1/";
	public static final String LOGIN_PATH = OPPIAMOBILE_API + "user/";
	public static final String REGISTER_PATH = OPPIAMOBILE_API + "register/";
	public static final String RESET_PATH = OPPIAMOBILE_API + "reset/";
	public static final String QUIZ_SUBMIT_PATH = OPPIAMOBILE_API + "quizattempt/";
	public static final String SERVER_COURSES_PATH = OPPIAMOBILE_API + "course/";
	public static final String SERVER_TAG_PATH = OPPIAMOBILE_API + "tag/";
	public static final String TRACKER_PATH = OPPIAMOBILE_API + "tracker/";
	public static final String SERVER_POINTS_PATH = OPPIAMOBILE_API + "points/";
	public static final String SERVER_AWARDS_PATH = OPPIAMOBILE_API + "awards/";
	public static final String SERVER_COURSES_NAME = "courses";
	public static final String COURSE_ACTIVITY_PATH = SERVER_COURSES_PATH + "%s/activity/";

    // admin security settings
    public static final boolean ADMIN_PROTECT_SETTINGS = true;
    public static final boolean ADMIN_PROTECT_COURSE_DELETE = true;
    public static final boolean ADMIN_PROTECT_COURSE_RESET = true;
    public static final boolean ADMIN_PROTECT_COURSE_INSTALL = true;
    public static final boolean ADMIN_PROTECT_COURSE_UPDATE = true;

	// general other settings
	public static final String MINT_API_KEY = "26c9c657";
	public static final int DOWNLOAD_COURSES_DISPLAY = 1; //this no of courses must be displayed for the 'download more courses' option to disappear
	public static final int PASSWORD_MIN_LENGTH = 6;
	public static final int PAGE_READ_TIME = 3;
	public static final int RESOURCE_READ_TIME = 3;
	public static final int URL_READ_TIME = 5;
	public static final String USER_AGENT = "OppiaMobile Android: ";
    public static final String DEFAULT_STORAGE_OPTION = PrefsActivity.STORAGE_OPTION_EXTERNAL;

    public static final int SCORECARD_ANIM_DURATION = 800;
    public static final long MEDIA_SCAN_TIME_LIMIT = 3600;

	public static final boolean DEFAULT_DISPLAY_COMPLETED = true;
	public static final boolean DEFAULT_DISPLAY_PROGRESS_BAR = true;
	
	public static final boolean MENU_ALLOW_COURSE_DOWNLOAD = true;
	public static final boolean MENU_ALLOW_SETTINGS = true;
	public static final boolean MENU_ALLOW_MONITOR = true;
	public static final boolean MENU_ALLOW_LOGOUT = true;
    public static final boolean SHOW_CATEGORIES_ACTIVITY = true;

    public static final boolean SESSION_EXPIRATION_ENABLED = false; // whether to force users to be logged out after inactivity
    public static final int SESSION_EXPIRATION_TIMEOUT = 15; // no seconds before user is logged out for inactivity
	
	public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	public static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");
	public static final DateTimeFormatter TIME_FORMAT = DateTimeFormat.forPattern("HH:mm:ss");
	public static final int MAX_TRACKER_SUBMIT = 10;
	public static final String[] SUPPORTED_ACTIVITY_TYPES = {"page","quiz","resource","feedback","url"};

    public static final String DEVICEADMIN_ADD_PATH = OPPIAMOBILE_API + "device/register/";
    public static final boolean DEVICEADMIN_ENABLED = false;

	// only used in case a course doesn't have any lang specified
	public static final String DEFAULT_LANG = "en";
	
	// for tracking if SubmitTrackerMultipleTask is already running
	public SubmitTrackerMultipleTask omSubmitTrackerMultipleTask = null;
	
	// for tracking if SubmitQuizAttemptsTask is already running
	public SubmitQuizAttemptsTask omSubmitQuizAttemptsTask = null;

    @Override
    public void onCreate() {
        super.onCreate();

        // this method fires once at application start
        Log.d(TAG, "Application start");

        Context ctx = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String storageOption = prefs.getString(PrefsActivity.PREF_STORAGE_OPTION, "");

        if (storageOption.trim().equals("")){
            //If there is not storage option set, set the default option

            storageOption = DEFAULT_STORAGE_OPTION;
            boolean defaultOptionSuccessful = setStorageOption(ctx, prefs, storageOption);
            if (!defaultOptionSuccessful){
                //If the default option didn't work (supposing it was external), fallback to internal
                Log.d(TAG, storageOption + " didn't work, trying internall fallback");
                storageOption = PrefsActivity.STORAGE_OPTION_INTERNAL;
                setStorageOption(ctx, prefs, storageOption);
            }
        }
        else{
            StorageAccessStrategy strategy = StorageAccessStrategyFactory.createStrategy(storageOption);
            Storage.setStorageStrategy(strategy);
        }
        Log.d(TAG, "Storage option set: " + storageOption);
    }

    private boolean setStorageOption(Context ctx, SharedPreferences prefs, String storageOption){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PrefsActivity.PREF_STORAGE_OPTION, storageOption).apply();

        StorageAccessStrategy strategy = StorageAccessStrategyFactory.createStrategy(storageOption);
        boolean success = strategy.updateStorageLocation(ctx);
        if (success) Storage.setStorageStrategy(strategy);
        return success;
    }

}
