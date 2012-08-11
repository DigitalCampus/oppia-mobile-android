package org.digitalcampus.mtrain.adapter;

import java.util.ArrayList;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.activity.DownloadActivity;
import org.digitalcampus.mtrain.activity.DownloadActivity.DownloadModule;
import org.digitalcampus.mtrain.listener.DownloadModuleListener;
import org.digitalcampus.mtrain.listener.InstallModuleListener;
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

public class DownloadListAdapter extends ArrayAdapter<DownloadModule> implements DownloadModuleListener,InstallModuleListener{

	public static final String TAG = "DownloadListAdapter";

	private final Context context;
	private final ArrayList<DownloadModule> moduleList;
	private ProgressDialog myProgress;

	public DownloadListAdapter(Activity context, ArrayList<DownloadModule> moduleList) {
		super(context, R.layout.module_list_row, moduleList);
		this.context = context;
		this.moduleList = moduleList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.module_download_row, parent, false);
	    DownloadModule m = moduleList.get(position);
	    rowView.setTag(m);
	    
	    TextView moduleTitle = (TextView) rowView.findViewById(R.id.module_title);
	    moduleTitle.setText(m.title);
	    
	    TextView moduleVersion = (TextView) rowView.findViewById(R.id.module_version);
	    moduleVersion.setText(String.format("%.0f",m.version));
	    
	    Button actionBtn = (Button) rowView.findViewById(R.id.action_btn);
	    
	    // TODO use proper lang strings
	    if(m.installed){
	    	if(m.toUpdate){
	    		actionBtn.setText("Update");
		    	actionBtn.setEnabled(true);
	    	} else {
	    		actionBtn.setText("Installed");
		    	actionBtn.setEnabled(false);
	    	}
	    } else {
	    	actionBtn.setText("Install");
	    	actionBtn.setEnabled(true);
	    }
	    if(!m.installed || m.toUpdate){
	    	actionBtn.setTag(m);
	    	actionBtn.setOnClickListener(new View.OnClickListener() {
             	public void onClick(View v) {
             		DownloadModule dm = (DownloadModule) v.getTag();
             		Log.d(TAG,dm.downloadUrl);
             		DownloadModule[] s = new DownloadModule[1];
             		s[0] = dm;
             		Payload p = new Payload(0,s);
             		
             		myProgress = new ProgressDialog(context);
                     // TODO change these to be lang strings
             		myProgress.setTitle("Installing");
             		myProgress.setMessage("Starting download");
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
		myProgress.setMessage("Download complete");
		// now set task to install
		InstallModulesTask imTask = new InstallModulesTask(context);
		imTask.setInstallerListener(DownloadListAdapter.this);
		imTask.execute();
	}

	public void installComplete() {
		Log.d(TAG,"install complete");
		myProgress.setMessage("Install complete");
		myProgress.dismiss();
		// new refresh the module list
		DownloadActivity da = (DownloadActivity) context;
		da.refreshModuleList();
	}

	public void downloadProgressUpdate(String msg) {
		// TODO Auto-generated method stub
		myProgress.setMessage(msg);
		
	}
	public void installProgressUpdate(String msg) {
		// TODO Auto-generated method stub
		Log.d(TAG,"hello!");
		myProgress.setMessage(msg);
		
	}
}
