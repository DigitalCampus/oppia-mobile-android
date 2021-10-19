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


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.analytics.Analytics;
import org.digitalcampus.oppia.analytics.BaseAnalytics;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.database.MyDatabase;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.di.AppModule;
import org.digitalcampus.oppia.di.DaggerAppComponent;
import org.digitalcampus.oppia.service.CoursesChecksWorker;
import org.digitalcampus.oppia.service.CoursesCompletionReminderWorker;
import org.digitalcampus.oppia.service.TrackerWorker;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategy;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategyFactory;
import org.digitalcampus.oppia.utils.storage.StorageUtils;
import org.digitalcampus.oppia.utils.ui.OppiaNotificationUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import androidx.multidex.MultiDex;
import androidx.preference.PreferenceManager;
import androidx.room.Room;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;

public class App extends Application {

    public static final String TAG = App.class.getSimpleName();

    public static final String COURSE_XML = "module.xml";
    public static final String COURSE_TRACKER_XML = "tracker.xml";
    public static final String PRE_INSTALL_COURSES_DIR = "www/preload/courses"; // don't include leading or trailing slash
    public static final String PRE_INSTALL_MEDIA_DIR = "www/preload/media"; // don't include leading or trailing slash

    // admin security settings
    public static final boolean ADMIN_PROTECT_SETTINGS = BuildConfig.ADMIN_PROTECT_SETTINGS;
    public static final boolean ADMIN_PROTECT_ADVANCED_SETTINGS = BuildConfig.ADMIN_PROTECT_ADVANCED_SETTINGS;
    public static final boolean ADMIN_PROTECT_SECURITY_SETTINGS = BuildConfig.ADMIN_PROTECT_SECURITY_SETTINGS;
    public static final boolean ADMIN_PROTECT_SERVER = BuildConfig.ADMIN_PROTECT_SERVER;
    public static final boolean ADMIN_PROTECT_COURSE_DELETE = BuildConfig.ADMIN_PROTECT_COURSE_DELETE;
    public static final boolean ADMIN_PROTECT_COURSE_RESET = BuildConfig.ADMIN_PROTECT_COURSE_RESET;
    public static final boolean ADMIN_PROTECT_COURSE_INSTALL = BuildConfig.ADMIN_PROTECT_COURSE_INSTALL;
    public static final boolean ADMIN_PROTECT_COURSE_UPDATE = BuildConfig.ADMIN_PROTECT_COURSE_UPDATE;
    public static final boolean ADMIN_PROTECT_ACTIVITY_SYNC = BuildConfig.ADMIN_PROTECT_ACTIVITY_SYNC;
    public static final boolean ADMIN_PROTECT_ACTIVITY_EXPORT = BuildConfig.ADMIN_PROTECT_ACTIVITY_EXPORT;

    // tracker metadata settings
    public static final boolean METADATA_INCLUDE_NETWORK = BuildConfig.METADATA_INCLUDE_NETWORK;
    public static final boolean METADATA_INCLUDE_APP_INSTANCE_ID = BuildConfig.METADATA_INCLUDE_APP_INSTANCE_ID;
    public static final boolean METADATA_INCLUDE_MANUFACTURER_MODEL = BuildConfig.METADATA_INCLUDE_MANUFACTURER_MODEL;
    public static final boolean METADATA_INCLUDE_WIFI_ON = BuildConfig.METADATA_INCLUDE_WIFI_ON;
    public static final boolean METADATA_INCLUDE_NETWORK_CONNECTED = BuildConfig.METADATA_INCLUDE_NETWORK_CONNECTED;
    public static final boolean METADATA_INCLUDE_BATTERY_LEVEL = BuildConfig.METADATA_INCLUDE_BATTERY_LEVEL;

    // general other settings
    public static final String MINT_API_KEY = BuildConfig.MINT_API_KEY;
    public static final int DOWNLOAD_COURSES_DISPLAY = BuildConfig.DOWNLOAD_COURSES_DISPLAY; //this no of courses must be displayed for the 'download more courses' option to disappear
    public static final int USERNAME_MIN_CHARACTERS = 4;
    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final int PAGE_READ_TIME = 3;
    public static final int RESOURCE_READ_TIME = 3;
    public static final int URL_READ_TIME = 5;

