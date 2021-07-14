package org.digitalcampus.oppia.fragments.prefs;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.utils.custom_prefs.AdminEditTextPreference;

import java.util.ArrayList;
import java.util.List;

public abstract class BasePreferenceFragment extends PreferenceFragmentCompat {

    protected List<String> adminProtectedValues = new ArrayList<>();
    protected SharedPreferences parentPrefs;

    public void setPrefs(SharedPreferences prefs) {
        this.parentPrefs = prefs;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        protectAdminEditTextPreferences();

    }

    void protectAdminEditTextPreferences() {
        for (String prefKey : adminProtectedValues) {

            final EditTextPreference editTextPreference = findPreference(prefKey);
            if (editTextPreference == null) {
                continue;
            }

            if (editTextPreference instanceof AdminEditTextPreference) {
                editTextPreference.setOnPreferenceClickListener(preference -> {

                    if (parentPrefs == null) {
                        parentPrefs = App.getPrefs(getActivity());
                    }

                    if (parentPrefs.getBoolean(PrefsActivity.PREF_ADMIN_PROTECTION, false)) {
                        AdminSecurityManager.with((Activity) getContext()).promptAdminPassword(() -> {
                            getPreferenceManager().showDialog(editTextPreference);
                        });
                        return true;
                    }

                    return false;
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
