package org.digitalcampus.oppia.application;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.utils.UIUtils;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class DrawerDelegate {

    private AppActivity activity;
    private NavigationView nav;
    private ArrayList<Course> courses;

    public DrawerDelegate(AppActivity activity,  ArrayList<Course> courses){
        this.activity = activity;
        this.courses = courses;
    }

    public void initializeDrawer( Toolbar toolbar){
        // Initializing Drawer Layout and ActionBarToggle
        final DrawerLayout drawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer);
        nav = (NavigationView) activity.findViewById(R.id.navigation_view);
        ((TextView) nav.getHeaderView(0).findViewById(R.id.drawer_user_fullname)).setText(SessionManager.getUserDisplayName(activity));
        ((TextView) nav.getHeaderView(0).findViewById(R.id.drawer_username)).setText(SessionManager.getUsername(activity));
        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                boolean result = onOptionsItemSelected(menuItem.getItemId());
                menuItem.setChecked(false);
                drawerLayout.closeDrawers();
                return result;
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(activity, drawerLayout, toolbar, R.string.open, R.string.close){
            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }
            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    public void createOptionsMenu(Menu menu){
        activity.getMenuInflater().inflate(R.menu.activity_main, menu);
    }

    public void prepareOptionsMenu(Menu menu){
        Menu drawerMenu = nav.getMenu();
        MenuItem itemLogout = drawerMenu.findItem(R.id.menu_logout);
        MenuItem itemSettings = drawerMenu.findItem(R.id.menu_settings);
        MenuItem itemMonitor = drawerMenu.findItem(R.id.menu_monitor);
        MenuItem itemCourseDownload = drawerMenu.findItem(R.id.menu_download);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        itemLogout.setVisible(prefs.getBoolean(PrefsActivity.PREF_LOGOUT_ENABLED, MobileLearning.MENU_ALLOW_LOGOUT));
        itemSettings.setVisible(MobileLearning.MENU_ALLOW_SETTINGS);
        itemMonitor.setVisible(MobileLearning.MENU_ALLOW_MONITOR);
        itemCourseDownload.setVisible(MobileLearning.MENU_ALLOW_COURSE_DOWNLOAD);
    }

    public boolean onOptionsItemSelected(final int itemId){
        activity.checkAdminPermission(itemId, new AdminSecurityManager.AuthListener() {
            public void onPermissionGranted() {
                if (itemId == R.id.menu_download) {
                    activity.startActivity(new Intent(activity, TagSelectActivity.class));
                } else if (itemId == R.id.menu_about) {
                    activity.startActivity(new Intent(activity, AboutActivity.class));
                } else if (itemId == R.id.menu_monitor) {
                    activity.startActivity(new Intent(activity, MonitorActivity.class));
                } else if (itemId == R.id.menu_scorecard) {
                    activity.startActivity(new Intent(activity, ScorecardActivity.class));
                } else if (itemId == R.id.menu_search) {
                    activity.startActivity(new Intent(activity, SearchActivity.class));
                } else if (itemId == R.id.menu_settings) {
                    startPrefsActivity();
                } else if (itemId == R.id.menu_language) {
                    createLanguageDialog();
                } else if (itemId == R.id.menu_logout) {
                    logout();
                }
            }
        });

        return true;
    }

    private void startPrefsActivity(){
        Intent i = new Intent(activity, PrefsActivity.class);
        Bundle tb = new Bundle();
        ArrayList<Lang> langs = new ArrayList<>();
        for(Course m: courses){ langs.addAll(m.getLangs()); }
        tb.putSerializable("langs", langs);
        i.putExtras(tb);
        activity.startActivity(i);
    }

    private void createLanguageDialog() {
        ArrayList<Lang> langs = new ArrayList<>();
        for(Course m: courses){ langs.addAll(m.getLangs()); }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        UIUtils.createLanguageDialog(activity, langs, prefs, new Callable<Boolean>() {
            public Boolean call() throws Exception {
                activity.recreate();
                return true;
            }
        });
    }

    private void logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.Oppia_AlertDialogStyle);
        builder.setCancelable(false);
        builder.setTitle(R.string.logout);
        builder.setMessage(R.string.logout_confirm);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                activity.logoutAndRestartApp();
            }
        });
        builder.setNegativeButton(R.string.no, null);
        builder.show();
    }
}
