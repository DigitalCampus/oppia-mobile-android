package org.digitalcampus.oppia.utils.custom_prefs;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;

public class AdminMultiSelectListPreference extends MultiSelectListPreference implements AdminPreference {

    private OnPreferenceClickListener customOnPreferenceClickListener;

    public AdminMultiSelectListPreference(Context context) {
        super(context);
    }

    public AdminMultiSelectListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdminMultiSelectListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onClick() {

        if (customOnPreferenceClickListener != null) {
            boolean handled = customOnPreferenceClickListener.onPreferenceClick(this);
            if (handled) {
                return;
            }
        }

        super.onClick();

    }

    @Override
    public void setOnPreferenceClickListener(OnPreferenceClickListener onPreferenceClickListener) {
        customOnPreferenceClickListener = onPreferenceClickListener;
    }

    @Override
    public void onAccessGranted() {
        super.onClick();
    }
}
