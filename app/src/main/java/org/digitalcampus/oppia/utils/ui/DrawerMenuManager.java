package org.digitalcampus.oppia.utils.ui;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.AboutActivity;
import org.digitalcampus.oppia.activity.AppActivity;
import org.digitalcampus.oppia.activity.MonitorActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.activity.ScorecardActivity;
import org.digitalcampus.oppia.activity.SearchActivity;
import org.digitalcampus.oppia.activity.TagSelectActivity;
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;

import java.util.HashMap;
import java.util.Map;

public class DrawerMenuManager {

    public interface MenuOption{ void onOptionSelected(); }

    private AppActivity drawerAct;
    private boolean isRootActivity = false;

    private NavigationView navigationView;
    private ActionBarDrawerToggle drawerToggle;
    private Map<Integer, MenuOption> additionalOptions = new HashMap<>();

    public DrawerMenuManager(AppActivity act){
        drawerAct = act;
    }

    public DrawerMenuManager(AppActivity act, boolean isRootActivity){
        drawerAct = act;
        this.isRootActivity = isRootActivity;
    }

    public void initializeDrawer(){
        // Initializing Drawer Layout and ActionBarToggle
        final Toolbar toolbar = (Toolbar) drawerAct.findViewById(R.id.toolbar);
        final DrawerLayout drawerLayout = (DrawerLayout) drawerAct.findViewById(R.id.drawer);
        navigationView = (NavigationView) drawerAct.findViewById(R.id.navigation_view);

        if (drawerLayout == null || navigationView == null) return;
        View headerView = navigationView.getHeaderView(0);
        ((TextView) headerView.findViewById(R.id.drawer_user_fullname)).setText(
                SessionManager.getUserDisplayName(drawerAct));
        ((TextView) headerView.findViewById(R.id.drawer_username)).setText(
                SessionManager.getUsername(drawerAct));
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                boolean result = onOptionsItemSelected(menuItem);
                menuItem.setChecked(false);
                drawerLayout.closeDrawers();
                return result;
            }
        });

        drawerToggle = new ActionBarDrawerToggle(drawerAct,drawerLayout,toolbar,R.string.open, R.string.close);
        //Setting the actionbarToggle to drawer layout
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    public void onPrepareOptionsMenu(Menu menu){
        this.onPrepareOptionsMenu(menu, null);
    }
    public void onPrepareOptionsMenu(Menu menu, Map<Integer, MenuOption> options){

        if (options != null)
            this.additionalOptions = options;

        Menu drawerMenu = navigationView.getMenu();
        MenuItem itemLogout = drawerMenu.findItem(R.id.menu_logout);
        MenuItem itemSettings = drawerMenu.findItem(R.id.menu_settings);
        MenuItem itemMonitor = drawerMenu.findItem(R.id.menu_monitor);
        MenuItem itemCourseDownload = drawerMenu.findItem(R.id.menu_download);
        MenuItem itemLanguageDialog = drawerMenu.findItem(R.id.menu_language);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(drawerAct);
        itemLogout.setVisible(prefs.getBoolean(PrefsActivity.PREF_LOGOUT_ENABLED, MobileLearning.MENU_ALLOW_LOGOUT));
        itemSettings.setVisible(MobileLearning.MENU_ALLOW_SETTINGS && additionalOptions.containsKey(R.id.menu_settings));
        itemMonitor.setVisible(MobileLearning.MENU_ALLOW_MONITOR);
        itemCourseDownload.setVisible(MobileLearning.MENU_ALLOW_COURSE_DOWNLOAD);
        itemLanguageDialog.setVisible(additionalOptions.containsKey(R.id.menu_language));
    }

    public void onPostCreate(Bundle savedInstanceState){
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    public void onConfigurationChanged(Configuration newConfig){
        drawerToggle.onConfigurationChanged(newConfig);
    }

    private boolean onOptionsItemSelected(MenuItem item){
        final int itemId = item.getItemId();
        AdminSecurityManager.checkAdminPermission(drawerAct, itemId, new AdminSecurityManager.AuthListener() {
            public void onPermissionGranted() {
                Intent i;

                if (itemId == R.id.menu_download) {
                    launchIntentForActivity(TagSelectActivity.class);
                } else if (itemId == R.id.menu_about) {
                    launchIntentForActivity(AboutActivity.class);
                } else if (itemId == R.id.menu_monitor) {
                    launchIntentForActivity(MonitorActivity.class);
                } else if (itemId == R.id.menu_scorecard) {
                    launchIntentForActivity(ScorecardActivity.class);
                } else if (itemId == R.id.menu_search) {
                    launchIntentForActivity(SearchActivity.class);
                } else if (itemId == R.id.menu_logout) {
                    logout();
                }else{
                    //If it is another option, check that we have it in the additional options
                    if(additionalOptions.containsKey(itemId)){
                        additionalOptions.get(itemId).onOptionSelected();
                    }
                }
            }
        });

        return true;
    }

    private void launchIntentForActivity(Class<?> activityClass){
        Intent i = new Intent(drawerAct, activityClass);
        if (!this.isRootActivity){
            //If the activity was not the root one, we close it
            drawerAct.finish();
        }
        drawerAct.startActivity(i);
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(drawerAct, R.style.Oppia_AlertDialogStyle);
        builder.setCancelable(false);
        builder.setTitle(R.string.logout);
        builder.setMessage(R.string.logout_confirm);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                drawerAct.logoutAndRestartApp();
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }

}
