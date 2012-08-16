package org.digitalcampus.mtrain.activity;

import org.digitalcampus.mtrain.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;

public class PrefsActivity extends PreferenceActivity {
	
	public static final String TAG = "PrefsActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) { //
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs); //
	}

	

}
