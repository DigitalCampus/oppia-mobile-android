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

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.databinding.ActivityCourseBinding;
import org.digitalcampus.oppia.adapter.ActivityPagerAdapter;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.MultiLangInfoModel;
import org.digitalcampus.oppia.model.Section;
import org.digitalcampus.oppia.utils.ImageUtils;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.ui.SimpleAnimator;
import org.digitalcampus.oppia.widgets.BaseWidget;
import org.digitalcampus.oppia.widgets.FeedbackWidget;
import org.digitalcampus.oppia.widgets.PageWidget;
import org.digitalcampus.oppia.widgets.QuizWidget;
import org.digitalcampus.oppia.widgets.ResourceWidget;
import org.digitalcampus.oppia.widgets.UrlWidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;

public class CourseActivity extends AppActivity implements OnInitListener, TabLayout.OnTabSelectedListener {

    public static final String BASELINE_TAG = "BASELINE";
    public static final String NUM_ACTIVITY_TAG = "num_activity";
    public static final int TTS_CHECK = 0;

    private Section section;
    private Course course;

    private int currentActivityNo = 0;

    private List<Activity> activities;
    private boolean isBaseline = false;
    private long userID;
    private boolean topicLocked = false;

    private TextToSpeech myTTS;
    private boolean ttsRunning = false;

    private ActivityPagerAdapter apAdapter;
    private boolean launchTTSAfterLanguageSelection;
    private ActivityCourseBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCourseBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        
        ActionBar actionBar = getSupportActionBar();

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            section = (Section) bundle.getSerializable(Section.TAG);
            course = (Course) bundle.getSerializable(Course.TAG);

            activities = section.getActivities();
            currentActivityNo = bundle.getInt(NUM_ACTIVITY_TAG);
            if (bundle.getSerializable(CourseActivity.BASELINE_TAG) != null) {
                this.isBaseline = bundle.getBoolean(CourseActivity.BASELINE_TAG);
            }
            // set image
            if (actionBar != null) {
                BitmapDrawable bm = ImageUtils.loadBMPsdcard(course.getImageFileFromRoot(), this.getResources(), R.drawable.course_icon_placeholder);
                actionBar.setHomeAsUpIndicator(bm);
                actionBar.setDisplayShowHomeEnabled(true);
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowTitleEnabled(true);
            }

