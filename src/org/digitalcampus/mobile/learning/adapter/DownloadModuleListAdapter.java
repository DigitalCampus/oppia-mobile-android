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

package org.digitalcampus.mobile.learning.adapter;

import java.util.ArrayList;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.activity.DownloadActivity;
import org.digitalcampus.mobile.learning.listener.InstallModuleListener;
import org.digitalcampus.mobile.learning.listener.UpdateScheduleListener;
import org.digitalcampus.mobile.learning.model.DownloadProgress;
import org.digitalcampus.mobile.learning.model.Module;
import org.digitalcampus.mobile.learning.task.DownloadModuleTask;
import org.digitalcampus.mobile.learning.task.InstallDownloadedModulesTask;
import org.digitalcampus.mobile.learning.task.Payload;
import org.digitalcampus.mobile.learning.task.ScheduleUpdateTask;
import org.digitalcampus.mobile.learning.utils.UIUtils;

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

public class DownloadModuleListAdapter extends ArrayAdapter<Module> implements InstallModuleListener, UpdateScheduleListener{

	public static final String TAG = DownloadModuleListAdapter.class.getSimpleName();

	private final Context ctx;
	private final ArrayList<Module> moduleList;
	private ProgressDialog myProgress;
	private SharedPreferences prefs;

	public DownloadModuleListAdapter(Activity context, ArrayList<Module> moduleList) {
		super(context, R.layout.module_download_row, moduleList);
		this.ctx = context;
		this.moduleList = moduleList;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.module_download_row, parent, false);
	    Module m = moduleList.get(position);
	    rowView.setTag(m);
	    
	    TextView moduleTitle = (TextView) rowView.findViewById(R.id.module_title);
	    moduleTitle.setText(m.getTitle(prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage())));
	    
	    Button actionBtn = (Button) rowView.findViewById(R.id.action_btn);
	    
	    if(m.isInstalled()){
	    	if(m.isToUpdate()){
	    		actionBtn.setText(R.string.update);
		    	actionBtn.setEnabled(true);
	    	} else if (m.isToUpdateSchedule()){
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
	    if(!m.isInstalled() || m.isToUpdate()){
	    	actionBtn.setTag(m);
	    	actionBtn.setOnClickListener(new View.OnClickListener() {
             	public void onClick(View v) {
             		Module dm = (Module) v.getTag();
             		Log.d(TAG,dm.getDownloadUrl());
             		
             		ArrayList<Object> data = new ArrayList<Object>();
             		data.add(dm);
             		Payload p = new Payload(data);
             		
             		myProgress = new ProgressDialog(ctx);
             		myProgress.setTitle(R.string.install);
             		myProgress.setMessage(ctx.getString(R.string.download_starting));
             		myProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
             		myProgress.setProgress(0);
             		myProgress.setMax(100);
             		myProgress.setCancelable(true);
             		myProgress.show();
                     
             		DownloadModuleTask dmt = new DownloadModuleTask(ctx);
             		dmt.setInstallerListener(DownloadModuleListAdapter.this);
             		dmt.execute(p);
             	}
             });
	    }
	    if(m.isToUpdateSchedule()){
	    	actionBtn.setTag(m);
	    	actionBtn.setOnClickListener(new View.OnClickListener() {
             	public void onClick(View v) {
             		Module dm = (Module) v.getTag();
             		
             		ArrayList<Object> data = new ArrayList<Object>();
             		data.add(dm);
             		Payload p = new Payload(data);
             		
             		myProgress = new ProgressDialog(ctx);
             		myProgress.setTitle(R.string.update);
             		myProgress.setMessage(ctx.getString(R.string.update_starting));
             		myProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
             		myProgress.setProgress(0);
             		myProgress.setMax(100);
             		myProgress.setCancelable(true);
             		myProgress.show();
                     
             		ScheduleUpdateTask sut = new ScheduleUpdateTask(ctx);
             		sut.setUpdateListener(DownloadModuleListAdapter.this);
             		sut.execute(p);
             	}
             });
	    }
	    return rowView;
	}

	public void downloadComplete(Payload p) {
		if (p.isResult()){
			// now set task to install
			myProgress.setMessage(ctx.getString(R.string.download_complete));
			myProgress.setIndeterminate(true);
			InstallDownloadedModulesTask imTask = new InstallDownloadedModulesTask(ctx);
			imTask.setInstallerListener(DownloadModuleListAdapter.this);
			imTask.execute(p);
		} else {
			myProgress.dismiss();
			UIUtils.showAlert(ctx, ctx.getString(R.string.error_download_failure), p.getResultResponse());
		}
	}

	public void installComplete(Payload p) {
		myProgress.dismiss();
		
		if(p.isResult()){
			Editor e = prefs.edit();
			e.putLong(ctx.getString(R.string.prefs_last_media_scan), 0);
			e.commit();
			UIUtils.showAlert(ctx, ctx.getString(R.string.install_complete), p.getResultResponse());
			// new refresh the module list
			DownloadActivity da = (DownloadActivity) ctx;
			da.refreshModuleList();
		} else {
			UIUtils.showAlert(ctx, ctx.getString(R.string.error_install_failure), p.getResultResponse());
		}
		
	}
	
	public void downloadProgressUpdate(DownloadProgress dp) {
		myProgress.setMessage(dp.getMessage());	
		myProgress.setProgress(dp.getProgress());
	}

	public void installProgressUpdate(DownloadProgress dp) {
		myProgress.setMessage(dp.getMessage());
		myProgress.setProgress(dp.getProgress());
	}
	
	public void updateProgressUpdate(DownloadProgress dp) {
		myProgress.setMessage(dp.getMessage());	
		myProgress.setProgress(dp.getProgress());
	}
	
	public void updateComplete(Payload p) {
		myProgress.dismiss();
		
		if(p.isResult()){
			UIUtils.showAlert(ctx, ctx.getString(R.string.update_complete), p.getResultResponse());
			// new refresh the module list
			DownloadActivity da = (DownloadActivity) ctx;
			da.refreshModuleList();
			Editor e = prefs.edit();
			e.putLong(ctx.getString(R.string.prefs_last_media_scan), 0);
			e.commit();
		} else {
			UIUtils.showAlert(ctx, ctx.getString(R.string.error_update_failure), p.getResultResponse());
		}
		
	}
	
	public void closeDialogs(){
		if (myProgress != null){
			myProgress.dismiss();
		}
	}

}
