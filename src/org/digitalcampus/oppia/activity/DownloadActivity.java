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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.CourseIntallViewAdapter;
import org.digitalcampus.oppia.adapter.DownloadCourseListAdapter;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.listener.CourseInstallerListener;
import org.digitalcampus.oppia.listener.ListInnerBtnOnClickListener;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.service.CourseIntallerService;
import org.digitalcampus.oppia.service.InstallerBroadcastReceiver;
import org.digitalcampus.oppia.task.APIRequestTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.splunk.mint.Mint;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

public class DownloadActivity extends AppActivity implements APIRequestListener, CourseInstallerListener {
	
	public static final String TAG = DownloadActivity.class.getSimpleName();
	
	private SharedPreferences prefs;
	private ProgressDialog progressDialog;
	private JSONObject json;
	private DownloadCourseListAdapter dla;
	private String url;
	private ArrayList<CourseIntallViewAdapter> courses;
	private boolean showUpdatesOnly = false;

    private InstallerBroadcastReceiver receiver;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
		Bundle bundle = this.getIntent().getExtras(); 
        if(bundle != null) {
        	Tag t = (Tag) bundle.getSerializable(Tag.TAG);
        	this.url = MobileLearning.SERVER_TAG_PATH + String.valueOf(t.getId()) + File.separator;
        } else {
        	this.url = MobileLearning.SERVER_COURSES_PATH;
        	this.showUpdatesOnly = true;
        }

        courses = new ArrayList<>();
        dla = new DownloadCourseListAdapter(this, courses);
        dla.setOnClickListener(new CourseListListener());
        ListView listView = (ListView) findViewById(R.id.tag_list);
        listView.setAdapter(dla);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(json == null){
            //The JSON download task has not started or been completed yet
			getCourseList();
		} else if ((courses != null) && courses.size()>0) {
            //We already have loaded JSON and courses (coming from orientationchange)
            dla.notifyDataSetChanged();
        }
        else{
            //The JSON is downloaded but course list is not
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
		//Kill any open dialogs
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
                //Only save the instance if the request has been proccessed already
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

		APIRequestTask task = new APIRequestTask(this);
		Payload p = new Payload(url);
		task.setAPIRequestListener(this);
		task.execute(p);
	}

