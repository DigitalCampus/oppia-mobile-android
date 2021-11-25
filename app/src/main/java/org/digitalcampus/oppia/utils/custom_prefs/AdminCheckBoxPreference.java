package org.digitalcampus.oppia.utils.custom_prefs;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.CheckBoxPreference;
import androidx.preference.EditTextPreference;

public class AdminCheckBoxPreference extends CheckBoxPreference implements AdminPreference {

    public enum PromptAdminDialogMode {
        ON_CHECK, ON_UNCHECK, ON_BOTH
    }

    private PromptAdminDialogMode mode = PromptAdminDialogMode.ON_BOTH;

    private OnPreferenceClickListener customOnPreferenceClickListener;

    public AdminCheckBoxPreference(Context context) {
        super(context);
    }

    public AdminCheckBoxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdminCheckBoxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onClick() {


        if (customOnPreferenceClickListener != null) {

            if ((!isChecked() && mode == PromptAdminDialogMode.ON_CHECK)
                    || (isChecked() && mode == PromptAdminDialogMode.ON_UNCHECK)
                    || mode == PromptAdminDialogMode.ON_BOTH) {

                boolean handled = customOnPreferenceClickListener.onPreferenceClick(this);
                if (handled) {
                    return;
                }
            }
        }

        super.onClick();

    }

    @Override
    public void setOnPreferenceClickListener(OnPreferenceClickListener onPreferenceClickListener) {
        customOnPreferenceClickListener = onPreferenceClickListener;
    }

    public void setPromptAdminDialogMode(PromptAdminDialogMode mode) {
        this.mode = mode;
    }

    @Override
    public void onAccessGranted() {
        super.onClick();
    }
}