    public static final String PAGE_COMPLETED_METHOD_TIME_SPENT = "TIME_SPENT";
    public static final String PAGE_COMPLETED_METHOD_WPM = "WPM";

    public static final long SCORECARD_ANIM_DURATION = 800;
    public static final long MEDIA_SCAN_TIME_LIMIT = 3600;
    public static final long LEADERBOARD_FETCH_EXPIRATION = 3600;

    public static final boolean DEFAULT_DISPLAY_COMPLETED = true;
    public static final boolean DEFAULT_DISPLAY_PROGRESS_BAR = true;

    public static final boolean MENU_ALLOW_COURSE_DOWNLOAD = BuildConfig.MENU_ALLOW_COURSE_DOWNLOAD;
    public static final boolean MENU_ALLOW_LANGUAGE = BuildConfig.MENU_ALLOW_LANGUAGE;
    public static final boolean MENU_ALLOW_SETTINGS = BuildConfig.MENU_ALLOW_SETTINGS;
    public static final boolean MENU_ALLOW_SYNC = BuildConfig.MENU_ALLOW_SYNC;
    public static final boolean MENU_ALLOW_LOGOUT = BuildConfig.MENU_ALLOW_LOGOUT;

    public static final boolean SESSION_EXPIRATION_ENABLED = BuildConfig.SESSION_EXPIRATION_ENABLED; // whether to force users to be logged out after inactivity
    public static final int SESSION_EXPIRATION_TIMEOUT = BuildConfig.SESSION_EXPIRATION_TIMEOUT; // no seconds before user is logged out for inactivity

    public static final int MAX_TRACKER_SUBMIT = 10;
    public static final String[] SUPPORTED_MEDIA_TYPES = {"video/m4v", "video/mp4", "audio/mpeg", "video/3gp", "video/3gpp"}; //NOSONAR

    // only used in case a course doesn't have any lang specified
    public static final String DEFAULT_LANG = "en";
    public static final String WORK_TRACKER_SEND = "tracker_send_work";
    public static final String WORK_COURSES_CHECKS = "no_course_worker";
    public static final String WORK_COURSES_NOT_COMPLETED_REMINDER = "courses_reminder";


    private AppComponent appComponent;
    private static MyDatabase db;
    private static volatile BaseAnalytics analytics;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        db = Room.databaseBuilder(getApplicationContext(),
                MyDatabase.class, MyDatabase.DB_NAME_ROOM)
                .allowMainThreadQueries()
                .addMigrations(MyDatabase.MIGRATIONS)
                .build();

        DbHelper.getInstance(this).getReadableDatabase();
        Analytics.initializeAnalytics(getApplicationContext());

        // this method fires once at application start
        Log.d(TAG, "Application start");

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Helvetica.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

        Context ctx = getApplicationContext();
        // Load the preferences from XML resources
        loadDefaultPreferenceValues(ctx, false);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        // First run or Updates configurations
        checkAdminProtectionOnFirstRun(prefs);
        checkAppInstanceIdCreated(prefs);
        checkShowDescriptionOverrideUpdate();

        configureStorageType();

        setupPeriodicWorkers();

