/*
 * This file is part of OppiaMobile - https://digital-campus.org/
 *
 * OppiaMobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OppiaMobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OppiaMobile. If not, see <http://www.gnu.org/licenses/>.
 */

package org.digitalcampus.oppia.utils.ui;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.AboutActivity;
import org.digitalcampus.oppia.activity.ActivityLogActivity;
import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.PrivacyActivity;
import org.digitalcampus.oppia.activity.SyncActivity;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.fragments.prefs.BasePreferenceFragment;
import org.digitalcampus.oppia.fragments.prefs.PreferenceChangedCallback;
import org.digitalcampus.oppia.repository.InterfaceLanguagesRepository;
import org.digitalcampus.oppia.utils.FragmentUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.EditTextPreferenceDialogFragmentCompat;
import androidx.preference.ListPreference;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreferenceDialogFragmentCompat;
import androidx.preference.MultiSelectListPreferenceDialogFragmentCompat;
import androidx.preference.Preference;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.PreferenceManager;

import javax.inject.Inject;

public class DrawerMenuManager extends BasePreferenceFragment implements PreferenceChangedCallback {

    private static final String DIALOG_FRAGMENT_TAG = "DIALOG_FRAGMENT_TAG";
  //  private PreferenceManager mPreferenceManager = new PreferenceManager(requireContext());
    @Override
    public void onPreferenceUpdated(String pref, String newValue) {

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final TypedValue tv = new TypedValue();
        requireContext().getTheme().resolveAttribute(R.attr.preferenceTheme, tv, true);
        int theme = tv.resourceId;
        if (theme == 0) {
            // Fallback to default theme.
            theme = R.style.PreferenceThemeOverlay;
        }
        requireContext().getTheme().applyStyle(theme, false);

//        mPreferenceManager = new PreferenceManager(requireContext());
//        mPreferenceManager.setOnNavigateToScreenListener(this);
        final Bundle args = getArguments();
        final String rootKey;
        if (args != null) {
            rootKey = getArguments().getString(ARG_PREFERENCE_ROOT);
        } else {
            rootKey = null;
        }
        onCreatePreferences(savedInstanceState, rootKey);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    public interface MenuOption {
        void onOptionSelected();
    }
    @Inject
    InterfaceLanguagesRepository interfaceLanguagesRepository;
    private AppActivity drawerAct;

    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private Map<Integer, MenuOption> customOptions = new HashMap<>();

    public DrawerMenuManager(AppActivity act, InterfaceLanguagesRepository interfaceLanguagesRepository) {
        drawerAct = act;
        this.interfaceLanguagesRepository = interfaceLanguagesRepository;

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from XML resources
        addPreferencesFromResource(R.xml.prefs_display);
        loadInterfaceLangs();
    }
    public void initializeDrawer() {
        // Initializing Drawer Layout and ActionBarToggle
        final Toolbar toolbar = drawerAct.findViewById(R.id.toolbar);
        drawerLayout = drawerAct.findViewById(R.id.drawer);
        navigationView = drawerAct.findViewById(R.id.navigation_view);

        if (drawerLayout == null || navigationView == null) return;
        View headerView = navigationView.getHeaderView(0);
        ((TextView) headerView.findViewById(R.id.drawer_user_fullname)).setText(
                SessionManager.getUserDisplayName(drawerAct));
        ((TextView) headerView.findViewById(R.id.drawer_username)).setText(
                SessionManager.getUsername(drawerAct));
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            boolean result = onOptionsItemSelected(menuItem);
            if (result) {
                menuItem.setChecked(false);
                drawerLayout.closeDrawers();
            }
            return result;
        });



