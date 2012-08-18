package org.digitalcampus.mtrain.adapter;

import java.util.ArrayList;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.activity.DownloadActivity;
import org.digitalcampus.mtrain.listener.DownloadModuleListener;
import org.digitalcampus.mtrain.listener.InstallModuleListener;
import org.digitalcampus.mtrain.model.Module;
import org.digitalcampus.mtrain.task.DownloadModuleTask;
import org.digitalcampus.mtrain.task.InstallModulesTask;
import org.digitalcampus.mtrain.task.Payload;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class DownloadListAdapter extends ArrayAdapter<Module> implements DownloadModuleListener,InstallModuleListener{

	public static final String TAG = "DownloadListAdapter";

	private final Context context;
	private final ArrayList<Module> moduleList;
	private ProgressDialog myProgress;

	public DownloadListAdapter(Activity context, ArrayList<Module> moduleList) {
		super(context, R.layout.module_list_row, moduleList);
		this.context = context;
		this.moduleList = moduleList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.module_download_row, parent, false);
	    Module m = moduleList.get(position);
	    rowView.setTag(m);
	    
	    TextView moduleTitle = (TextView) rowView.findViewById(R.id.module_title);
	    moduleTitle.setText(m.getTitle());
	    
	    TextView moduleVersion = (TextView) rowView.findViewById(R.id.module_version);
	    moduleVersion.setText(String.format("%.0f",m.getVersionId()));
	    
	    Button actionBtn = (Button) rowView.findViewById(R.id.action_btn);
	    
	    if(m.isInstalled()){
	    	if(m.isToUpdate()){
	    		actionBtn.setText(R.string.update);
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
             		Module[] s = new Module[1];
             		s[0] = dm;
             		Payload p = new Payload(0,s);
             		
             		myProgress = new ProgressDialog(context);
             		myProgress.setTitle(R.string.install);
             		myProgress.setMessage(context.getString(R.string.download_starting));
             		myProgress.setCancelable(true);
             		myProgress.show();
                     
             		DownloadModuleTask dmt = new DownloadModuleTask();
             		dmt.setInstallerListener(DownloadListAdapter.this);
             		dmt.execute(p);
             	}
             });
	    }
	    return rowView;
	}

	public void downloadComplete() {
		myProgress.setMessage(context.getString(R.string.download_complete));
		// now set task to install
		InstallModulesTask imTask = new InstallModulesTask(context);
		imTask.setInstallerListener(DownloadListAdapter.this);
		imTask.execute();
	}

	public void installComplete() {
		Log.d(TAG,"install complete");
		myProgress.setMessage(context.getString(R.string.install_complete));
		myProgress.dismiss();
		// new refresh the module list
		DownloadActivity da = (DownloadActivity) context;
		da.refreshModuleList();
	}

	public void downloadProgressUpdate(String msg) {
		myProgress.setMessage(msg);
	}
	
	public void installProgressUpdate(String msg) {
		myProgress.setMessage(msg);
	}
}
