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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import org.digitalcampus.oppia.listener.CourseInstallerListener;
import org.digitalcampus.oppia.listener.DeleteCourseListener;
import org.digitalcampus.oppia.listener.ScanMediaListener;
import org.digitalcampus.oppia.listener.UpdateActivityListener;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.service.CourseIntallerService;
import org.digitalcampus.oppia.service.InstallerBroadcastReceiver;
import org.digitalcampus.oppia.task.DeleteCourseTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.ScanMediaTask;
import org.digitalcampus.oppia.task.UpdateCourseActivityTask;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.ui.CourseContextMenuCustom;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;

public class OppiaMobileActivity extends AppActivity implements OnSharedPreferenceChangeListener, ScanMediaListener, DeleteCourseListener, CourseInstallerListener, UpdateActivityListener, CourseContextMenuCustom.OnContextMenuListener {

	public static final String TAG = OppiaMobileActivity.class.getSimpleName();
	private SharedPreferences prefs;
	private ArrayList<Course> courses;
	private Course tempCourse;
	private long userId = 0;
    private int initialCourseListPadding = 0;

    private TextView messageText;
    private Button messageButton;
    private View messageContainer;

    private CourseListAdapter courseListAdapter;
    private ListView courseList;

    private ProgressDialog progressDialog;
    private InstallerBroadcastReceiver receiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
        prefs.registerOnSharedPreferenceChangeListener(this);

		// set preferred lang to the default lang
		if ("".equals(prefs.getString(PrefsActivity.PREF_LANGUAGE, ""))) {
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

        CourseContextMenuCustom courseMenu = new CourseContextMenuCustom(this);
        courseMenu.registerForContextMenu(courseList, this);
        //registerForContextMenu(courseList);

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

        initialCourseListPadding = courseList.getPaddingTop();
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

        receiver = new InstallerBroadcastReceiver();
        receiver.setCourseInstallerListener(this);
        IntentFilter broadcastFilter = new IntentFilter(CourseIntallerService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(receiver, broadcastFilter);
	}
	
	@Override
	public void onPause(){
        super.onPause();
        unregisterReceiver(receiver);
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		UIUtils.showUserData(menu, this, null);
		MenuItem item = menu.findItem(R.id.menu_logout);
		item.setVisible(prefs.getBoolean(PrefsActivity.PREF_LOGOUT_ENABLED, true));
	    return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		Log.d(TAG, "Menu item selected: " + item.getTitle());
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

        UIUtils.createLanguageDialog(this, langs, prefs, new Callable<Boolean>() {
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
				editor.commit();

				// restart the app
				OppiaMobileActivity.this.startActivity(new Intent(OppiaMobileActivity.this, StartUpActivity.class));
				OppiaMobileActivity.this.finish();

			}
		});
		builder.setNegativeButton(R.string.no, null);
		builder.show();
	}

    //@Override
    public void onContextMenuItemSelected(int position, int itemId) {
        tempCourse = courses.get(position);
        if (itemId == R.id.course_context_delete) {
            if (prefs.getBoolean(PrefsActivity.PREF_DELETE_COURSE_ENABLED, true)){
                confirmCourseDelete();
            } else {
                Toast.makeText(this, this.getString(R.string.warning_delete_disabled), Toast.LENGTH_LONG).show();
            }
        } else if (itemId == R.id.course_context_reset) {
            confirmCourseReset();
        } else if (itemId == R.id.course_context_update_activity){
            confirmCourseUpdateActivity();
        }
    }

	private void confirmCourseDelete() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setTitle(R.string.course_context_delete);
		builder.setMessage(R.string.course_context_delete_confirm);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
                DeleteCourseTask task = new DeleteCourseTask(OppiaMobileActivity.this);
                ArrayList<Object> payloadData = new ArrayList<Object>();
                payloadData.add(tempCourse);
                Payload p = new Payload(payloadData);
                task.setOnDeleteCourseListener(OppiaMobileActivity.this);
                task.execute(p);

                progressDialog = new ProgressDialog(OppiaMobileActivity.this);
                progressDialog.setMessage("Deleting course...");
                progressDialog.setCancelable(false);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
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
                db.resetCourse(tempCourse.getCourseId(), OppiaMobileActivity.this.userId);
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
	
