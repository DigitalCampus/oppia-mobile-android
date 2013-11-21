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

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.R.id;
import org.digitalcampus.oppia.adapter.CourseListAdapter;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.exception.ModuleNotFoundException;
import org.digitalcampus.oppia.listener.InstallModuleListener;
import org.digitalcampus.oppia.listener.PostInstallListener;
import org.digitalcampus.oppia.listener.ScanMediaListener;
import org.digitalcampus.oppia.listener.UpgradeListener;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.task.InstallDownloadedModulesTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.PostInstallTask;
import org.digitalcampus.oppia.task.ScanMediaTask;
import org.digitalcampus.oppia.task.UpgradeManagerTask;
import org.digitalcampus.oppia.utils.FileUtils;
import org.digitalcampus.oppia.utils.UIUtils;

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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

public class OppiaMobileActivity extends AppActivity implements InstallModuleListener,
		OnSharedPreferenceChangeListener, ScanMediaListener, UpgradeListener, PostInstallListener {

	public static final String TAG = OppiaMobileActivity.class.getSimpleName();
	private SharedPreferences prefs;
	private Course tempMod;
	private ArrayList<Course> courses;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BugSenseHandler.initAndStartSession(this, MobileLearning.BUGSENSE_API_KEY);
		setContentView(R.layout.activity_main);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
		
		this.drawHeader();
		this.drawMessages();
		
		// set preferred lang to the default lang
		if (prefs.getString(getString(R.string.prefs_language), "").equals("")) {
			Editor editor = prefs.edit();
			editor.putString(getString(R.string.prefs_language), Locale.getDefault().getLanguage());
			editor.commit();
		}

		// set up local dirs
		if(!MobileLearning.createDirs()){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setCancelable(false);
			builder.setTitle(R.string.error);
			builder.setMessage(R.string.error_sdcard);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					OppiaMobileActivity.this.finish();
				}
			});
			builder.show();
			return;
		}
		// do upgrade if required
		UpgradeManagerTask umt = new UpgradeManagerTask(this);
		umt.setUpgradeListener(this);
		ArrayList<Object> data = new ArrayList<Object>();
 		Payload p = new Payload(data);
		umt.execute(p);
	}

	@Override
	public void onStart() {
		super.onStart();
		if (!MobileLearning.isLoggedIn(this)) {
			startActivity(new Intent(OppiaMobileActivity.this, LoginActivity.class));
			return;
		}
		
		displayCourses();		
	}

	@Override
	public void onResume(){
		super.onResume();
		this.updateHeader();
		this.updateReminders();
	}
	
	@Override
	public void onPause(){
		this.stopMessages();
		super.onPause();
	}
	
	private void displayCourses() {

		DbHelper db = new DbHelper(this);
		courses = db.getModules();
		db.close();
		
		if(MobileLearning.createDirs()){
			// only remove courses if the SD card is present 
			//- else it will remove the courses just because the SD card isn't in
			ArrayList<Course> removeCourses = new ArrayList<Course>();
			for (Course c : courses) {
				try {
					c.validate();
				} catch (ModuleNotFoundException mnfe){
					// remove from database
					mnfe.deleteModule(this, c.getModId());
					removeCourses.add(c);
				}
			}
			
			for(Course c: removeCourses){
				// remove from current list
				courses.remove(c);
			}
		}

		LinearLayout llLoading = (LinearLayout) this.findViewById(R.id.loading_courses);
		llLoading.setVisibility(View.GONE);
		LinearLayout llNone = (LinearLayout) this.findViewById(R.id.no_modules);
		if (courses.size() > 0) {
			llNone.setVisibility(View.GONE);
		} else {
			llNone.setVisibility(View.VISIBLE);
			Button manageBtn = (Button) this.findViewById(R.id.manage_modules_btn);
			manageBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					startActivity(new Intent(OppiaMobileActivity.this, TagSelectActivity.class));
				}
			});
		}

		CourseListAdapter mla = new CourseListAdapter(this, courses);
		ListView listView = (ListView) findViewById(R.id.module_list);
		listView.setAdapter(mla);
		registerForContextMenu(listView);

		listView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Course m = (Course) view.getTag();
				Intent i = new Intent(OppiaMobileActivity.this, CourseIndexActivity.class);
				Bundle tb = new Bundle();
				tb.putSerializable(Course.TAG, m);
				i.putExtras(tb);
				startActivity(i);
			}
		});

		this.updateReminders();
		
		// scan media
		this.scanMedia();
	}

	private void updateReminders(){
		if(prefs.getBoolean(getString(R.string.prefs_schedule_reminders_show), true)){
			DbHelper db = new DbHelper(OppiaMobileActivity.this);
			int max = Integer.valueOf(prefs.getString(getString(R.string.prefs_schedule_reminders_no), "3"));
			ArrayList<Activity> activities = db.getActivitiesDue(max);
			db.close();
			this.drawReminders(activities);
		} else {
			LinearLayout ll = (LinearLayout) findViewById(R.id.schedule_reminders);
			ll.setVisibility(View.GONE);
		}		
	}
	
	private void scanMedia() {
		long now = System.currentTimeMillis()/1000;
		if (prefs.getLong(getString(R.string.prefs_last_media_scan), 0)+3600 > now) {
			LinearLayout ll = (LinearLayout) this.findViewById(id.home_messages);
			ll.setVisibility(View.GONE);
			return;
		}
		ScanMediaTask task = new ScanMediaTask();
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
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_about:
				startActivity(new Intent(this, AboutActivity.class));
				return true;
			case R.id.menu_download:
				startActivity(new Intent(this, TagSelectActivity.class));
				return true;
			case R.id.menu_settings:
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
			case R.id.menu_language:
				createLanguageDialog();
				return true;
			case R.id.menu_help:
				startActivity(new Intent(this, HelpActivity.class));
				return true;
			case R.id.menu_monitor:
				startActivity(new Intent(this, MonitorActivity.class));
				return true;
			case R.id.menu_logout:
				logout();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
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
				// wipe activity data
				DbHelper db = new DbHelper(OppiaMobileActivity.this);
				db.onLogout();
				db.close();

				// wipe user prefs
				Editor editor = prefs.edit();
				editor.putString(getString(R.string.prefs_username), "");
				editor.putString(getString(R.string.prefs_api_key), "");
				editor.putInt(getString(R.string.prefs_badges), 0);
				editor.putInt(getString(R.string.prefs_points), 0);
				editor.commit();

				// restart this activity
				OppiaMobileActivity.this.onStart();

			}
		});
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return; // do nothing
			}
		});
		builder.show();
	}

	public void installComplete(Payload p) {
		if(p.getResponseData().size()>0){
			Editor e = prefs.edit();
			e.putLong(getString(R.string.prefs_last_media_scan), 0);
			e.commit();
			displayCourses();
		}
	}

	public void installProgressUpdate(DownloadProgress dp) {
		//do nothing
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.course_context_menu, menu);
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		tempMod = (Course) info.targetView.getTag();
		switch (item.getItemId()) {
			case R.id.course_context_delete:
				confirmModuleDelete();
				return true;
			case R.id.course_context_reset:
				confirmModuleReset();
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}

	private void confirmModuleDelete() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setTitle(R.string.course_context_delete);
		builder.setMessage(R.string.course_context_delete_confirm);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// remove db records
				DbHelper db = new DbHelper(OppiaMobileActivity.this);
				db.deleteModule(tempMod.getModId());
				db.close();
				// remove files
				File f = new File(tempMod.getLocation());
				FileUtils.deleteDir(f);
				Editor e = prefs.edit();
				e.putLong(getString(R.string.prefs_last_media_scan), 0);
				e.commit();
				displayCourses();
			}
		});
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				tempMod = null;
			}
		});
		builder.show();
	}

	private void confirmModuleReset() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setTitle(R.string.course_context_reset);
		builder.setMessage(R.string.course_context_reset_confirm);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				DbHelper db = new DbHelper(OppiaMobileActivity.this);
				db.resetModule(tempMod.getModId());
				db.close();
				displayCourses();
			}
		});
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				tempMod = null;
			}
		});
		builder.show();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.d(TAG, key + " changed");
		if(key.equalsIgnoreCase(getString(R.string.prefs_server))){
			Editor editor = sharedPreferences.edit();
			if(!sharedPreferences.getString(getString(R.string.prefs_server), "").endsWith("/")){
				String newServer = sharedPreferences.getString(getString(R.string.prefs_server), "").trim()+"/";
				editor.putString(getString(R.string.prefs_server), newServer);
		    	editor.commit();
			}
		}
		if(key.equalsIgnoreCase(getString(R.string.prefs_schedule_reminders_show)) || key.equalsIgnoreCase(getString(R.string.prefs_schedule_reminders_no))){
			displayCourses();
		}
		super.onSharedPreferenceChanged(sharedPreferences, key);
	}

	public void downloadComplete(Payload p) {
		// do nothing

	}

	public void scanStart() {
		TextView tv = (TextView) this.findViewById(id.home_message);
		tv.setText(this.getString(R.string.info_scan_media_start));
	}

	public void scanProgressUpdate(String msg) {
		TextView tv = (TextView) this.findViewById(id.home_message);
		tv.setText(this.getString(R.string.info_scan_media_checking, msg));
	}

	public void scanComplete(Payload response) {
		Editor e = prefs.edit();
		LinearLayout ll = (LinearLayout) this.findViewById(id.home_messages);
		TextView tv = (TextView) this.findViewById(id.home_message);
		Button btn = (Button) this.findViewById(R.id.message_action_button);
		
		if (response.getResponseData().size() > 0) {
			ll.setVisibility(View.VISIBLE);
			tv.setText(this.getString(R.string.info_scan_media_missing));
			btn.setText(this.getString(R.string.scan_media_download_button));
			btn.setTag(response.getResponseData());
			btn.setOnClickListener(new OnClickListener() {

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
			e.putLong(getString(R.string.prefs_last_media_scan), 0);
			e.commit();
		} else {
			ll.setVisibility(View.GONE);
			tv.setText("");
			btn.setText("");
			btn.setOnClickListener(null);
			btn.setTag(null);
			long now = System.currentTimeMillis()/1000;
			e.putLong(getString(R.string.prefs_last_media_scan), now);
			e.commit();
		}
	}

	public void downloadProgressUpdate(DownloadProgress dp) {
		// do nothing
		
	}

	public void upgradeComplete(Payload p) {
		if(p.isResult()){
			displayCourses();
			Payload payload = new Payload();
			PostInstallTask piTask = new PostInstallTask(OppiaMobileActivity.this);
			piTask.setPostInstallListener(this);
			piTask.execute(payload);
		} else {
			// now install any new courses
			this.installCourses();
		}
	}

	public void upgradeProgressUpdate(String s) {
		// do nothing
		
	}

	public void postInstallComplete(Payload response) {
		// now install any new courses
		this.installCourses();
	}
	
	private void installCourses(){
		File dir = new File(MobileLearning.DOWNLOAD_PATH);
		String[] children = dir.list();
		if (children != null) {
			ArrayList<Object> data = new ArrayList<Object>();
     		Payload payload = new Payload(data);
			InstallDownloadedModulesTask imTask = new InstallDownloadedModulesTask(OppiaMobileActivity.this);
			imTask.setInstallerListener(this);
			imTask.execute(payload);
		}
	}

}
