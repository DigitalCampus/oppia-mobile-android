package org.digitalcampus.oppia.fragments.prefs;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.utils.TextUtilsJava;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class DisplayPrefsFragment extends BasePreferenceFragment implements PreferenceChangedCallback{

    public static final String TAG = PrefsActivity.class.getSimpleName();

    public static final String ARG_CONTENT_LANGS = "arg_content_langs";

    public static DisplayPrefsFragment newInstance() {
        return new DisplayPrefsFragment();
    }

    public DisplayPrefsFragment(){
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from XML resources
        addPreferencesFromResource(R.xml.prefs_display);
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        Bundle bundle = getArguments();
        if ((bundle != null) && bundle.containsKey(ARG_CONTENT_LANGS)) {
            ArrayList<Lang> langs = (ArrayList<Lang>) bundle.getSerializable(ARG_CONTENT_LANGS);
            if (langs != null){
                updateContentLangsList(langs);
            }
        }

        loadInterfaceLangs();

        liveUpdateSummary(PrefsActivity.PREF_TEXT_SIZE);
    }

    private void loadInterfaceLangs() {
        String[] langs = BuildConfig.INTERFACE_LANGUAGE_OPTIONS.split(",");
        Map<String, String> langNames = Arrays.stream(langs).collect(Collectors.toMap(
                langCode -> new Locale(langCode).getDisplayLanguage(new Locale(langCode)),
                langCode -> langCode));

        ListPreference prefInterfaceLangs = findPreference(PrefsActivity.PREF_INTERFACE_LANGUAGE);
        prefInterfaceLangs.setEntries(langNames.keySet().toArray(new String[0]));
        prefInterfaceLangs.setEntryValues(langNames.values().toArray(new String[0]));

        liveUpdateSummary(PrefsActivity.PREF_INTERFACE_LANGUAGE);

    }

    private void updateContentLangsList(List<Lang> langs){
        List<String> entries = new ArrayList<>();
        List<String> entryValues = new ArrayList<>();

        for(Lang l: langs){
            if(!entryValues.contains(l.getLanguage())){
                entryValues.add(l.getLanguage());
                Locale loc = new Locale(l.getLanguage());
                entries.add(loc.getDisplayLanguage(loc));
            }
        }

        ListPreference langsList = findPreference(PrefsActivity.PREF_CONTENT_LANGUAGE);
        if (entries.size() > 1){
            langsList.setEntries( entries.toArray(new CharSequence[0]) );
            langsList.setEntryValues( entryValues.toArray(new CharSequence[0]) );
            liveUpdateSummary(PrefsActivity.PREF_CONTENT_LANGUAGE);
        }
        else{
            getPreferenceScreen().removePreference(langsList);
        }

    }

    @Override
    protected boolean onPreferenceChangedDelegate(Preference preference, Object newValue) {
        if (TextUtilsJava.equals(preference.getKey(), PrefsActivity.PREF_INTERFACE_LANGUAGE)) {
            LocaleListCompat appLocale = LocaleListCompat.forLanguageTags((String) newValue);
            // Call this on the main thread as it may require Activity.restart()
            AppCompatDelegate.setApplicationLocales(appLocale);
        }

        return true;
    }

    @Override
    public void onPreferenceUpdated(String pref, String newValue) {

        if (TextUtilsJava.equals(pref, PrefsActivity.PREF_INTERFACE_LANGUAGE)) {
            LocaleListCompat appLocale = LocaleListCompat.forLanguageTags(newValue);
            // Call this on the main thread as it may require Activity.restart()
            AppCompatDelegate.setApplicationLocales(appLocale);
        }
    }
}