	private void confirmCourseUpdateActivity(){
		UpdateCourseActivityTask task = new UpdateCourseActivityTask(OppiaMobileActivity.this, this.userId);
	     ArrayList<Object> payloadData = new ArrayList<Object>();
	     payloadData.add(tempCourse);
	     Payload p = new Payload(payloadData);
	     task.setUpdateActivityListener(OppiaMobileActivity.this);
	     task.execute(p);
	
	     progressDialog = new ProgressDialog(OppiaMobileActivity.this);
	     progressDialog.setMessage("Updating activity...");
	     progressDialog.setCancelable(false);
	     progressDialog.setCanceledOnTouchOutside(false);
	     progressDialog.show();
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

		if(key.equalsIgnoreCase(PrefsActivity.PREF_DOWNLOAD_VIA_CELLULAR_ENABLED)){
			boolean newPref = sharedPreferences.getBoolean(PrefsActivity.PREF_DOWNLOAD_VIA_CELLULAR_ENABLED, false);
			Log.d(TAG, "PREF_DOWNLOAD_VIA_CELLULAR_ENABLED" + newPref);
		}
		
		// update the points/badges by invalidating the menu
		if(key.equalsIgnoreCase(PrefsActivity.PREF_TRIGGER_POINTS_REFRESH)){
			supportInvalidateOptionsMenu();
		}
	}

    //region ScanMedia
    ///Everything related to the ScanMediaTask, including UI management

    private void scanMedia() {
        long now = System.currentTimeMillis()/1000;
        long lastScan = prefs.getLong(PrefsActivity.PREF_LAST_MEDIA_SCAN, 0);
        if (lastScan + MobileLearning.MEDIA_SCAN_TIME_LIMIT > now) {
            hideScanMediaMessage();
            return;
        }
        ScanMediaTask task = new ScanMediaTask(this);
        Payload p = new Payload(this.courses);
        task.setScanMediaListener(this);
        task.execute(p);
    }

    private void updateLastScan(long scanTime){
        Editor e = prefs.edit();
        e.putLong(PrefsActivity.PREF_LAST_MEDIA_SCAN, scanTime);
        e.commit();
    }

    private void animateScanMediaMessage(){
        TranslateAnimation anim = new TranslateAnimation(0, 0, -200, 0);
        anim.setDuration(900);
        messageContainer.startAnimation(anim);

        ValueAnimator animator = ValueAnimator.ofInt(initialCourseListPadding, 90);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            //@Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                courseList.setPadding(0, (Integer) valueAnimator.getAnimatedValue(), 0, 0);
                courseList.setSelectionAfterHeaderView();
            }
        });
        animator.setStartDelay(200);
        animator.setDuration(700);
        animator.start();
    }

    private void hideScanMediaMessage(){
        messageContainer.setVisibility(View.GONE);
        courseList.setPadding(0, initialCourseListPadding, 0, 0);
    }

    //Implementation of ScanMediaListener

	public void scanStart() {
        messageText.setText(this.getString(R.string.info_scan_media_start));
	}

	public void scanProgressUpdate(String msg) {
        messageText.setText(this.getString(R.string.info_scan_media_checking, msg));
	}

	public void scanComplete(Payload response) {
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
                animateScanMediaMessage();
            }

            messageText.setText(this.getString(R.string.info_scan_media_missing));
            messageButton.setText(this.getString(R.string.scan_media_download_button));
            messageButton.setTag(response.getResponseData());
            updateLastScan(0);
		} else {
            hideScanMediaMessage();
            messageButton.setOnClickListener(null);
            messageButton.setTag(null);
			long now = System.currentTimeMillis()/1000;
			updateLastScan(now);
		}
	}
    //endregion

    //@Override
    public void onCourseDeletionComplete(Payload response) {
        if (response.isResult()){
            updateLastScan(0);
        }
        if (progressDialog != null){
            progressDialog.dismiss();
        }
        displayCourses(userId);
    }


    /* CourseInstallerListener implementation */
    // @Override
    public void onInstallComplete(String fileUrl) {
        Toast.makeText(this, this.getString(R.string.install_complete), Toast.LENGTH_LONG).show();
        displayCourses(userId);
    }
    public void onDownloadProgress(String fileUrl, int progress) {}
    public void onInstallProgress(String fileUrl, int progress) {}
    public void onInstallFailed(String fileUrl, String message) {}

	public void updateActivityComplete(Payload response) {
        if (progressDialog != null){
            progressDialog.dismiss();
        }
        displayCourses(userId);
		
	}

	public void updateActivityProgressUpdate(DownloadProgress dp) {
		// TODO Auto-generated method stub
		
	}


}
