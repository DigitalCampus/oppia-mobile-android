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

package org.digitalcampus.oppia.adapter;

import java.util.ArrayList;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.DownloadActivity;
import org.digitalcampus.oppia.listener.InstallCourseListener;
import org.digitalcampus.oppia.listener.UpdateScheduleListener;
import org.digitalcampus.oppia.model.DownloadProgress;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.task.DownloadCourseTask;
import org.digitalcampus.oppia.task.InstallDownloadedCoursesTask;
import org.digitalcampus.oppia.task.Payload;
import org.digitalcampus.oppia.task.ScheduleUpdateTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class DownloadCourseListAdapter extends ArrayAdapter<Course> implements InstallCourseListener, UpdateScheduleListener{

	public static final String TAG = DownloadCourseListAdapter.class.getSimpleName();

	private final Context ctx;
	private final ArrayList<Course> courseList;
	private ProgressDialog downloadDialog;
	private SharedPreferences prefs;
	private boolean inProgress = false;
	
	public DownloadCourseListAdapter(Activity context, ArrayList<Course> courseList) {
		super(context, R.layout.course_download_row, courseList);
		this.ctx = context;
		this.courseList = courseList;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.course_download_row, parent, false);
	    Course c = courseList.get(position);
	    rowView.setTag(c);
	    
	    TextView courseTitle = (TextView) rowView.findViewById(R.id.course_title);
	    courseTitle.setText(c.getTitle(prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage())));
	    
	    TextView courseDraft = (TextView) rowView.findViewById(R.id.course_draft);
	    if (c.isDraft()){
	    	courseDraft.setText(ctx.getString(R.string.course_draft));
	    } else {
	    	courseDraft.setVisibility(View.GONE);
	    }
	    Button actionBtn = (Button) rowView.findViewById(R.id.action_btn);
	    
	    if(c.isInstalled()){
	    	if(c.isToUpdate()){
	    		actionBtn.setText(R.string.update);
		    	actionBtn.setEnabled(true);
	    	} else if (c.isToUpdateSchedule()){
	    		actionBtn.setText(R.string.update_schedule);
		    	actionBtn.setEnabled(true);
	    	} else {
	    		actionBtn.setText(R.string.installed);
		    	actionBtn.setEnabled(false);
	    	}
	    } else {
	    	actionBtn.setText(R.string.install);
	    	actionBtn.setEnabled(true);
	    }
	    if(!c.isInstalled() || c.isToUpdate()){
	    	actionBtn.setTag(c);
	    	actionBtn.setOnClickListener(new View.OnClickListener() {
             	public void onClick(View v) {
             		Course dm = (Course) v.getTag();
             		Log.d(TAG,dm.getDownloadUrl());
             		
             		ArrayList<Object> data = new ArrayList<Object>();
             		data.add(dm);
             		Payload p = new Payload(data);
             		
             		DownloadCourseListAdapter.this.showProgressDialog();
            		DownloadCourseListAdapter.this.inProgress = true;
                     
             		DownloadCourseTask dmt = new DownloadCourseTask(ctx);
             		dmt.setInstallerListener(DownloadCourseListAdapter.this);
             		dmt.execute(p);
             	}
             });
	    }
	    if(c.isToUpdateSchedule()){
	    	actionBtn.setTag(c);
	    	actionBtn.setOnClickListener(new View.OnClickListener() {
             	public void onClick(View v) {
             		Course dm = (Course) v.getTag();
             		
             		ArrayList<Object> data = new ArrayList<Object>();
             		data.add(dm);
             		Payload p = new Payload(data);
	             		
	             	// show progress dialog
            		DownloadCourseListAdapter.this.showProgressDialog();
            		DownloadCourseListAdapter.this.inProgress = true;
                     
             		ScheduleUpdateTask sut = new ScheduleUpdateTask(ctx);
             		sut.setUpdateListener(DownloadCourseListAdapter.this);
             		sut.execute(p);
             	}
             });
	    }
	    return rowView;
	}

	public void downloadComplete(Payload p) {
		if (p.isResult()){
			// now set task to install
			downloadDialog.setMessage(ctx.getString(R.string.download_complete));
			downloadDialog.setIndeterminate(true);
			InstallDownloadedCoursesTask imTask = new InstallDownloadedCoursesTask(ctx);
			imTask.setInstallerListener(DownloadCourseListAdapter.this);
			imTask.execute(p);
		} else {
			downloadDialog.setTitle(ctx.getString(R.string.error_download_failure));
			downloadDialog.setMessage(p.getResultResponse());
			downloadDialog.setIndeterminate(true);
		}
	}

	public void installComplete(Payload p) {
		
		
		if(p.isResult()){
			Editor e = prefs.edit();
			e.putLong(ctx.getString(R.string.prefs_last_media_scan), 0);
			e.commit();
			downloadDialog.setTitle(ctx.getString(R.string.install_complete));	
			downloadDialog.setMessage(p.getResultResponse());
			downloadDialog.setIndeterminate(false);
			downloadDialog.setProgress(100);
			// new refresh the course list
			DownloadActivity da = (DownloadActivity) ctx;
			da.refreshCourseList();
		} else {
			downloadDialog.setTitle(ctx.getString(R.string.error_install_failure));	
			downloadDialog.setMessage(p.getResultResponse());
			downloadDialog.setIndeterminate(false);
			downloadDialog.setProgress(100);
		}
		
		this.inProgress = false;
	}
	
	public void downloadProgressUpdate(DownloadProgress dp) {
		downloadDialog.setMessage(dp.getMessage());	
		downloadDialog.setProgress(dp.getProgress());
	}

	public void installProgressUpdate(DownloadProgress dp) {
		downloadDialog.setMessage(dp.getMessage());
		downloadDialog.setProgress(dp.getProgress());
	}
	
	public void updateProgressUpdate(DownloadProgress dp) {
		downloadDialog.setMessage(dp.getMessage());	
		downloadDialog.setProgress(dp.getProgress());
	}
	
	public void updateComplete(Payload p) {
		
		if(p.isResult()){
			downloadDialog.setTitle(ctx.getString(R.string.update_complete));	
			downloadDialog.setMessage(p.getResultResponse());
			downloadDialog.setIndeterminate(false);
			downloadDialog.setProgress(100);
			// new refresh the course list
			DownloadActivity da = (DownloadActivity) ctx;
			da.refreshCourseList();
			Editor e = prefs.edit();
			e.putLong(ctx.getString(R.string.prefs_last_media_scan), 0);
			e.commit();
		} else {
			downloadDialog.setTitle(ctx.getString(R.string.error_update_failure));	
			downloadDialog.setMessage(p.getResultResponse());
			downloadDialog.setIndeterminate(false);
			downloadDialog.setProgress(100);
		}
		
		this.inProgress = false;
	}
	
	
	public void showProgressDialog(){
		// show progress dialog
		downloadDialog = new ProgressDialog(ctx);
 		downloadDialog.setTitle(R.string.install);
 		downloadDialog.setMessage(ctx.getString(R.string.download_starting));
 		downloadDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 		downloadDialog.setProgress(0);
 		downloadDialog.setMax(100);
 		downloadDialog.setCancelable(true);
 		downloadDialog.show();
	}
	
	public void closeDialog(){
		if (downloadDialog != null){
			downloadDialog.dismiss();
		}
	}
	
	public void openDialog(){
		if (downloadDialog != null && this.inProgress){
			downloadDialog.show();
		}
	}
	
	public boolean isInProgress(){
		return this.inProgress;
	}
	
	public void setInProgress(boolean inProgress){
		this.inProgress = inProgress;
	}

}
