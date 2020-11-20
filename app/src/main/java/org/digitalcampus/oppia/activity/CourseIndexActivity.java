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

import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.CourseIndexRecyclerViewAdapter;
import org.digitalcampus.oppia.adapter.RecyclerViewClickableAdapter;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.CompleteCourse;
import org.digitalcampus.oppia.model.CompleteCourseProvider;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CourseMetaPage;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.Section;
import org.digitalcampus.oppia.service.TrackerWorker;
import org.digitalcampus.oppia.task.ParseCourseXMLTask;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.ui.ExpandableRecyclerView;
import org.digitalcampus.oppia.utils.ui.MediaScanView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

public class CourseIndexActivity extends AppActivity implements OnSharedPreferenceChangeListener, ParseCourseXMLTask.OnParseXmlListener {

    public static final String JUMPTO_TAG = "JumpTo";
    public static final int RESULT_JUMPTO = 99;
    public static final String EXTRA_FROM_WEBLINK = "extra_from_weblink";

    private Course course;
    private CompleteCourse parsedCourse;
    private ArrayList<Section> sections;
    @Inject SharedPreferences prefs;
    private View loadingCourseView;
    private CourseIndexRecyclerViewAdapter adapter;
    private String digestJumpTo;

    @Inject
    CompleteCourseProvider completeCourseProvider;
    private AlertDialog aDialog;
    private MediaScanView mediaScanView;
    private RecyclerView recyclerCourseSections;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_index);
        getAppComponent().inject(this);

        prefs.registerOnSharedPreferenceChangeListener(this);
        loadingCourseView = findViewById(R.id.loading_course);
        mediaScanView = findViewById(R.id.view_media_scan);
        recyclerCourseSections = findViewById(R.id.recycler_course_sections);


        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            course = (Course) bundle.getSerializable(Course.TAG);

            String digest = (String) bundle.getSerializable(JUMPTO_TAG);
            if (digest != null) {
                //If there is a digest, we have to parse the course synchronously to avoid showing this activity
                parsedCourse = completeCourseProvider.getCompleteCourseSync(this, course);

                //We also check first if the baseline is completed before jumping to digest
                boolean baselineCompleted = isBaselineCompleted();
                if (baselineCompleted) {
                    course.setMetaPages(parsedCourse.getMetaPages());
                    sections = (ArrayList<Section>) parsedCourse.getSections();
                    startCourseActivityByDigest(digest);
                    initializeCourseIndex(false);
                } else {
                    sections = (ArrayList<Section>) parsedCourse.getSections();
                    initializeCourseIndex(false);
                    showBaselineMessage(digest);
                }
            } else {
                completeCourseProvider.getCompleteCourseAsync(this, course);
            }
        }

        mediaScanView.setMessage(getString(R.string.info_scan_course_media_missing));
        mediaScanView.setViewBelow(findViewById(R.id.view_course_sections));
    }

    @Override
    public void onStart() {
        super.onStart();
        initialize(false);

        mediaScanView.scanMedia(Arrays.asList(course));
    }

    @Override
    public void onResume() {
        super.onResume();

        if (digestJumpTo != null && isBaselineCompleted()) {
            startCourseActivityByDigest(digestJumpTo);
            digestJumpTo = null;
            return;
        }

        sendTrackers();

        // remove any saved state info from shared prefs in case they interfere with subsequent page views
        SharedPreferences.Editor editor = prefs.edit();
        Map<String, ?> keys = prefs.getAll();

        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            if (entry.getKey().startsWith("widget_")) {
                editor.remove(entry.getKey());
            }
        }
        editor.apply();

        checkParsedCourse();
    }

    private void sendTrackers() {

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest trackerSendWork = new OneTimeWorkRequest.Builder(TrackerWorker.class)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(this).enqueue(trackerSendWork);
    }

    private void checkParsedCourse() {

        if ((parsedCourse != null) && (sections != null) && (!sections.isEmpty())) {
            parsedCourse.setCourseId(course.getCourseId());
            parsedCourse.updateCourseActivity(this);
            adapter.notifyDataSetChanged();
            if (!isBaselineCompleted()) {
                showBaselineMessage(null);
            } else {
                closeBaselineDialogIfOpen();
            }
        }
    }

    private void closeBaselineDialogIfOpen() {
        if (aDialog != null && aDialog.isShowing()) {
            aDialog.dismiss();
            aDialog = null;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.activity_course_index, menu);
        ArrayList<CourseMetaPage> ammp = (ArrayList<CourseMetaPage>) course.getMetaPages();
        int order = 104;
        for (CourseMetaPage mmp : ammp) {
            Lang titleLang = mmp.getLang(
                    prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()));

            if (titleLang != null) {
                String title = titleLang.getContent();
                menu.add(0, mmp.getId(), order, title).setIcon(android.R.drawable.ic_menu_info_details);
                order++;
            }

        }
        UIUtils.showUserData(menu, this, course);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent i;
        Bundle tb = new Bundle();

        switch (item.getItemId()) {
            case R.id.menu_language:
                createLanguageDialog();
                return true;

            case R.id.menu_help:
                i = new Intent(this, AboutActivity.class);
                tb.putSerializable(AboutActivity.TAB_ACTIVE, AboutActivity.TAB_HELP);
                break;

            case R.id.menu_scorecard:
                i = new Intent(this, ScorecardActivity.class);
                tb.putSerializable(Course.TAG, course);
                break;

            case R.id.menu_expand_all_sections:
                adapter.expandCollapseAllSections(true);
                return true;

            case R.id.menu_collapse_all_sections:
                adapter.expandCollapseAllSections(false);
                return true;

            case android.R.id.home:
                onUpButtonPressed();
                return true;

            default:
                i = new Intent(this, CourseMetaPageActivity.class);
                tb.putSerializable(Course.TAG, course);
                tb.putInt(CourseMetaPage.TAG, item.getItemId());
                break;
        }

        i.putExtras(tb);
        startActivityForResult(i, 1);


        return super.onOptionsItemSelected(item);

    }

    private void onUpButtonPressed() {
        if (getIntent().hasExtra(EXTRA_FROM_WEBLINK)
                && getIntent().getBooleanExtra(EXTRA_FROM_WEBLINK, false)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        finish();
    }

    private void createLanguageDialog() {
        UIUtils.createLanguageDialog(this, course.getLangs(), prefs, () -> {
            CourseIndexActivity.this.initialize(false);
            return true;
        });
    }

    private void initializeCourseIndex(boolean animate) {

        adapter = new CourseIndexRecyclerViewAdapter(this, sections, course);
        adapter.setOnChildItemClickedListener((section, position) -> {
            Activity act = sections.get(section).getActivities().get(position);
            startCourseActivityByDigest(act.getDigest());
        });

        if (animate) {
            AlphaAnimation fadeOutAnimation = new AlphaAnimation(1f, 0f);
            fadeOutAnimation.setDuration(700);
            fadeOutAnimation.setFillAfter(true);

            recyclerCourseSections.setAlpha(0f);
            ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
            animator.addUpdateListener(valueAnimator -> {
                recyclerCourseSections.setTranslationX((Float) valueAnimator.getAnimatedValue() * 80);
                recyclerCourseSections.setAlpha(1f - (Float) valueAnimator.getAnimatedValue());
            });
            animator.setDuration(700);
            animator.start();
            loadingCourseView.startAnimation(fadeOutAnimation);

        } else {
            loadingCourseView.setVisibility(View.GONE);
            recyclerCourseSections.setVisibility(View.VISIBLE);
        }
        recyclerCourseSections.setAdapter(adapter);

    }

    private Activity getBaselineActivity(){
        ArrayList<Activity> baselineActs = (ArrayList<Activity>) parsedCourse.getBaselineActivities();
        for (Activity a : baselineActs) {
            if (!a.isAttempted()) {
                return a;
            }
        }
        return null;
    }

    private boolean isBaselineCompleted() {
        return getBaselineActivity() == null;
    }

    private void showBaselineMessage(final String digest) {
        aDialog = new AlertDialog.Builder(this, R.style.Oppia_AlertDialogStyle).create();
        aDialog.setCancelable(false);
        aDialog.setTitle(R.string.alert_pretest);
        aDialog.setMessage(this.getString(R.string.alert_pretest_summary));

        aDialog.setButton(DialogInterface.BUTTON_NEGATIVE, this.getString(R.string.open),
                (dialog, which) -> {
                    //We set the digest to be able to jump to this activity after passing the baseline
                    digestJumpTo = digest;

                    Intent intent = new Intent(CourseIndexActivity.this, CourseActivity.class);
                    Bundle tb = new Bundle();
                    Section section = new Section();
                    section.addActivity(getBaselineActivity());
                    tb.putSerializable(Section.TAG, section);
                    tb.putSerializable(CourseActivity.BASELINE_TAG, true);
                    tb.putSerializable(CourseActivity.NUM_ACTIVITY_TAG, 0);
                    tb.putSerializable(Course.TAG, CourseIndexActivity.this.course);
                    intent.putExtras(tb);
                    startActivity(intent);
                });
        aDialog.setButton(DialogInterface.BUTTON_POSITIVE, this.getString(R.string.cancel),
                (dialog, which) -> CourseIndexActivity.this.finish());
        aDialog.show();
    }

    private void startActivityOrShowSequencingRationale(Section s, int position, boolean previousSectionsCompleted, boolean previousActivitiesCompleted){
        if ((course.getSequencingMode().equals(Course.SEQUENCING_MODE_COURSE)) &&
                (!previousSectionsCompleted || !previousActivitiesCompleted)) {
            UIUtils.showAlert(this, R.string.sequencing_dialog_title, R.string.sequencing_course_message);
        } else if ((course.getSequencingMode().equals(Course.SEQUENCING_MODE_SECTION))
                && (!previousActivitiesCompleted)) {
            UIUtils.showAlert(this, R.string.sequencing_dialog_title, R.string.sequencing_section_message);
        } else {
            Intent intent = new Intent(this, CourseActivity.class);
            Bundle tb = new Bundle();
            tb.putSerializable(Section.TAG, s);
            tb.putSerializable(Course.TAG, course);
            tb.putSerializable(CourseActivity.NUM_ACTIVITY_TAG, position);
            intent.putExtras(tb);
            startActivity(intent);
        }
    }

    private void startCourseActivityByDigest(String digest) {

        boolean allSectionsCompleted = true;
        for (Section section : sections) {
            boolean allActivitiesCompleted = true;
            for (int i = 0; i < section.getActivities().size(); i++) {
                Activity act = section.getActivities().get(i);
                if (act.getDigest().equals(digest)) {
                    // When we find the activity we are looking for, we can stop the search
                    startActivityOrShowSequencingRationale(section, i, allSectionsCompleted, allActivitiesCompleted);
                    return;
                } else {
                    // If it's not the activity we are searching for, we check if it's completed (for sequencing purposes)
                    allActivitiesCompleted = allActivitiesCompleted && act.getCompleted();
                }
            }
            allSectionsCompleted = allSectionsCompleted && allActivitiesCompleted;
        }
    }

    private void showErrorMessage() {
        UIUtils.showAlert(CourseIndexActivity.this, R.string.error, R.string.error_reading_xml, () -> {
            CourseIndexActivity.this.finish();
            return true;
        });
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // update the points/badges by invalidating the menu
        if (key.equalsIgnoreCase(PrefsActivity.PREF_TRIGGER_POINTS_REFRESH)) {
            supportInvalidateOptionsMenu();
        }
    }

    //@Override
    public void onParseComplete(CompleteCourse parsed) {
        loadingCourseView.setVisibility(View.GONE);
        parsedCourse = parsed;
        course.setMetaPages(parsedCourse.getMetaPages());
        course.setMedia(parsedCourse.getMedia());
        course.setGamificationEvents(parsedCourse.getGamification());
        sections = (ArrayList<Section>) parsedCourse.getSections();

        boolean baselineCompleted = isBaselineCompleted();
        if (!baselineCompleted) {
            showBaselineMessage(null);
        }
        initializeCourseIndex(true);
    }

    //@Override
    public void onParseError() {
        loadingCourseView.setVisibility(View.GONE);
        showErrorMessage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_JUMPTO) {
            String digest = data.getStringExtra( JUMPTO_TAG);
            startCourseActivityByDigest(digest);
        }
    }

    @Override
    public void onGamificationEvent(String message, int points) {
        super.onGamificationEvent(message, points);

        // This solves a non usual bug which shows pretest dialog event when it is done
        // This happens in few devices when GamificationService ends (and stores data) after onResume() of this class is called
        // Fixed with this workaround
        checkParsedCourse();

    }
}
