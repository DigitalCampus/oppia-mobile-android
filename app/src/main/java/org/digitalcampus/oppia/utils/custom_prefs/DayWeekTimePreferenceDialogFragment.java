package org.digitalcampus.oppia.utils.custom_prefs;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceViewHolder;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ViewPrefDayWeekTimeBinding;

public class DayWeekTimePreferenceDialogFragment extends PreferenceDialogFragmentCompat {

    // https://stackoverflow.com/a/54722684/1365440

    private ViewPrefDayWeekTimeBinding binding;

    public static DayWeekTimePreferenceDialogFragment newInstance(String key) {
        final DayWeekTimePreferenceDialogFragment
                fragment = new DayWeekTimePreferenceDialogFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected View onCreateDialogView(Context context) {
        binding = ViewPrefDayWeekTimeBinding.inflate(LayoutInflater.from(context));
        return binding.getRoot();

    }

    @Override
    public void onDialogClosed(boolean positiveResult) {

    }
}
