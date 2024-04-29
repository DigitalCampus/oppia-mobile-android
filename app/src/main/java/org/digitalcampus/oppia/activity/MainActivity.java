package org.digitalcampus.oppia.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.digitalcampus.mobile.learning.BuildConfig;
import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivityMainBinding;
import org.digitalcampus.mobile.learning.databinding.DrawerHeaderBinding;
import org.digitalcampus.mobile.learning.databinding.ViewBadgeBinding;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.PermissionsManager;
import org.digitalcampus.oppia.fragments.CoursesListFragment;
import org.digitalcampus.oppia.fragments.MainPointsFragment;
import org.digitalcampus.oppia.fragments.MainScorecardFragment;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.repository.InterfaceLanguagesRepository;
import org.digitalcampus.oppia.task.FetchServerInfoTask;
import org.digitalcampus.oppia.utils.ConnectionUtils;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.ui.DrawerMenuManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

public class MainActivity extends AppActivity implements BottomNavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener {

    public static final String EXTRA_FIRST_LOGIN = "extra_first_login";

    private DrawerMenuManager drawer;
    private MenuItem searchMenuItem;

    @Inject
    CoursesRepository coursesRepository;
    private ActivityMainBinding binding;
    private DrawerHeaderBinding bindingHeader;
    private ViewBadgeBinding bindingBadgeView;
    private boolean stagingWarningClosed;

    @Inject
    InterfaceLanguagesRepository interfaceLanguagesRepository;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        getAppComponent().inject(this);

        bindingHeader = DrawerHeaderBinding.bind(binding.navigationView.getHeaderView(0));

        bindingHeader.btnExpandProfileOptions.setOnClickListener(this);
        bindingHeader.btnEditProfile.setOnClickListener(this);
        bindingHeader.btnChangePassword.setOnClickListener(this);
        bindingHeader.btnLogout.setOnClickListener(this);

        binding.navBottomView.setOnNavigationItemSelectedListener(this);

        configureBadgePointsView();

        bindingHeader.viewProfileOptions.setVisibility(View.GONE);

        getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, getCoursesListFragment()).commit();

        saveServerBadgeAwardCriteria();

        binding.drawerVersionName.setText(getString(R.string.version, BuildConfig.VERSION_NAME));

        checkNotificationPermission();

    }


    private void checkNotificationPermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            final List<String> notGrantedPerms = PermissionsManager.filterNotGrantedPermissions(
                    this, PermissionsManager.NOTIFICATIONS_PERMISSIONS_REQUIRED);
            if (!notGrantedPerms.isEmpty()) {
                PermissionsManager.requestPermissions(this, notGrantedPerms);
            }
        }
    }

    private Fragment getCoursesListFragment() {
        Fragment fragment = new CoursesListFragment();
        fragment.setArguments(getIntent().getExtras());
        return fragment;
    }

    private void saveServerBadgeAwardCriteria() {
        if (getPrefs().getString(PrefsActivity.PREF_BADGE_AWARD_CRITERIA, null) == null) {
            new FetchServerInfoTask(this).execute();
        }
    }

    private void configureBadgePointsView() {

        BottomNavigationMenuView bottomNavigationMenuView =
                (BottomNavigationMenuView) binding.navBottomView.getChildAt(0);
        BottomNavigationItemView itemView = (BottomNavigationItemView) bottomNavigationMenuView.getChildAt(2);

        bindingBadgeView = ViewBadgeBinding.inflate(LayoutInflater.from(this));
        itemView.addView(bindingBadgeView.getRoot());
    }

    @Override
    public void onResume() {
        super.onResume();

        configureUserOptions();
        updateUserTotalPoints();

        checkStagingServer();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionsManager.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void checkStagingServer() {
        String server = prefs.getString(PrefsActivity.PREF_SERVER, getString(R.string.prefServerDefault));
        if (server == null) return;
        boolean staging = server.startsWith("http://staging.") || server.startsWith("https://staging.");
        boolean showWarning = staging && !stagingWarningClosed;
        binding.viewStagingWarning.setVisibility(showWarning ? View.VISIBLE : View.GONE);

        binding.imgCloseStagingWarning.setOnClickListener(view -> {
            binding.viewStagingWarning.setVisibility(View.GONE);
            stagingWarningClosed = true;
        });
    }

    private void updateUserTotalPoints() {

        App app = (App) getApplicationContext();
        User u = app.getComponent().getUser();

        bindingBadgeView.tvBadgeNumber.setText(String.valueOf(u.getPoints()));
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();
        drawer = new DrawerMenuManager(this , getSupportFragmentManager());
        drawer.initializeDrawer();

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        Map<Integer, DrawerMenuManager.MenuOption> options = getMenuOptions();
        drawer.onPrepareOptionsMenu(null,options);

        configureSearchButtonVisibility(binding.navBottomView.getSelectedItemId());

        return super.onPrepareOptionsMenu(menu);
    }

    private Map<Integer, DrawerMenuManager.MenuOption> getMenuOptions() {

        Map<Integer, DrawerMenuManager.MenuOption> options = new HashMap<>();
        Map<String, String> interfaceLanguages = LangCodesWithNames();
        //String[] interfaceLangs = interfaceLanguagesRepository.getLanguageOptions();
        final ArrayList<Lang> langs = new ArrayList<>();
        List<Course> courses = coursesRepository.getCourses(this);


        for (Course course : courses) {
            for (Lang courseLang : course.getLangs()) {
                if (!langs.contains(courseLang)) {
                    langs.add(courseLang);
                }
            }
        }

        // Change language menu option only should be visible if there is more than one language
        if (langs.size() > 1) {
            options.put(R.id.menu_language, () -> showLanguageSelectDialog(langs));
        }
        // show interface language
        if (interfaceLanguages.size() > 1) {
            options.put(R.id.menu_interface_language, () -> showInterfaceLanguageSelectDialog(interfaceLanguages));
        }

        return options;
    }

    private void showLanguageSelectDialog(ArrayList<Lang> langs) {

        UIUtils.createLanguageDialog(this, langs, prefs, () -> {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_main);
            if (fragment instanceof CoursesListFragment) {
                // Refresh courses list
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, new CoursesListFragment()).commit();
            }

            return false;
        });
    }
    private void showInterfaceLanguageSelectDialog(Map<String, String> langs) {

        UIUtils.createInterfaceLanguageDialog(this, langs, prefs, () -> {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_main);
            if (fragment instanceof CoursesListFragment) {
                // Refresh courses list
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_main, new CoursesListFragment()).commit();
            }

            return false;
        });
    }

    private Map<String, String> LangCodesWithNames() {
        String[] langs = interfaceLanguagesRepository.getLanguageOptions();
        List<String> langCodes = Arrays.asList(langs);
        if (langs.length > 0) {

            return langCodes.stream().collect(Collectors.toMap(
                    langCode -> new Locale(langCode).getDisplayLanguage(new Locale(langCode)),
                    langCode -> langCode));
        }
        return null;
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
    private void configureUserOptions() {
        boolean logoutVisible = getPrefs().getBoolean(PrefsActivity.PREF_LOGOUT_ENABLED, BuildConfig.MENU_ALLOW_LOGOUT);
        bindingHeader.btnLogout.setVisibility(logoutVisible ? View.VISIBLE : View.GONE);
        if (!logoutVisible) {
            setupProfileOptionsView(false);
        }

        bindingHeader.btnEditProfile.setVisibility(BuildConfig.MENU_ALLOW_EDIT_PROFILE ? View.VISIBLE : View.GONE);
        bindingHeader.btnChangePassword.setVisibility(BuildConfig.MENU_ALLOW_CHANGE_PASSWORD ? View.VISIBLE : View.GONE);
    }

    private void setupProfileOptionsView(boolean visible) {
        bindingHeader.viewProfileOptions.setVisibility(visible ? View.VISIBLE : View.GONE);
        bindingHeader.btnExpandProfileOptions.setRotation(visible ? 180f : 0f);
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
                boolean isProfileOptionsViewVisible = bindingHeader.viewProfileOptions.getVisibility() == View.VISIBLE;
                setupProfileOptionsView(!isProfileOptionsViewVisible);
                break;

            case R.id.btn_edit_profile:
                checkConnectionAndOpenActivity(EditProfileActivity.class);
                break;

            case R.id.btn_change_password:
                checkConnectionAndOpenActivity(ChangePasswordActivity.class);
                break;

            case R.id.btn_logout:
                drawer.logout();
                break;

            default:
                // do nothing
        }
    }

    private void checkConnectionAndOpenActivity(Class<? extends AppActivity> activityClass) {
        if (ConnectionUtils.isNetworkConnected(this)) {
            startActivity(new Intent(this, activityClass));
        } else {
            alert(R.string.option_available_online);
        }

        setupProfileOptionsView(false);
        drawer.close();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_search) {
            drawer.launchIntentForActivity(SearchActivity.class);
        }
        return super.onOptionsItemSelected(item);
    }

}
