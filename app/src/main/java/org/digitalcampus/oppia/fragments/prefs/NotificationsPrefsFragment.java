package org.digitalcampus.oppia.fragments.prefs;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;

import androidx.preference.PreferenceFragmentCompat;

public class NotificationsPrefsFragment extends PreferenceFragmentCompat {

    public static final String TAG = PrefsActivity.class.getSimpleName();

    public static NotificationsPrefsFragment newInstance() {
        return new NotificationsPrefsFragment();
    }

    public NotificationsPrefsFragment(){
        // Required empty public constructor
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from XML resources
        addPreferencesFromResource(R.xml.prefs_notifications);
    }

}