        drawerToggle = new ActionBarDrawerToggle(drawerAct, drawerLayout, toolbar, R.string.open, R.string.close);
        //Setting the actionbarToggle to drawer layout
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();


    }

    public void onPrepareOptionsMenu(Map<Integer, MenuOption> options) {
        this.onPrepareOptionsMenu(null, options);
    }

    public void onPrepareOptionsMenu(Integer currentOption, Map<Integer, MenuOption> options) {

        if (options != null)
            this.customOptions = options;

        Menu drawerMenu = navigationView.getMenu();
        MenuItem itemSettings = drawerMenu.findItem(R.id.menu_settings);
        MenuItem itemCourseDownload = drawerMenu.findItem(R.id.menu_download);
        MenuItem itemLanguageDialog = drawerMenu.findItem(R.id.menu_language);
        MenuItem itemSync = drawerMenu.findItem(R.id.menu_sync);

        if (currentOption != null) {
            MenuItem current = drawerMenu.findItem(currentOption);
            if (current != null) {
                current.setCheckable(true);
                current.setChecked(true);
            }
        }

        SharedPreferences prefs = drawerAct.getPrefs();
        itemSettings.setVisible(BuildConfig.MENU_ALLOW_SETTINGS);
        itemCourseDownload.setVisible(prefs.getBoolean(PrefsActivity.PREF_DOWNLOAD_ENABLED, BuildConfig.MENU_ALLOW_COURSE_DOWNLOAD));
        itemLanguageDialog.setVisible(customOptions.containsKey(R.id.menu_language)
                && prefs.getBoolean(PrefsActivity.PREF_CHANGE_LANGUAGE_ENABLED, BuildConfig.MENU_ALLOW_LANGUAGE));
        itemSync.setVisible(BuildConfig.MENU_ALLOW_SYNC);
        loadInterfaceLangs();
        ListPreference prefInterfaceLangs = findPreference(PrefsActivity.PREF_INTERFACE_LANGUAGE);
        if (prefInterfaceLangs != null) {

        }
        liveUpdateSummary(PrefsActivity.PREF_TEXT_SIZE);
    }

    public void onPostCreate() {
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // If it is the current selected item, we do nothing
        if (item.isChecked()) return false;

        final int itemId = item.getItemId();
        AdminSecurityManager.with(drawerAct).checkAdminPermission(itemId, () -> {
            // Check if the option has custom manager
            if (customOptions.containsKey(itemId)) {
                customOptions.get(itemId).onOptionSelected();
            } else if (itemId == R.id.menu_download) {
                launchIntentForActivity(TagSelectActivity.class);
            } else if (itemId == R.id.menu_privacy) {
                launchIntentForActivity(PrivacyActivity.class);
            } else if (itemId == R.id.menu_activitylog) {
                launchIntentForActivity(ActivityLogActivity.class);
            } else if (itemId == R.id.menu_about) {
                launchIntentForActivity(AboutActivity.class);
            } else if (itemId == R.id.menu_settings) {
                launchIntentForActivity(PrefsActivity.class);
            } else if (itemId == R.id.menu_sync) {
                launchIntentForActivity(SyncActivity.class);
            }
            else if (itemId == R.id.prefInterfaceLanguage) {
                Preference preference = findPreference("PREF_INTERFACE_LANGUAGE");
                onDisplayPreferenceDialog(preference);
            }
        });

        return true;
    }
    public void onDisplayPreferenceDialog(@NonNull Preference preference) {
        boolean handled = false;
        Fragment callbackFrag = FragmentUtils.findCallbackFragment(this);
        if (callbackFrag instanceof OnPreferenceDisplayDialogCallback) {
            ((OnPreferenceDisplayDialogCallback) callbackFrag).onPreferenceDisplayDialog(this, preference);
        }
        //  If the callback fragment doesn't handle OnPreferenceDisplayDialogCallback, looks up
        //  its parent fragment in the hierarchy that implements the callback until the first
        //  one that returns true
        Fragment callbackFragment = this;
        while (!handled && callbackFragment != null) {
            if (callbackFragment instanceof OnPreferenceDisplayDialogCallback) {
                handled = ((OnPreferenceDisplayDialogCallback) callbackFragment)
                        .onPreferenceDisplayDialog(this, preference);
            }
            callbackFragment = callbackFragment.getParentFragment();
        }
        if (!handled && getContext() instanceof OnPreferenceDisplayDialogCallback) {
            handled = ((OnPreferenceDisplayDialogCallback) getContext())
                    .onPreferenceDisplayDialog(this, preference);
        }
        // Check the Activity as well in case getContext was overridden to return something other
        // than the Activity.
        if (!handled && getActivity() instanceof OnPreferenceDisplayDialogCallback) {
            handled = ((OnPreferenceDisplayDialogCallback) getActivity())
                    .onPreferenceDisplayDialog(this, preference);
        }

        if (handled) {
            return;
        }

        // check if dialog is already showing
        if (getParentFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
            return;
        }

        final DialogFragment f;
        if (preference instanceof EditTextPreference) {
            f = EditTextPreferenceDialogFragmentCompat.newInstance(preference.getKey());
        } else if (preference instanceof ListPreference) {
            f = ListPreferenceDialogFragmentCompat.newInstance(preference.getKey());
        } else if (preference instanceof MultiSelectListPreference) {
            f = MultiSelectListPreferenceDialogFragmentCompat.newInstance(preference.getKey());
        }  else {
            throw new IllegalArgumentException(
                    "Cannot display dialog for an unknown Preference type: "
                            + preference.getClass().getSimpleName()
                            + ". Make sure to implement onPreferenceDisplayDialog() to handle "
                            + "displaying a custom dialog for this Preference.");
        }
        f.setTargetFragment(this, 0);
        f.show(getParentFragmentManager(), DIALOG_FRAGMENT_TAG);
    }

    public void launchIntentForActivity(Class<?> activityClass) {
        Intent i = new Intent(drawerAct, activityClass);
        drawerAct.overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.fade_out);
        drawerAct.startActivity(i);
    }
    private Map<String, String> mapLangCodesToNames(List<String> langCodes) {
        return langCodes.stream().collect(Collectors.toMap(
                langCode -> new Locale(langCode).getDisplayLanguage(new Locale(langCode)),
                langCode -> langCode));
    }

    private void loadInterfaceLangs() {
        try {
            final TypedValue tv = new TypedValue();
            requireContext().getTheme().resolveAttribute(R.attr.preferenceTheme, tv, true);
            int theme = tv.resourceId;
            if (theme == 0) {
                // Fallback to default theme.
                theme = R.style.PreferenceThemeOverlay;
            }
            requireContext().getTheme().applyStyle(theme, false);

//            mPreferenceManager = new PreferenceManager(requireContext());
//            mPreferenceManager.setOnNavigateToScreenListener(this);
            final Bundle args = getArguments();
            final String rootKey;
            if (args != null) {
                rootKey = getArguments().getString(ARG_PREFERENCE_ROOT);
            } else {
                rootKey = null;
            }
            onCreatePreferences(null, rootKey);
            String[] langs = interfaceLanguagesRepository.getLanguageOptions();
            if (langs != null && langs.length > 0) {
                Map<String, String> langNames = mapLangCodesToNames(Arrays.asList(langs));
                Menu drawerMenu = navigationView.getMenu();
                MenuItem languagePref = drawerMenu.findItem(R.id.prefInterfaceLanguage);
                String prefKey= PrefsActivity.PREF_INTERFACE_LANGUAGE;
                Preference pref = findPreference(prefKey);
                String selectedLanguage =languagePref.getTitle() + " -- " +  langNames.get(langs[0]);
                languagePref.setTitle(selectedLanguage);

                liveUpdateSummary(PrefsActivity.PREF_INTERFACE_LANGUAGE);
            } else {
                // Handle case when langs is null or empty
                Log.e(null, "No language options available.");
            }
        } catch (Exception e) {
               Log.e(null, "Error loading interface languages: " + e.getMessage());
        }
    }
    public void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(drawerAct);
        builder.setCancelable(false);
        builder.setTitle(R.string.logout);
        builder.setMessage(R.string.logout_confirm);
        builder.setPositiveButton(R.string.yes, (dialog, which) -> drawerAct.logoutAndRestartApp());
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

    public void close() {
        drawerLayout.closeDrawer(GravityCompat.START);
    }
}
