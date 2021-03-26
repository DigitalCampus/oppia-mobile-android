package org.digitalcampus.oppia.fragments.prefs;

public interface PreferenceChangedCallback {
    void onPreferenceUpdated(String pref, String newValue);
}
