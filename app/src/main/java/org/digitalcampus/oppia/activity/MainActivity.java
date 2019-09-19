package org.digitalcampus.oppia.activity;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.fragments.CoursesListFragment;
import org.digitalcampus.oppia.fragments.MainPointsFragment;
import org.digitalcampus.oppia.fragments.MainScorecardFragment;
import org.digitalcampus.oppia.utils.ui.DrawerMenuManager;

public class MainActivity extends AppActivity implements BottomNavigationView.OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener {

    private BottomNavigationView navView;
    private DrawerMenuManager drawer;
    private View btnEditProfile;
    private View btnLogout;
    private NavigationView navDrawerView;
    private View btnExpandProfileOptions;
    private View viewProfileOptions;
    private MenuItem searchMenuItem;

    private void findViews() {

        navView = findViewById(R.id.nav_bottom_view);
        navDrawerView = findViewById(R.id.navigation_view);

        View headerDrawer = navDrawerView.getHeaderView(0);
        btnEditProfile = headerDrawer.findViewById(R.id.btn_edit_profile);
        btnLogout = headerDrawer.findViewById(R.id.btn_logout);
        btnExpandProfileOptions = headerDrawer.findViewById(R.id.btn_expand_profile_options);
        viewProfileOptions = headerDrawer.findViewById(R.id.view_profile_options);

        btnExpandProfileOptions.setOnClickListener(this);
        btnEditProfile.setOnClickListener(this);
        btnLogout.setOnClickListener(this);

        navView.setOnNavigationItemSelectedListener(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();

        viewProfileOptions.setVisibility(View.GONE);

        btnEditProfile.setVisibility(View.GONE); // TODO Edit profile feature.

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, new CoursesListFragment()).commit();

//        startActivity(new Intent(this, ScorecardActivity.class));

    }

    @Override
    public void onResume() {
        super.onResume();

        configureLogoutOption();
    }

    @Override
    public void onStart() {
        super.onStart();
        drawer = new DrawerMenuManager(this);
        drawer.initializeDrawer();

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawer.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        drawer.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        searchMenuItem = menu.findItem(R.id.menu_search);
        return super.onCreateOptionsMenu(menu);
    }

    // CONFIGURATIONS
    private void configureLogoutOption() {
        boolean logoutVisible = getPrefs().getBoolean(PrefsActivity.PREF_LOGOUT_ENABLED, MobileLearning.MENU_ALLOW_LOGOUT);
        btnLogout.setVisibility(logoutVisible ? View.VISIBLE : View.GONE);
        btnExpandProfileOptions.setVisibility(logoutVisible ? View.VISIBLE : View.GONE); // TODO Edit profile feature.
        if (!logoutVisible) {
            setupProfileOptionsView(false);
        }
    }

    private void setupProfileOptionsView(boolean visible) {
        viewProfileOptions.setVisibility(visible ? View.VISIBLE : View.GONE);
        btnExpandProfileOptions.setRotation(visible ? 180f : 0f);
    }

    // INTERACTIONS
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        Fragment fragment = null;

        switch (menuItem.getItemId()) {
            case R.id.nav_bottom_home:
                fragment = new CoursesListFragment();
                break;

            case R.id.nav_bottom_scorecard:
                fragment = MainScorecardFragment.newInstance();
                break;

            case R.id.nav_bottom_points:
                fragment = MainPointsFragment.newInstance();
                break;
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, fragment).commit();

        searchMenuItem.setVisible(menuItem.getItemId() == R.id.nav_bottom_home);

        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_expand_profile_options:
                boolean isProfileOptionsViewVisible = viewProfileOptions.getVisibility() == View.VISIBLE;
                setupProfileOptionsView(!isProfileOptionsViewVisible);
                break;

            case R.id.btn_edit_profile:
                // TODO Edit profile feature.
                break;

            case R.id.btn_logout:
                drawer.logout();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                drawer.launchIntentForActivity(SearchActivity.class);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(key.equalsIgnoreCase(PrefsActivity.PREF_DOWNLOAD_VIA_CELLULAR_ENABLED)){
            boolean newPref = sharedPreferences.getBoolean(PrefsActivity.PREF_DOWNLOAD_VIA_CELLULAR_ENABLED, false);
            Log.d(TAG, PrefsActivity.PREF_DOWNLOAD_VIA_CELLULAR_ENABLED + ": " + newPref);
        }

        // update the points/badges by invalidating the menu
        if(key.equalsIgnoreCase(PrefsActivity.PREF_TRIGGER_POINTS_REFRESH)){
            // TODO adapt this
        }

    }

}
