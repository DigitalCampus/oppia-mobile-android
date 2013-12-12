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
import java.util.Iterator;
import java.util.concurrent.Callable;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.DownloadCourseListAdapter;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.listener.APIRequestListener;
import org.digitalcampus.oppia.model.Lang;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Tag;
import org.digitalcampus.oppia.task.APIRequestTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.bugsense.trace.BugSenseHandler;

public class DownloadActivity extends AppActivity implements APIRequestListener {
	
	public static final String TAG = DownloadActivity.class.getSimpleName();

	private ProgressDialog pDialog;
	private JSONObject json;
	private DownloadCourseListAdapter dla;
	private String url;
	private ArrayList<Course> courses;
	private boolean inProgress;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        
		Bundle bundle = this.getIntent().getExtras(); 
        if(bundle != null) {
        	Tag t = (Tag) bundle.getSerializable(Tag.TAG);
        	url = MobileLearning.SERVER_TAG_PATH + String.valueOf(t.getId()) + "/";
        } else {
        	url = MobileLearning.SERVER_COURSES_PATH;
        }        	
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
		if (dla != null && dla.isInProgress()){
			dla.openDialog();
		}
	}

	@Override
	public void onPause(){
		// kill any open dialogs
		if (pDialog != null){
			pDialog.dismiss();
		}
		if (dla != null){
			dla.closeDialog();
		}
		super.onPause();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	    this.courses = (ArrayList<Course>) savedInstanceState.getSerializable("courses");
	    this.inProgress = savedInstanceState.getBoolean("inprogress");
	    try {
			this.json = new JSONObject(savedInstanceState.getString("json"));
		} catch (JSONException e) {
			// error in the json so just get the list again
		}
	    Log.d(TAG,"restored instance state");
	    
	}

	@Override
	protected void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
	    savedInstanceState.putString("json", json.toString());
	    savedInstanceState.putSerializable("courses", courses);
	    savedInstanceState.putBoolean("inprogress", dla.isInProgress());
	    Log.d(TAG,"saved instance state");
	}
	
	private void getCourseList() {
		// show progress dialog
		pDialog = new ProgressDialog(this);
		pDialog.setTitle(R.string.loading);
		pDialog.setMessage(getString(R.string.loading));
		pDialog.setCancelable(true);
		pDialog.show();

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
			this.courses = new ArrayList<Course>();
			
			for (int i = 0; i < (json.getJSONArray(MobileLearning.SERVER_COURSES_NAME).length()); i++) {
				JSONObject json_obj = (JSONObject) json.getJSONArray(MobileLearning.SERVER_COURSES_NAME).get(i);
				Course dc = new Course();
				
				ArrayList<Lang> titles = new ArrayList<Lang>();
				JSONObject jsonTitles = json_obj.getJSONObject("title");
				Iterator<?> keys = jsonTitles.keys();
		        while( keys.hasNext() ){
		            String key = (String) keys.next();
		            Lang l = new Lang(key,jsonTitles.getString(key));
					titles.add(l);
		        }
		        dc.setTitles(titles);
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
				this.courses.add(dc);
			}
			
			dla = new DownloadCourseListAdapter(this, courses);
			ListView listView = (ListView) findViewById(R.id.tag_list);
			listView.setAdapter(dla);

		} catch (Exception e) {
			db.close();
			if(!MobileLearning.DEVELOPER_MODE){
				BugSenseHandler.sendException(e);
			} else {
				e.printStackTrace();
			}
			UIUtils.showAlert(this, R.string.loading, R.string.error_processing_response);
		}
		db.close();

	}
	
	public void apiRequestComplete(Payload response) {
		// close dialog and process results
		pDialog.dismiss();
	
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

}
