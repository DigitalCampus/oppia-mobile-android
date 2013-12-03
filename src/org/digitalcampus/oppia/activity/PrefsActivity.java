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

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class PrefsActivity extends PreferenceActivity {
	
	public static final String TAG = PrefsActivity.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs); 
        
		ListPreference langsList = (ListPreference) findPreference(getString(R.string.prefs_language)); 
		
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
	}

	

}
