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


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.AboutActivity;
import org.digitalcampus.oppia.activity.ActivityLogActivity;
import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.SyncActivity;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.SessionManager;

import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

public class DrawerMenuManager {

    public interface MenuOption {
        void onOptionSelected();
    }

    private AppActivity drawerAct;

    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private Map<Integer, MenuOption> customOptions = new HashMap<>();

    public DrawerMenuManager(AppActivity act) {
        drawerAct = act;
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
        itemSettings.setVisible(App.MENU_ALLOW_SETTINGS);
        itemCourseDownload.setVisible(prefs.getBoolean(PrefsActivity.PREF_DOWNLOAD_ENABLED, App.MENU_ALLOW_COURSE_DOWNLOAD));
        itemLanguageDialog.setVisible(customOptions.containsKey(R.id.menu_language) && prefs.getBoolean(PrefsActivity.PREF_CHANGE_LANGUAGE_ENABLED, App.MENU_ALLOW_LANGUAGE));
        itemSync.setVisible(App.MENU_ALLOW_SYNC);
    }

    public void onPostCreate() {
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private boolean onOptionsItemSelected(MenuItem item) {
        // If it is the current selected item, we do nothing
        if (item.isChecked()) return false;

        final int itemId = item.getItemId();
        AdminSecurityManager.with(drawerAct).checkAdminPermission(itemId, () -> {
            // Check if the option has custom manager
            if (customOptions.containsKey(itemId)) {
                customOptions.get(itemId).onOptionSelected();
            } else if (itemId == R.id.menu_download) {
                launchIntentForActivity(TagSelectActivity.class);
            } else if (itemId == R.id.menu_activitylog) {
                launchIntentForActivity(ActivityLogActivity.class);
            } else if (itemId == R.id.menu_about) {
                launchIntentForActivity(AboutActivity.class);
            } else if (itemId == R.id.menu_settings) {
                launchIntentForActivity(PrefsActivity.class);
            } else if (itemId == R.id.menu_sync) {
                launchIntentForActivity(SyncActivity.class);
            }
        });

        return true;
    }

    public void launchIntentForActivity(Class<?> activityClass) {
        Intent i = new Intent(drawerAct, activityClass);
        drawerAct.overridePendingTransition(
                android.R.anim.slide_in_left,
                android.R.anim.fade_out);
        drawerAct.startActivity(i);
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
