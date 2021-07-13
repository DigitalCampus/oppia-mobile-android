package org.digitalcampus.oppia.utils.custom_prefs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.EditTextPreference;
import androidx.preference.Preference;

import org.digitalcampus.oppia.application.AdminSecurityManager;

public class AdminEditTextPreference extends EditTextPreference {

    private OnPreferenceClickListener customOnPreferenceClickListener;

    public AdminEditTextPreference(Context context) {
        super(context);
    }

    public AdminEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdminEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
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
}
