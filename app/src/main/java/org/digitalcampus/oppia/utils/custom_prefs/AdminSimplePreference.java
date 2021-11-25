package org.digitalcampus.oppia.utils.custom_prefs;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;

public class AdminSimplePreference extends Preference implements AdminPreference {

    private OnPreferenceClickListener customOnPreferenceClickListener;

    public AdminSimplePreference(Context context) {
        super(context);
    }

    public AdminSimplePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdminSimplePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    public void setOnPreferenceClickListener(OnPreferenceClickListener onPreferenceClickListener) {
        super.setOnPreferenceClickListener(onPreferenceClickListener);
    }

    @Override
    public void onAccessGranted() {
        super.setOnPreferenceClickListener(null);
        super.performClick();
    }
}
