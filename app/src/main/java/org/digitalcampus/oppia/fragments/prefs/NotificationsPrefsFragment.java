package org.digitalcampus.oppia.fragments.prefs;

import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.utils.custom_prefs.DayWeekTimePreference;
import org.digitalcampus.oppia.utils.custom_prefs.DayWeekTimePreferenceDialogFragment;

public class NotificationsPrefsFragment extends BasePreferenceFragment implements PreferenceChangedCallback{

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

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        liveUpdateSummary(PrefsActivity.PREF_GAMIFICATION_POINTS_ANIMATION);

    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof DayWeekTimePreference) {
            DialogFragment f = DayWeekTimePreferenceDialogFragment.newInstance(preference.getKey());
            f.setTargetFragment(this, 0);
            f.show(getParentFragmentManager(), null);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onPreferenceUpdated(String pref, String newValue) {
        // do nothing
    }
}
