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
import java.util.List;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.fragments.PreferencesFragment;
import org.digitalcampus.oppia.listener.MoveStorageListener;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.task.ChangeStorageOptionTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.storage.FileUtils;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class PrefsActivity extends AppActivity implements SharedPreferences.OnSharedPreferenceChangeListener, MoveStorageListener {
	
	public static final String TAG = PrefsActivity.class.getSimpleName();
	
	public static final String PREF_STORAGE_LOCATION = "prefStorageLocation";
	public static final String PREF_LANGUAGE = "prefLanguage";
	public static final String PREF_USER_NAME = "prefUsername";
	public static final String PREF_API_KEY = "prefApiKey";
	public static final String PREF_PHONE_NO = "prefPhoneNo";
	public static final String PREF_SERVER = "prefServer";
	public static final String PREF_BADGES = "prefBadges";
	public static final String PREF_POINTS = "prefPoints";
	public static final String PREF_COURSE_POINTS = "prefCoursePoints";
	public static final String PREF_SCORING_ENABLED = "prefScoringEnabled";
	public static final String PREF_BADGING_ENABLED = "prefBadgingEnabled";
	public static final String PREF_SHOW_SCHEDULE_REMINDERS = "prefShowScheduleReminders";
	public static final String PREF_NO_SCHEDULE_REMINDERS = "prefNoScheduleReminders";
	public static final String PREF_LAST_MEDIA_SCAN = "prefLastMediaScan";
	public static final String PREF_SHOW_COURSE_DESC = "prefShowCourseDescription";
	public static final String PREF_SHOW_PROGRESS_BAR = "prefShowProgressBar";
	public static final String PREF_SHOW_SECTION_NOS = "prefShowSectionNumbers";
	public static final String PREF_HIGHLIGHT_COMPLETED = "prefHighlightCompleted";
	public static final String PREF_TEXT_SIZE = "prefTextSize";
	public static final String PREF_SERVER_TIMEOUT_CONN = "prefServerTimeoutConnection";
	public static final String PREF_SERVER_TIMEOUT_RESP = "prefServerTimeoutResponse";
	public static final String PREF_METADATA = "prefMetadata";
	public static final String PREF_BACKGROUND_DATA_CONNECT = "prefBackgroundDataConnect";

	public static final String PREF_LOGOUT_ENABLED = "prefLogoutEnabled";
	public static final String PREF_DELETE_COURSE_ENABLED = "prefDeleteCourseEnabled";
	public static final String PREF_DOWNLOAD_VIA_CELLULAR_ENABLED = "prefDownloadViaCellularEnabled";

    public static final String PREF_STORAGE_OPTION = "prefStorageOption";
    public static final String STORAGE_OPTION_INTERNAL = "internal";
    public static final String STORAGE_OPTION_EXTERNAL = "external";

    private SharedPreferences prefs;
    private ProgressDialog pDialog;
    private PreferencesFragment mPrefsFragment;


	@Override
	protected void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

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

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "Preference changed: " + key);
        if (key.equalsIgnoreCase(PREF_STORAGE_OPTION)) {
            String currentStorage = FileUtils.getStorageStrategy().getStorageType();
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
    public void moveStorageProgressUpdate(String s) {

    }

}
