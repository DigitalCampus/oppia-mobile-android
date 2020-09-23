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

package org.digitalcampus.oppia.activity;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.di.AppComponent;
import org.digitalcampus.oppia.gamification.Gamification;
import org.digitalcampus.oppia.gamification.GamificationBroadcastReceiver;
import org.digitalcampus.oppia.gamification.GamificationService;
import org.digitalcampus.oppia.listener.APIKeyRequestListener;
import org.digitalcampus.oppia.listener.GamificationEventListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.UIUtils;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;


public class AppActivity extends AppCompatActivity implements APIKeyRequestListener, GamificationEventListener {

    protected static final String TAG = AppActivity.class.getSimpleName();

    GamificationBroadcastReceiver gamificationReceiver;
    private Menu optionsMenu;

    @Inject
    SharedPreferences prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeDaggerBase();
    }

    public AppComponent getAppComponent(){
        App app = (App) getApplication();
        return app.getComponent();
    }

    private void initializeDaggerBase() {
        getAppComponent().inject(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    public void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void toast(int stringId) {
        toast(getString(stringId));
    }

    public void alert(int stringId) {
        new AlertDialog.Builder(this)
                .setMessage(stringId)
                .setNegativeButton(R.string.close, null)
                .show();
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        } else {
            return false;
        }
    }

    protected void initialize(boolean overrideTitle, boolean configureActionBar) {
        if (!configureActionBar)
            return;

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);

            //If we are in a course-related activity, we show its title
            if (overrideTitle) {
                Bundle bundle = this.getIntent().getExtras();
                if (bundle != null) {
                    Course course = (Course) bundle.getSerializable(Course.TAG);
                    if (course == null) return;
                    String title = course.getTitle(prefs);
                    setTitle(title);
                    actionBar.setTitle(title);
                }
            }
        }
    }

    protected void initialize(boolean overrideTitle) {
        initialize(overrideTitle, true);
    }

    protected void initialize() {
        initialize(true, true);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Check if the apiKey of the current user is valid
        boolean apiKeyValid = SessionManager.isUserApiKeyValid(this);
        if (!apiKeyValid) {
            apiKeyInvalidated();
        }

        // Register the receiver for gamification events
        gamificationReceiver = new GamificationBroadcastReceiver();
        gamificationReceiver.setGamificationEventListener(this);
        IntentFilter broadcastFilter = new IntentFilter(GamificationService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(gamificationReceiver, broadcastFilter);

        //We check if the user session time has expired to log him out
        if (App.SESSION_EXPIRATION_ENABLED) {
            SharedPreferences prefsReload = PreferenceManager.getDefaultSharedPreferences(this);
            long now = System.currentTimeMillis() / 1000;
            long lastTimeActive = prefsReload.getLong(PrefsActivity.LAST_ACTIVE_TIME, now);
            long timePassed = now - lastTimeActive;

            prefsReload.edit().putLong(PrefsActivity.LAST_ACTIVE_TIME, now).apply();
            if (timePassed > App.SESSION_EXPIRATION_TIMEOUT) {
                Log.d(TAG, "Session timeout (passed " + timePassed + " seconds), logging out");
                logoutAndRestartApp();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (App.SESSION_EXPIRATION_ENABLED) {
            long now = System.currentTimeMillis() / 1000;
            PreferenceManager
                    .getDefaultSharedPreferences(this).edit()
                    .putLong(PrefsActivity.LAST_ACTIVE_TIME, now).apply();
        }

        unregisterReceiver(gamificationReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionsMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    public void logoutAndRestartApp() {
        SessionManager.logoutCurrentUser(this);

        Intent restartIntent = new Intent(this, StartUpActivity.class);
        restartIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(restartIntent);
        this.finish();
    }

    @Override
    public void apiKeyInvalidated() {
        UIUtils.showAlert(this, R.string.error, R.string.error_apikey_expired, () -> {
            logoutAndRestartApp();
            return true;
        });
    }

    @Override
    public void onGamificationEvent(String message, int points) {
        SharedPreferences prefsGame = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notifEnabled = prefsGame.getBoolean(PrefsActivity.PREF_SHOW_GAMIFICATION_EVENTS, true);
        if (notifEnabled) {

            final View rootView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);

            Snackbar snackbar = Snackbar.make(rootView, "", BaseTransientBottomBar.LENGTH_INDEFINITE);
            Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
            layout.setClickable(false);

            // Hide the text
            TextView textView = layout.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setVisibility(View.INVISIBLE);

            // Inflate our custom view
            View snackView = View.inflate(this, R.layout.view_gamification_notif, null);
            TextView tvGamificationMessage = snackView.findViewById(R.id.tv_gamification_notif_message);
            TextView tvGamificationPoints = snackView.findViewById(R.id.tv_gamification_notif_points);
            tvGamificationMessage.setText(message);
            tvGamificationPoints.setText(String.valueOf(points));

            //If the view is not covering the whole snackbar layout, add this line
            layout.setPadding(0, 0, 0, 0);

            layout.addView(snackView, 0);
            layout.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

            final int gamifPointsViewType = Integer.parseInt(prefsGame.getString(PrefsActivity.PREF_GAMIFICATION_POINTS_ANIMATION, Gamification.GAMIFICATION_POINTS_ANIMATION));

            final boolean fullAnimation = gamifPointsViewType == 2 || gamifPointsViewType == 3;
            final boolean withSound = gamifPointsViewType == 3;

            if (fullAnimation) {
                UIUtils.showUserData(optionsMenu, AppActivity.this, null, false, points);
            }

            snackbar.setDuration(BaseTransientBottomBar.LENGTH_INDEFINITE);

            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onShown(Snackbar sb) {
                    super.onShown(sb);

                    if (fullAnimation) {
                        animatePoints(sb, withSound);
                    } else {
                        UIUtils.showUserData(optionsMenu, AppActivity.this, null, false, 0);
                        waitAndClose(sb);
                    }
                }

                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    super.onDismissed(transientBottomBar, event);
                    UIUtils.showUserData(optionsMenu, AppActivity.this, null, false, 0);
                }
            });
            snackbar.show();

        }
    }


    private void animatePoints(final Snackbar snackbar, boolean withSound) {

        if (withSound) {
            MediaPlayer mp = MediaPlayer.create(this, R.raw.sound_gamification_points);
            mp.start();
        }

        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
        View snackView = layout.getChildAt(0);
        TextView tvGamificationNotifPoints = snackView.findViewById(R.id.tv_gamification_notif_points);

        ObjectAnimator animCoinIni = ObjectAnimator.ofFloat(tvGamificationNotifPoints, "rotationY", 0, 720);
        animCoinIni.setDuration(800);

        AnimatorSet animSetGlobal = new AnimatorSet();
        animSetGlobal.setStartDelay(200);

        if (optionsMenu != null && optionsMenu.findItem(R.id.points) != null) {

            ObjectAnimator animY = ObjectAnimator.ofFloat(tvGamificationNotifPoints, "y",
                    tvGamificationNotifPoints.getTop(), 50);
            animY.setInterpolator(new AccelerateDecelerateInterpolator());

            // Nice workaround! https://stackoverflow.com/a/47694906/1365440
            View menuItemView = findViewById(R.id.points);
            int[] itemWindowLocation = new int[2];
            menuItemView.getLocationInWindow(itemWindowLocation);

            int itemX = itemWindowLocation[0];

            ObjectAnimator animX = ObjectAnimator.ofFloat(tvGamificationNotifPoints, "x",
                    tvGamificationNotifPoints.getLeft(), itemX);

            AnimatorSet animXY = new AnimatorSet();
            animXY.playTogether(animX, animY);
            animXY.setDuration(1200);


            ObjectAnimator animCoinJumpX = ObjectAnimator.ofFloat(tvGamificationNotifPoints, "rotationX", 0, 360);
            animCoinJumpX.setDuration(1000);

            AnimatorSet animSetJump = new AnimatorSet();
            animSetJump.playTogether(animXY, animCoinJumpX);

            ObjectAnimator scaleFinalX = ObjectAnimator.ofFloat(tvGamificationNotifPoints, "scaleX", 1, 0);
            ObjectAnimator scaleFinalY = ObjectAnimator.ofFloat(tvGamificationNotifPoints, "scaleY", 1, 0);

            AnimatorSet animSetScaleFinal = new AnimatorSet();
            animSetScaleFinal.playTogether(scaleFinalX, scaleFinalY);
            animSetScaleFinal.setInterpolator(new AnticipateInterpolator());
            animSetScaleFinal.setDuration(500);

            animSetGlobal.playSequentially(animCoinIni, animSetJump, animSetScaleFinal);
        } else {

            animSetGlobal.play(animCoinIni);
            animSetGlobal.setDuration(1000);
        }

        animSetGlobal.start();

        animSetGlobal.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                // do nothing
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                UIUtils.showUserData(optionsMenu, AppActivity.this, null, true, 0);
                waitAndClose(snackbar);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // do nothing
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                // do nothing
            }
        });

    }

    private void waitAndClose(final Snackbar snackbar) {

        int durationViewPoints = Integer.parseInt(prefs.getString(PrefsActivity.PREF_DURATION_GAMIFICATION_POINTS_VIEW,
                String.valueOf(Gamification.DURATION_GAMIFICATION_POINTS_VIEW)));

        new Handler().postDelayed(snackbar::dismiss, TimeUnit.SECONDS.toMillis(durationViewPoints));
    }
}
