package org.digitalcampus.oppia.activity;

import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.DrawerDelegate;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.CourseInstallerListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.service.CourseIntallerService;
import org.digitalcampus.oppia.service.InstallerBroadcastReceiver;
import org.digitalcampus.oppia.utils.UIUtils;

import java.util.ArrayList;

public class SelectCategoryActivity extends AppActivity implements CourseInstallerListener {

    private SharedPreferences prefs;
    private ArrayList<Course> courses;

    private NavigationView navigationView;
    private InstallerBroadcastReceiver receiver;
    private DrawerDelegate drawerDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
        courses = new ArrayList<>();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerDelegate = new DrawerDelegate(this, courses);
        drawerDelegate.initializeDrawer(toolbar);
    }

    @Override
    public void onResume(){
        super.onResume();

        receiver = new InstallerBroadcastReceiver();
        receiver.setCourseInstallerListener(this);
        IntentFilter broadcastFilter = new IntentFilter(CourseIntallerService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(receiver, broadcastFilter);
    }

    @Override
    public void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        drawerDelegate.createOptionsMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        UIUtils.showUserData(menu, this, null);
        drawerDelegate.prepareOptionsMenu(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Log.d(TAG, "Menu item selected: " + item.getTitle());
        return drawerDelegate.onOptionsItemSelected(item.getItemId());
    }

    @Override public void onDownloadProgress(String fileUrl, int progress) {}
    @Override public void onInstallProgress(String fileUrl, int progress) {}
    @Override public void onInstallFailed(String fileUrl, String message) {}

    @Override
    public void onInstallComplete(String fileUrl) {
        //Refresh list
    }
}
