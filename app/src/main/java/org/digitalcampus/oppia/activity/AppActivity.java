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
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.widget.TextView;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.gamification.Gamification;
import org.digitalcampus.oppia.gamification.GamificationBroadcastReceiver;
import org.digitalcampus.oppia.gamification.GamificationService;
import org.digitalcampus.oppia.listener.APIKeyRequestListener;
import org.digitalcampus.oppia.listener.GamificationEventListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.utils.UIUtils;

import java.util.concurrent.Callable;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;


public class AppActivity extends AppCompatActivity implements APIKeyRequestListener, GamificationEventListener {

    public static final String TAG = AppActivity.class.getSimpleName();

    GamificationBroadcastReceiver gamificationReceiver;
    private Menu optionsMenu;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return false;
        }
    }

    protected void onStart(boolean overrideTitle, boolean configureActionBar) {
        super.onStart();

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
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
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

    protected void onStart(boolean overrideTitle) {
        onStart(overrideTitle, true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        onStart(true, true);
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
        if (MobileLearning.SESSION_EXPIRATION_ENABLED) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            long now = System.currentTimeMillis() / 1000;
            long lastTimeActive = prefs.getLong(PrefsActivity.LAST_ACTIVE_TIME, now);
            long timePassed = now - lastTimeActive;

            prefs.edit().putLong(PrefsActivity.LAST_ACTIVE_TIME, now).apply();
            if (timePassed > MobileLearning.SESSION_EXPIRATION_TIMEOUT) {
                Log.d(TAG, "Session timeout (passed " + timePassed + " seconds), logging out");
                logoutAndRestartApp();
            }
        }

//        onGamificationEvent("esprueba", 20);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (MobileLearning.SESSION_EXPIRATION_ENABLED) {
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
        UIUtils.showAlert(this, R.string.error, R.string.error_apikey_expired, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                logoutAndRestartApp();
                return true;
            }
        });
    }

    @Override
    public void onGamificationEvent(String message, int points) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notifEnabled = prefs.getBoolean(PrefsActivity.PREF_SHOW_GAMIFICATION_EVENTS, true);
        if (notifEnabled) {
            final View rootView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);

            Snackbar snackbar = Snackbar.make(rootView, "", Snackbar.LENGTH_INDEFINITE);
            Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();
            layout.setClickable(false);

            // Hide the text
            TextView textView = (TextView) layout.findViewById(android.support.design.R.id.snackbar_text);
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

            final int gamifPointsViewType = Integer.parseInt(prefs.getString(PrefsActivity.PREF_GAMIFICATION_POINTS_VIEW_TYPE, Gamification.PREF_GAMIFICATION_POINTS_VIEW_TYPE));

            final boolean fullAnimation = gamifPointsViewType > 1;
            final boolean withSound = gamifPointsViewType == 3;

            if (fullAnimation) {
                UIUtils.showUserData(optionsMenu, AppActivity.this, null, false, points);
            }

            snackbar.setDuration(fullAnimation ? Snackbar.LENGTH_INDEFINITE : Snackbar.LENGTH_SHORT);

            snackbar.addCallback(new Snackbar.Callback() {
                @Override
                public void onShown(Snackbar sb) {
                    super.onShown(sb);

                    if (fullAnimation) {
                        animatePoints(sb, withSound);
                    } else {
                        UIUtils.showUserData(optionsMenu, AppActivity.this, null, false, 0);
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

            AnimatorSet animXY = new AnimatorSet();

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

            animXY = new AnimatorSet();
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

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                snackbar.dismiss();
                UIUtils.showUserData(optionsMenu, AppActivity.this, null, true, 0);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });


    }
}
