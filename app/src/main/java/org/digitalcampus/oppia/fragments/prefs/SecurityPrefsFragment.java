package org.digitalcampus.oppia.fragments.prefs;

import android.os.Bundle;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.application.App;

import java.util.Arrays;

import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;

public class SecurityPrefsFragment extends BasePreferenceFragment implements PreferenceChangedCallback{

    public static final String TAG = PrefsActivity.class.getSimpleName();

    public static SecurityPrefsFragment newInstance() {
        return new SecurityPrefsFragment();
    }

    public SecurityPrefsFragment(){
        // Required empty public constructor
        this.adminProtectedValues = Arrays.asList(PrefsActivity.PREF_ADMIN_PASSWORD);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from XML resources
        addPreferencesFromResource(R.xml.prefs_security);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        if (!App.ADMIN_PROTECT_SETTINGS){
            // If the whole settings activity is not protected by password, we need to protect admin settings
            protectAdminPreferences();
        }
    }

    private void protectAdminPreferences(){
        final CheckBoxPreference adminEnabled = findPreference(PrefsActivity.PREF_ADMIN_PROTECTION);
        adminEnabled.setOnPreferenceChangeListener((preference, newValue) -> {
            final Boolean enableProtection = (Boolean) newValue;
            if (Boolean.TRUE.equals(enableProtection)) {
                //If we are going to re-enable the preference, there is no need to prompt for the previous password
                return true;
            }
            AdminSecurityManager.with(getActivity()).promptAdminPassword(() -> {
                adminEnabled.setChecked(enableProtection);
                preference.getSharedPreferences().edit().putBoolean(preference.getKey(), enableProtection).apply();
            });
            return false;
        });

    }


    @Override
    public void onPreferenceUpdated(String pref, String newValue) {
        if (pref.equals(PrefsActivity.PREF_ADMIN_PASSWORD)){
            EditTextPreference passwordPref = findPreference(PrefsActivity.PREF_ADMIN_PASSWORD);
            passwordPref.setText(newValue);
        }
        else if (pref.equals(PrefsActivity.PREF_ADMIN_PROTECTION)){
            CheckBoxPreference adminEnabled = findPreference(PrefsActivity.PREF_ADMIN_PROTECTION);
            if (adminEnabled != null){
                adminEnabled.setChecked("true".equals(newValue));
            }
        }
    }
}
