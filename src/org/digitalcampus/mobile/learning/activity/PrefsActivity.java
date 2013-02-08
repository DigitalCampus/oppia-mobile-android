package org.digitalcampus.mobile.learning.activity;

import org.digitalcampus.mobile.learning.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class PrefsActivity extends PreferenceActivity {
	
	public static final String TAG = PrefsActivity.class.getSimpleName();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) { //
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.prefs); //
	}

	

}
