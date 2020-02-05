package org.digitalcampus.oppia.fragments.prefs;

import android.os.Bundle;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;

public class DisplayPrefsFragment extends BasePreferenceFragment implements PreferenceChangedCallback{

    public static final String TAG = PrefsActivity.class.getSimpleName();
    private ListPreference storagePref;
    private EditTextPreference serverPref;

    public static DisplayPrefsFragment newInstance() {
        return new DisplayPrefsFragment();
    }

    public DisplayPrefsFragment(){
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from XML resources
        addPreferencesFromResource(R.xml.prefs_display);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        liveUpdateSummary(PrefsActivity.PREF_LANGUAGE);
        liveUpdateSummary(PrefsActivity.PREF_TEXT_SIZE);
    }

    @Override
    public void onPreferenceUpdated(String pref, String newValue) {

    }
}
