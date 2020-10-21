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

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.fragments.prefs.MainPreferencesFragment;
import org.digitalcampus.oppia.fragments.prefs.PreferenceChangedCallback;
import org.digitalcampus.oppia.listener.MoveStorageListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.task.ChangeStorageOptionTask;
import org.digitalcampus.oppia.task.FetchServerInfoTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.storage.ExternalStorageStrategy;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategy;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategyFactory;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class PrefsActivity extends AppActivity implements SharedPreferences.OnSharedPreferenceChangeListener,
        MoveStorageListener, PreferenceFragmentCompat.OnPreferenceStartFragmentCallback{
	
	public static final String PREF_STORAGE_LOCATION = "prefStorageLocation";

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
    public static final String PREF_METADATA_DEVICE_ID = "DEVICE_ID";
    public static final String PREF_METADATA_SIM_SERIAL = "SIM_SERIAL";
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

	/*
	 * End personal prefs
	 */
	public static final String PREF_LAST_MEDIA_SCAN = "prefLastMediaScan";
	public static final String PREF_LOGOUT_ENABLED = "prefLogoutEnabled";
    public static final String PREF_DOWNLOAD_ENABLED = "prefDownloadEnabled";
    public static final String PREF_CHANGE_LANGUAGE_ENABLED = "prefChangeLanguageEnabled";
	public static final String PREF_DELETE_COURSE_ENABLED = "prefDeleteCourseEnabled";
	public static final String PREF_DOWNLOAD_VIA_CELLULAR_ENABLED = "prefDownloadViaCellularEnabled";

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
    public static final String PREF_DATA_ROOM_MIGRATON_VERSION= "prefDataRoomMigrationVersion";
    public static final String PREF_DISPLAY_SCREEN = "prefDisplay";
    public static final String PREF_SECURITY_SCREEN = "prefSecurity";
    public static final String PREF_NOTIFICATIONS_SCREEN = "prefNotifications";
    public static final String PREF_ADVANCED_SCREEN = "prefsAdvanced";

    private ProgressDialog pDialog;
    private PreferenceChangedCallback currentPrefScreen;

    @Inject
    CoursesRepository coursesRepository;
    private FetchServerInfoTask fetchServerInfoTask;

    @Override
    public void onStart() {
        super.onStart();
        initialize();
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        getAppComponent().inject(this);

        if (savedInstanceState == null) {
            // Create the fragment only when the activity is created for the first time.
            // ie. not after orientation changes
            MainPreferencesFragment mPrefsFragment = (MainPreferencesFragment) getSupportFragmentManager().findFragmentByTag(MainPreferencesFragment.FRAGMENT_TAG);
            if (mPrefsFragment == null) {
                mPrefsFragment = MainPreferencesFragment.newInstance();
                Bundle bundle = this.getIntent().getExtras();
                if(bundle != null) {
                    mPrefsFragment.setArguments(bundle);
                }
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.root_layout, mPrefsFragment, MainPreferencesFragment.FRAGMENT_TAG)
                    .commit();
        }

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
            this.onBackPressed();
            return true;
        } else {
            return false;
        }
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

        if(key.equalsIgnoreCase(PrefsActivity.PREF_SERVER)){
            checkUpdatedServer(sharedPreferences);
        }
        else if (key.equalsIgnoreCase(PREF_STORAGE_OPTION)) {
            checkStorageOption(sharedPreferences);
        }
        else if (key.equalsIgnoreCase(PREF_ADMIN_PROTECTION)){
            checkAdminProtection(sharedPreferences);
        }
        else if (key.equalsIgnoreCase(PREF_ADMIN_PASSWORD)){
            String newPassword = sharedPreferences.getString(PrefsActivity.PREF_ADMIN_PASSWORD, "");
            if (TextUtils.equals(newPassword, "")){
                //If the user introduced an empty password, disable the password protection
                disableAdminProtection(sharedPreferences);
            }
        }
    }

    private void checkUpdatedServer(SharedPreferences sharedPreferences){
        String newServerURL = sharedPreferences.getString(PrefsActivity.PREF_SERVER, "").trim();
        if(!newServerURL.endsWith("/")){
            newServerURL = newServerURL +"/";
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
                if (currentPrefScreen != null){
                    currentPrefScreen.onPreferenceUpdated(PrefsActivity.PREF_SERVER, null);
                }
            }

            @Override
            public void onValidServer(String version, String name) {
                if (currentPrefScreen != null){
                    currentPrefScreen.onPreferenceUpdated(PrefsActivity.PREF_SERVER, null);
                }
            }

            @Override
            public void onUnchecked() {
                if (currentPrefScreen != null){
                    currentPrefScreen.onPreferenceUpdated(PrefsActivity.PREF_SERVER, null);
                }
            }
        });
        fetchServerInfoTask.execute();

        if (currentPrefScreen != null){
            currentPrefScreen.onPreferenceUpdated(PrefsActivity.PREF_SERVER, null);
        }
    }

    private void checkStorageOption(SharedPreferences sharedPreferences){
        String currentLocation = sharedPreferences.getString(PrefsActivity.PREF_STORAGE_LOCATION, "");
        String storageOption   = sharedPreferences.getString(PrefsActivity.PREF_STORAGE_OPTION, "");
        String path = null;
        boolean needsUserPermission = false;

        Log.d(TAG, "Storage option selected: " + storageOption);

        if ((!storageOption.equals(STORAGE_OPTION_EXTERNAL)) &&
                (!storageOption.equals(STORAGE_OPTION_INTERNAL))){
            //The option selected is a path
            path = storageOption;
            storageOption = STORAGE_OPTION_EXTERNAL;
            needsUserPermission = ExternalStorageStrategy.needsUserPermissions(this, path);
        }
        if (
            //The storage option is different from the current one
                (!storageOption.equals(Storage.getStorageStrategy().getStorageType())) ||
                        //The storage is set to external, and is a different path
                        ((path != null) && !currentLocation.startsWith(path))
        ) {
            return;
        }

        StorageAccessStrategy newStrategy = StorageAccessStrategyFactory.createStrategy(storageOption);
        if (needsUserPermission){
            Log.d(TAG, "Asking user for storage permissions");
            final String finalStorageOption = storageOption;
            final String finalPath = path;
            newStrategy.askUserPermissions(this, isGranted -> {
                Log.d(TAG, "Access granted for storage: " + isGranted);
                if (isGranted){
                    executeChangeStorageTask(finalPath, finalStorageOption);
                }
                else{
                    Toast.makeText(PrefsActivity.this, getString(R.string.storageAccessNotGranted), Toast.LENGTH_LONG).show();
                    //If the user didn't grant access, we revert the preference selection
                    String currentStorageOpt = Storage.getStorageStrategy().getStorageType();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString(PrefsActivity.PREF_STORAGE_OPTION, currentStorageOpt).apply();
                    if (currentPrefScreen != null){
                        currentPrefScreen.onPreferenceUpdated(PrefsActivity.PREF_STORAGE_OPTION, currentStorageOpt);
                    }
                }

            });
        } else {
            executeChangeStorageTask(path, storageOption);
        }

    }

    private void checkAdminProtection(SharedPreferences sharedPreferences){
        boolean protectionEnabled = sharedPreferences.getBoolean(PrefsActivity.PREF_ADMIN_PROTECTION, false);
        if (protectionEnabled){
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
                if (!password.equals("")) { passwordDialog.dismiss(); }
            });
            passwordDialog.setOnDismissListener(dialog -> {
                String password = passwordInput.getText().toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (password.equals("")) {
                    disableAdminProtection(sharedPreferences);
                }
                else{
                    //Update the password preference
                    editor.putString(PrefsActivity.PREF_ADMIN_PASSWORD, password).apply();
                    //Update the UI value of the fragment
                    currentPrefScreen.onPreferenceUpdated(PREF_ADMIN_PASSWORD, password);
                }
            });
        }
    }

    private void disableAdminProtection(SharedPreferences prefs){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PrefsActivity.PREF_ADMIN_PROTECTION, false).apply();
        //Update the UI value of the MainPreferencesFragment
        currentPrefScreen.onPreferenceUpdated(PREF_ADMIN_PROTECTION, "false");
    }

    private void executeChangeStorageTask(String path, String storageOption){
        ArrayList<Object> data = new ArrayList<>();
        data.add(storageOption);
        if (path != null){ data.add(path); }
        Payload p = new Payload(data);
        ChangeStorageOptionTask changeStorageTask = new ChangeStorageOptionTask(PrefsActivity.this.getApplicationContext());
        changeStorageTask.setMoveStorageListener(this);

        pDialog = new ProgressDialog(this, R.style.Oppia_AlertDialogStyle);
        pDialog.setTitle(R.string.loading);
        pDialog.setMessage(getString(R.string.moving_storage_location));
        pDialog.setCancelable(false);
        pDialog.show();

        changeStorageTask.execute(p);
    }

    //@Override
    public void moveStorageComplete(Payload p) {
        pDialog.dismiss();

        String storageOption = prefs.getString(PREF_STORAGE_OPTION, "");
        if (p.isResult()){
            Log.d(TAG, "Move storage completed!");
            Toast.makeText(this, this.getString(R.string.move_storage_completed), Toast.LENGTH_LONG).show();
        }
        else{
            Log.d(TAG, "Move storage failed:" + p.getResultResponse());
            UIUtils.showAlert(this, R.string.error, p.getResultResponse());
            //We set the actual storage option (remove the one set by the user)
            if (currentPrefScreen != null){
                currentPrefScreen.onPreferenceUpdated(PrefsActivity.PREF_STORAGE_OPTION, storageOption);
            }
        }

        //Finally, to handle the possibility that is in an inconsistent state
        if (!TextUtils.equals(storageOption, STORAGE_OPTION_INTERNAL)){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PrefsActivity.PREF_STORAGE_OPTION, STORAGE_OPTION_EXTERNAL).apply();
        }
    }

    //@Override
    public void moveStorageProgressUpdate(String s) {
        // no need to show storage progress in this activity
    }

    @Override
    public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
        PreferenceFragmentCompat fragment = null;
        try {
            fragment = (PreferenceFragmentCompat) Class.forName(pref.getFragment()).newInstance();
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "Class not found exception", e);
        } catch (IllegalAccessException e) {
            Log.d(TAG, "IllegalAccessException", e);
        } catch (InstantiationException e) {
            Log.d(TAG, "InstantiationException", e);
        }

        if (fragment == null){
            // The fragment creation is not handled
            return false;
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Bundle args = new Bundle();
        args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, caller.getTag());
        if (pref.getKey().equals(PrefsActivity.PREF_DISPLAY_SCREEN)){
            args.putSerializable("langs", getLanguagesCourses());
            Log.d(TAG, "Langs added!");
        }
        fragment.setArguments(args);
        ft.replace(R.id.root_layout, fragment, fragment.getTag());
        ft.addToBackStack( caller.getTag());
        ft.commit();
        currentPrefScreen = (PreferenceChangedCallback) fragment;
        setTitle(pref.getTitle());

        return true;
    }
}
