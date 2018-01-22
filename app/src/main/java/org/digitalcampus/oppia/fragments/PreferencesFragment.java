package org.digitalcampus.oppia.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Patterns;
import android.webkit.URLUtil;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.storage.StorageLocationInfo;
import org.digitalcampus.oppia.utils.storage.StorageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PreferencesFragment extends PreferenceFragment {

    private ListPreference storagePref;
    private EditTextPreference serverPref;

    public static PreferencesFragment newInstance() {
        return new PreferencesFragment();
    }

    public PreferencesFragment(){ }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs);

        storagePref = (ListPreference) findPreference(PrefsActivity.PREF_STORAGE_OPTION);
        serverPref = (EditTextPreference) findPreference(PrefsActivity.PREF_SERVER);
        serverPref.setSummary(serverPref.getText());
        serverPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String url = (String) newValue;
                if (!URLUtil.isNetworkUrl(url) || !Patterns.WEB_URL.matcher(url).matches()){
                    UIUtils.showAlert(getActivity(),
                            R.string.prefServer_errorTitle,
                            R.string.prefServer_errorDescription);
                    return false;
                }
                // If it is correct, we allow the change
                return true;
            }
        });

        MaxIntOnStringPreferenceListener maxIntListener = new MaxIntOnStringPreferenceListener();
        Preference connTimeout = findPreference(PrefsActivity.PREF_SERVER_TIMEOUT_CONN);
        Preference responseTimeout = findPreference(PrefsActivity.PREF_SERVER_TIMEOUT_RESP);
        connTimeout.setOnPreferenceChangeListener(maxIntListener);
        responseTimeout.setOnPreferenceChangeListener(maxIntListener);

        Bundle bundle = getArguments();
        ArrayList<Lang> langs = new ArrayList<>();
        if ((bundle != null) && bundle.getSerializable("langs") != null) {
	        langs = (ArrayList<Lang>) bundle.getSerializable("langs");
        }
        updateLangsList(langs);
        updateStorageList(this.getActivity());

        EditTextPreference username = (EditTextPreference) findPreference(PrefsActivity.PREF_USER_NAME);
        username.setSummary( username.getText().equals("") ?
                getString(R.string.about_not_logged_in) :
                getString(R.string.about_logged_in, username.getText()) );

    }

    public void updateServerPref(String url){
        serverPref.setText(url);
        serverPref.setSummary(url);
    }

    public void updateStoragePref(String storageOption){
        if (PrefsActivity.STORAGE_OPTION_EXTERNAL.equals(storageOption)){
            storagePref.setValue(storagePref.getEntryValues()[1].toString());
        }
        else{
            storagePref.setValue(storageOption);
        }
    }

    public void updateStorageList(Context ctx){

        List<StorageLocationInfo> storageLocations = StorageUtils.getStorageList(ctx);
        if (storageLocations.size() > 1){
            //If there is more than one storage option, we create a preferences list

            int writableLocations = 0;
            List<String> entries = new ArrayList<>();
            List<String> entryValues = new ArrayList<>();

            String currentLocation =  getPreferenceManager().getSharedPreferences().getString(PrefsActivity.PREF_STORAGE_LOCATION, "");
            String currentPath = "";

            entries.add(getString(R.string.prefStorageOptionInternal));
            entryValues.add(PrefsActivity.STORAGE_OPTION_INTERNAL);

            for (StorageLocationInfo storageLoc : storageLocations){
                //Only add it as an option if it is writable
                if (!storageLoc.readonly){
                    entries.add(storageLoc.getDisplayName(getActivity()));
                    entryValues.add(storageLoc.path);
                    writableLocations++;

                    if (currentLocation.startsWith(storageLoc.path)){
                        currentPath = storageLoc.path;
                    }
                }
            }

            //If there is only one writable location, we'll use the default prefsList
            if (writableLocations > 1){
                storagePref.setEntryValues(entryValues.toArray(new CharSequence[entryValues.size()]));
                storagePref.setEntries(entries.toArray(new CharSequence[entries.size()]));
                storagePref.setValue((currentPath.equals(""))? PrefsActivity.STORAGE_OPTION_INTERNAL : currentPath);
            }
        }

    }

    private void updateLangsList(ArrayList<Lang> langs){
        List<String> entries = new ArrayList<>();
        List<String> entryValues = new ArrayList<>();

        for(Lang l: langs){
            if(!entryValues.contains(l.getLang())){
                entryValues.add(l.getLang());
                Locale loc = new Locale(l.getLang());
                entries.add(loc.getDisplayLanguage(loc));
            }
        }

        ListPreference langsList = (ListPreference) findPreference(PrefsActivity.PREF_LANGUAGE);
        if (entryValues.size() > 0){
            langsList.setEntries( entries.toArray(new CharSequence[entries.size()]) );
            langsList.setEntryValues( entryValues.toArray(new CharSequence[entryValues.size()]) );
        }
        else{
            getPreferenceScreen().removePreference(langsList);
        }

    }

    public class MaxIntOnStringPreferenceListener implements Preference.OnPreferenceChangeListener{

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            boolean valid;
            try{
                String intValue = (String) newValue;
                valid = (intValue.length() <= 9); //it'll be bigger than int's max value
                int value = Integer.parseInt(intValue);
            }
            catch (NumberFormatException e){
                valid = false;
            }

            if (!valid){
                UIUtils.showAlert(getActivity(),
                        R.string.prefInt_errorTitle,
                        R.string.prefInt_errorDescription);
            }
            return valid;
        }
    }
}
