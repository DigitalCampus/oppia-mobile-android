package org.digitalcampus.oppia.fragments.prefs;

import android.os.Bundle;

import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.application.App;

import java.util.ArrayList;
import java.util.List;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public abstract class BasePreferenceFragment extends PreferenceFragmentCompat {

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
            editTextPreference.setOnPreferenceChangeListener((preference, newValue) -> {

                if (!App.getPrefs(getActivity()).getBoolean(PrefsActivity.PREF_ADMIN_PROTECTION, false)) {
                    return true;
                }

                AdminSecurityManager.with(getActivity()).promptAdminPassword(() -> {
                    editTextPreference.setText((String) newValue);
                    preference.getSharedPreferences().edit().putString(preference.getKey(), (String) newValue).apply();
                });
                return false;
            });
        }
    }

    void liveUpdateSummary(String prefKey){
        liveUpdateSummary(prefKey, "");
    }

    void liveUpdateSummary(String prefKey, final String append){

        Preference pref = findPreference(prefKey);
        if (pref instanceof ListPreference){
            final ListPreference listPref = (ListPreference) pref;
            listPref.setSummary(listPref.getEntry() + append);
            listPref.setOnPreferenceChangeListener((preference, newValue) -> {
                CharSequence[] entryValues = listPref.getEntryValues();
                for (int i=0; i< entryValues.length; i++){
                    if (entryValues[i].equals(newValue)){
                        listPref.setSummary(listPref.getEntries()[i] + append);
                        break;
                    }
                }
                return true;
            });
        }
        else if (pref instanceof EditTextPreference){
            final EditTextPreference editPref = (EditTextPreference) pref;
            editPref.setSummary(editPref.getText() + append) ;
            editPref.setOnPreferenceChangeListener((preference, newValue) -> {
                editPref.setText(newValue + append);
                return true;
            });
        }

    }

}
