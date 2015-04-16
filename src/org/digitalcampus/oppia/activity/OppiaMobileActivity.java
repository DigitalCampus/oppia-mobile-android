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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.CourseListAdapter;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.ScanMediaListener;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.ScanMediaTask;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.storage.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;

public class OppiaMobileActivity extends AppActivity implements OnSharedPreferenceChangeListener, ScanMediaListener {

	public static final String TAG = OppiaMobileActivity.class.getSimpleName();
	private SharedPreferences prefs;
	private ArrayList<Course> courses;
	private Course tempCourse;
	private long userId = 0;

    private TextView messageText;
    private Button messageButton;
    private View messageContainer;

    private CourseListAdapter courseListAdapter;
    private ListView courseList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
        prefs.registerOnSharedPreferenceChangeListener(this);

		// set preferred lang to the default lang
		if (prefs.getString(PrefsActivity.PREF_LANGUAGE, "").equals("")) {
			Editor editor = prefs.edit();
			editor.putString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
			editor.commit();
		}

        messageContainer = this.findViewById(R.id.home_messages);
        messageText = (TextView) this.findViewById(R.id.home_message);
        messageButton = (Button) this.findViewById(R.id.message_action_button);

        courses = new ArrayList<Course>();
        courseListAdapter = new CourseListAdapter(this, courses);
        courseList = (ListView) findViewById(R.id.course_list);
        courseList.setAdapter(courseListAdapter);
        registerForContextMenu(courseList);

