package org.digitalcampus.oppia.fragments;

import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.utils.storage.StorageLocationInfo;
import org.digitalcampus.oppia.utils.storage.StorageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PreferencesFragment extends PreferenceFragment {

    private ListPreference storagePref;

    public static PreferencesFragment newInstance() {
        return new PreferencesFragment();
    }

    public PreferencesFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prefs);

        storagePref = (ListPreference) findPreference(PrefsActivity.PREF_STORAGE_OPTION);

        Bundle bundle = getArguments();

        ArrayList<Lang> langs = new ArrayList<>();
        if (bundle != null && bundle.getSerializable("langs") != null) {
	        langs = (ArrayList<Lang>) bundle.getSerializable("langs");
        }
        updateLangsList(langs);
        updateStorageList(this.getActivity());

        EditTextPreference username = (EditTextPreference) findPreference(PrefsActivity.PREF_USER_NAME);
        username.setSummary( username.getText().equals("") ?
                getString(R.string.about_not_logged_in) :
                getString(R.string.about_logged_in, username.getText()) );

        EditTextPreference server = (EditTextPreference) findPreference(PrefsActivity.PREF_SERVER);
        server.setSummary(server.getText());

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
            List<String> entries = new ArrayList<String>();
            List<String> entryValues = new ArrayList<String>();

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

                    if ((currentLocation != null) && currentLocation.startsWith(storageLoc.path)){
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
        List<String> entries = new ArrayList<String>();
        List<String> entryValues = new ArrayList<String>();

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
}