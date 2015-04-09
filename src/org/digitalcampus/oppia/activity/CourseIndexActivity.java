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

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.SectionListAdapter;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CourseMetaPage;
import org.digitalcampus.oppia.model.Section;
import org.digitalcampus.oppia.service.TrackerService;
import org.digitalcampus.oppia.utils.xmlreaders.CourseXMLReader;
import org.digitalcampus.oppia.utils.ImageUtils;
import org.digitalcampus.oppia.utils.UIUtils;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ListView;

public class CourseIndexActivity extends AppActivity implements OnSharedPreferenceChangeListener {

	public static final String TAG = CourseIndexActivity.class.getSimpleName();

	private Course course;
	private CourseXMLReader cxr;
	private ArrayList<Section> sections;
	private SharedPreferences prefs;
	private Activity baselineActivity;
	private AlertDialog aDialog;
    private View loadingCourseView;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_course_index);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
        loadingCourseView =  findViewById(R.id.loading_course);

		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			course = (Course) bundle.getSerializable(Course.TAG);

            String digest = (String) bundle.getSerializable("JumpTo");
            if (digest != null){
                //If there is a digest, we have to parse the course synchronously to avoid showing this activity
                try {
                    cxr = new CourseXMLReader(course.getCourseXMLLocation(), course.getCourseId(), this);
                } catch (InvalidXMLException e) {
                    e.printStackTrace();
                    showErrorMessage();
                }
                boolean baselineCompleted = isBaselineCompleted();
                if (baselineCompleted) {
                    course.setMetaPages(cxr.getMetaPages());
                    sections = cxr.getSections(course.getCourseId());
                    for (Section section : sections) {
                        for (int i = 0; i < section.getActivities().size(); i++) {
                            Activity act = section.getActivities().get(i);

                            if (act.getDigest().equals(digest)) {
                                Intent intent = new Intent(this, CourseActivity.class);
                                Bundle tb = new Bundle();
                                tb.putSerializable(Section.TAG, section);
                                tb.putSerializable(Course.TAG, course);
                                tb.putSerializable(SectionListAdapter.TAG_PLACEHOLDER, i);
                                intent.putExtras(tb);
                                startActivity(intent);
                            }
                        }
                    }
                    initializeCourseIndex(false);
                }
                else{
                    sections = cxr.getSections(course.getCourseId());
                    initializeCourseIndex(false);
                    showBaselineMessage();
                }
            }
            else{
                new ParseXMLTask().execute("");
            }
        }

	}

	@Override
	public void onStart() {
		super.onStart();

		setTitle(course.getTitle(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage())));
		// set image
		if (course.getImageFile() != null) {
			BitmapDrawable bm = ImageUtils.LoadBMPsdcard(course.getImageFileFromRoot(), this.getResources(),
					R.drawable.dc_logo);
			getActionBar().setIcon(bm);
		}


	}

	@Override
	public void onResume() {
		super.onResume();
		if (aDialog == null) {
			//this.isBaselineCompleted();
		} else {
			aDialog.show();
		}
		// start a new tracker service
		Intent service = new Intent(this, TrackerService.class);

		Bundle tb = new Bundle();
		tb.putBoolean("backgroundData", true);
		service.putExtras(tb);
		this.startService(service);
		
		// remove any saved state info from shared prefs in case they interfere with subsequent page views
		Editor editor = prefs.edit();
		Map<String,?> keys = prefs.getAll();

		for(Map.Entry<String,?> entry : keys.entrySet()){
			if (entry.getKey().startsWith("widget_")){
				editor.remove(entry.getKey());
			}            
		 }
		editor.commit();
	}

	@Override
	public void onPause() {
		if (aDialog != null) {
			aDialog.dismiss();
			aDialog = null;
		}
		super.onPause();
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getMenuInflater().inflate(R.menu.activity_course_index, menu);
		ArrayList<CourseMetaPage> ammp = course.getMetaPages();
		int order = 104;
		for (CourseMetaPage mmp : ammp) {
			String title = mmp.getLang(
					prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()))
					.getContent();
			menu.add(0, mmp.getId(), order, title).setIcon(android.R.drawable.ic_menu_info_details);
			order++;
		}
		UIUtils.showUserData(menu, this, course);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent i;
		Bundle tb = new Bundle();
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_language:
			createLanguageDialog();
			return true;
		case R.id.menu_help:
			i = new Intent(this, AboutActivity.class);
			tb.putSerializable(AboutActivity.TAB_ACTIVE, AboutActivity.TAB_HELP);
			i.putExtras(tb);
			startActivity(i);
			return true;
		case android.R.id.home:
			this.finish();
			return true;
		case R.id.menu_scorecard:
			i = new Intent(this, ScorecardActivity.class);
			tb.putSerializable(Course.TAG, course);
			i.putExtras(tb);
			startActivity(i);
			return true;
		default:
			i = new Intent(this, CourseMetaPageActivity.class);
			tb.putSerializable(Course.TAG, course);
			tb.putSerializable(CourseMetaPage.TAG, item.getItemId());
			i.putExtras(tb);
			startActivity(i);
			return true;
		}
	}

	private void createLanguageDialog() {
		UIUtils ui = new UIUtils();
		ui.createLanguageDialog(this, course.getLangs(), prefs, new Callable<Boolean>() {
			public Boolean call() throws Exception {
				CourseIndexActivity.this.onStart();
				return true;
			}
		});
	}

	private boolean isBaselineCompleted() {
		ArrayList<Activity> baselineActs = cxr.getBaselineActivities(course.getCourseId());
		// TODO how to handle if more than one baseline activity
		for (Activity a : baselineActs) {
			if (!a.isAttempted()) {
				this.baselineActivity = a;
				return false;
			}
		}
		return true;
	}

    private void initializeCourseIndex(boolean animate){

        final ListView listView = (ListView) findViewById(R.id.section_list);
        SectionListAdapter sla = new SectionListAdapter(CourseIndexActivity.this, course, sections);

        if (animate){
            AlphaAnimation fadeOutAnimation = new AlphaAnimation(1f, 0f);
            fadeOutAnimation.setDuration(700);
            fadeOutAnimation.setFillAfter(true);

            listView.setAlpha(0f);
            ValueAnimator animator = ValueAnimator.ofFloat(1f, 0f);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator){
                    listView.setTranslationX( (Float) valueAnimator.getAnimatedValue() * 80 );
                    listView.setAlpha(1f - (Float) valueAnimator.getAnimatedValue());
                }
            });
            animator.setDuration(700);
            animator.start();
            loadingCourseView.startAnimation(fadeOutAnimation);

        }
        else{
            loadingCourseView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }

        listView.setAdapter(sla);

    }

    private void showBaselineMessage(){
        aDialog = new AlertDialog.Builder(this).create();
        aDialog.setCancelable(false);
        aDialog.setTitle(R.string.alert_pretest);
        aDialog.setMessage(this.getString(R.string.alert_pretest_summary));

        aDialog.setButton(DialogInterface.BUTTON_NEGATIVE, this.getString(R.string.open),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(CourseIndexActivity.this, CourseActivity.class);
                        Bundle tb = new Bundle();
                        Section section = new Section();
                        section.addActivity(CourseIndexActivity.this.baselineActivity);
                        tb.putSerializable(Section.TAG, section);
                        tb.putSerializable(CourseActivity.BASELINE_TAG, true);
                        tb.putSerializable(SectionListAdapter.TAG_PLACEHOLDER, 0);
                        tb.putSerializable(Course.TAG, CourseIndexActivity.this.course);
                        intent.putExtras(tb);
                        startActivity(intent);
                    }
                });
        aDialog.setButton(DialogInterface.BUTTON_POSITIVE, this.getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        CourseIndexActivity.this.finish();
                    }
                });
        aDialog.show();
    }

    private void showErrorMessage(){
        UIUtils.showAlert(CourseIndexActivity.this, R.string.error, R.string.error_reading_xml, new Callable<Boolean>() {
            public Boolean call() throws Exception {
                CourseIndexActivity.this.finish();
                return true;
            }
        });
    }

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equalsIgnoreCase(PrefsActivity.PREF_POINTS)
				|| key.equalsIgnoreCase(PrefsActivity.PREF_BADGES)) {
			supportInvalidateOptionsMenu();
		}

	}

    private class ParseXMLTask extends AsyncTask<String, Object, CourseXMLReader>{

        @Override
        protected CourseXMLReader doInBackground(String... courses) {
            try {
                cxr = new CourseXMLReader(course.getCourseXMLLocation(), course.getCourseId(), CourseIndexActivity.this);
                course.setMetaPages(cxr.getMetaPages());
                sections = cxr.getSections(course.getCourseId());
            } catch (InvalidXMLException e) {
                e.printStackTrace();
            }
            return cxr;
        }

        @Override
        protected void onPostExecute(CourseXMLReader parseResults) {
            if (cxr == null){
                showErrorMessage();
            }
            else{
                boolean baselineCompleted = isBaselineCompleted();
                if (!baselineCompleted){
                    showBaselineMessage();
                }
                initializeCourseIndex(true);
                invalidateOptionsMenu();
            }
        }


    }
}
