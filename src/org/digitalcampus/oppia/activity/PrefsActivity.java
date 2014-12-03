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
import org.digitalcampus.oppia.model.Lang;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class PrefsActivity extends SherlockPreferenceActivity {
	
	public static final String TAG = PrefsActivity.class.getSimpleName();
	
	public static final String PREF_STORAGE_LOCATION = "prefStorageLocation";
	public static final String PREF_LANGUAGE = "prefLanguage";
	public static final String PREF_USER_NAME = "prefUsername";
	public static final String PREF_API_KEY = "prefApiKey";
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs); 
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        
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
        
        /*ListPreference storageList = (ListPreference) findPreference(PrefsActivity.PREF_STORAGE_LOCATION);
        List<StorageInfo> storageOptionsList = StorageUtils.getStorageList();
        
        List<String> storageEntries = new ArrayList<String>();
	    List<String> storageEntryValues = new ArrayList<String>();
	    
	    
	    storageEntryValues.add(Environment.getExternalStorageDirectory().getPath());
	    storageEntries.add(getString(R.string.storage_default));
	    Log.d(TAG,Environment.getExternalStorageDirectory().getPath());
	    
        for (StorageInfo temp : storageOptionsList) {
    		Log.d(TAG,temp.getDisplayName());
    		Log.d(TAG,temp.path);
    		storageEntryValues.add(temp.path);
    		storageEntries.add(temp.getDisplayName());
    	}
        final CharSequence[] storageEntryCharSeq = storageEntries.toArray(new CharSequence[storageEntries.size()]);
        final CharSequence[] storageEntryValsChar = storageEntryValues.toArray(new CharSequence[storageEntryValues.size()]);
        storageList.setEntries(storageEntryCharSeq);
        storageList.setEntryValues(storageEntryValsChar);
        
        */
        
        EditTextPreference username = (EditTextPreference) findPreference(PrefsActivity.PREF_USER_NAME);
        if (username.getText().equals("")){
        	username.setSummary(R.string.about_not_logged_in);
        } else {
        	 username.setSummary(getString(R.string.about_logged_in,username.getText()));
        }
        
        EditTextPreference server = (EditTextPreference) findPreference(PrefsActivity.PREF_SERVER);
        server.setSummary(server.getText());
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
	

}
