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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
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
import org.digitalcampus.oppia.application.AdminSecurityManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.listener.CourseInstallerListener;
import org.digitalcampus.oppia.listener.DeleteCourseListener;
import org.digitalcampus.oppia.listener.ScanMediaListener;
import org.digitalcampus.oppia.listener.UpdateActivityListener;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CoursesRepository;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.service.courseinstall.CourseIntallerService;
import org.digitalcampus.oppia.service.courseinstall.InstallerBroadcastReceiver;
import org.digitalcampus.oppia.task.DeleteCourseTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.ScanMediaTask;
import org.digitalcampus.oppia.task.UpdateCourseActivityTask;
import org.digitalcampus.oppia.utils.UIUtils;
import org.digitalcampus.oppia.utils.ui.CourseContextMenuCustom;
import org.digitalcampus.oppia.utils.ui.DrawerMenuManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;

public class OppiaMobileActivity
        extends AppActivity
        implements
            OnSharedPreferenceChangeListener,
            ScanMediaListener,
            DeleteCourseListener,
            CourseInstallerListener,
            UpdateActivityListener,
            CourseContextMenuCustom.OnContextMenuListener {

	public static final String TAG = OppiaMobileActivity.class.getSimpleName();
	private ArrayList<Course> courses;
	private Course tempCourse;
    private int initialCourseListPadding = 0;

    private TextView messageText;
    private Button messageButton;
    private View messageContainer;
    private ListView courseList;
    private View noCoursesView;

    private CourseListAdapter courseListAdapter;
    private ProgressDialog progressDialog;
    private InstallerBroadcastReceiver receiver;
    private DrawerMenuManager drawer;

    @Inject CoursesRepository coursesRepository;
    @Inject SharedPreferences prefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);

        initializeDagger();


        prefs.registerOnSharedPreferenceChangeListener(this);

		// set preferred lang to the default lang
		if ("".equals(prefs.getString(PrefsActivity.PREF_LANGUAGE, ""))) {
			prefs.edit().putString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage()).apply();
		}

        messageContainer = this.findViewById(R.id.home_messages);
        messageText = this.findViewById(R.id.home_message);
        messageButton = this.findViewById(R.id.message_action_button);

        courses = new ArrayList<>();
        courseListAdapter = new CourseListAdapter(this, courses);
        courseList = findViewById(R.id.course_list);
        courseList.setAdapter(courseListAdapter);

        CourseContextMenuCustom courseMenu = new CourseContextMenuCustom(this);
        courseMenu.registerForContextMenu(courseList, this);

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

        noCoursesView = this.findViewById(R.id.no_courses);
        initialCourseListPadding = courseList.getPaddingTop();

	}

    private void initializeDagger() {
        MobileLearning app = (MobileLearning) getApplication();
        app.getComponent().inject(this);
    }

    @Override
	public void onStart() {
		super.onStart();
        drawer = new DrawerMenuManager(this, true);
        drawer.initializeDrawer();
		displayCourses();
	}

	@Override
	public void onResume(){
		super.onResume();

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
	
	private void displayCourses() {
        courses.clear();
		courses.addAll(coursesRepository.getCourses(this));
		
		LinearLayout llLoading = this.findViewById(R.id.loading_courses);
		llLoading.setVisibility(View.GONE);
		
		if (courses.size() < MobileLearning.DOWNLOAD_COURSES_DISPLAY){
			displayDownloadSection();
		} else {
			TextView tv = this.findViewById(R.id.manage_courses_text);
			tv.setText(R.string.no_courses);
            noCoursesView.setVisibility(View.GONE);
		}

        courseListAdapter.notifyDataSetChanged();
		this.scanMedia();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		UIUtils.showUserData(menu, this, null, false);
        Map<Integer, DrawerMenuManager.MenuOption> mainOptions = new HashMap<>();
        mainOptions.put(R.id.menu_settings, new DrawerMenuManager.MenuOption(){
            public void onOptionSelected(){ startPrefsActivity(); }
        });
        mainOptions.put(R.id.menu_language, new DrawerMenuManager.MenuOption() {
            @Override
            public void onOptionSelected() { createLanguageDialog(); }
        });
        drawer.onPrepareOptionsMenu(menu, mainOptions);
	    return super.onPrepareOptionsMenu(menu);
	}

    private void startPrefsActivity(){
        Intent i = new Intent(OppiaMobileActivity.this, PrefsActivity.class);
        Bundle tb = new Bundle();
        ArrayList<Lang> langs = new ArrayList<>();
        for(Course m: courses){ langs.addAll(m.getLangs()); }
        tb.putSerializable("langs", langs);
        i.putExtras(tb);
        startActivity(i);
    }

    private void displayDownloadSection(){
        noCoursesView.setVisibility(View.VISIBLE);

        TextView tv = this.findViewById(R.id.manage_courses_text);
        tv.setText((!courses.isEmpty())? R.string.more_courses : R.string.no_courses);

        Button manageBtn = this.findViewById(R.id.manage_courses_btn);
        manageBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AdminSecurityManager.checkAdminPermission(OppiaMobileActivity.this, R.id.menu_download, new AdminSecurityManager.AuthListener() {
                public void onPermissionGranted() {
                    startActivity(new Intent(OppiaMobileActivity.this, TagSelectActivity.class));
                }
            });
            }
        });
    }
	private void createLanguageDialog() {
		ArrayList<Lang> langs = new ArrayList<>();
		for(Course m: courses){ langs.addAll(m.getLangs()); }

        UIUtils.createLanguageDialog(this, langs, prefs, new Callable<Boolean>() {
            public Boolean call(){
                OppiaMobileActivity.this.onStart();
                return true;
            }
        });
	}

    //@Override
    public void onContextMenuItemSelected(final int position, final int itemId) {
        AdminSecurityManager.checkAdminPermission(this, itemId, new AdminSecurityManager.AuthListener() {
            public void onPermissionGranted() {
                tempCourse = courses.get(position);
                if (itemId == R.id.course_context_delete) {
                    if (prefs.getBoolean(PrefsActivity.PREF_DELETE_COURSE_ENABLED, true)){
                        confirmCourseDelete();
                    } else {
                        Toast.makeText(OppiaMobileActivity.this, getString(R.string.warning_delete_disabled), Toast.LENGTH_LONG).show();
                    }
                } else if (itemId == R.id.course_context_reset) {
                    confirmCourseReset();
                } else if (itemId == R.id.course_context_update_activity){
                    confirmCourseUpdateActivity();
                }
            }
        });
    }

	private void confirmCourseDelete() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Oppia_AlertDialogStyle);
		builder.setCancelable(false);
		builder.setTitle(R.string.course_context_delete);
		builder.setMessage(R.string.course_context_delete_confirm);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
                DeleteCourseTask task = new DeleteCourseTask(OppiaMobileActivity.this);
                ArrayList<Object> payloadData = new ArrayList<>();
                payloadData.add(tempCourse);
                Payload p = new Payload(payloadData);
                task.setOnDeleteCourseListener(OppiaMobileActivity.this);
                task.execute(p);

                progressDialog = new ProgressDialog(OppiaMobileActivity.this, R.style.Oppia_AlertDialogStyle);
                progressDialog.setMessage(getString(R.string.course_deleting));
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
		AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.Oppia_AlertDialogStyle);
		builder.setCancelable(false);
		builder.setTitle(R.string.course_context_reset);
		builder.setMessage(R.string.course_context_reset_confirm);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DbHelper db = DbHelper.getInstance(OppiaMobileActivity.this);
                db.resetCourse(tempCourse.getCourseId(), SessionManager.getUserId(OppiaMobileActivity.this));
                displayCourses();
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
        UpdateCourseActivityTask task = new UpdateCourseActivityTask(OppiaMobileActivity.this, SessionManager.getUserId(this));
        ArrayList<Object> payloadData = new ArrayList<>();
        payloadData.add(tempCourse);
        Payload p = new Payload(payloadData);
        task.setUpdateActivityListener(OppiaMobileActivity.this);
        task.execute(p);

        progressDialog = new ProgressDialog(OppiaMobileActivity.this, R.style.Oppia_AlertDialogStyle);
        progressDialog.setMessage(getString(R.string.course_updating));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

		if(key.equalsIgnoreCase(PrefsActivity.PREF_SHOW_SCHEDULE_REMINDERS) || key.equalsIgnoreCase(PrefsActivity.PREF_NO_SCHEDULE_REMINDERS)){
			displayCourses();
		}

		if(key.equalsIgnoreCase(PrefsActivity.PREF_DOWNLOAD_VIA_CELLULAR_ENABLED)){
			boolean newPref = sharedPreferences.getBoolean(PrefsActivity.PREF_DOWNLOAD_VIA_CELLULAR_ENABLED, false);
			Log.d(TAG, "PREF_DOWNLOAD_VIA_CELLULAR_ENABLED" + newPref);
		}
		
		// update the points/badges by invalidating the menu
		if(key.equalsIgnoreCase(PrefsActivity.PREF_TRIGGER_POINTS_REFRESH) || key.equalsIgnoreCase(PrefsActivity.PREF_LOGOUT_ENABLED)){
			supportInvalidateOptionsMenu();
		}
	}

    //region ScanMedia
    ///Everything related to the ScanMediaTask, including UI management

    private void scanMedia() {

	    if (Media.shouldScanMedia(prefs)){
            ScanMediaTask task = new ScanMediaTask(this);
            Payload p = new Payload(this.courses);
            task.setScanMediaListener(this);
            task.execute(p);
        }
        else{
            hideScanMediaMessage();
        }
    }


    private void animateScanMediaMessage(){
        TranslateAnimation anim = new TranslateAnimation(0, 0, -200, 0);
        anim.setDuration(900);
        messageContainer.startAnimation(anim);

        messageContainer.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        ValueAnimator animator = ValueAnimator.ofInt(initialCourseListPadding, messageContainer.getMeasuredHeight());
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

    /* ScanMediaListener implementation */
	public void scanStart() {
        messageText.setText(this.getString(R.string.info_scan_media_start));
	}

	public void scanProgressUpdate(String msg) {
        messageText.setText(this.getString(R.string.info_scan_media_checking, msg));
	}

	public void scanComplete(Payload response) {
		if (!response.getResponseData().isEmpty()) {
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
            Media.resetMediaScan(prefs);
		} else {
            hideScanMediaMessage();
            messageButton.setOnClickListener(null);
            messageButton.setTag(null);
			Media.updateMediaScan(prefs);
		}
	}
    //endregion

    //@Override
    public void onCourseDeletionComplete(Payload response) {
        if (response.isResult()){
            Media.resetMediaScan(prefs);
        }
        if (progressDialog != null){
            progressDialog.dismiss();
        }
        Toast.makeText(OppiaMobileActivity.this,
                getString(response.isResult()? R.string.course_deleting_success : R.string.course_deleting_error),
                Toast.LENGTH_LONG).show();
        displayCourses();
    }

    /* CourseInstallerListener implementation */
    public void onInstallComplete(String fileUrl) {
        Toast.makeText(this, this.getString(R.string.install_complete), Toast.LENGTH_LONG).show();
        displayCourses();
    }

    public void onDownloadProgress(String fileUrl, int progress) {
        // no need to show download progress in this activity
    }

    public void onInstallProgress(String fileUrl, int progress) {
        // no need to show install progress in this activity
    }

    public void onInstallFailed(String fileUrl, String message) {
        // no need to show install failed in this activity
    }

    /* UpdateActivityListener implementation */
    public void updateActivityProgressUpdate(DownloadProgress dp) {
        // no need to show download progress in this activity
    }

	public void updateActivityComplete(Payload response) {
        Course course = (Course) response.getData().get(0);
        if (progressDialog != null){
            progressDialog.dismiss();
        }
        Toast.makeText(OppiaMobileActivity.this,
                getString(
                    response.isResult() ? R.string.course_updating_success : R.string.course_updating_error,
                    (course!=null) ? course.getShortname() : ""),
                Toast.LENGTH_LONG).show();
        displayCourses();
	}


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawer.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        drawer.onConfigurationChanged(newConfig);
    }

}
