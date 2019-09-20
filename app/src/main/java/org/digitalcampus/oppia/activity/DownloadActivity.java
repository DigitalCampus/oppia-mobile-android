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

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.CourseIntallViewAdapter;
import org.digitalcampus.oppia.adapter.DownloadCourseListAdapter;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.listener.CourseInstallerListener;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;
import org.digitalcampus.oppia.model.CourseInstallRepository;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.service.courseinstall.CourseInstallerServiceDelegate;
import org.digitalcampus.oppia.service.courseinstall.CourseIntallerService;
import org.digitalcampus.oppia.service.courseinstall.InstallerBroadcastReceiver;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.inject.Inject;

public class DownloadActivity extends AppActivity implements APIRequestListener, CourseInstallerListener {
	
	private SharedPreferences prefs;
	private ProgressDialog progressDialog;
	private JSONObject json;
	private DownloadCourseListAdapter dla;
	private String url;
	private ArrayList<CourseIntallViewAdapter> courses;
	private boolean showUpdatesOnly = false;

    private InstallerBroadcastReceiver receiver;

    @Inject CourseInstallRepository courseInstallRepository;
    @Inject CourseInstallerServiceDelegate courseInstallerServiceDelegate;

    @Override
    public void onStart(){
        super.onStart();
        initialize();
    }

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        initializeDagger();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            Tag t = (Tag) bundle.getSerializable(Tag.TAG);
            if (t != null){
                this.url = MobileLearning.SERVER_TAG_PATH + String.valueOf(t.getId()) + File.separator;
                TextView tagTitle = findViewById(R.id.category_title);
                tagTitle.setVisibility(View.VISIBLE);
                tagTitle.setText(t.getName());
            }

        } else {
            this.url = MobileLearning.SERVER_COURSES_PATH;
            this.showUpdatesOnly = true;
        }

        courses = new ArrayList<>();
        dla = new DownloadCourseListAdapter(this, courses);
        dla.setOnClickListener(new CourseListListener());
        ListView listView = findViewById(R.id.tag_list);
        if (listView != null) {
            listView.setAdapter(dla);
        }

    }

    private void initializeDagger() {
        MobileLearning app = (MobileLearning) getApplication();
        app.getComponent().inject(this);
    }
	
	@Override
	public void onResume(){
		super.onResume();
		if(json == null){
            // The JSON download task has not started or been completed yet
			getCourseList();
		} else if ((courses != null) && !courses.isEmpty()) {
            // We already have loaded JSON and courses (coming from orientationchange)
            dla.notifyDataSetChanged();
        }
        else{
            // The JSON is downloaded but course list is not
	        refreshCourseList();
		}
        receiver = new InstallerBroadcastReceiver();
        receiver.setCourseInstallerListener(this);
        IntentFilter broadcastFilter = new IntentFilter(CourseIntallerService.BROADCAST_ACTION);
        broadcastFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(receiver, broadcastFilter);

	}

	@Override
	public void onPause(){
		// Kill any open dialogs
		if (progressDialog != null){
            progressDialog.dismiss();
        }
		super.onPause();
        unregisterReceiver(receiver);
	}

    @Override
    public void onDestroy(){
        super.onDestroy();
    }
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

	    try {
			this.json = new JSONObject(savedInstanceState.getString("json"));
            ArrayList<CourseIntallViewAdapter> savedCourses = (ArrayList<CourseIntallViewAdapter>) savedInstanceState.getSerializable("courses");
            if (savedCourses!=null) this.courses.addAll(savedCourses);
		} catch (Exception e) {
            // error in the json so just get the list again
        }
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
            if (json != null){
                // Only save the instance if the request has been proccessed already
                savedInstanceState.putString("json", json.toString());
                savedInstanceState.putSerializable("courses", courses);
            }
	}
	
	private void getCourseList() {
		// show progress dialog
		progressDialog = new ProgressDialog(this, R.style.Oppia_AlertDialogStyle);
		progressDialog.setTitle(R.string.loading);
		progressDialog.setMessage(getString(R.string.loading));
		progressDialog.setCancelable(false);
		progressDialog.show();

        courseInstallRepository.getCourseList(this, url);
	}

	public void refreshCourseList() {
		// process the response and display on screen in listview
		// Create an array of courses, that will be put to our ListActivity
		try {
            String storage = prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, "");
            courses.clear();

            courseInstallRepository.refreshCourseList(this, courses, json, storage, showUpdatesOnly);

            dla.notifyDataSetChanged();
            findViewById(R.id.empty_state).setVisibility((courses.isEmpty()) ? View.VISIBLE : View.GONE);

		} catch (Exception e) {
			Mint.logException(e);
            Log.d(TAG, "Error processing response: ", e);
			UIUtils.showAlert(this, R.string.loading, R.string.error_processing_response);
		}
		
	}
	
	public void apiRequestComplete(Payload response) {
		progressDialog.dismiss();

        Callable<Boolean> finishActivity = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                DownloadActivity.this.finish();
                return true;
            }
        };

		if(response.isResult()){
			try {
				json = new JSONObject(response.getResultResponse());
				refreshCourseList();

			} catch (JSONException e) {
				Mint.logException(e);
                Log.d(TAG, "Error connecting to server: ", e);
				UIUtils.showAlert(this, R.string.loading, R.string.error_connection, finishActivity);
			}
		} else {
            String errorMsg = response.getResultResponse();
            UIUtils.showAlert(this, R.string.error, errorMsg, finishActivity);
		}
	}

    //@Override
    public void onDownloadProgress(String fileUrl, int progress) {
        CourseIntallViewAdapter course = findCourse(fileUrl);
        if (course != null){
            course.setDownloading(true);
            course.setInstalling(false);
            course.setProgress(progress);
            dla.notifyDataSetChanged();
        }
    }

    //@Override
    public void onInstallProgress(String fileUrl, int progress) {
        CourseIntallViewAdapter course = findCourse(fileUrl);
        if (course != null){
            course.setDownloading(false);
            course.setInstalling(true);
            course.setProgress(progress);
            dla.notifyDataSetChanged();
        }
    }

    //@Override
    public void onInstallFailed(String fileUrl, String message) {
        CourseIntallViewAdapter course = findCourse(fileUrl);
        if (course != null){
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            resetCourseProgress(course, false, false);
        }
    }

    //@Override
    public void onInstallComplete(String fileUrl) {
        CourseIntallViewAdapter course = findCourse(fileUrl);
        if (course != null){
            Toast.makeText(this, this.getString(R.string.install_course_complete, course.getShortname()), Toast.LENGTH_LONG).show();
            course.setInstalled(true);
            course.setToUpdate(false);
            course.setToUpdateSchedule(false);
            resetCourseProgress(course, false, false);
        }
    }

    private CourseIntallViewAdapter findCourse(String fileUrl){
        if (!courses.isEmpty()){
            for (CourseIntallViewAdapter course : courses){
                if (course.getDownloadUrl().equals(fileUrl)){
                    return course;
                }
            }
        }
        return null;
    }

    protected void resetCourseProgress(CourseIntallViewAdapter courseSelected,
                               boolean downloading, boolean installing ){

        courseSelected.setDownloading(downloading);
        courseSelected.setInstalling(installing);
        courseSelected.setProgress(0);
        dla.notifyDataSetChanged();
    }

    private class CourseListListener implements ListInnerBtnOnClickListener {
        @Override
        public void onClick(int position) {
            Log.d("course-download", "Clicked " + position);
            CourseIntallViewAdapter courseSelected = courses.get(position);

            // When installing, don't do anything on click
            if (courseSelected.isInstalling()) return;


            Intent mServiceIntent = new Intent(DownloadActivity.this, CourseIntallerService.class);

            if (!courseSelected.isDownloading()){
                if(!courseSelected.isInstalled() || courseSelected.isToUpdate()){
                    courseInstallerServiceDelegate.installCourse(DownloadActivity.this, mServiceIntent, courseSelected);

                    resetCourseProgress(courseSelected, true, false);
                }
                else if(courseSelected.isToUpdateSchedule()){
                    courseInstallerServiceDelegate.updateCourse(DownloadActivity.this, mServiceIntent, courseSelected);

                    resetCourseProgress(courseSelected, false, true);
                }
            }
            else{
                //If it's already downloading, send an intent to cancel the task
                courseInstallerServiceDelegate.cancelCourseInstall(DownloadActivity.this, mServiceIntent, courseSelected);

                resetCourseProgress(courseSelected, false, false);
            }

        }
    }

}
