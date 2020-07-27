package org.digitalcampus.oppia.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.fragments.CoursesListFragment;
import org.digitalcampus.oppia.fragments.MainPointsFragment;
import org.digitalcampus.oppia.fragments.MainScorecardFragment;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.ConnectionUtils;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.ui.DrawerMenuManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

public class MainActivity extends AppActivity implements BottomNavigationView.OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener {

    private BottomNavigationView navBottomView;
    private DrawerMenuManager drawer;
    private View btnLogout;
    private View btnExpandProfileOptions;
    private View viewProfileOptions;
    private MenuItem searchMenuItem;
    private TextView tvBadgeNumber;

    @Inject
    CoursesRepository coursesRepository;

    private void findViews() {

        navBottomView = findViewById(R.id.nav_bottom_view);
        NavigationView navDrawerView = findViewById(R.id.navigation_view);

        View headerDrawer = navDrawerView.getHeaderView(0);
        View btnEditProfile = headerDrawer.findViewById(R.id.btn_edit_profile);
        btnLogout = headerDrawer.findViewById(R.id.btn_logout);
        btnExpandProfileOptions = headerDrawer.findViewById(R.id.btn_expand_profile_options);
        viewProfileOptions = headerDrawer.findViewById(R.id.view_profile_options);

        btnExpandProfileOptions.setOnClickListener(this);
        btnEditProfile.setOnClickListener(this);
        btnLogout.setOnClickListener(this);

        navBottomView.setOnNavigationItemSelectedListener(this);
    }


    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        getAppComponent().inject(this);

        configureBadgePointsView();

        viewProfileOptions.setVisibility(View.GONE);

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, new CoursesListFragment()).commit();
    }

    private void configureBadgePointsView() {

        BottomNavigationMenuView bottomNavigationMenuView =
                (BottomNavigationMenuView) navBottomView.getChildAt(0);
        BottomNavigationItemView itemView = (BottomNavigationItemView) bottomNavigationMenuView.getChildAt(2);

        View badgeView = LayoutInflater.from(this).inflate(R.layout.view_badge, null);

        tvBadgeNumber = badgeView.findViewById(R.id.tv_badge_number);

        itemView.addView(badgeView);
    }

    @Override
    public void onResume() {
        super.onResume();

        configureLogoutOption();
        updateUserTotalPoints();
    }

    private void updateUserTotalPoints() {

        App app = (App) getApplicationContext();
        User u = app.getComponent().getUser();

        tvBadgeNumber.setText(String.valueOf(u.getPoints()));
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();
        drawer = new DrawerMenuManager(this);
        drawer.initializeDrawer();

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        Map<Integer, DrawerMenuManager.MenuOption> options = getMenuOptions();
        drawer.onPrepareOptionsMenu(options);

        configureSearchButtonVisibility(navBottomView.getSelectedItemId());

        return super.onPrepareOptionsMenu(menu);
    }

    private Map<Integer, DrawerMenuManager.MenuOption> getMenuOptions() {

        Map<Integer, DrawerMenuManager.MenuOption> options = new HashMap<>();

        final ArrayList<Lang> langs = new ArrayList<>();
        List<Course> courses = coursesRepository.getCourses(this);

        for (Course course : courses) {
            for (Lang courseLang : course.getLangs()) {
                if (!langs.contains(courseLang)) {
                    langs.add(courseLang);
                }
            }
        }

        // Change language menu option only should be visible if there are more than one language
        if (langs.size() > 1) {
            options.put(R.id.menu_language, new DrawerMenuManager.MenuOption() {
                @Override
                public void onOptionSelected() {
                    showLanguageSelectDialog(langs);
                }
            });
        }

        return options;
    }

    private void showLanguageSelectDialog(ArrayList<Lang> langs) {

        UIUtils.createLanguageDialog(this, langs, prefs, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_main);
                if (fragment instanceof CoursesListFragment) {
                    // Refresh courses list
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, new CoursesListFragment()).commit();
                }

                return false;
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawer.onPostCreate();
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
        boolean logoutVisible = getPrefs().getBoolean(PrefsActivity.PREF_LOGOUT_ENABLED, App.MENU_ALLOW_LOGOUT);
        btnLogout.setVisibility(logoutVisible ? View.VISIBLE : View.GONE);
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

        Fragment fragment;

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

                default:
                    throw new IllegalArgumentException("menuItem not valid: " + menuItem.toString());
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, fragment).commit();

        configureSearchButtonVisibility(menuItem.getItemId());

        return true;
    }

    private void configureSearchButtonVisibility(int itemId) {
        searchMenuItem.setVisible(itemId == R.id.nav_bottom_home);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btn_expand_profile_options:
                boolean isProfileOptionsViewVisible = viewProfileOptions.getVisibility() == View.VISIBLE;
                setupProfileOptionsView(!isProfileOptionsViewVisible);
                break;

            case R.id.btn_edit_profile:
                if (ConnectionUtils.isNetworkConnected(this)) {
                    startActivity(new Intent(this, EditProfileActivity.class));
                } else {
                    alert(R.string.edit_profile_available_online);
                }

                setupProfileOptionsView(false);
                drawer.close();
                break;

            case R.id.btn_logout:
                drawer.logout();
                break;

            default:
                // do nothing
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() ==  R.id.menu_search) {
            drawer.launchIntentForActivity(SearchActivity.class);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(key.equalsIgnoreCase(PrefsActivity.PREF_DOWNLOAD_VIA_CELLULAR_ENABLED)){
            boolean newPref = sharedPreferences.getBoolean(PrefsActivity.PREF_DOWNLOAD_VIA_CELLULAR_ENABLED, false);
            Log.d(TAG, PrefsActivity.PREF_DOWNLOAD_VIA_CELLULAR_ENABLED + ": " + newPref);
        }
    }

}
