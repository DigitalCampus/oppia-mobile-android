package org.digitalcampus.oppia.fragments.prefs;

import android.os.Bundle;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.model.Lang;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;

public class MainPreferencesFragment extends BasePreferenceFragment {

    public static final String TAG = PrefsActivity.class.getSimpleName();
    public static final String FRAGMENT_TAG = "main_prefs_fragment";
    public static final String PAGE_ID = "page_id";

    public static MainPreferencesFragment newInstance(String pageId) {

        MainPreferencesFragment f = new MainPreferencesFragment();
        Bundle args = new Bundle();
        args.putString(PAGE_ID, pageId);
        f.setArguments(args);
        return f;
    }

    public MainPreferencesFragment(){
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()){
            getActivity().setTitle(R.string.menu_settings);
        }
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from XML resources
        setPreferencesFromResource(R.xml.prefs_main, rootKey);
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

        ListPreference langsList = findPreference(PrefsActivity.PREF_LANGUAGE);
        if (entries.size() > 1){
            langsList.setEntries( entries.toArray(new CharSequence[0]) );
            langsList.setEntryValues( entryValues.toArray(new CharSequence[0]) );
        }
        else{
            getPreferenceScreen().removePreference(langsList);
        }

    }




}
