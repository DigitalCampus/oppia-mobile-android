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

import java.util.ArrayList;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.fragments.PreferencesFragment;
import org.digitalcampus.oppia.listener.MoveStorageListener;
import org.digitalcampus.oppia.task.ChangeStorageOptionTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.storage.FileUtils;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class PrefsActivity extends AppActivity implements SharedPreferences.OnSharedPreferenceChangeListener, MoveStorageListener {
	
	public static final String TAG = PrefsActivity.class.getSimpleName();
	
	public static final String PREF_STORAGE_LOCATION = "prefStorageLocation";
	
	public static final String PREF_USER_NAME = "prefUsername";
	public static final String PREF_SERVER = "prefServer";
	
	public static final String PREF_TRIGGER_POINTS_REFRESH = "prefScoreRefresh";
	public static final String PREF_SCORING_ENABLED = "prefScoringEnabled";
	public static final String PREF_BADGING_ENABLED = "prefBadgingEnabled";
	public static final String PREF_SERVER_TIMEOUT_CONN = "prefServerTimeoutConnection";
	public static final String PREF_SERVER_TIMEOUT_RESP = "prefServerTimeoutResponse";
	public static final String PREF_METADATA = "prefMetadata";
	public static final String PREF_BACKGROUND_DATA_CONNECT = "prefBackgroundDataConnect";
	
	/*
	 * Start personal prefs - move these to UserProps table
	 */
	public static final String PREF_PHONE_NO = "prefPhoneNo";
	public static final String PREF_LANGUAGE = "prefLanguage";
	public static final String PREF_SHOW_SCHEDULE_REMINDERS = "prefShowScheduleReminders";
	public static final String PREF_NO_SCHEDULE_REMINDERS = "prefNoScheduleReminders";
	public static final String PREF_SHOW_COURSE_DESC = "prefShowCourseDescription";
	public static final String PREF_SHOW_PROGRESS_BAR = "prefShowProgressBar";
	public static final String PREF_SHOW_SECTION_NOS = "prefShowSectionNumbers";
	public static final String PREF_HIGHLIGHT_COMPLETED = "prefHighlightCompleted";
	public static final String PREF_TEXT_SIZE = "prefTextSize";
	/*
	 * End personal prefs
	 */
	
	public static final String PREF_LAST_MEDIA_SCAN = "prefLastMediaScan";
	public static final String PREF_LOGOUT_ENABLED = "prefLogoutEnabled";
	public static final String PREF_DELETE_COURSE_ENABLED = "prefDeleteCourseEnabled";
	public static final String PREF_DOWNLOAD_VIA_CELLULAR_ENABLED = "prefDownloadViaCellularEnabled";

    public static final String PREF_STORAGE_OPTION = "prefStorageOption";
    public static final String STORAGE_OPTION_INTERNAL = "internal";
    public static final String STORAGE_OPTION_EXTERNAL = "external";

    public static final String PREF_ADMIN_PROTECTION = "prefAdminProtection";
    public static final String PREF_ADMIN_PASSWORD = "prefAdminPassword";

    private SharedPreferences prefs;
    private ProgressDialog pDialog;
    private PreferencesFragment mPrefsFragment;


	@Override
	protected void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        FragmentManager mFragmentManager = getFragmentManager();
        FragmentTransaction mFragmentTransaction = mFragmentManager.beginTransaction();
        mPrefsFragment = PreferencesFragment.newInstance();
        mFragmentTransaction.replace(android.R.id.content, mPrefsFragment);
        mFragmentTransaction.commit();

        Bundle bundle = this.getIntent().getExtras();
        if(bundle != null) { mPrefsFragment.setArguments(bundle); }

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
    protected void onResume() {
        super.onResume();
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "Preference changed: " + key);
        if (key.equalsIgnoreCase(PREF_STORAGE_OPTION)) {
            String currentLocation = sharedPreferences.getString(PrefsActivity.PREF_STORAGE_LOCATION, "");
            String storageOption   = sharedPreferences.getString(PrefsActivity.PREF_STORAGE_OPTION, "");
            String path = null;

            Log.d(TAG, "Storage option selected: " + storageOption);

            if ((!storageOption.equals(STORAGE_OPTION_EXTERNAL)) &&
                (!storageOption.equals(STORAGE_OPTION_INTERNAL))){
                //The option selected is a path
                path = storageOption;
                storageOption = STORAGE_OPTION_EXTERNAL;
            }
            if (
                //The storage option is different from the current one
                (!storageOption.equals(FileUtils.getStorageStrategy().getStorageType())) ||
                //The storage is set to external, and is a different path
                ((path != null) && !currentLocation.startsWith(path))
            ){
                ArrayList<Object> data = new ArrayList<Object>();
                data.add(storageOption);
                if (path != null){ data.add(path); }
                Payload p = new Payload(data);
                ChangeStorageOptionTask changeStorageTask = new ChangeStorageOptionTask(PrefsActivity.this.getApplicationContext());
                changeStorageTask.setMoveStorageListener(this);

                pDialog = new ProgressDialog(this);
                pDialog.setTitle(R.string.loading);
                pDialog.setMessage(getString(R.string.moving_storage_location));
                pDialog.setCancelable(false);
                pDialog.show();

                changeStorageTask.execute(p);
            }
        }
        else if (key.equalsIgnoreCase(PREF_ADMIN_PROTECTION)){
            boolean protectionEnabled = sharedPreferences.getBoolean(PrefsActivity.PREF_ADMIN_PROTECTION, false);
            if (protectionEnabled){
                Log.d(TAG, "Admin protection enabled, prompting for new password");
                final EditText passwordInput = new EditText(this);
                final AlertDialog passwordDialog = new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.admin_password_newpassword_dialog_title))
                    .setMessage(getString(R.string.admin_password_newpassword_dialog_message))
                    .setView(passwordInput)
                    .setPositiveButton(R.string.ok, null)
                    .create();
                passwordDialog.show();
                passwordDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String password = passwordInput.getText().toString();
                        if (!password.equals("")) { passwordDialog.dismiss(); }
                    }
                });
                passwordDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        String password = passwordInput.getText().toString();
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        if (password.equals("")) {
                            disableAdminProtection(sharedPreferences);
                        }
                        else{
                            //Update the password preference
                            editor.putString(PrefsActivity.PREF_ADMIN_PASSWORD, password);
                            editor.commit();
                            //Update the UI value of the PreferencesFragment
                            EditTextPreference passwordPref = (EditTextPreference) mPrefsFragment.findPreference(PREF_ADMIN_PASSWORD);
                            passwordPref.setText(password);
                        }

                    }
                });
            }
        }
        else if (key.equalsIgnoreCase(PREF_ADMIN_PASSWORD)){
            String newPassword = sharedPreferences.getString(PrefsActivity.PREF_ADMIN_PASSWORD, "");
            if (newPassword.equals("")){
                //If the user introduced an empty password, disable the password protection
                disableAdminProtection(sharedPreferences);
            }
        }
    }

    private void disableAdminProtection(SharedPreferences prefs){
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PrefsActivity.PREF_ADMIN_PROTECTION, false);
        editor.commit();
        //Update the UI value of the PreferencesFragment
        CheckBoxPreference passwordPref = (CheckBoxPreference) mPrefsFragment.findPreference(PREF_ADMIN_PROTECTION);
        passwordPref.setChecked(false);
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
            mPrefsFragment.updateStoragePref(storageOption);
        }

        //Finally, to handle the possibility that is in an inconsistent state
        if (!storageOption.equals(STORAGE_OPTION_INTERNAL)){
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PrefsActivity.PREF_STORAGE_OPTION, STORAGE_OPTION_EXTERNAL);
            editor.commit();
        }

    }

    //@Override
    public void moveStorageProgressUpdate(String s) { }

}
