package org.digitalcampus.oppia.fragments.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.fragment.app.DialogFragment;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.service.CoursesCompletionReminderWorkerManager;
import org.digitalcampus.oppia.utils.custom_prefs.DayWeekTimePreference;
import org.digitalcampus.oppia.utils.custom_prefs.DayWeekTimePreferenceDialogFragment;
import org.digitalcampus.oppia.utils.custom_prefs.TimePreference;
import org.digitalcampus.oppia.utils.custom_prefs.TimePreferenceDialogFragmentCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

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

        configurePreferencesSummary();


        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    private void configurePreferencesSummary() {

        findPreference(PrefsActivity.PREF_COURSES_REMINDER_INTERVAL).setOnPreferenceChangeListener((preference, newValue) -> {

            return true;
        });

        final Preference prefReminderDays = findPreference(PrefsActivity.PREF_COURSES_REMINDER_DAYS);

        Set<String> coursesReminderDayTimeMillis = getPreferenceManager().getSharedPreferences().getStringSet(
                PrefsActivity.PREF_COURSES_REMINDER_DAYS,
                null);

        prefReminderDays.setSummary("MON, TUE, WED");
        prefReminderDays.setOnPreferenceChangeListener((preference, newValue) -> {
            //your code to change values.
            String days = "";
            for (String dayNum : (Set<String>) newValue) {
                days += dayNum + ", ";
            }
            preference.setSummary(days);
            return true;
        });

        final Preference prefReminderTime = findPreference(PrefsActivity.PREF_COURSES_REMINDER_TIME);

        String reminderTime = getPreferenceManager().getSharedPreferences().getString(
                PrefsActivity.PREF_COURSES_REMINDER_TIME,
                getString(R.string.prefCoursesReminderTimeDefault));

        prefReminderTime.setSummary(reminderTime);
        prefReminderTime.setOnPreferenceChangeListener((preference, newValue) -> {
            preference.setSummary((String) newValue);
            return true;
        });
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
        } else if (preference instanceof TimePreference) {
            DialogFragment f = new TimePreferenceDialogFragmentCompat();
            Bundle bundle = new Bundle(1);
            bundle.putString("key", preference.getKey());
            f.setArguments(bundle);
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
