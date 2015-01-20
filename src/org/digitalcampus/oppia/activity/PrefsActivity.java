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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.listener.MoveStorageListener;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.task.ChangeStorageOptionTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategy;
import org.digitalcampus.oppia.utils.storage.StorageAccessStrategyFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PrefsActivity extends SherlockPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener, MoveStorageListener {
	
	public static final String TAG = PrefsActivity.class.getSimpleName();
	
	public static final String PREF_STORAGE_LOCATION = "prefStorageLocation";
	public static final String PREF_LANGUAGE = "prefLanguage";
	public static final String PREF_USER_NAME = "prefUsername";
	public static final String PREF_API_KEY = "prefApiKey";
	public static final String PREF_PHONE_NO = "prefPhoneNo";
	public static final String PREF_SERVER = "prefServer";
	public static final String PREF_BADGES = "prefBadges";
	public static final String PREF_POINTS = "prefPoints";
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



    private ListPreference storagePref;
    private SharedPreferences prefs;
    private ProgressDialog pDialog;


	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs); 
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
		ListPreference langsList = (ListPreference) findPreference(PrefsActivity.PREF_LANGUAGE); 
		
		List<String> entries = new ArrayList<String>();
	    List<String> entryValues = new ArrayList<String>();
	    
	    Bundle bundle = this.getIntent().getExtras(); 
        if(bundle != null) {
        	@SuppressWarnings("unchecked")
			ArrayList<Lang> langs = (ArrayList<Lang>) bundle.getSerializable("langs");
        	for(Lang l: langs){
        		if(!entryValues.contains(l.getLang())){
	        		entryValues.add(l.getLang());
	        		Locale loc = new Locale(l.getLang());
	        		entries.add(loc.getDisplayLanguage(loc));
        		}
        	}
        }
        
        final CharSequence[] entryCharSeq = entries.toArray(new CharSequence[entries.size()]);
        final CharSequence[] entryValsChar = entryValues.toArray(new CharSequence[entryValues.size()]);
        
        langsList.setEntries(entryCharSeq);
        langsList.setEntryValues(entryValsChar);

        EditTextPreference username = (EditTextPreference) findPreference(PrefsActivity.PREF_USER_NAME);
        if (username.getText().equals("")){
        	username.setSummary(R.string.about_not_logged_in);
        } else {
        	 username.setSummary(getString(R.string.about_logged_in,username.getText()));
        }
        
        EditTextPreference server = (EditTextPreference) findPreference(PrefsActivity.PREF_SERVER);
        server.setSummary(server.getText());

        storagePref = (ListPreference) findPreference(PrefsActivity.PREF_STORAGE_OPTION);

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
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "Preference changed: " + key);

        if (key.equalsIgnoreCase(PrefsActivity.PREF_STORAGE_OPTION)) {
            Preference storagePref = findPreference(key);

            String storageOption = sharedPreferences.getString(PrefsActivity.PREF_STORAGE_OPTION, "");
            Log.d(TAG, "Storage option selected: " + storageOption);
            if (!storageOption.equals(FileUtils.getStorageStrategy().getStorageType())){

                ArrayList<Object> data = new ArrayList<Object>();
                data.add(storageOption);
                Payload p = new Payload(data);
                ChangeStorageOptionTask changeStorageTask = new ChangeStorageOptionTask(PrefsActivity.this.getApplicationContext());
                changeStorageTask.setMoveStorageListener(this);

                pDialog = new ProgressDialog(this);
                pDialog.setTitle(R.string.loading);
                pDialog.setMessage(getString(R.string.loading));
                pDialog.setCancelable(false);
                pDialog.show();

                changeStorageTask.execute(p);
            }
        }
    }

    //@Override
    public void moveStorageComplete(Payload p) {
        pDialog.dismiss();

        if (p.isResult()){
            Log.d(TAG, "Move storage completed!");
            Toast.makeText(this, this.getString(R.string.move_storage_completed), Toast.LENGTH_LONG).show();
        }
        else{
            Log.d(TAG, "Move storage failed:" + p.getResultResponse());
            UIUtils.showAlert(this, R.string.error, p.getResultResponse());

            //We set the actual storage option (remove the one set by the user)
            String storageOption = prefs.getString(PrefsActivity.PREF_STORAGE_OPTION, "");
            storagePref.setValue(storageOption);
        }

    }

    //@Override
    public void moveStorageProgressUpdate(String s) {

    }
}
