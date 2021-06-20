package org.digitalcampus.oppia.utils.custom_prefs;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.preference.DialogPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceViewHolder;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ViewPrefDayWeekTimeBinding;
import org.digitalcampus.oppia.service.CoursesCompletionReminderWorkerManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class DayWeekTimePreferenceDialogFragment extends PreferenceDialogFragmentCompat {

    private static final String TAG = DayWeekTimePreferenceDialogFragment.class.getSimpleName();

    // https://stackoverflow.com/a/54722684/1365440

    private ViewPrefDayWeekTimeBinding binding;
    private int dayOfWeek;
    private int hour;
    private int minute;
    private int[] daysOfWeekValues;

    public static DayWeekTimePreferenceDialogFragment newInstance(String key) {
        final DayWeekTimePreferenceDialogFragment
                fragment = new DayWeekTimePreferenceDialogFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String dayTimeMillis = getDayWeekTimePreference().getDayTimeMillis();

        if (TextUtils.isEmpty(dayTimeMillis)) {
            dayTimeMillis = CoursesCompletionReminderWorkerManager.DEFAULT_COURSES_REMINDER_TIME_MILLIS;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(dayTimeMillis));

        dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

    }

    @Override
    protected View onCreateDialogView(Context context) {
        binding = ViewPrefDayWeekTimeBinding.inflate(LayoutInflater.from(context));
        binding.timePickerReminder.setIs24HourView(true);

        daysOfWeekValues = getResources().getIntArray(R.array.days_of_week_values);

        binding.btnDisableReminder.setOnClickListener(v -> {

            final DayWeekTimePreference preference = getDayWeekTimePreference();
            if (preference.callChangeListener("")) {
                preference.setDayTimeMillis("");
            }

            dismiss();
        });

        return binding.getRoot();

    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        binding.timePickerReminder.setCurrentHour(hour);
        binding.timePickerReminder.setCurrentMinute(minute);
        binding.spinnerDayWeekReminder.setSelection(getPositionOfDayOfWeek(dayOfWeek));

    }

    private int getPositionOfDayOfWeek(int dayOfWeek) {

        for (int i = 0; i < daysOfWeekValues.length; i++) {
            int daysOfWeekValue = daysOfWeekValues[i];
            if (daysOfWeekValue == dayOfWeek) {
                return i;
            }
        }
        return 0;
    }

    private DayWeekTimePreference getDayWeekTimePreference() {
        return (DayWeekTimePreference) getPreference();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            hour = binding.timePickerReminder.getCurrentHour();
            minute = binding.timePickerReminder.getCurrentMinute();
            dayOfWeek = daysOfWeekValues[binding.spinnerDayWeekReminder.getSelectedItemPosition()];

            Log.i(TAG, "onDialogClosed: dayofweek selected: " + dayOfWeek);

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);

            String value = String.valueOf(calendar.getTimeInMillis());

            final DayWeekTimePreference preference = getDayWeekTimePreference();
            if (preference.callChangeListener(value)) {
                preference.setDayTimeMillis(value);
            }
        }
    }
}
