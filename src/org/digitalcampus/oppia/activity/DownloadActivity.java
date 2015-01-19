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
import java.util.Iterator;
import java.util.concurrent.Callable;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.DownloadCourseListAdapter;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.listener.DownloadCourseListClickListener;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.listener.UpdateScheduleListener;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.task.APIRequestTask;
import org.digitalcampus.oppia.task.DownloadCourseTask;
import org.digitalcampus.oppia.task.InstallDownloadedCoursesTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.ScheduleUpdateTask;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ListView;

import com.bugsense.trace.BugSenseHandler;

public class DownloadActivity extends AppActivity implements APIRequestListener, InstallCourseListener, UpdateScheduleListener {
	
	public static final String TAG = DownloadActivity.class.getSimpleName();
	
	private SharedPreferences prefs;
	private ProgressDialog progressDialog;
	private JSONObject json;
	private DownloadCourseListAdapter dla;
	private String url;
	private ArrayList<Course> courses;
	private boolean showUpdatesOnly = false;
    private boolean taskInProgress = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
		Bundle bundle = this.getIntent().getExtras(); 
        if(bundle != null) {
        	Tag t = (Tag) bundle.getSerializable(Tag.TAG);
        	this.url = MobileLearning.SERVER_TAG_PATH + String.valueOf(t.getId()) + File.separator;
        } else {
        	this.url = MobileLearning.SERVER_COURSES_PATH;
        	this.showUpdatesOnly = true;
        }

        courses = new ArrayList<Course>();
        dla = new DownloadCourseListAdapter(this, courses);
        dla.setOnClickListener(new CourseListListener());
        ListView listView = (ListView) findViewById(R.id.tag_list);
        listView.setAdapter(dla);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		// Get Course list
		if(this.json == null){
			this.getCourseList();
		} else {
	        this.refreshCourseList(); 
		}