	public void refreshCourseList() {
		// process the response and display on screen in listview
		// Create an array of courses, that will be put to our ListActivity

        ArrayList<String> downloadingCourses = CourseIntallerService.getTasksDownloading();
		
		try {
			this.courses.clear();
			
			for (int i = 0; i < (json.getJSONArray(MobileLearning.SERVER_COURSES_NAME).length()); i++) {
				JSONObject json_obj = (JSONObject) json.getJSONArray(MobileLearning.SERVER_COURSES_NAME).get(i);
                CourseIntallViewAdapter course = new CourseIntallViewAdapter(prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, ""));
				
				ArrayList<Lang> titles = new ArrayList<>();
				JSONObject jsonTitles = json_obj.getJSONObject("title");
				Iterator<?> keys = jsonTitles.keys();
		        while( keys.hasNext() ){
		            String key = (String) keys.next();
		            Lang l = new Lang(key,jsonTitles.getString(key));
					titles.add(l);
		        }
                course.setTitles(titles);
		        
		        ArrayList<Lang> descriptions = new ArrayList<>();
		        if (json_obj.has("description") && !json_obj.isNull("description")){
		        	try {
						JSONObject jsonDescriptions = json_obj.getJSONObject("description");
						Iterator<?> dkeys = jsonDescriptions.keys();
				        while( dkeys.hasNext() ){
				            String key = (String) dkeys.next();
				            if (!jsonDescriptions.isNull(key)){
					            Lang l = new Lang(key,jsonDescriptions.getString(key));
					            descriptions.add(l);
				            }
				        }
                        course.setDescriptions(descriptions);
		        	} catch (JSONException jsone){
		        		//do nothing
		        	}
		        }

                course.setShortname(json_obj.getString("shortname"));
                course.setVersionId(json_obj.getDouble("version"));
                course.setDownloadUrl(json_obj.getString("url"));
		        try {
                    course.setDraft(json_obj.getBoolean("is_draft"));
		        }catch (JSONException je){
                    course.setDraft(false);
		        }
		        DbHelper db = new DbHelper(this);
                course.setInstalled(db.isInstalled(course.getShortname()));
                course.setToUpdate(db.toUpdate(course.getShortname(), course.getVersionId()));
				if (json_obj.has("schedule_uri")){
                    course.setScheduleVersionID(json_obj.getDouble("schedule"));
                    course.setScheduleURI(json_obj.getString("schedule_uri"));
                    course.setToUpdateSchedule(db.toUpdateSchedule(course.getShortname(), course.getScheduleVersionID()));
				}
				DatabaseManager.getInstance().closeDatabase();
                if (downloadingCourses!=null && downloadingCourses.contains(course.getDownloadUrl())){
                    course.setDownloading(true);
                }
				if (!this.showUpdatesOnly || course.isToUpdate()){
					this.courses.add(course);
				} 
			}
            dla.notifyDataSetChanged();

		} catch (Exception e) {
			Mint.logException(e);
			e.printStackTrace();
			UIUtils.showAlert(this, R.string.loading, R.string.error_processing_response);
		}
		
	}
	
	public void apiRequestComplete(Payload response) {
		progressDialog.dismiss();
	
		if(response.isResult()){
			try {
				json = new JSONObject(response.getResultResponse());
				refreshCourseList();
			} catch (JSONException e) {
				Mint.logException(e);
				e.printStackTrace();
				UIUtils.showAlert(this, R.string.loading, R.string.error_connection);
			}
		} else {
			UIUtils.showAlert(this, R.string.error, R.string.error_connection_required, new Callable<Boolean>() {
				public Boolean call() throws Exception {
                DownloadActivity.this.finish();
                return true;
				}
			});
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
            course.setInstalling(false);
            course.setDownloading(false);
            dla.notifyDataSetChanged();
        }
    }

    //@Override
    public void onInstallComplete(String fileUrl) {
        CourseIntallViewAdapter course = findCourse(fileUrl);
        if (course != null){
            Toast.makeText(this, this.getString(R.string.install_course_complete, course.getShortname()), Toast.LENGTH_LONG).show();
            course.setInstalled(true);
            course.setInstalling(false);
            course.setDownloading(false);
            dla.notifyDataSetChanged();
        }
    }

    private CourseIntallViewAdapter findCourse(String fileUrl){
        if ( courses.size()>0){
            for (CourseIntallViewAdapter course : courses){
                if (course.getDownloadUrl().equals(fileUrl)){
                    return course;
                }
            }
        }
        return null;
    }

    private class CourseListListener implements ListInnerBtnOnClickListener {
        //@Override
        public void onClick(int position) {
            Log.d("course-download", "Clicked " + position);
            CourseIntallViewAdapter courseSelected = courses.get(position);

            //When installing, don't do anything on click
            if (courseSelected.isInstalling()) return;

            if (!courseSelected.isDownloading()){
                if(!courseSelected.isInstalled() || courseSelected.isToUpdate()){
                    Intent mServiceIntent = new Intent(DownloadActivity.this, CourseIntallerService.class);
                    mServiceIntent.putExtra(CourseIntallerService.SERVICE_ACTION, CourseIntallerService.ACTION_DOWNLOAD);
                    mServiceIntent.putExtra(CourseIntallerService.SERVICE_URL, courseSelected.getDownloadUrl());
                    mServiceIntent.putExtra(CourseIntallerService.SERVICE_VERSIONID, courseSelected.getVersionId());
                    mServiceIntent.putExtra(CourseIntallerService.SERVICE_SHORTNAME, courseSelected.getShortname());
                    DownloadActivity.this.startService(mServiceIntent);

                    courseSelected.setDownloading(true);
                    courseSelected.setInstalling(false);
                    courseSelected.setProgress(0);
                    dla.notifyDataSetChanged();
                }
                else if(courseSelected.isToUpdateSchedule()){
                    Intent mServiceIntent = new Intent(DownloadActivity.this, CourseIntallerService.class);
                    mServiceIntent.putExtra(CourseIntallerService.SERVICE_ACTION, CourseIntallerService.ACTION_UPDATE);
                    mServiceIntent.putExtra(CourseIntallerService.SERVICE_SCHEDULEURL, courseSelected.getScheduleURI());
                    mServiceIntent.putExtra(CourseIntallerService.SERVICE_SHORTNAME, courseSelected.getShortname());

                    DownloadActivity.this.startService(mServiceIntent);
                    courseSelected.setDownloading(false);
                    courseSelected.setInstalling(true);
                    courseSelected.setProgress(0);
                    dla.notifyDataSetChanged();
                }
            }
            else{
                //If it's already downloading, send an intent to cancel the task
                Intent mServiceIntent = new Intent(DownloadActivity.this, CourseIntallerService.class);
                mServiceIntent.putExtra(CourseIntallerService.SERVICE_ACTION, CourseIntallerService.ACTION_CANCEL);
                mServiceIntent.putExtra(CourseIntallerService.SERVICE_URL, courseSelected.getDownloadUrl());
                DownloadActivity.this.startService(mServiceIntent);

                courseSelected.setDownloading(false);
                courseSelected.setInstalling(false);
                courseSelected.setProgress(0);

                dla.notifyDataSetChanged();
            }

        }
    }

}
