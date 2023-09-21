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
package org.digitalcampus.oppia.application

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.multidex.MultiDex
import androidx.preference.PreferenceManager
import androidx.room.Room
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import org.digitalcampus.mobile.learning.BuildConfig
import org.digitalcampus.mobile.learning.R
import org.digitalcampus.oppia.activity.PrefsActivity
import org.digitalcampus.oppia.analytics.Analytics
import org.digitalcampus.oppia.analytics.BaseAnalytics
import org.digitalcampus.oppia.database.DbHelper
import org.digitalcampus.oppia.database.MyDatabase
import org.digitalcampus.oppia.di.AppComponent
import org.digitalcampus.oppia.di.AppModule
import org.digitalcampus.oppia.di.DaggerAppComponent
import org.digitalcampus.oppia.service.CoursesChecksWorker
import org.digitalcampus.oppia.service.CoursesCompletionReminderWorkerManager
import org.digitalcampus.oppia.service.TrackerWorker
import org.digitalcampus.oppia.service.UpdateUserProfileWorker
import org.digitalcampus.oppia.utils.TextUtilsJava
import org.digitalcampus.oppia.utils.storage.Storage
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategyFactory
import org.digitalcampus.oppia.utils.storage.StorageUtils
import org.digitalcampus.oppia.utils.ui.OppiaNotificationUtils
import java.util.UUID
import java.util.concurrent.TimeUnit

class App : Application() {

    companion object {
        val TAG = App::class.simpleName
        const val COURSE_XML = "module.xml"
        const val COURSE_TRACKER_XML = "tracker.xml"
        const val PRE_INSTALL_COURSES_DIR = "www/preload/courses" // don't include leading or trailing slash
        const val PRE_INSTALL_MEDIA_DIR = "www/preload/media" // don't include leading or trailing slash

        // general other settings
        const val USERNAME_MIN_CHARACTERS = 4
        const val PASSWORD_MIN_LENGTH = 6
        const val PAGE_READ_TIME = 3
        const val RESOURCE_READ_TIME = 3
        const val URL_READ_TIME = 5
        const val PAGE_COMPLETED_METHOD_TIME_SPENT = "TIME_SPENT"
        const val PAGE_COMPLETED_METHOD_WPM = "WPM"
        const val SCORECARD_ANIM_DURATION: Long = 800
        const val MEDIA_SCAN_TIME_LIMIT: Long = 3600
        const val LEADERBOARD_FETCH_EXPIRATION: Long = 3600
        const val DEFAULT_DISPLAY_COMPLETED = true
        const val DEFAULT_DISPLAY_PROGRESS_BAR = true
        const val MAX_TRACKER_SUBMIT = 10

        @JvmField
        val SUPPORTED_MEDIA_TYPES = arrayOf("video/m4v", "video/mp4", "audio/mpeg", "video/3gp", "video/3gpp") //NOSONAR

        // only used in case a course doesn't have any lang specified
        const val DEFAULT_LANG = "en"
        const val WORK_TRACKER_SEND = "tracker_send_work"
        const val WORK_COURSES_CHECKS = "no_course_worker"
        const val WORK_COURSES_NOT_COMPLETED_REMINDER = "courses_reminder"
        const val WORK_COURSES_NOT_COMPLETED_REMINDER_ = "courses_reminder_"
        const val WORK_USER_PROFILE_CHECKS = "user_profile_checks_worker"

        @JvmStatic
        var db: MyDatabase? = null
            private set

        @JvmStatic
        fun getPrefs(context: Context): SharedPreferences {
            return PreferenceManager.getDefaultSharedPreferences(context)
        }

        @JvmStatic
        fun loadDefaultPreferenceValues(ctx: Context?, readAgain: Boolean) {
            PreferenceManager.setDefaultValues(ctx!!, R.xml.prefs, readAgain)
        }
    }

    private lateinit var appComponent: AppComponent

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(
            applicationContext,
            MyDatabase::class.java, MyDatabase.DB_NAME_ROOM
        )
            .allowMainThreadQueries()
            .addMigrations(*MyDatabase.MIGRATIONS)
            .build()

        DbHelper.getInstance(this).readableDatabase
        Analytics.initializeAnalytics(this)

        // this method fires once at application start
        Log.d(TAG, "Application start")
        ViewPump.init(
            ViewPump.builder()
                .addInterceptor(
                    CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                            .setDefaultFontPath("fonts/lato.ttf")
                            .setFontAttrId(R.attr.fontPath)
                            .build()
                    )
                )
                .build()
        )
        val ctx = applicationContext
        // Load the preferences from XML resources
        loadDefaultPreferenceValues(ctx, false)
        val prefs = PreferenceManager.getDefaultSharedPreferences(ctx)

        // First run or Updates configurations
        checkAdminProtectionOnFirstRun(prefs)
        checkAppInstanceIdCreated(prefs)
        checkShowDescriptionOverrideUpdate()
        configureStorageType()
        setupPeriodicWorkers()
        OppiaNotificationUtils.initializeOreoNotificationChannels(this)