		if (this.taskInProgress){
            startTaskDialog();
		}
	}

	@Override
	public void onPause(){
		//Kill any open dialogs
		if (progressDialog != null){
            progressDialog.dismiss();
        }
		super.onPause();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	    this.courses = (ArrayList<Course>) savedInstanceState.getSerializable("courses");
        this.taskInProgress = (Boolean) savedInstanceState.getBoolean("inProgress");
	    try {
			this.json = new JSONObject(savedInstanceState.getString("json"));
		} catch (JSONException e) {
			// error in the json so just get the list again
		}	    
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	    savedInstanceState.putString("json", json.toString());
	    savedInstanceState.putSerializable("courses", courses);
	    savedInstanceState.putBoolean("inProgress", taskInProgress);
	}
	
	private void getCourseList() {
		// show progress dialog
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle(R.string.loading);
		progressDialog.setMessage(getString(R.string.loading));
		progressDialog.setCancelable(true);
		progressDialog.show();

		APIRequestTask task = new APIRequestTask(this);
		Payload p = new Payload(url);
		task.setAPIRequestListener(this);
		task.execute(p);
	}

	public void refreshCourseList() {
		// process the response and display on screen in listview
		// Create an array of courses, that will be put to our ListActivity

		DbHelper db = new DbHelper(this);
		try {
			this.courses.clear();
			
			for (int i = 0; i < (json.getJSONArray(MobileLearning.SERVER_COURSES_NAME).length()); i++) {
				JSONObject json_obj = (JSONObject) json.getJSONArray(MobileLearning.SERVER_COURSES_NAME).get(i);
				Course dc = new Course(prefs.getString(PrefsActivity.PREF_STORAGE_LOCATION, ""));
				
				ArrayList<Lang> titles = new ArrayList<Lang>();
				JSONObject jsonTitles = json_obj.getJSONObject("title");
				Iterator<?> keys = jsonTitles.keys();
		        while( keys.hasNext() ){
		            String key = (String) keys.next();
		            Lang l = new Lang(key,jsonTitles.getString(key));
					titles.add(l);
		        }
		        dc.setTitles(titles);
		        
		        ArrayList<Lang> descriptions = new ArrayList<Lang>();
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
				        dc.setDescriptions(descriptions);
		        	} catch (JSONException jsone){
		        		//do nothing
		        	}
		        }
		        
		        dc.setShortname(json_obj.getString("shortname"));
		        dc.setVersionId(json_obj.getDouble("version"));
		        dc.setDownloadUrl(json_obj.getString("url"));
		        try {
		        	dc.setDraft(json_obj.getBoolean("is_draft"));
		        }catch (JSONException je){
		        	dc.setDraft(false);
		        }
		        dc.setInstalled(db.isInstalled(dc.getShortname()));
		        dc.setToUpdate(db.toUpdate(dc.getShortname(), dc.getVersionId()));
				if (json_obj.has("schedule_uri")){
					dc.setScheduleVersionID(json_obj.getDouble("schedule"));
					dc.setScheduleURI(json_obj.getString("schedule_uri"));
					dc.setToUpdateSchedule(db.toUpdateSchedule(dc.getShortname(), dc.getScheduleVersionID()));
				}
				if (!this.showUpdatesOnly || dc.isToUpdate()){
					this.courses.add(dc);
				} 
			}
            dla.notifyDataSetChanged();

		} catch (Exception e) {
			db.close();
			if(!MobileLearning.DEVELOPER_MODE){
				BugSenseHandler.sendException(e);
			} else {
				e.printStackTrace();
			}
			UIUtils.showAlert(this, R.string.loading, R.string.error_processing_response);
		}
		DatabaseManager.getInstance().closeDatabase();
	}
	
	public void apiRequestComplete(Payload response) {
		// close dialog and process results
		progressDialog.dismiss();
	
		if(response.isResult()){
			try {
				json = new JSONObject(response.getResultResponse());
				refreshCourseList();
			} catch (JSONException e) {
				if(!MobileLearning.DEVELOPER_MODE){
					BugSenseHandler.sendException(e);
				} else {
					e.printStackTrace();
				}
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


    private void startTaskDialog(){
        taskInProgress = true;

        if (progressDialog != null){
            progressDialog.dismiss();
        }
        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(R.string.install);
        progressDialog.setMessage(getString(R.string.download_starting));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgress(0);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    /******* InstallCourseListener methods ************/
    @Override
    public void downloadComplete(Payload p) {
        Log.d("download-task", "downloadComplete");
        if (p.isResult()){
            // If it finished correctly, start the task to install the course
            progressDialog.setMessage(getString(R.string.download_complete));
            progressDialog.setIndeterminate(true);

            InstallDownloadedCoursesTask installTask = new InstallDownloadedCoursesTask(this);
            installTask.setInstallerListener(this);
            installTask.execute(p);
            
        } else {
            progressDialog.setTitle(getString(R.string.error_download_failure));
            progressDialog.setMessage(p.getResultResponse());
            progressDialog.setIndeterminate(true);
        }
    }

    @Override
    public void downloadProgressUpdate(DownloadProgress dp) {
        Log.d("download-task", "downloadProgressUpdate " + dp.getProgress());
        progressDialog.setMessage(dp.getMessage());
        progressDialog.setProgress(dp.getProgress());
    }

    @Override
    public void installComplete(Payload p) {
        Log.d("download-task", "installComplete");
        if(p.isResult()){
            SharedPreferences.Editor e = prefs.edit();
            e.putLong(PrefsActivity.PREF_LAST_MEDIA_SCAN, 0);
            e.commit();
            progressDialog.setTitle(getString(R.string.install_complete));
            progressDialog.setMessage(p.getResultResponse());
            progressDialog.setIndeterminate(false);
            progressDialog.setProgress(100);

            refreshCourseList();
            progressDialog.dismiss();

        } else {
            progressDialog.setTitle(getString(R.string.error_install_failure));
            progressDialog.setMessage(p.getResultResponse());
            progressDialog.setIndeterminate(false);
            progressDialog.setProgress(100);
        }

        this.taskInProgress = false;

    }

    @Override
    public void installProgressUpdate(DownloadProgress dp) {
        Log.d("download-task", "installProgressUpdate " + dp.getProgress());
        progressDialog.setMessage(dp.getMessage());
        progressDialog.setProgress(dp.getProgress());
    }

    /******* UpdateScheduleListener methods ************/
    @Override
    public void updateComplete(Payload p) {
        Log.d("download-task", "updateComplete");
        if(p.isResult()){
            progressDialog.setTitle(getString(R.string.update_complete));
            progressDialog.setMessage(p.getResultResponse());
            progressDialog.setIndeterminate(false);
            progressDialog.setProgress(100);

            // new refresh the course list
            refreshCourseList();
            SharedPreferences.Editor e = prefs.edit();
            e.putLong(PrefsActivity.PREF_LAST_MEDIA_SCAN, 0);
            e.commit();
            progressDialog.dismiss();

        } else {
            progressDialog.setTitle(getString(R.string.error_update_failure));
            progressDialog.setMessage(p.getResultResponse());
            progressDialog.setIndeterminate(false);
            progressDialog.setProgress(100);
        }

        this.taskInProgress = false;
    }

    @Override
    public void updateProgressUpdate(DownloadProgress dp) {
        Log.d("download-task", "updateProgressUpdate");
        progressDialog.setMessage(dp.getMessage());
        progressDialog.setProgress(dp.getProgress());
    }

    private class CourseListListener implements DownloadCourseListClickListener {

        @Override
        public void onClick(int position) {
            Course courseSelected = courses.get(position);
            if (!taskInProgress){
                ArrayList<Object> data = new ArrayList<Object>();
                data.add(courseSelected);
                Payload p = new Payload(data);

                if(!courseSelected.isInstalled() || courseSelected.isToUpdate()){

                    startTaskDialog();
                    DownloadCourseTask downloadTask = new DownloadCourseTask(DownloadActivity.this);
                    downloadTask.setInstallerListener(DownloadActivity.this);
                    downloadTask.execute(p);
                }
                else if(courseSelected.isToUpdateSchedule()){

                    startTaskDialog();
                    ScheduleUpdateTask updateTask = new ScheduleUpdateTask(DownloadActivity.this);
                    updateTask.setUpdateListener(DownloadActivity.this);
                    updateTask.execute(p);
                }
            }
        }
    }

}
