package org.digitalcampus.oppia.fragments.prefs;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class NotificationsPrefsFragment extends BasePreferenceFragment implements PreferenceChangedCallback, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = PrefsActivity.class.getSimpleName();


    public static NotificationsPrefsFragment newInstance() {
        return new NotificationsPrefsFragment();
    }

    public NotificationsPrefsFragment() {
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
            String interval = (String) newValue;
            if (TextUtils.equals(interval, getString(R.string.interval_weekly))) {

                Set<String> days = getPreferenceManager().getSharedPreferences().getStringSet(
                        PrefsActivity.PREF_COURSES_REMINDER_DAYS, getDefaultReminderDays());

                if (days.size() > 1) {
                    // Weekly interval can only have one day of week. Set to default:
                    Set<String> firstDefaultDays = new HashSet<>(Arrays.asList(new String[]{getDefaultReminderDays().iterator().next()}));

                    final MultiSelectListPreference prefReminderDays = findPreference(PrefsActivity.PREF_COURSES_REMINDER_DAYS);
                    prefReminderDays.setValues(firstDefaultDays);

                }

            }
            return true;
        });

        final MultiSelectListPreference prefReminderDays = findPreference(PrefsActivity.PREF_COURSES_REMINDER_DAYS);

        prefReminderDays.setSummary(getWeekDaysNames(prefReminderDays.getValues()));
        prefReminderDays.setOnPreferenceChangeListener((preference, newValue) -> {

            Set<String> values = (Set<String>) newValue;

            if (values.isEmpty()) {
                alert(R.string.warning_reminder_at_least_one_day);
                return false;
            }

            String interval = getPreferenceManager().getSharedPreferences().getString(
                    PrefsActivity.PREF_COURSES_REMINDER_INTERVAL, getString(R.string.prefCoursesReminderIntervalDefault));

            if (TextUtils.equals(interval, getString(R.string.interval_weekly))
                    && values.size() > 1) {
                alert(R.string.warning_reminder_weekly_just_one_day);
                return false;
            }

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

    private void alert(int stringId) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.warning)
                .setMessage(stringId)
                .setNegativeButton(R.string.ok, null)
                .show();
    }

    private Set<String> getDefaultReminderDays() {
        return new HashSet<>(Arrays.asList(
                getResources().getStringArray(R.array.days_of_week_values_default)));
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
        } else if (key.equals(PrefsActivity.PREF_COURSES_REMINDER_DAYS)) {
            final MultiSelectListPreference preference = findPreference(key);
            preference.setSummary(getWeekDaysNames(preference.getValues()));
        }

    }

    private String getWeekDaysNames(Set<String> dayCodes) {
        String dayNames = "";

        int i = 0;
        for (String dayCode : dayCodes) {
            int dayNameId = getResources().getIdentifier("week_day_" + dayCode, "string", getContext().getPackageName());
            dayNames += getString(dayNameId) + (i < dayCodes.size() - 1 ? ", " : "");
            i++;
        }
        return dayNames;
    }
}