            if (section.isProtectedByPassword() && !section.isUnlocked()){
                topicLocked = true;
                supportInvalidateOptionsMenu();
                binding.unlockTopicForm.setVisibility(View.VISIBLE);
                binding.submitPassword.setOnClickListener(view -> {
                    String password = binding.sectionPasswordField.getText().toString();
                    if (section.checkPassword(password)){
                        DbHelper.getInstance(this).saveSectionUnlockedByUser(course, section, userID, password);
                        binding.unlockTopicForm.setVisibility(View.GONE);
                        topicLocked = false;
                        loadActivities();
                        supportInvalidateOptionsMenu();
                    }
                    else{
                        binding.sectionPasswordError.setVisibility(View.VISIBLE);
                        SimpleAnimator.fade(binding.sectionPasswordError, SimpleAnimator.FADE_IN);
                        binding.sectionPasswordField.setText("");
                    }

                });
            }
            else{
                loadActivities();
            }

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize();
        binding.activityWidgetPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(binding.tabsToolbar));

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentActivityNo", currentActivityNo);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentActivityNo = savedInstanceState.getInt("currentActivityNo");
    }

    @Override
    public void onPause() {
        super.onPause();

        if (!ttsRunning && !topicLocked) {
            BaseWidget currentWidget = (BaseWidget) apAdapter.getItem(currentActivityNo);
            currentWidget.pauseTimeTracking();
            currentWidget.saveTracker();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!ttsRunning && !topicLocked) {
            BaseWidget currentWidget = (BaseWidget) apAdapter.getItem(currentActivityNo);
            currentWidget.resumeTimeTracking();
        }

        DbHelper db = DbHelper.getInstance(this);
        userID = db.getUserId(SessionManager.getUsername(this));
    }

    @Override
    protected void onDestroy() {

        if (ttsRunning) {
            BaseWidget currentWidget = (BaseWidget) apAdapter.getItem(currentActivityNo);
            currentWidget.pauseTimeTracking();
            currentWidget.saveTracker();
        }

        ttsRunning = false;

        if (myTTS != null) {
            myTTS.shutdown();
            myTTS = null;
        }

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_course, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Hide all the menu items if the topic is locked
        menu.setGroupVisible(0, !topicLocked);

        MenuItem ttsMenuItem = menu.findItem(R.id.menu_tts);
        ttsMenuItem.setTitle(ttsRunning ? R.string.menu_stop_read_aloud : R.string.menu_read_aloud);

        // If there is only one language for this course, makes no sense to show the language menu
        if (course.getLangs().size() <= 1){
            MenuItem langMenuItem = menu.findItem(R.id.menu_language);
            langMenuItem.setVisible(false);
            langMenuItem.setEnabled(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        Bundle tb = new Bundle();
        Intent i;

        switch (item.getItemId()) {

            case R.id.menu_language:
                createLanguageDialog();
                return true;

            case R.id.menu_text_size:
                UIUtils.showChangeTextSizeDialog(this,
                        () -> sendBroadcast(new Intent(BaseWidget.ACTION_TEXT_SIZE_CHANGED)));
                return true;

            case R.id.menu_help:
                i = new Intent(this, AboutActivity.class);
                tb.putSerializable(AboutActivity.TAB_ACTIVE, AboutActivity.TAB_HELP);
                i.putExtras(tb);
                startActivity(i);
                return true;

            case R.id.menu_scorecard:
                i = new Intent(this, ScorecardActivity.class);
                tb.putSerializable(Course.TAG, course);
                i.putExtras(tb);
                startActivity(i);
                return true;

            case R.id.menu_tts:
                manageTTS();
                supportInvalidateOptionsMenu();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    private void manageTTS() {
        if (myTTS == null && !ttsRunning && !topicLocked) {
            if (checkLanguageSelected()) {
                launchTTS();
            } else {
                launchTTSAfterLanguageSelection = true;
                createLanguageDialog();
            }
        } else if (myTTS != null && ttsRunning) {
            this.stopReading();
        } else {
            // TTS not installed so show message
            Toast.makeText(this, this.getString(R.string.error_tts_start), Toast.LENGTH_LONG).show();
        }
    }

    private void launchTTS() {
        // check for TTS data
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, TTS_CHECK);
    }

    private boolean checkLanguageSelected() {
        String currentLang = prefs.getString(PrefsActivity.PREF_LANGUAGE, null);
        return currentLang != null && checkCourseHasLanguage(currentLang);
    }

    private boolean checkCourseHasLanguage(String currentLang) {
        for (Lang lang : course.getLangs()) {
            if (lang.getLanguage().equalsIgnoreCase(currentLang)) {
                return true;
            }
        }
        return false;
    }

    private void loadActivities() {
        String currentLang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
        String actionBarTitle = section.getTitle(currentLang);
        if (actionBarTitle != null && !actionBarTitle.equals(MultiLangInfoModel.DEFAULT_NOTITLE)) {
            setTitle(actionBarTitle);
        } else {
            List<Activity> sectionActivities = section.getActivities();
            String preTestTitle = getString(R.string.alert_pretest);
            String actBaselineTitle = isBaseline ? getString(R.string.title_baseline) : "";
            setTitle(!sectionActivities.isEmpty() && sectionActivities.get(0).getTitle(currentLang).equalsIgnoreCase(preTestTitle) ?
                    preTestTitle : actBaselineTitle);
        }

        List<Fragment> fragments = new ArrayList<>();
        List<String> titles = new ArrayList<>();

        for (int i = 0; i < activities.size(); i++) {
            Activity activity = determineActivityType(i, fragments);
            if (activity != null){
                titles.add(activity.getTitle(currentLang));
            }
        }

        apAdapter = new ActivityPagerAdapter(this, getSupportFragmentManager(), fragments, titles);
        binding.tabsToolbar.removeOnTabSelectedListener(this);
        binding.activityWidgetPager.setAdapter(apAdapter);
        binding.tabsToolbar.setupWithViewPager(binding.activityWidgetPager);
        binding.tabsToolbar.setTabMode(activities.size() > 1 ? TabLayout.MODE_SCROLLABLE : TabLayout.MODE_FIXED);
        binding.tabsToolbar.addOnTabSelectedListener(this);
        apAdapter.updateTabViews(binding.tabsToolbar);

        if (currentActivityNo >= fragments.size()){
            //Wrong activity number passed
            Toast.makeText(this, "Wrong activity parameter", Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }
        binding.activityWidgetPager.setCurrentItem(currentActivityNo);
    }

    private Activity determineActivityType(int i, List<Fragment> fragments){
        Activity activity = activities.get(i);
        //Fragment creation
        if (activity.getActType().equalsIgnoreCase("page")) {
            fragments.add(PageWidget.newInstance(activity, course, isBaseline));
        } else if (activity.getActType().equalsIgnoreCase("quiz")) {
            QuizWidget newQuiz = QuizWidget.newInstance(activity, course, isBaseline);
            if (apAdapter != null) {
                //If there was a previous quizWidget, we apply its current config to the new one
                QuizWidget previousQuiz = (QuizWidget) apAdapter.getItem(i);
                newQuiz.setWidgetConfig(previousQuiz.getWidgetConfig());
            }
            fragments.add(newQuiz);
        } else if (activity.getActType().equalsIgnoreCase("resource")) {
            fragments.add(ResourceWidget.newInstance(activity, course, isBaseline));
        } else if (activity.getActType().equalsIgnoreCase("feedback")) {
            FeedbackWidget newFeedback = FeedbackWidget.newInstance(activity, course, isBaseline);
            if (apAdapter != null) {
                //If there was a previous feedbackWidget, we apply its current config to the new one
                FeedbackWidget previousWidget = (FeedbackWidget) apAdapter.getItem(i);
                newFeedback.setWidgetConfig(previousWidget.getWidgetConfig());
            }
            fragments.add(newFeedback);
        } else if (activities.get(i).getActType().equalsIgnoreCase("url")) {
            UrlWidget f = UrlWidget.newInstance(activities.get(i), course, isBaseline);
            fragments.add(f);
        }
        else {
            return null;
        }

        return activity;
    }

    private void createLanguageDialog() {
        UIUtils.createLanguageDialog(this, course.getLangs(), prefs, () -> {
            CourseActivity.this.loadActivities();
            if (launchTTSAfterLanguageSelection) {
                launchTTSAfterLanguageSelection = false;
                launchTTS();
            }
            return true;
        });
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        int tabSelected = tab.getPosition();
        Log.d(TAG, "Tab selected " + tabSelected + " current act " + currentActivityNo);

        if (canNavigateTo(tabSelected)) {
            binding.activityWidgetPager.setCurrentItem(tabSelected);
            currentActivityNo = tabSelected;
            this.stopReading();
            BaseWidget currentWidget = (BaseWidget) apAdapter.getItem(currentActivityNo);
            currentWidget.resetTimeTracking();
        } else {
            Runnable setPreviousTab = () -> {
                UIUtils.showAlert(CourseActivity.this, R.string.sequencing_dialog_title, R.string.sequencing_section_message);
                TabLayout.Tab target = binding.tabsToolbar.getTabAt(currentActivityNo);
                if (target != null) {
                    target.select();
                }
            };
            new Handler().post(setPreviousTab);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        ((BaseWidget) apAdapter.getItem(currentActivityNo)).saveTracker();
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        int tabSelected = tab.getPosition();
        Log.d(TAG, "Tab selected " + tabSelected + " current act " + currentActivityNo);

        BaseWidget currentWidget = (BaseWidget) apAdapter.getItem(currentActivityNo);
        currentWidget.resetTimeTracking();
    }

    public void onQuizFinished() {
        int currentActivityPosition = binding.activityWidgetPager.getCurrentItem();
        if (currentActivityPosition < activities.size() - 1) {
            binding.activityWidgetPager.setCurrentItem(currentActivityPosition + 1, true);
        } else {
            finish();
        }
    }

    private boolean canNavigateTo(int newTab) {
        //If the course does not have a sequencing mode, we can navigate freely
        if (course.getSequencingMode().equals(Course.SEQUENCING_MODE_NONE)) return true;
        // if it is a previous activity (or the first), no need for further checks
        if ((newTab == 0) || (newTab <= currentActivityNo)) return true;
        Activity previousActivity = activities.get(newTab - 1);
        //the user can navigate to the activity if its directly preceding one is completed
        DbHelper db = DbHelper.getInstance(this);
        return db.activityCompleted(course.getCourseId(), previousActivity.getDigest(), userID);
    }

    public void onInit(int status) {
        // check for successful instantiation
        if (status == TextToSpeech.SUCCESS) {
            ttsRunning = true;
            ((BaseWidget) apAdapter.getItem(currentActivityNo)).setReadAloud(true);
            supportInvalidateOptionsMenu();

            String currentLang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
            Locale localeContent = new Locale(currentLang);
            List<Integer> validLangAvailableCodes = new ArrayList<>(Arrays.asList(
                    TextToSpeech.LANG_AVAILABLE, TextToSpeech.LANG_COUNTRY_AVAILABLE, TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE));
            if (validLangAvailableCodes.contains(myTTS.isLanguageAvailable(localeContent))) {
                myTTS.setLanguage(localeContent);
            }

            myTTS.speak(((BaseWidget) apAdapter.getItem(currentActivityNo)).getContentToRead(), TextToSpeech.QUEUE_FLUSH, null, TAG);
            myTTS.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onDone(String utteranceId) {
                    CourseActivity.this.ttsRunning = false;
                    myTTS = null;
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }

                @Override
                public void onError(String utteranceId) {
                    // does not need completing
                }

                @Override
                public void onStart(String utteranceId) {
                    // does not need completing
                }
            });

        } else {
            // TTS not installed so show message
            Toast.makeText(this, this.getString(R.string.error_tts_start), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_CHECK && resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
            // the user has the necessary data - create the TTS
            myTTS = new TextToSpeech(this, this);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void stopReading() {
        if (myTTS != null) {
            myTTS.stop();
            myTTS = null;
        }
        this.ttsRunning = false;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
