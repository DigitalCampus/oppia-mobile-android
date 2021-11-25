package org.digitalcampus.oppia.fragments.prefs;

import android.os.Bundle;
import android.text.InputType;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.utils.custom_prefs.AdminCheckBoxPreference;

import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;

public class SecurityPrefsFragment extends BasePreferenceFragment implements PreferenceChangedCallback {

    public static final String TAG = PrefsActivity.class.getSimpleName();

    public static SecurityPrefsFragment newInstance() {
        return new SecurityPrefsFragment();
    }

    public SecurityPrefsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from XML resources
        addPreferencesFromResource(R.xml.prefs_security);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        configureAdminPreferences();

        EditTextPreference adminPassPref = findPreference(PrefsActivity.PREF_ADMIN_PASSWORD);
        adminPassPref.setOnBindEditTextListener(editText -> {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        });
    }

    private void configureAdminPreferences() {
        final AdminCheckBoxPreference adminEnabled = findPreference(PrefsActivity.PREF_ADMIN_PROTECTION);
        adminEnabled.setPromptAdminDialogMode(AdminCheckBoxPreference.PromptAdminDialogMode.ON_UNCHECK);

    }


    @Override
    public void onPreferenceUpdated(String pref, String newValue) {
        if (pref.equals(PrefsActivity.PREF_ADMIN_PASSWORD)) {
            EditTextPreference passwordPref = findPreference(PrefsActivity.PREF_ADMIN_PASSWORD);
            passwordPref.setText(newValue);
        } else if (pref.equals(PrefsActivity.PREF_ADMIN_PROTECTION)) {
            CheckBoxPreference adminEnabled = findPreference(PrefsActivity.PREF_ADMIN_PROTECTION);
            if (adminEnabled != null) {
                adminEnabled.setChecked("true".equals(newValue));
            }
        }
    }
}
