package org.digitalcampus.oppia.fragments.prefs;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;

import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.utils.custom_prefs.AdminPreference;

public abstract class BasePreferenceFragment extends PreferenceFragmentCompat {

    private AdminSecurityManager adminSecurityManager;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adminSecurityManager = AdminSecurityManager.with(getActivity());

        protectAdminPreferences(getPreferenceScreen());

    }

    private void protectAdminPreferences(PreferenceGroup preferenceGroup) {
        for (int i = 0; i < preferenceGroup.getPreferenceCount(); i++) {
            Preference preference = preferenceGroup.getPreference(i);
            if (preference instanceof PreferenceCategory || preference instanceof PreferenceScreen) {
                protectAdminPreferences((PreferenceGroup) preference);
            }

            if (preference instanceof AdminPreference && adminSecurityManager.isPreferenceProtected(preference.getKey())) {
                preference.setOnPreferenceClickListener(adminPref -> {
                    adminSecurityManager.promptAdminPassword(() -> ((AdminPreference) preference).onAccessGranted());
                    return true;
                });

            }
        }
    }


    protected boolean onPreferenceChangedDelegate(Preference preference, Object newValue) {
        return true;
    }

    void liveUpdateSummary(String prefKey) {
        liveUpdateSummary(prefKey, "");
    }

    void liveUpdateSummary(String prefKey, final String append) {

        Preference pref = findPreference(prefKey);
        if (pref instanceof ListPreference) {
            final ListPreference listPref = (ListPreference) pref;
            listPref.setSummary(listPref.getEntry() + append);
            listPref.setOnPreferenceChangeListener((preference, newValue) -> {
                CharSequence[] entryValues = listPref.getEntryValues();
                for (int i = 0; i < entryValues.length; i++) {
                    if (entryValues[i].equals(newValue)) {
                        listPref.setSummary(listPref.getEntries()[i] + append);
                        break;
                    }
                }
                return true;
            });
        } else if (pref instanceof EditTextPreference) {
            final EditTextPreference editPref = (EditTextPreference) pref;
            editPref.setSummary(editPref.getText() + append);
            editPref.setOnPreferenceChangeListener((preference, newValue) -> {

                boolean mustUpdate = onPreferenceChangedDelegate(preference, newValue);
                if (!mustUpdate) {
                    return false;
                }

                editPref.setSummary(newValue + append);
                return true;
            });
        }

    }

}
