package org.digitalcampus.oppia.fragments.prefs;

import android.os.Bundle;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.model.Lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.preference.ListPreference;

public class DisplayPrefsFragment extends BasePreferenceFragment implements PreferenceChangedCallback{

    public static final String TAG = PrefsActivity.class.getSimpleName();

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
        if ((bundle != null) && bundle.containsKey("langs")) {
            ArrayList<Lang> langs = (ArrayList<Lang>) bundle.getSerializable("langs");
            if (langs != null){
                updateLangsList(langs);
            }
        }

        liveUpdateSummary(PrefsActivity.PREF_TEXT_SIZE);
    }


    private void updateLangsList(ArrayList<Lang> langs){
        List<String> entries = new ArrayList<>();
        List<String> entryValues = new ArrayList<>();

        for(Lang l: langs){
            if(!entryValues.contains(l.getLanguage())){
                entryValues.add(l.getLanguage());
                Locale loc = new Locale(l.getLanguage());
                entries.add(loc.getDisplayLanguage(loc));
            }
        }

        ListPreference langsList = findPreference(PrefsActivity.PREF_LANGUAGE);
        if (entries.size() > 1){
            langsList.setEntries( entries.toArray(new CharSequence[0]) );
            langsList.setEntryValues( entryValues.toArray(new CharSequence[0]) );
            liveUpdateSummary(PrefsActivity.PREF_LANGUAGE);
        }
        else{
            getPreferenceScreen().removePreference(langsList);
        }

    }

    @Override
    public void onPreferenceUpdated(String pref, String newValue) {
        // do nothing
    }
}