        OppiaNotificationUtils.initializeOreoNotificationChannels(this);

//        launchWorkerToTest();

    }



    private void checkAppInstanceIdCreated(SharedPreferences prefs) {
        if (prefs.getString(PrefsActivity.PREF_APP_INSTANCE_ID, null) == null) {
            String appInstanceId = UUID.randomUUID().toString();
            prefs.edit().putString(PrefsActivity.PREF_APP_INSTANCE_ID, appInstanceId).apply();
        }
    }

    private void configureStorageType() {

        String storageOption = getPrefs(this).getString(PrefsActivity.PREF_STORAGE_OPTION, "");
        if (TextUtils.isEmpty(storageOption)) {
            storageOption = PrefsActivity.STORAGE_OPTION_EXTERNAL;
        }

        StorageAccessStrategy strategy = StorageAccessStrategyFactory.createStrategy(storageOption);
        if (!strategy.isStorageAvailable(this)) {
            strategy = StorageAccessStrategyFactory.createStrategy(PrefsActivity.STORAGE_OPTION_INTERNAL);
        }

        StorageUtils.saveStorageData(this, strategy.getStorageType());
        Storage.setStorageStrategy(strategy);


        Log.d(TAG, "Storage option set: " + Storage.getStorageStrategy().getStorageType());
    }


    public static MyDatabase getDb() {
        return db;
    }

    private void launchWorkerToTest() {

        OneTimeWorkRequest request = OneTimeWorkRequest.from(CoursesCompletionReminderWorker.class);
        WorkManager.getInstance(this)
                .enqueueUniqueWork("worker_test", ExistingWorkPolicy.REPLACE, request);
    }

    private void setupPeriodicWorkers() {

        boolean backgroundData = getPrefs(this).getBoolean(PrefsActivity.PREF_BACKGROUND_DATA_CONNECT, true);

        if (backgroundData) {
            scheduleTrackerWork();
            scheduleCoursesChecksWork();
        } else {
            cancelWorks(WORK_COURSES_CHECKS, WORK_TRACKER_SEND);
        }

    }


    private void scheduleTrackerWork() {

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest trackerSendWork = new PeriodicWorkRequest.Builder(TrackerWorker.class, 1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(WORK_TRACKER_SEND,
                ExistingPeriodicWorkPolicy.REPLACE, trackerSendWork);

    }

    public void cancelWorks(String... uniqueNames) {
        for (String workName : uniqueNames) {
            WorkManager.getInstance(this).cancelUniqueWork(workName);
        }
    }

    private void scheduleCoursesChecksWork() {

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest trackerSendWork = new PeriodicWorkRequest.Builder(CoursesChecksWorker.class, 12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(WORK_COURSES_CHECKS,
                ExistingPeriodicWorkPolicy.REPLACE, trackerSendWork);

    }


    public static SharedPreferences getPrefs(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void loadDefaultPreferenceValues(Context ctx, boolean readAgain) {
        PreferenceManager.setDefaultValues(ctx, R.xml.prefs, readAgain);
    }

    private void checkAdminProtectionOnFirstRun(SharedPreferences prefs) {
        if (prefs.getBoolean(PrefsActivity.PREF_APPLICATION_FIRST_RUN, true)) {
            Log.d(TAG, "First run! Checking if default admin password");
            if (!prefs.getString(PrefsActivity.PREF_ADMIN_PASSWORD, "").isEmpty()) {
                //If the initial Admin password protection is set, enable de admin protection
                prefs.edit().putBoolean(PrefsActivity.PREF_ADMIN_PROTECTION, true).apply();
                Log.d(TAG, "Admin password protection enabled");
            }

            prefs.edit().putBoolean(PrefsActivity.PREF_APPLICATION_FIRST_RUN, false).apply();
        }
    }

    private void checkShowDescriptionOverrideUpdate() {
        SharedPreferences prefs = getPrefs(this);
        if (prefs.getBoolean(PrefsActivity.PREF_UPDATE_OVERRIDE_SHOW_DESCRIPTION, true)) {

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(PrefsActivity.PREF_SHOW_COURSE_DESC, BuildConfig.SHOW_COURSE_DESCRIPTION);
            editor.putBoolean(PrefsActivity.PREF_UPDATE_OVERRIDE_SHOW_DESCRIPTION, false);
            editor.apply();
        }
    }

    public AppComponent getComponent() {
        if (appComponent == null) {
            appComponent = DaggerAppComponent.builder()
                    .appModule(new AppModule(this))
                    .build();
        }
        return appComponent;
    }

    public void setComponent(AppComponent appComponent) {
        this.appComponent = appComponent;
    }

}
