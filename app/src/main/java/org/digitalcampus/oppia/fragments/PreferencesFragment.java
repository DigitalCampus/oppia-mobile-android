package org.digitalcampus.oppia.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Patterns;
import android.webkit.URLUtil;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.storage.StorageLocationInfo;
import org.digitalcampus.oppia.utils.storage.StorageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PreferencesFragment extends PreferenceFragment {

    public static final String TAG = PrefsActivity.class.getSimpleName();

    private ListPreference storagePref;
    private EditTextPreference serverPref;

    public static PreferencesFragment newInstance() {
        return new PreferencesFragment();
    }

    public PreferencesFragment(){
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        // Load the preferences from XML resources
        addPreferencesFromResource(R.xml.common_prefs);
        addPreferencesFromResource(R.xml.prefs);

        storagePref = (ListPreference) findPreference(PrefsActivity.PREF_STORAGE_OPTION);
        serverPref = (EditTextPreference) findPreference(PrefsActivity.PREF_SERVER);
        serverPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String url = ((String) newValue).trim();
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
        findPreference(PrefsActivity.PREF_SERVER_TIMEOUT_CONN).setOnPreferenceChangeListener(maxIntListener);
        findPreference(PrefsActivity.PREF_SERVER_TIMEOUT_RESP).setOnPreferenceChangeListener(maxIntListener);

        if (!App.ADMIN_PROTECT_SETTINGS){
            // If the whole settings activity is not protected by password, we need to protect admin settings
            protectAdminPreferences();
        }

        Bundle bundle = getArguments();
        ArrayList<Lang> langs;
        if ((bundle != null) && bundle.getSerializable("langs") != null) {
	        langs = (ArrayList<Lang>) bundle.getSerializable("langs");
	        if (langs != null){
                updateLangsList(langs);
            }

        }

        updateServerPref();

        updateStorageList(this.getActivity());

        EditTextPreference username = (EditTextPreference) findPreference(PrefsActivity.PREF_USER_NAME);
        username.setSummary( username.getText().equals("") ?
                getString(R.string.about_not_logged_in) :
                getString(R.string.about_logged_in, username.getText()) );

    }

    public void updateServerPref(){

        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        String server = prefs.getString(PrefsActivity.PREF_SERVER, "");
        String status;

        boolean checked = prefs.getBoolean(PrefsActivity.PREF_SERVER_CHECKED, false);
        if (!checked){
            status = getString(R.string.prefServer_notChecked);
        }
        else{
            boolean valid = prefs.getBoolean(PrefsActivity.PREF_SERVER_VALID, false);
            if (valid){
                String name = prefs.getString(PrefsActivity.PREF_SERVER_NAME, server);
                String version = prefs.getString(PrefsActivity.PREF_SERVER_VERSION, "");
                status = name + " (" + version + ")";
            }
            else{
                status = getString(R.string.prefServer_errorTitle);
            }
        }

        serverPref.setText(server);
        serverPref.setSummary(server + "\n" + status);
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
                storagePref.setEntryValues(entryValues.toArray(new CharSequence[0]));
                storagePref.setEntries(entries.toArray(new CharSequence[0]));
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
        if (entries.size() > 1){
            langsList.setEntries( entries.toArray(new CharSequence[0]) );
            langsList.setEntryValues( entryValues.toArray(new CharSequence[0]) );
        }
        else{
            getPreferenceScreen().removePreference(langsList);
        }

    }

    private void protectAdminPreferences(){
        final CheckBoxPreference adminEnabled = (CheckBoxPreference) findPreference(PrefsActivity.PREF_ADMIN_PROTECTION);
        protectAdminEditTextPreferences(PrefsActivity.PREF_ADMIN_PASSWORD, PrefsActivity.PREF_SERVER);

        adminEnabled.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, final Object newValue) {
                final Boolean enableProtection = (Boolean) newValue;
                if (enableProtection) {
                    //If we are going to re-enable the preference, there is no need to prompt for the previous password
                    return true;
                }
                AdminSecurityManager.with(getActivity()).promptAdminPassword(new AdminSecurityManager.AuthListener() {
                    @Override
                    public void onPermissionGranted() {
                        adminEnabled.setChecked(enableProtection);
                        preference.getEditor().putBoolean(preference.getKey(), enableProtection);
                    }
                });
                return false;
            }
        });

    }

    private void protectAdminEditTextPreferences(String... prefsKeys) {

        for (String prefKey : prefsKeys) {

            final EditTextPreference editTextPreference = (EditTextPreference) findPreference(prefKey);

            editTextPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(final Preference preference, final Object newValue) {

                    if (!App.getPrefs(getActivity()).getBoolean(PrefsActivity.PREF_ADMIN_PROTECTION, false)) {
                        return true;
                    }

                    AdminSecurityManager.with(getActivity()).promptAdminPassword( new AdminSecurityManager.AuthListener() {
                        @Override
                        public void onPermissionGranted() {
                            editTextPreference.setText((String) newValue);
                            preference.getEditor().putString(preference.getKey(), (String) newValue);
                        }
                    });
                    return false;
                }
            });
        }
    }

    public class MaxIntOnStringPreferenceListener implements Preference.OnPreferenceChangeListener{

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {

            boolean valid;
            try{
                String intValue = (String) newValue;
                valid = (intValue.length() <= 9); //it'll be bigger than int's max value
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
