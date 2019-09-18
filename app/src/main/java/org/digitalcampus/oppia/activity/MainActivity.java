package org.digitalcampus.oppia.activity;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.fragments.CoursesListFragment;
import org.digitalcampus.oppia.fragments.GlobalScorecardFragment;
import org.digitalcampus.oppia.fragments.PointsFragment;
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
        drawer = new DrawerMenuManager(this, true);
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

    // CONFIGURATIONS
    private void configureLogoutOption() {
        boolean logoutVisible = getPrefs().getBoolean(PrefsActivity.PREF_LOGOUT_ENABLED, MobileLearning.MENU_ALLOW_LOGOUT);
        btnLogout.setVisibility(logoutVisible ? View.VISIBLE : View.GONE);
        btnExpandProfileOptions.setVisibility(logoutVisible ? View.VISIBLE : View.GONE); // TODO Edit profile feature.
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
                fragment = GlobalScorecardFragment.newInstance();
                break;

            case R.id.nav_bottom_points:
                fragment = PointsFragment.newInstance(null);
                break;
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, fragment).commit();
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_expand_profile_options:
                boolean isProfileOptionsViewVisible = viewProfileOptions.getVisibility() == View.VISIBLE;
                viewProfileOptions.setVisibility(isProfileOptionsViewVisible ? View.GONE : View.VISIBLE);
                btnExpandProfileOptions.setRotation(!isProfileOptionsViewVisible ? 180f : 0f);
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
