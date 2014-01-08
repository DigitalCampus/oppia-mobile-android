/* 
 * This file is part of OppiaMobile - http://oppia-mobile.org/
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
import org.digitalcampus.oppia.utils.CourseXMLReader;
import org.digitalcampus.oppia.utils.ImageUtils;
import org.digitalcampus.oppia.utils.UIUtils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class CourseIndexActivity extends AppActivity implements OnSharedPreferenceChangeListener {

	public static final String TAG = CourseIndexActivity.class.getSimpleName();

	private Course course;
	private CourseXMLReader mxr;
	private ArrayList<Section> sections;
	private SharedPreferences prefs;
	private Activity baselineActivity;
	private AlertDialog aDialog;
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_course_index);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);

		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			course = (Course) bundle.getSerializable(Course.TAG);
			try {
				mxr = new CourseXMLReader(course.getCourseXMLLocation());

				course.setMetaPages(mxr.getMetaPages());

				boolean baselineCompleted = this.isBaselineCompleted();

				String digest = (String) bundle.getSerializable("JumpTo");
				if (digest != null && baselineCompleted) {
					// code to directly jump to a specific activity
					sections = mxr.getSections(course.getModId(), CourseIndexActivity.this);
					for (Section s : sections) {
						for (int i = 0; i < s.getActivities().size(); i++) {
							Activity a = s.getActivities().get(i);
							if (a.getDigest().equals(digest)) {
								Intent intent = new Intent(this, CourseActivity.class);
								Bundle tb = new Bundle();
								tb.putSerializable(Section.TAG, (Section) s);
								tb.putSerializable(Course.TAG, (Course) course);
								tb.putSerializable(SectionListAdapter.TAG_PLACEHOLDER, (Integer) i);
								intent.putExtras(tb);
								startActivity(intent);
							}
						}
					}

				}
			} catch (InvalidXMLException e) {
				UIUtils.showAlert(this, R.string.error, R.string.error_reading_xml, new Callable<Boolean>() {
					public Boolean call() throws Exception {
						CourseIndexActivity.this.finish();
						return true;
					}
				});
			}
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		sections = mxr.getSections(course.getModId(), CourseIndexActivity.this);
		setTitle(course
				.getTitle(prefs.getString(getString(R.string.prefs_language), Locale.getDefault().getLanguage())));

		// set image
		if (course.getImageFile() != null) {
			BitmapDrawable bm = ImageUtils.LoadBMPsdcard(course.getImageFile(), this.getResources(),
					R.drawable.dc_logo);
			getSupportActionBar().setIcon(bm);
		}

		ListView listView = (ListView) findViewById(R.id.section_list);
		SectionListAdapter sla = new SectionListAdapter(this, course, sections);
		listView.setAdapter(sla);

	}

	@Override
	public void onResume() {
		super.onResume();
		if (aDialog == null) {
			this.isBaselineCompleted();
		} else {
			aDialog.show();
		}
		// start a new tracker service
		Log.d(TAG, "Starting tracker service");
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
		getSupportMenuInflater().inflate(R.menu.activity_course_index, menu);
		ArrayList<CourseMetaPage> ammp = course.getMetaPages();
		int order = 104;
		for (CourseMetaPage mmp : ammp) {
			String title = mmp.getLang(
					prefs.getString(getString(R.string.prefs_language), Locale.getDefault().getLanguage()))
					.getContent();
			menu.add(0, mmp.getId(), order, title).setIcon(android.R.drawable.ic_menu_info_details);
			order++;
		}
		UIUtils.showUserData(menu, this);
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
			startActivity(new Intent(this, HelpActivity.class));
			return true;
		case android.R.id.home:
			this.finish();
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
		ArrayList<Activity> baselineActs = mxr.getBaselineActivities(course.getModId(), this);
		// TODO how to handle if more than one baseline activity
		for (Activity a : baselineActs) {
			if (!a.isAttempted()) {
				this.baselineActivity = a;
				Log.d(TAG, "adding dialog");
				aDialog = new AlertDialog.Builder(this).create();
				aDialog.setCancelable(false);
				aDialog.setTitle(R.string.alert_pretest);
				aDialog.setMessage(this.getString(R.string.alert_pretest_summary));

				aDialog.setButton(DialogInterface.BUTTON_NEGATIVE, (CharSequence) this.getString(R.string.open),
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
				aDialog.setButton(DialogInterface.BUTTON_POSITIVE, (CharSequence) this.getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								CourseIndexActivity.this.finish();
							}
						});
				aDialog.show();
				return false;
			}
		}
		return true;
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equalsIgnoreCase(getString(R.string.prefs_points))
				|| key.equalsIgnoreCase(getString(R.string.prefs_badges))) {
			supportInvalidateOptionsMenu();
		}

	}

}
