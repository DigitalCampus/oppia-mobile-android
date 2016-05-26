package org.digitalcampus.oppia.activity;

import android.content.IntentFilter;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.CourseInstallerListener;
import org.digitalcampus.oppia.service.CourseIntallerService;
import org.digitalcampus.oppia.service.InstallerBroadcastReceiver;

public class SelectCategoryActivity extends AppActivity implements CourseInstallerListener {

    private NavigationView navigationView;
    private InstallerBroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_category);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initializing Drawer Layout and ActionBarToggle
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.drawer_user_fullname)).setText(SessionManager.getUserDisplayName(this));
        ((TextView) navigationView.getHeaderView(0).findViewById(R.id.drawer_username)).setText(SessionManager.getUsername(this));
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                boolean result = onOptionsItemSelected(menuItem);
                menuItem.setChecked(false);
                drawerLayout.closeDrawers();
                return result;
            }
        });
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

    @Override public void onDownloadProgress(String fileUrl, int progress) {}
    @Override public void onInstallProgress(String fileUrl, int progress) {}
    @Override public void onInstallFailed(String fileUrl, String message) {}

    @Override
    public void onInstallComplete(String fileUrl) {
        //Refresh list
    }
}