//        launchWorkerToTest();
    }

    private fun launchWorkerToTest() {
        val request = OneTimeWorkRequest.from(CoursesChecksWorker::class.java)
        WorkManager.getInstance(this)
            .enqueueUniqueWork("worker_test", ExistingWorkPolicy.REPLACE, request)
    }

    private fun checkAppInstanceIdCreated(prefs: SharedPreferences) {
        if (prefs.getString(PrefsActivity.PREF_APP_INSTANCE_ID, null) == null) {
            val appInstanceId = UUID.randomUUID().toString()
            prefs.edit().putString(PrefsActivity.PREF_APP_INSTANCE_ID, appInstanceId).apply()
        }
    }

    private fun configureStorageType() {
        var storageOption = getPrefs(this).getString(PrefsActivity.PREF_STORAGE_OPTION, null)
        if (TextUtilsJava.isEmpty(storageOption)) {
            val strategy = StorageAccessStrategyFactory.createStrategy(PrefsActivity.STORAGE_OPTION_EXTERNAL)
            storageOption = if (!strategy.isStorageAvailable(this)) {
                PrefsActivity.STORAGE_OPTION_INTERNAL
            } else {
                PrefsActivity.STORAGE_OPTION_EXTERNAL
            }
        }
        val strategy = StorageAccessStrategyFactory.createStrategy(storageOption)
        StorageUtils.saveStorageData(this, strategy.getStorageType())
        Storage.storageStrategy = strategy
        Log.d(TAG, "Storage option set: " + Storage.storageStrategy?.getStorageType())
    }

    private fun setupPeriodicWorkers() {
        val backgroundData = getPrefs(this).getBoolean(PrefsActivity.PREF_BACKGROUND_DATA_CONNECT, true)
        if (backgroundData) {
            scheduleTrackerWork()
            scheduleUserProfileChecksWork()
            scheduleCoursesChecksWork()
        } else {
            cancelWorks(WORK_COURSES_CHECKS, WORK_TRACKER_SEND, WORK_USER_PROFILE_CHECKS)
        }
        scheduleCoursesReminderWork()
        cancelWorks(WORK_COURSES_NOT_COMPLETED_REMINDER) // For retrocompatibility after reminder changes
    }

    private fun scheduleCoursesReminderWork() {
        CoursesCompletionReminderWorkerManager.configureCoursesCompletionReminderWorker(
            this,
            ExistingPeriodicWorkPolicy.KEEP
        )
    }

    private fun scheduleTrackerWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val trackerSendWork = PeriodicWorkRequest.Builder(TrackerWorker::class.java, 1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WORK_TRACKER_SEND,
            ExistingPeriodicWorkPolicy.REPLACE,
            trackerSendWork
        )
    }

    fun cancelWorks(vararg uniqueNames: String?) {
        for (workName in uniqueNames) {
            WorkManager.getInstance(this).cancelUniqueWork(workName!!)
        }
    }

    private fun scheduleCoursesChecksWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val trackerSendWork = PeriodicWorkRequest.Builder(CoursesChecksWorker::class.java, 12, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WORK_COURSES_CHECKS,
            ExistingPeriodicWorkPolicy.REPLACE,
            trackerSendWork
        )
    }

    private fun scheduleUserProfileChecksWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val userProfileCheckWork = PeriodicWorkRequest.Builder(
            UpdateUserProfileWorker::class.java, 12, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WORK_USER_PROFILE_CHECKS,
            ExistingPeriodicWorkPolicy.REPLACE,
            userProfileCheckWork
        )
    }

    private fun checkAdminProtectionOnFirstRun(prefs: SharedPreferences) {
        if (prefs.getBoolean(PrefsActivity.PREF_APPLICATION_FIRST_RUN, true)) {
            Log.d(TAG, "First run! Checking if default admin password")
            if (!prefs.getString(PrefsActivity.PREF_ADMIN_PASSWORD, "")!!.isEmpty()) {
                //If the initial Admin password protection is set, enable de admin protection
                prefs.edit().putBoolean(PrefsActivity.PREF_ADMIN_PROTECTION, true).apply()
                Log.d(TAG, "Admin password protection enabled")
            }
            prefs.edit().putBoolean(PrefsActivity.PREF_APPLICATION_FIRST_RUN, false).apply()
        }
    }

    private fun checkShowDescriptionOverrideUpdate() {
        val prefs = getPrefs(this)
        if (prefs.getBoolean(PrefsActivity.PREF_UPDATE_OVERRIDE_SHOW_DESCRIPTION, true)) {
            val editor = prefs.edit()
            editor.putBoolean(PrefsActivity.PREF_SHOW_COURSE_DESC, BuildConfig.SHOW_COURSE_DESCRIPTION)
            editor.putBoolean(PrefsActivity.PREF_UPDATE_OVERRIDE_SHOW_DESCRIPTION, false)
            editor.apply()
        }
    }

    var component: AppComponent
        get() {
            if (!::appComponent.isInitialized) {
                appComponent = DaggerAppComponent.builder()
                    .appModule(AppModule(this))
                    .build()
            }
            return appComponent
        }
        set(appComponent) {
            this.appComponent = appComponent
        }

}