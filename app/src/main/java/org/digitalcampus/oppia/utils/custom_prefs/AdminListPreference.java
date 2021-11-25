package org.digitalcampus.oppia.utils.custom_prefs;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;

public class AdminListPreference extends ListPreference implements AdminPreference {

    private OnPreferenceClickListener customOnPreferenceClickListener;

    public AdminListPreference(Context context) {
        super(context);
    }

    public AdminListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdminListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
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
