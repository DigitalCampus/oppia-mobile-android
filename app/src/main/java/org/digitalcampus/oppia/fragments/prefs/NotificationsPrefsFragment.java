package org.digitalcampus.oppia.fragments.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.service.CoursesCompletionReminderWorkerManager;
import org.digitalcampus.oppia.utils.custom_prefs.DayWeekTimePreference;
import org.digitalcampus.oppia.utils.custom_prefs.DayWeekTimePreferenceDialogFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class NotificationsPrefsFragment extends BasePreferenceFragment implements PreferenceChangedCallback, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = PrefsActivity.class.getSimpleName();



    public static NotificationsPrefsFragment newInstance() {
        return new NotificationsPrefsFragment();
    }

    public NotificationsPrefsFragment(){
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from XML resources
        addPreferencesFromResource(R.xml.prefs_notifications);

        final DayWeekTimePreference pref = findPreference(PrefsActivity.PREF_COURSES_REMINDER_DAY_TIME_MILLIS);

        String coursesReminderDayTimeMillis = getPreferenceManager().getSharedPreferences().getString(
                PrefsActivity.PREF_COURSES_REMINDER_DAY_TIME_MILLIS,
                CoursesCompletionReminderWorkerManager.DEFAULT_COURSES_REMINDER_TIME_MILLIS);

        pref.setSummary(getFormattedDayWeekTime(coursesReminderDayTimeMillis));
        pref.setOnPreferenceChangeListener((preference, newValue) -> {
            //your code to change values.
            pref.setSummary(getFormattedDayWeekTime(newValue != null ? (String) newValue : null));
            return true;
        });

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private String getFormattedDayWeekTime(String dayWeekTimeMillis) {
        if (TextUtils.isEmpty(dayWeekTimeMillis)) {
            return getString(R.string.disabled);
        }

        DateFormat dateFormat = new SimpleDateFormat("EEEE 'at' HH:mm");
        String dayWeekTime = dateFormat.format(new Date(Long.parseLong(dayWeekTimeMillis)));
        return dayWeekTime;
    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        liveUpdateSummary(PrefsActivity.PREF_GAMIFICATION_POINTS_ANIMATION);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof DayWeekTimePreference) {
            DialogFragment f = DayWeekTimePreferenceDialogFragment.newInstance(preference.getKey());
            f.setTargetFragment(this, 0);
            f.show(getParentFragmentManager(), null);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onPreferenceUpdated(String pref, String newValue) {
        // do nothing
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(PrefsActivity.PREF_COURSES_REMINDER_DAY_TIME_MILLIS)) {
            CoursesCompletionReminderWorkerManager.configureCoursesCompletionReminderWorker(getActivity());
        }

    }
}
