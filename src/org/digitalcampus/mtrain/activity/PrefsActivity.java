package org.digitalcampus.mtrain.activity;

import org.digitalcampus.mtrain.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PrefsActivity extends PreferenceActivity {
	
	public static final String TAG = "PrefsActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) { //
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs); //
	}

	

}