        courseList.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Course selectedCourse = courses.get(position);
                Intent i = new Intent(OppiaMobileActivity.this, CourseIndexActivity.class);
                Bundle tb = new Bundle();
                tb.putSerializable(Course.TAG, selectedCourse);
                i.putExtras(tb);
                startActivity(i);
            }
        });

	}

	@Override
	public void onStart() {
		super.onStart();
		DbHelper db = new DbHelper(this);
		userId = db.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
		DatabaseManager.getInstance().closeDatabase();

		displayCourses(userId);
	}

	@Override
	public void onResume(){
		super.onResume();
		this.updateReminders();
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}
	
	private void displayCourses(long userId) {

		DbHelper db = new DbHelper(this);
        courses.clear();
		courses.addAll(db.getCourses(userId));
		DatabaseManager.getInstance().closeDatabase();
		
		LinearLayout llLoading = (LinearLayout) this.findViewById(R.id.loading_courses);
		llLoading.setVisibility(View.GONE);
		LinearLayout llNone = (LinearLayout) this.findViewById(R.id.no_courses);
		
		if (courses.size() < MobileLearning.DOWNLOAD_COURSES_DISPLAY){
			llNone.setVisibility(View.VISIBLE);
			Button manageBtn = (Button) this.findViewById(R.id.manage_courses_btn);
            if (courses.size() > 0){
                TextView tv = (TextView) this.findViewById(R.id.manage_courses_text);
                tv.setText(R.string.more_courses);
            }
			manageBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					startActivity(new Intent(OppiaMobileActivity.this, TagSelectActivity.class));
				}
			});
		} else {
			TextView tv = (TextView) this.findViewById(R.id.manage_courses_text);
			tv.setText(R.string.no_courses);
			llNone.setVisibility(View.GONE);
		}

        courseListAdapter.notifyDataSetChanged();
		this.updateReminders();
		
		// scan media
		this.scanMedia();
	}

	private void updateReminders(){
		if(prefs.getBoolean(PrefsActivity.PREF_SHOW_SCHEDULE_REMINDERS, false)){
			DbHelper db = new DbHelper(OppiaMobileActivity.this);
			int max = Integer.valueOf(prefs.getString(PrefsActivity.PREF_NO_SCHEDULE_REMINDERS, "2"));
			long userId = db.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
			ArrayList<Activity> activities = db.getActivitiesDue(max, userId);
			DatabaseManager.getInstance().closeDatabase();

			this.drawReminders(activities);
		} else {
			LinearLayout ll = (LinearLayout) findViewById(R.id.schedule_reminders);
			ll.setVisibility(View.GONE);
		}		
	}
	
	private void scanMedia() {
		long now = System.currentTimeMillis()/1000;
		if (prefs.getLong(PrefsActivity.PREF_LAST_MEDIA_SCAN, 0)+3600 > now) {
			LinearLayout ll = (LinearLayout) this.findViewById(R.id.home_messages);
			ll.setVisibility(View.GONE);
			return;
		}
		ScanMediaTask task = new ScanMediaTask(this);
		Payload p = new Payload(this.courses);
		task.setScanMediaListener(this);
		task.execute(p);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		UIUtils.showUserData(menu,this, null);
		MenuItem item = (MenuItem) menu.findItem(R.id.menu_logout);
		item.setVisible(prefs.getBoolean(PrefsActivity.PREF_LOGOUT_ENABLED, true));
	    return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		Log.d(TAG,"selected:" + item.getItemId());
		int itemId = item.getItemId();
		if (itemId == R.id.menu_about) {
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		} else if (itemId == R.id.menu_download) {
			startActivity(new Intent(this, TagSelectActivity.class));
			return true;
		} else if (itemId == R.id.menu_settings) {
			Intent i = new Intent(this, PrefsActivity.class);
			Bundle tb = new Bundle();
			ArrayList<Lang> langs = new ArrayList<Lang>();
			for(Course m: courses){
				langs.addAll(m.getLangs());
			}
			tb.putSerializable("langs", langs);
			i.putExtras(tb);
			startActivity(i);
			return true;
		} else if (itemId == R.id.menu_language) {
			createLanguageDialog();
			return true;
		} else if (itemId == R.id.menu_monitor) {
			startActivity(new Intent(this, MonitorActivity.class));
			return true;
		} else if (itemId == R.id.menu_scorecard) {
			startActivity(new Intent(this, ScorecardActivity.class));
			return true;
		} else if (itemId == R.id.menu_search) {
			startActivity(new Intent(this, SearchActivity.class));
			return true;
		} else if (itemId == R.id.menu_logout) {
			logout();
			return true;
		}
		return true;
	}

	private void createLanguageDialog() {
		ArrayList<Lang> langs = new ArrayList<Lang>();
		for(Course m: courses){
			langs.addAll(m.getLangs());
		}
		
		UIUtils ui = new UIUtils();
    	ui.createLanguageDialog(this, langs, prefs, new Callable<Boolean>() {	
			public Boolean call() throws Exception {
				OppiaMobileActivity.this.onStart();
				return true;
			}
		});
	}

	private void logout() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setTitle(R.string.logout);
		builder.setMessage(R.string.logout_confirm);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// wipe user prefs
				Editor editor = prefs.edit();
				editor.putString(PrefsActivity.PREF_USER_NAME, "");
				editor.putString(PrefsActivity.PREF_API_KEY, "");
				editor.putInt(PrefsActivity.PREF_BADGES, 0);
				editor.putInt(PrefsActivity.PREF_POINTS, 0);
				editor.commit();

				// restart the app
				OppiaMobileActivity.this.startActivity(new Intent(OppiaMobileActivity.this, StartUpActivity.class));
				OppiaMobileActivity.this.finish();

			}
		});
		builder.setNegativeButton(R.string.no, null);
		builder.show();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		getMenuInflater().inflate(R.menu.course_context_menu, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
	}
	
	@Override
	public boolean onContextItemSelected(android.view.MenuItem item) {

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        tempCourse = courses.get(info.position);
		int itemId = item.getItemId();
		if (itemId == R.id.course_context_delete) {
			if (prefs.getBoolean(PrefsActivity.PREF_DELETE_COURSE_ENABLED, true)){
				confirmCourseDelete();
			} else {
				Toast.makeText(this, this.getString(R.string.warning_delete_disabled), Toast.LENGTH_LONG).show();
			}
			return true;
		} else if (itemId == R.id.course_context_reset) {
			confirmCourseReset();
			return true;
		}
		return true;
	}

	private void confirmCourseDelete() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setTitle(R.string.course_context_delete);
		builder.setMessage(R.string.course_context_delete_confirm);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// remove db records
				DbHelper db = new DbHelper(OppiaMobileActivity.this);
				db.deleteCourse(tempCourse.getCourseId());
				DatabaseManager.getInstance().closeDatabase();

				// remove files
                String courseLocation = tempCourse.getLocation();
				File f = new File(courseLocation);
				FileUtils.deleteDir(f);
				Editor e = prefs.edit();
				e.putLong(PrefsActivity.PREF_LAST_MEDIA_SCAN, 0);
				e.commit();
				displayCourses(userId);
			}
		});
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				tempCourse = null;
			}
		});
		builder.show();
	}

	private void confirmCourseReset() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setTitle(R.string.course_context_reset);
		builder.setMessage(R.string.course_context_reset_confirm);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				DbHelper db = new DbHelper(OppiaMobileActivity.this);
				long userId = db.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
				db.resetCourse(tempCourse.getCourseId(),userId);
				DatabaseManager.getInstance().closeDatabase();
				displayCourses(userId);
			}
		});
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				tempCourse = null;
			}
		});
		builder.show();
	}

    private void animateTopMessage(){
        TranslateAnimation anim = new TranslateAnimation(0, 0, -200, 0);
        anim.setDuration(900);
        messageContainer.startAnimation(anim);

        ValueAnimator animator = ValueAnimator.ofInt(courseList.getPaddingTop(), 90);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            //@Override
            public void onAnimationUpdate(ValueAnimator valueAnimator){
                courseList.setPadding(0, (Integer) valueAnimator.getAnimatedValue(), 0, 0);
                courseList.setSelectionAfterHeaderView();
            }
        });
        animator.setStartDelay(200);
        animator.setDuration(700);
        animator.start();
    }

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		
		if(key.equalsIgnoreCase(PrefsActivity.PREF_SERVER)){
			Editor editor = sharedPreferences.edit();
			if(!sharedPreferences.getString(PrefsActivity.PREF_SERVER, "").endsWith("/")){
				String newServer = sharedPreferences.getString(PrefsActivity.PREF_SERVER, "").trim()+"/";
				editor.putString(PrefsActivity.PREF_SERVER, newServer);
		    	editor.commit();
			}
		}
		
		if(key.equalsIgnoreCase(PrefsActivity.PREF_SHOW_SCHEDULE_REMINDERS) || key.equalsIgnoreCase(PrefsActivity.PREF_NO_SCHEDULE_REMINDERS)){
			displayCourses(userId);
		}
		
		if(key.equalsIgnoreCase(PrefsActivity.PREF_POINTS)
				|| key.equalsIgnoreCase(PrefsActivity.PREF_BADGES)){
			supportInvalidateOptionsMenu();
		}

		if(key.equalsIgnoreCase(PrefsActivity.PREF_DOWNLOAD_VIA_CELLULAR_ENABLED)){
			boolean newPref = sharedPreferences.getBoolean(PrefsActivity.PREF_DOWNLOAD_VIA_CELLULAR_ENABLED, false);
			Log.d(TAG, "PREF_DOWNLOAD_VIA_CELLULAR_ENABLED" + newPref);
		}
	}

	public void scanStart() {
        messageText.setText(this.getString(R.string.info_scan_media_start));
	}

	public void scanProgressUpdate(String msg) {
        messageText.setText(this.getString(R.string.info_scan_media_checking, msg));
	}

	public void scanComplete(Payload response) {
		Editor e = prefs.edit();

		if (response.getResponseData().size() > 0) {
            if (messageContainer.getVisibility() != View.VISIBLE){
                messageContainer.setVisibility(View.VISIBLE);
                messageButton.setOnClickListener(new OnClickListener() {

                    public void onClick(View view) {
                        @SuppressWarnings("unchecked")
                        ArrayList<Object> m = (ArrayList<Object>) view.getTag();
                        Intent i = new Intent(OppiaMobileActivity.this, DownloadMediaActivity.class);
                        Bundle tb = new Bundle();
                        tb.putSerializable(DownloadMediaActivity.TAG, m);
                        i.putExtras(tb);
                        startActivity(i);
                    }
                });
                animateTopMessage();
            }

            messageText.setText(this.getString(R.string.info_scan_media_missing));
            messageButton.setText(this.getString(R.string.scan_media_download_button));
            messageButton.setTag(response.getResponseData());
			e.putLong(PrefsActivity.PREF_LAST_MEDIA_SCAN, 0);
			e.commit();
		} else {
            messageContainer.setVisibility(View.GONE);
            messageButton.setOnClickListener(null);
            messageButton.setTag(null);
			long now = System.currentTimeMillis()/1000;
			e.putLong(PrefsActivity.PREF_LAST_MEDIA_SCAN, now);
			e.commit();
		}
	}
}
