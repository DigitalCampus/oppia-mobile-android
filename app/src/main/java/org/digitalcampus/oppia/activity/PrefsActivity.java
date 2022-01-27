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

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivityPreferencesBinding;
import org.digitalcampus.oppia.fragments.prefs.BasePreferenceFragment;
import org.digitalcampus.oppia.fragments.prefs.MainPreferencesFragment;
import org.digitalcampus.oppia.fragments.prefs.NotificationsPrefsFragment;
import org.digitalcampus.oppia.fragments.prefs.PreferenceChangedCallback;
import org.digitalcampus.oppia.listener.MoveStorageListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.service.CoursesCompletionReminderWorkerManager;
import org.digitalcampus.oppia.task.ChangeStorageOptionTask;
import org.digitalcampus.oppia.task.FetchServerInfoTask;
import org.digitalcampus.oppia.task.result.BasicResult;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.ui.OppiaNotificationUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class PrefsActivity extends AppActivity implements SharedPreferences.OnSharedPreferenceChangeListener,
        MoveStorageListener, PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {


    public static final String PREF_INFO = "prefInfo";
    public static final String PREF_USER_NAME = "prefUsername";
    public static final String PREF_SERVER = "prefServer";
    public static final String PREF_SERVER_CHECKED = "prefServerChecked";
    public static final String PREF_SERVER_VALID = "prefServerValid";
    public static final String PREF_SERVER_NAME = "prefServerName";
    public static final String PREF_SERVER_VERSION = "prefServerVersion";

    public static final String PREF_TRIGGER_POINTS_REFRESH = "prefScoreRefresh";
    public static final String PREF_SCORING_ENABLED = "prefScoringEnabled";
    public static final String PREF_BADGING_ENABLED = "prefBadgingEnabled";
    public static final String PREF_SERVER_TIMEOUT_CONN = "prefServerTimeoutConnection";
    public static final String PREF_SERVER_TIMEOUT_RESP = "prefServerTimeoutResponse";

    public static final String PREF_METADATA = "prefMetadata";
    public static final String PREF_METADATA_NETWORK = "NETWORK";
    public static final String PREF_METADATA_APP_INSTANCE_ID = "APP_INSTANCE_ID";
    public static final String PREF_METADATA_MANUFACTURER_MODEL = "MANUFACTURER_MODEL";
    public static final String PREF_METADATA_WIFI_ON = "WIFI_ON";
    public static final String PREF_METADATA_NETWORK_CONNECTED = "NETWORK_CONNECTED";
    public static final String PREF_METADATA_BATTERY_LEVEL = "BATTERY_LEVEL";
    public static final String PREF_METADATA_GPS = "GPS";

    public static final String PREF_BACKGROUND_DATA_CONNECT = "prefBackgroundDataConnect";

    public static final String PREF_APPLICATION_FIRST_RUN = "prefFirstRun";
    public static final String PREF_UPDATE_OVERRIDE_SHOW_DESCRIPTION = "prefUpdateOverrideShowDescription";

    /*
     * Start personal prefs - move these to UserProps table
     */
    public static final String PREF_PHONE_NO = "prefPhoneNo";
    public static final String PREF_LANGUAGE = "prefLanguage";
    public static final String PREF_SHOW_SCHEDULE_REMINDERS = "prefShowScheduleReminders";
    public static final String PREF_NO_SCHEDULE_REMINDERS = "prefNoScheduleReminders";
    public static final String PREF_SHOW_COURSE_DESC = "prefShowCourseDescription";
    public static final String PREF_START_COURSEINDEX_COLLAPSED = "prefStartCourseIndexCollapsed";
    public static final String PREF_SHOW_PROGRESS_BAR = "prefShowProgressBar";
    public static final String PREF_SHOW_SECTION_NOS = "prefShowSectionNumbers";
    public static final String PREF_HIGHLIGHT_COMPLETED = "prefHighlightCompleted";
    public static final String PREF_TEXT_SIZE = "prefTextSize";

    public static final String PREF_DISABLE_NOTIFICATIONS = "prefDisableNotifications";
    public static final String PREF_SHOW_GAMIFICATION_EVENTS = "prefShowGamificationEvents";
    public static final String PREF_GAMIFICATION_POINTS_ANIMATION = "prefGamificationPointsAnimation";
    public static final String PREF_DURATION_GAMIFICATION_POINTS_VIEW = "prefDurationGamificationPointsView";

    public static final String PREF_BADGE_AWARD_CRITERIA = "prefBadgeAwardCriteria";
    public static final String PREF_BADGE_AWARD_CRITERIA_PERCENT = "prefBadgeAwardCriteriaPercent";


    /*
     * End personal prefs
     */
    public static final String PREF_LAST_MEDIA_SCAN = "prefLastMediaScan";
    public static final String PREF_LOGOUT_ENABLED = "prefLogoutEnabled";
    public static final String PREF_DOWNLOAD_ENABLED = "prefDownloadEnabled";
    public static final String PREF_CHANGE_LANGUAGE_ENABLED = "prefChangeLanguageEnabled";
    public static final String PREF_DELETE_COURSE_ENABLED = "prefDeleteCourseEnabled";

    public static final String PREF_LAST_LEADERBOARD_FETCH = "prefLastLeaderboardFetch";

    public static final String PREF_STORAGE_OPTION = "prefStorageOption";
    public static final String STORAGE_OPTION_INTERNAL = "internal";
    public static final String STORAGE_OPTION_EXTERNAL = "external";
    public static final String STORAGE_NEEDS_PERMISSIONS = "storageNeedsPermissions";

    public static final String PREF_ADMIN_PROTECTION = "prefAdminProtection";
    public static final String PREF_ADMIN_PASSWORD = "prefAdminPassword"; //NOSONAR
    public static final String LAST_ACTIVE_TIME = "prefLastActiveTime";

    //Google Cloud Messaging preferences
    public static final String PREF_TEST_ACTION_PROTECTED = "prefTestActionProtected";
    public static final String PREF_DATA_ROOM_MIGRATON_VERSION = "prefDataRoomMigrationVersion";
    public static final String PREF_DISPLAY_SCREEN = "prefDisplay";
    public static final String PREF_SECURITY_SCREEN = "prefSecurity";
    public static final String PREF_NOTIFICATIONS_SCREEN = "prefNotifications";
    public static final String PREF_ADVANCED_SCREEN = "prefAdvanced";
    public static final String PREF_APP_INSTANCE_ID = "prefAppInstanceId";

    public static final String PREF_LAST_COURSE_VERSION_TIMESTAMP_CHECKED = "prefLastCourseVersionTimestampChecked";
    public static final String PREF_LAST_COURSES_CHECKS_SUCCESSFUL_TIME = "prefLastCoursesChecksSuccessful";
    public static final String PREF_SERVER_COURSES_CACHE = "prefServerCoursesCache";
    public static final String PREF_NEW_COURSES_LIST_NOTIFIED = "prefNewCoursesListNotified";
    public static final String PREF_LAST_NEW_COURSE_SEEN_TIMESTAMP = "prefNewCourseTimestamp";

    public static final String PREF_FULL_ACTIVITY_EXPORT = "prefFullActivityExport";
    public static final String PREF_FLUSH_COURSE_LISTING_CACHE = "prefFlushCourseListingCache";

    //Privacy tracking preferences
    public static final String PREF_ANALYTICS_INITIAL_PROMPT = "prefAnalyticsInitialPrompt";
    public static final String PREF_BUG_REPORT_ENABLED = "prefBugReportEnabled";
    public static final String PREF_ANALYTICS_ENABLED = "prefAnalyticsEnabled";
    public static final String PREF_COURSES_REMINDER_ENABLED = "prefCoursesReminderEnabled";
    public static final String PREF_COURSES_REMINDER_INTERVAL = "prefCoursesReminderInterval";
    public static final String PREF_COURSES_REMINDER_DAYS = "prefCoursesReminderDays";
    public static final String PREF_COURSES_REMINDER_TIME = "prefCoursesReminderTime";
    public static final String PREF_REMINDERS_LOG = "prefRemindersLog";

    private PreferenceChangedCallback currentPrefScreen;

    @Inject
    CoursesRepository coursesRepository;
    private FetchServerInfoTask fetchServerInfoTask;
    private boolean forzeGoToLoginScreen;

    @Override
    public void onStart() {
        super.onStart();
        initialize();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        org.digitalcampus.mobile.learning.databinding.ActivityPreferencesBinding binding = ActivityPreferencesBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        getAppComponent().inject(this);

        if (savedInstanceState == null) {
            // Create the fragment only when the activity is created for the first time.
            // ie. not after orientation changes
            MainPreferencesFragment mPrefsFragment = (MainPreferencesFragment) getSupportFragmentManager().findFragmentByTag(MainPreferencesFragment.FRAGMENT_TAG);
            if (mPrefsFragment == null) {
                mPrefsFragment = MainPreferencesFragment.newInstance();
                Bundle bundle = this.getIntent().getExtras();
                if (bundle != null) {
                    mPrefsFragment.setArguments(bundle);
                }
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.root_layout, mPrefsFragment, MainPreferencesFragment.FRAGMENT_TAG)
                    .commit();
        }

        if (getIntent().getBooleanExtra(CoursesCompletionReminderWorkerManager.EXTRA_GO_TO_NOTIFICATIONS_SETTINGS, false)) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.root_layout, NotificationsPrefsFragment.newInstance(), null)
                    .addToBackStack(null)
                    .commit();
        }

        OppiaNotificationUtils.cancelNotifications(this, OppiaNotificationUtils.NOTIF_ID_COURSES_REMINDER);
    }


    private ArrayList<Lang> getLanguagesCourses() {

        final ArrayList<Lang> langs = new ArrayList<>();
        List<Course> courses = coursesRepository.getCourses(this);

        for (Course course : courses) {
            for (Lang courseLang : course.getLangs()) {
                if (!langs.contains(courseLang)) {
                    langs.add(courseLang);
                }
            }
        }
        return langs;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            return;
        }

        if (forzeGoToLoginScreen) {
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return;
        }

        super.onBackPressed();
    }

    public void forzeGoToLoginScreen() {
        forzeGoToLoginScreen = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fetchServerInfoTask != null && !fetchServerInfoTask.isCancelled()) {
            fetchServerInfoTask.setListener(null);
        }
    }


    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "Preference changed: " + key);

        if (key.equalsIgnoreCase(PrefsActivity.PREF_SERVER)) {
            checkUpdatedServer(sharedPreferences);
        } else if (key.equalsIgnoreCase(PREF_STORAGE_OPTION)) {
            checkStorageOption(sharedPreferences);
        } else if (key.equalsIgnoreCase(PREF_ADMIN_PROTECTION)) {
            checkAdminProtection(sharedPreferences);
        } else if (key.equalsIgnoreCase(PREF_ADMIN_PASSWORD)) {
            String newPassword = sharedPreferences.getString(PrefsActivity.PREF_ADMIN_PASSWORD, "");
            if (TextUtils.equals(newPassword, "")) {
                //If the user introduced an empty password, disable the password protection
                disableAdminProtection(sharedPreferences);
            }
        }
    }

    private void checkUpdatedServer(SharedPreferences sharedPreferences) {
        String newServerURL = sharedPreferences.getString(PrefsActivity.PREF_SERVER, "").trim();
        if (!newServerURL.endsWith("/")) {
            newServerURL = newServerURL + "/";
            sharedPreferences.edit().putString(PrefsActivity.PREF_SERVER, newServerURL).apply();
        }
        sharedPreferences.edit().putBoolean(PrefsActivity.PREF_SERVER_CHECKED, false).apply();

        if (fetchServerInfoTask != null && !fetchServerInfoTask.isCancelled()) {
            fetchServerInfoTask.cancel(true);
        }

        fetchServerInfoTask = new FetchServerInfoTask(this);
        fetchServerInfoTask.setListener(new FetchServerInfoTask.FetchServerInfoListener() {
            @Override
            public void onError(String message) {
                Toast.makeText(PrefsActivity.this, message, Toast.LENGTH_LONG).show();
                if (currentPrefScreen != null) {
                    currentPrefScreen.onPreferenceUpdated(PrefsActivity.PREF_SERVER, null);
                }
            }

            @Override
            public void onValidServer(String version, String name) {
                if (currentPrefScreen != null) {
                    currentPrefScreen.onPreferenceUpdated(PrefsActivity.PREF_SERVER, null);
                }
            }

            @Override
            public void onUnchecked() {
                if (currentPrefScreen != null) {
                    currentPrefScreen.onPreferenceUpdated(PrefsActivity.PREF_SERVER, null);
                }
            }
        });
        fetchServerInfoTask.execute();

        if (currentPrefScreen != null) {
            currentPrefScreen.onPreferenceUpdated(PrefsActivity.PREF_SERVER, null);
        }
    }

    private void checkStorageOption(SharedPreferences sharedPreferences) {

        String storageOption = sharedPreferences.getString(PrefsActivity.PREF_STORAGE_OPTION, "");

        Log.d(TAG, "Storage option selected: " + storageOption);

        if (!storageOption.equals(Storage.getStorageStrategy().getStorageType())) {
            executeChangeStorageTask(storageOption);
        }
    }

    private void checkAdminProtection(SharedPreferences sharedPreferences) {
        boolean protectionEnabled = sharedPreferences.getBoolean(PrefsActivity.PREF_ADMIN_PROTECTION, false);
        if (protectionEnabled) {
            Log.d(TAG, "Admin protection enabled, prompting for new password");
            final EditText passwordInput = new EditText(this);
            final AlertDialog passwordDialog = new AlertDialog.Builder(this, R.style.Oppia_AlertDialogStyle)
                    .setTitle(getString(R.string.admin_password_newpassword_dialog_title))
                    .setMessage(getString(R.string.admin_password_newpassword_dialog_message))
                    .setView(passwordInput)
                    .setPositiveButton(R.string.ok, null)
                    .create();
            passwordDialog.show();
            passwordDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> {
                String password = passwordInput.getText().toString();
                if (!password.equals("")) {
                    passwordDialog.dismiss();
                }
            });
            passwordDialog.setOnDismissListener(dialog -> {
                String password = passwordInput.getText().toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (password.equals("")) {
                    disableAdminProtection(sharedPreferences);
                } else {
                    //Update the password preference
                    editor.putString(PrefsActivity.PREF_ADMIN_PASSWORD, password).apply();
                    //Update the UI value of the fragment
                    currentPrefScreen.onPreferenceUpdated(PREF_ADMIN_PASSWORD, password);
                }
            });
        }
    }

    private void disableAdminProtection(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PrefsActivity.PREF_ADMIN_PROTECTION, false).apply();
        //Update the UI value of the MainPreferencesFragment
        currentPrefScreen.onPreferenceUpdated(PREF_ADMIN_PROTECTION, "false");
    }

    private void executeChangeStorageTask(String storageOption) {

        Log.i(TAG, "executeChangeStorageTask: enter");

        ChangeStorageOptionTask changeStorageTask = new ChangeStorageOptionTask(PrefsActivity.this.getApplicationContext());
        changeStorageTask.setMoveStorageListener(this);

        if (isProgressDialogShowing()) {
            Log.i(TAG, "executeChangeStorageTask: Exiting. Previous task is being executed");
            return;

        }

        showProgressDialog(getString(R.string.moving_storage_location), false);

        changeStorageTask.execute(storageOption);

        Log.i(TAG, "executeChangeStorageTask: executing task");
    }

    //@Override
    public void moveStorageComplete(BasicResult result) {

        String storageOption = prefs.getString(PREF_STORAGE_OPTION, "");
        if (result.isSuccess()) {
            Log.d(TAG, "Move storage completed!");
            Toast.makeText(this, this.getString(R.string.move_storage_completed), Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "Move storage failed:" + result.getResultMessage());
            UIUtils.showAlert(this, R.string.error, result.getResultMessage());
            //We set the actual storage option (remove the one set by the user)
            if (currentPrefScreen != null) {
                currentPrefScreen.onPreferenceUpdated(PrefsActivity.PREF_STORAGE_OPTION, storageOption);
            }
        }

        //Finally, to handle the possibility that is in an inconsistent state
        if (!TextUtils.equals(storageOption, STORAGE_OPTION_INTERNAL)) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PrefsActivity.PREF_STORAGE_OPTION, STORAGE_OPTION_EXTERNAL).apply();
        }

        hideProgressDialog();
    }

    //@Override
    public void moveStorageProgressUpdate(String s) {
        // no need to show storage progress in this activity
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        BasePreferenceFragment fragment = null;
        try {
            fragment = (BasePreferenceFragment) Class.forName(pref.getFragment()).newInstance();
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "Class not found exception", e);
        } catch (IllegalAccessException e) {
            Log.d(TAG, "IllegalAccessException", e);
        } catch (InstantiationException e) {
            Log.d(TAG, "InstantiationException", e);
        }

        if (fragment == null) {
            // The fragment creation is not handled
            return false;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Bundle args = new Bundle();
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, caller.getTag());
        if (pref.getKey().equals(PrefsActivity.PREF_DISPLAY_SCREEN)) {
            args.putSerializable("langs", getLanguagesCourses());
            Log.d(TAG, "Langs added!");
        }
        fragment.setArguments(args);
        ft.replace(R.id.root_layout, fragment, fragment.getTag());
        ft.addToBackStack(caller.getTag());
        ft.commit();
        currentPrefScreen = (PreferenceChangedCallback) fragment;
        setTitle(pref.getTitle());

        return true;
    }
}
