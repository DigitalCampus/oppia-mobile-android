package org.digitalcampus.oppia.fragments.prefs;

import android.os.Bundle;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;

import androidx.preference.EditTextPreference;

public class MainPreferencesFragment extends BasePreferenceFragment {

    public static final String TAG = PrefsActivity.class.getSimpleName();
    public static final String FRAGMENT_TAG = "main_prefs_fragment";

    public static MainPreferencesFragment newInstance() {
        return new MainPreferencesFragment();
    }

    public MainPreferencesFragment(){
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        EditTextPreference info = findPreference(PrefsActivity.PREF_INFO);

        info.setTitle(getString(R.string.prefCurrentVersion, BuildConfig.VERSION_NAME));
        info.setSummary(BuildConfig.APPLICATION_ID);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()){
            getActivity().setTitle(R.string.menu_settings);
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from XML resources
        setPreferencesFromResource(R.xml.prefs_main, rootKey);
    }

}
