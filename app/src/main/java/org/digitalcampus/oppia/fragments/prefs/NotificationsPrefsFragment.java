package org.digitalcampus.oppia.fragments.prefs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.fragment.app.DialogFragment;
import androidx.preference.CheckBoxPreference;
import androidx.preference.ListPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.common.util.concurrent.ListenableFuture;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.service.CoursesCompletionReminderWorkerManager;
import org.digitalcampus.oppia.utils.DateUtils;
import org.digitalcampus.oppia.utils.ReminderLogHelper;
import org.digitalcampus.oppia.utils.custom_prefs.TimePreference;
import org.digitalcampus.oppia.utils.custom_prefs.TimePreferenceDialogFragmentCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

public class NotificationsPrefsFragment extends BasePreferenceFragment implements PreferenceChangedCallback, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = PrefsActivity.class.getSimpleName();
    private ReminderLogHelper reminderLogHelper;

    @Inject
    SharedPreferences prefs;

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

    }

    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        initializeDagger();

        configurePreferencesSummary();

        configureRemindersLog();

        liveUpdateSummary(PrefsActivity.PREF_GAMIFICATION_POINTS_ANIMATION);

        getPrefs().registerOnSharedPreferenceChangeListener(this);
    }

    private void initializeDagger() {
        App app = (App) getActivity().getApplication();
        app.getComponent().inject(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPrefs().unregisterOnSharedPreferenceChangeListener(this);
    }


    private SharedPreferences getPrefs() {
        return prefs;
    }

    private void configureRemindersLog() {

        reminderLogHelper = new ReminderLogHelper(getActivity());

        Preference prefReminderLog = findPreference(PrefsActivity.PREF_REMINDERS_LOG);
        prefReminderLog.setVisible(BuildConfig.DEBUG);
        prefReminderLog.setOnPreferenceClickListener(preference -> {

            String message = "CURRENT REMINDERS CONFIG:\n" + getCurrentReminderConfig() + getWorkerStatuses() + "\n\nLOG:\n\n" + reminderLogHelper.getLog();
            new AlertDialog.Builder(getActivity())
                    .setTitle("Reminders log")
                    .setMessage(message)
                    .setNegativeButton(R.string.back, null)
                    .show();
            return false;
        });
    }

    private String getWorkerStatuses() {

        String statusesText = "WORKERS STATUSES:\n";
        for (int i = 1; i <= 7; i++) {
            statusesText += "Day " + i + " scheduled: " + isWorkScheduled(App.WORK_COURSES_NOT_COMPLETED_REMINDER_ + i) + "\n";
        }

        return statusesText + "\n";

    }

    private boolean isWorkScheduled(String tag) {
        WorkManager instance = WorkManager.getInstance(getActivity());
        ListenableFuture<List<WorkInfo>> statuses = instance.getWorkInfosForUniqueWork(tag);
        try {
            boolean running = false;
            List<WorkInfo> workInfoList = statuses.get();
            for (WorkInfo workInfo : workInfoList) {
                WorkInfo.State state = workInfo.getState();
                running = state == WorkInfo.State.RUNNING | state == WorkInfo.State.ENQUEUED;
            }
            return running;
        } catch (ExecutionException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String getCurrentReminderConfig() {
        String configText = "";
        boolean enabled = ((CheckBoxPreference) findPreference(PrefsActivity.PREF_COURSES_REMINDER_ENABLED)).isChecked();
        String interval = ((ListPreference) findPreference(PrefsActivity.PREF_COURSES_REMINDER_INTERVAL)).getValue();
        String time = getPrefs().getString(PrefsActivity.PREF_COURSES_REMINDER_TIME, getString(R.string.prefCoursesReminderTimeDefault));
        Set<String> dayCodes = ((MultiSelectListPreference) findPreference(PrefsActivity.PREF_COURSES_REMINDER_DAYS)).getValues();
        configText += String.format("Enabled: %s\nInterval: %s\nTime: %s\nDays: %s", enabled, interval, time, getWeekDaysNames(getActivity(), dayCodes));
        return configText + "\n\n";
    }

    private void configurePreferencesSummary() {

        findPreference(PrefsActivity.PREF_COURSES_REMINDER_INTERVAL).setOnPreferenceChangeListener((preference, newValue) -> {
            String interval = (String) newValue;
            if (TextUtils.equals(interval, getString(R.string.interval_weekly_value))) {

                Set<String> days = getPrefs().getStringSet(
                        PrefsActivity.PREF_COURSES_REMINDER_DAYS, getDefaultReminderDays());

                if (days.size() > 1) {
                    // Weekly interval can only have one day of week. Set to default:
                    Set<String> firstDefaultDays = new HashSet<>(Arrays.asList(new String[]{getDefaultReminderDays().iterator().next()}));

                    final MultiSelectListPreference prefReminderDays = findPreference(PrefsActivity.PREF_COURSES_REMINDER_DAYS);
                    prefReminderDays.setValues(firstDefaultDays);

                }

            }
            logNewConfig();
            return true;
        });

        final MultiSelectListPreference prefReminderDays = findPreference(PrefsActivity.PREF_COURSES_REMINDER_DAYS);

        prefReminderDays.setSummary(getWeekDaysNames(getActivity(), prefReminderDays.getValues()));
        prefReminderDays.setOnPreferenceChangeListener((preference, newValue) -> {

            Set<String> values = (Set<String>) newValue;

            if (values.isEmpty()) {
                alert(R.string.warning_reminder_at_least_one_day);
                return false;
            }

            String interval = getPrefs().getString(
                    PrefsActivity.PREF_COURSES_REMINDER_INTERVAL, getString(R.string.prefCoursesReminderIntervalDefault));

            if (TextUtils.equals(interval, getString(R.string.interval_weekly_value))
                    && values.size() > 1) {
                alert(R.string.warning_reminder_weekly_just_one_day);
                return false;
            }

            logNewConfig();
            return true;
        });

        final TimePreference prefReminderTime = findPreference(PrefsActivity.PREF_COURSES_REMINDER_TIME);

        prefReminderTime.setSummary(prefReminderTime.getValue());
        prefReminderTime.setOnPreferenceChangeListener((preference, newValue) -> {
            preference.setSummary((String) newValue);
            logNewConfig();
            return true;
        });

        findPreference(PrefsActivity.PREF_COURSES_REMINDER_ENABLED).setOnPreferenceChangeListener((preference, newValue) -> {
            logNewConfig();
            return true;
        });
    }

    private void logNewConfig() {

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            reminderLogHelper.saveLogEntry("CONFIGURATION CHANGED", getCurrentReminderConfig());
        }, 200);

    }

    private void alert(int stringId) {
        alert(getString(stringId));
    }

    private void alert(String message) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.warning)
                .setMessage(message)
                .setNegativeButton(R.string.ok, null)
                .show();
    }

    private Set<String> getDefaultReminderDays() {
        return new HashSet<>(Arrays.asList(
                getResources().getStringArray(R.array.days_of_week_values_default)));
    }


    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (preference instanceof TimePreference) {
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

        if (key.equals(PrefsActivity.PREF_COURSES_REMINDER_DAYS)) {
            final MultiSelectListPreference preference = findPreference(key);
            preference.setSummary(getWeekDaysNames(getActivity(), preference.getValues()));
            CoursesCompletionReminderWorkerManager.configureCoursesCompletionReminderWorker(getActivity());
        } else if (key.equals(PrefsActivity.PREF_COURSES_REMINDER_TIME)) {
            CoursesCompletionReminderWorkerManager.configureCoursesCompletionReminderWorker(getActivity());
        } else if (key.equals(PrefsActivity.PREF_COURSES_REMINDER_ENABLED)) {
            CoursesCompletionReminderWorkerManager.configureCoursesCompletionReminderWorker(getActivity());
        } else if (key.equals(PrefsActivity.PREF_COURSES_REMINDER_INTERVAL)) {
            CoursesCompletionReminderWorkerManager.configureCoursesCompletionReminderWorker(getActivity());
        }

    }

    public static String getWeekDaysNames(Context context, Set<String> dayCodes) {
        String dayNames = "";

        int i = 0;
        for (String dayCode : dayCodes) {
            int dayNameId = context.getResources().getIdentifier("week_day_" + dayCode, "string", context.getPackageName());
            dayNames += context.getString(dayNameId) + (i < dayCodes.size() - 1 ? ", " : "");
            i++;
        }
        return dayNames;
    }
}
