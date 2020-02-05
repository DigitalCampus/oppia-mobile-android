package org.digitalcampus.oppia.fragments.prefs;

import android.os.Bundle;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.application.App;

import java.util.ArrayList;
import java.util.List;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public abstract class AdminProtectedPreferenceFragment extends PreferenceFragmentCompat {

    protected List<String> adminProtectedValues = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        if (!App.ADMIN_PROTECT_SETTINGS){
            // If the whole settings activity is not protected by password, we need to protect admin settings
            protectAdminEditTextPreferences();
        }
    }

    void protectAdminEditTextPreferences() {

        for (String prefKey : adminProtectedValues) {

            final EditTextPreference editTextPreference = findPreference(prefKey);
            if (editTextPreference == null){
                continue;
            }
            editTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(final Preference preference, final Object newValue) {

                    if (!App.getPrefs(getActivity()).getBoolean(PrefsActivity.PREF_ADMIN_PROTECTION, false)) {
                        return true;
                    }

                    AdminSecurityManager.with(getActivity()).promptAdminPassword(new AdminSecurityManager.AuthListener() {
                        @Override
                        public void onPermissionGranted() {
                            editTextPreference.setText((String) newValue);
                            preference.getSharedPreferences().edit().putString(preference.getKey(), (String) newValue).apply();
                        }
                    });
                    return false;
                }
            });
        }
    }

}
