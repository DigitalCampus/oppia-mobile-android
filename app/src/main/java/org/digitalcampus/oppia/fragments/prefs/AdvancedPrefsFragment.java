package org.digitalcampus.oppia.fragments.prefs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Patterns;
import android.webkit.URLUtil;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.api.ApiEndpoint;
import org.digitalcampus.oppia.api.RemoteApiEndpoint;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.task.ExportActivityTask;
import org.digitalcampus.oppia.task.FetchUserTask;
import org.digitalcampus.oppia.utils.ConnectionUtils;
import org.digitalcampus.oppia.utils.CourseUtils;
import org.digitalcampus.oppia.utils.TextUtilsJava;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.resources.ExternalResourceOpener;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.digitalcampus.oppia.utils.storage.StorageLocationInfo;
import org.digitalcampus.oppia.utils.storage.StorageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class AdvancedPrefsFragment extends BasePreferenceFragment implements PreferenceChangedCallback {

    public static final String TAG = PrefsActivity.class.getSimpleName();
    private ListPreference storagePref;
    private EditTextPreference serverPref;
    private EditTextPreference usernamePref;
    private ListPreference updateActivityOnLoginPref;

    @Inject
    SharedPreferences prefs;
    @Inject
    ApiEndpoint apiEndpoint;

    public static AdvancedPrefsFragment newInstance() {
        return new AdvancedPrefsFragment();
    }

    public AdvancedPrefsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from XML resources
        addPreferencesFromResource(R.xml.prefs_advanced);
    }


    @Override
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        initializeDagger();

        loadPrefs();
    }

    private void initializeDagger() {
        App app = (App) getActivity().getApplication();
        app.getComponent().inject(this);
    }

    private void loadPrefs() {
        storagePref = findPreference(PrefsActivity.PREF_STORAGE_OPTION);
        serverPref = findPreference(PrefsActivity.PREF_SERVER);
        usernamePref = findPreference(PrefsActivity.PREF_USER_NAME);
        updateActivityOnLoginPref = findPreference(PrefsActivity.PREF_UPDATE_ACTIVITY_ON_LOGIN);

        if (serverPref == null || storagePref == null) {
            return;
        }

        updateServerPref();
        updateStorageList(this.getActivity());
        liveUpdateSummary(PrefsActivity.PREF_STORAGE_OPTION);
        liveUpdateSummary(PrefsActivity.PREF_SERVER_TIMEOUT_CONN, " ms");
        liveUpdateSummary(PrefsActivity.PREF_SERVER_TIMEOUT_RESP, " ms");
        usernamePref.setSummary(TextUtilsJava.isEmpty(usernamePref.getText()) ?
                getString(R.string.about_not_logged_in) :
                getString(R.string.about_logged_in, usernamePref.getText()));

        serverPref.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean mustUpdate = onPreferenceChangedDelegate(preference, newValue);
            if (!mustUpdate) {
                return false;
            }

            String currentValue = prefs.getString(preference.getKey(), getString(R.string.prefServerDefault));
            if (checkMustShowWarningLogout(preference.getKey(), currentValue, (String) newValue)) {
                showApiKeyWarning(() -> App.getPrefs(getActivity()).edit().putString(PrefsActivity.PREF_SERVER, currentValue).apply());
            }

            return true;
        });

        updateActivityOnLoginPref.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean mustUpdate = onPreferenceChangedDelegate(preference, newValue);
            if (!mustUpdate) {
                return false;
            }

            String currentValue = prefs.getString(preference.getKey(), getString(R.string.prefUpdateActivityOnLoginDefault));
            if (checkMustShowWarningLogout(preference.getKey(), currentValue, (String) newValue)) {
                showWarningLogout(() -> ((ListPreference) preference).setValue(currentValue));
            }

            return true;
        });

        findPreference(PrefsActivity.PREF_FULL_ACTIVITY_EXPORT).setOnPreferenceClickListener(preference -> {
            showCreateExportDataDialog();
            return true;
        });

        findPreference(PrefsActivity.PREF_FLUSH_APP_CACHE).setOnPreferenceClickListener(preference -> {
            flushAppCache();
            return true;
        });

    }

    private boolean checkMustShowWarningLogout(String key, String currentValue, String newValue) {

        switch (key) {
            case PrefsActivity.PREF_SERVER:
                boolean allApiKeysInvalid = SessionManager.areAllApiKeysInvalid(getContext());
                //If all API keys are already invalidated, we don't need to show the warning
                return !TextUtilsJava.equals(currentValue, newValue) &&
                        (isLoggedIn() || !allApiKeysInvalid);

            case PrefsActivity.PREF_UPDATE_ACTIVITY_ON_LOGIN:
                if (!isLoggedIn()) {
                    return false;
                }
                boolean currentIsNone = TextUtilsJava.equals(getString(R.string.update_activity_on_login_value_none), currentValue);
                boolean newIsOptionalOrForce = TextUtilsJava.equals(getString(R.string.update_activity_on_login_value_optional), newValue)
                        || TextUtilsJava.equals(getString(R.string.update_activity_on_login_value_force), newValue);
                return currentIsNone && newIsOptionalOrForce;

            default:
                return false;
        }

    }

    private void flushAppCache() {
        flushCourseListingCache();
        flushUserProfileInfo();

        ((AppActivity) getActivity()).toast(R.string.cache_flushed_successfuly);
    }

    private void flushCourseListingCache() {
        prefs.edit()
                .remove(PrefsActivity.PREF_LAST_COURSES_CHECKS_SUCCESSFUL_TIME)
                .remove(PrefsActivity.PREF_SERVER_COURSES_CACHE)
                .commit();

        if (ConnectionUtils.isNetworkConnected(getActivity())) {
            CourseUtils.updateCourseCache(getActivity(), prefs, apiEndpoint);
        }
    }

    private void flushUserProfileInfo() {
        try {
            User user = DbHelper.getInstance(getContext()).getUser(SessionManager.getUsername(getContext()));
            new FetchUserTask().updateLoggedUserProfile(getContext(), apiEndpoint, user);
        } catch (UserNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showCreateExportDataDialog() {

        getAppActivity().showProgressDialog(getString(R.string.loading));

        ExportActivityTask task = new ExportActivityTask(getActivity());
        task.setListener(result -> {
            getAppActivity().hideProgressDialog();
            if (result.isSuccess()) {
                showFullExportShareDialog(result.getResultMessage());
            } else {
                getAppActivity().alert(result.getResultMessage());
            }
        });
        task.execute(ExportActivityTask.FULL_EXPORT_ACTIVTY);
    }

    private void showFullExportShareDialog(String filename) {

        final File fileToShare = new File(Storage.getActivityFullExportPath(getActivity()), filename);
        new AlertDialog.Builder(getActivity())
                .setMessage(getString(R.string.full_activity_exported_success)
                        + getString(R.string.full_activity_export_path, fileToShare.getPath()))
                .setPositiveButton(R.string.share, (dialog, which) ->
                        ExternalResourceOpener.shareFile(getActivity(), fileToShare, "text/json"))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private AppActivity getAppActivity() {
        return ((AppActivity) getActivity());
    }

    @Override
    protected boolean onPreferenceChangedDelegate(Preference preference, Object newValue) {
        if (preference == serverPref) {

            String url = ((String) newValue).trim();
            if (!URLUtil.isNetworkUrl(url) || !Patterns.WEB_URL.matcher(url).matches()) {
                UIUtils.showAlert(getActivity(),
                        R.string.prefServer_errorTitle,
                        R.string.prefServer_errorDescription);
                return false;
            }
            // If it is correct, we allow the change
            return true;
        }

        if (preference == findPreference(PrefsActivity.PREF_SERVER_TIMEOUT_CONN) ||
                preference == findPreference(PrefsActivity.PREF_SERVER_TIMEOUT_RESP)) {
            return checkMaxIntOnString(newValue);
        }

        return super.onPreferenceChangedDelegate(preference, newValue);
    }

//    protected void showWarningIfLoggedIn(String newValue) {
//        if (isLoggedIn()) {
//            String currentUrl = App.getPrefs(getActivity()).getString(PrefsActivity.PREF_SERVER, null);
//            String newUrl = newValue.trim();
//            if (!TextUtilsJava.equals(currentUrl, newUrl)) {
//                showWarningLogout(() -> App.getPrefs(getActivity()).edit().putString(PrefsActivity.PREF_SERVER, currentUrl).apply());
//            }
//        }
//    }

    private boolean isLoggedIn() {
        return !TextUtilsJava.isEmpty(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
    }

    private void showWarningLogout(Runnable restoreValueRunnable) {
        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.warning)
                .setMessage(R.string.change_setting_logout_warning)
                .setPositiveButton(R.string.accept, (dialog, which) -> launchLogoutTasks())
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    if (restoreValueRunnable != null) restoreValueRunnable.run();
                })
                .show();
    }

    private void showApiKeyWarning(Runnable restoreValueRunnable) {

        String message = getString(R.string.change_setting_apikey_warning);
        if (isLoggedIn()){
            message += "\n\n" + getString(R.string.change_setting_logout_warning);
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.warning)
                .setMessage(message)
                .setPositiveButton(R.string.accept, (dialog, which) -> {
                    SessionManager.invalidateAllApiKeys(getContext());
                    launchLogoutTasks();
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    if (restoreValueRunnable != null) restoreValueRunnable.run();
                })
                .show();
    }

    private void launchLogoutTasks() {

        SessionManager.invalidateCurrentUserApiKey(getContext());
        SessionManager.logoutCurrentUser(getActivity());
        clearNotificationProcessCachedData();
        usernamePref.setSummary(R.string.about_not_logged_in);
        ((PrefsActivity) getActivity()).forzeGoToLoginScreen();
    }

    private void clearNotificationProcessCachedData() {

        prefs.edit()
                .remove(PrefsActivity.PREF_SERVER_COURSES_CACHE)
                .remove(PrefsActivity.PREF_NEW_COURSES_LIST_NOTIFIED)
                .remove(PrefsActivity.PREF_LAST_NEW_COURSE_SEEN_TIMESTAMP)
                .remove(PrefsActivity.PREF_LAST_COURSE_VERSION_TIMESTAMP_CHECKED)
                .apply();
    }

    private boolean checkMaxIntOnString(Object newValue) {

        boolean valid;
        try {
            String intValue = (String) newValue;
            valid = (intValue.length() <= 9); //it'll be bigger than int's max value
        } catch (NumberFormatException e) {
            valid = false;
        }

        if (!valid) {
            UIUtils.showAlert(getActivity(),
                    R.string.prefInt_errorTitle,
                    R.string.prefInt_errorDescription);
        }
        return valid;
    }

    public void updateServerPref() {

        if (serverPref == null) {
            loadPrefs();
        }
        if (serverPref == null || storagePref == null) {
            return;
        }

        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
        boolean compatible = true;
        String server = prefs.getString(PrefsActivity.PREF_SERVER, "");
        String status;

        boolean checked = prefs.getBoolean(PrefsActivity.PREF_SERVER_CHECKED, false);
        if (!checked) {
            status = getString(R.string.prefServer_notChecked);
        } else {
            boolean valid = prefs.getBoolean(PrefsActivity.PREF_SERVER_VALID, false);
            if (valid) {
                String name = prefs.getString(PrefsActivity.PREF_SERVER_NAME, server);
                String version = prefs.getString(PrefsActivity.PREF_SERVER_VERSION, "");
                compatible = RemoteApiEndpoint.isServerVersionCompatible(version);
                status = name + " (" + version + ")";
            } else {
                status = getString(R.string.prefServer_errorTitle);
            }
        }

        String summary = server + "\n" + status;
        SpannableStringBuilder summarySpan = new SpannableStringBuilder(summary);

        if (!compatible) {
            String uncompatible = "\n" + getContext().getString(R.string.prefServerIncompatible);
            summarySpan.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.red)), summary.length(), summary.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            summarySpan.setSpan(new StyleSpan(Typeface.BOLD), summary.length(), summary.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            summarySpan.insert(summary.length(), uncompatible);
        }

        serverPref.setText(server);
        serverPref.setSummary(summarySpan);
    }

    public void updateStoragePref(String storageOption) {
        if (storagePref == null) {
            loadPrefs();
        }
        if (PrefsActivity.STORAGE_OPTION_EXTERNAL.equals(storageOption)) {
            storagePref.setValue(storagePref.getEntryValues()[1].toString());
        } else {
            storagePref.setValue(storageOption);
        }
    }

    private void updateStorageList(Context ctx) {

        List<StorageLocationInfo> storageLocations = StorageUtils.getStorageList(ctx);

        List<String> entries = new ArrayList<>();
        List<String> entryValues = new ArrayList<>();

        String currentOption = App.getPrefs(getActivity()).getString(PrefsActivity.PREF_STORAGE_OPTION, "");

        for (StorageLocationInfo storageLoc : storageLocations) {
            //Only add it as an option if it is writable
            if (!storageLoc.readonly) {
                entries.add(storageLoc.getDisplayName(getActivity()));
                entryValues.add(storageLoc.type);
            }
        }

        storagePref.setEntryValues(entryValues.toArray(new CharSequence[0]));
        storagePref.setEntries(entries.toArray(new CharSequence[0]));
        storagePref.setValue(currentOption);

    }

    @Override
    public void onPreferenceUpdated(String pref, String newValue) {
        if (pref.equals(PrefsActivity.PREF_SERVER)) {
            updateServerPref();
        } else if (pref.equals(PrefsActivity.PREF_STORAGE_OPTION) && (newValue != null)) {
            updateStoragePref(newValue);
        }
    }

}
