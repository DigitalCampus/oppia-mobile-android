/* 
 * This file is part of OppiaMobile - http://oppia-mobile.org/
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
import org.digitalcampus.oppia.utils.StorageUtils;
import org.digitalcampus.oppia.utils.StorageUtils.StorageInfo;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.util.Log;

public class PrefsActivity extends SherlockPreferenceActivity {
	
	public static final String TAG = PrefsActivity.class.getSimpleName();
	
	public static final String PREF_STORAGE_LOCATION = "prefStorageLocation";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs); 
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        
		ListPreference langsList = (ListPreference) findPreference("prefLanguage"); 
		
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
        
        
        
        ListPreference storageList = (ListPreference) findPreference(PrefsActivity.PREF_STORAGE_LOCATION);
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
        
        
        EditTextPreference username = (EditTextPreference) findPreference("prefUsername");
        if (username.getText().equals("")){
        	username.setSummary(R.string.about_not_logged_in);
        } else {
        	 username.setSummary(getString(R.string.about_logged_in,username.getText()));
        }
        
        EditTextPreference server = (EditTextPreference) findPreference("prefServer");
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
