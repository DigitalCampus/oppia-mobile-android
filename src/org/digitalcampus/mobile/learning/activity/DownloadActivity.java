package org.digitalcampus.mobile.learning.activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.adapter.DownloadListAdapter;
import org.digitalcampus.mobile.learning.application.DbHelper;
import org.digitalcampus.mobile.learning.listener.GetModuleListListener;
import org.digitalcampus.mobile.learning.model.Lang;
import org.digitalcampus.mobile.learning.model.Module;
import org.digitalcampus.mobile.learning.task.GetModuleListTask;
import org.digitalcampus.mobile.learning.task.Payload;
import org.digitalcampus.mobile.learning.utils.UIUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

public class DownloadActivity extends AppActivity implements GetModuleListListener {

	public static final String TAG = DownloadActivity.class.getSimpleName();

	private ProgressDialog pDialog;
	private JSONObject json;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download);
		this.drawHeader();
		
		TextView tv = (TextView) getHeader().findViewById(R.id.page_title);
		tv.setText(R.string.title_download_activity);
		// Get Module list
		getModuleList();
	}

	private void getModuleList() {
		// show progress dialog
		pDialog = new ProgressDialog(this);
		pDialog.setTitle(R.string.loading);
		pDialog.setMessage(getString(R.string.loading_module_list));
		pDialog.setCancelable(true);
		pDialog.show();

		GetModuleListTask task = new GetModuleListTask(this);
		Payload p = new Payload(0,null);
		task.setGetModuleListListener(this);
		task.execute(p);
	}

	public void refreshModuleList() {
		// process the response and display on screen in listview
		// Create an array of Modules, that will be put to our ListActivity

		DbHelper db = new DbHelper(this);
		try {
			ArrayList<Module> modules = new ArrayList<Module>();
			
			for (int i = 0; i < (json.getJSONArray("modules").length()); i++) {
				JSONObject json_obj = (JSONObject) json.getJSONArray("modules").get(i);
				Module dm = new Module();
				
				ArrayList<Lang> titles = new ArrayList<Lang>();
				JSONObject jsonTitles = json_obj.getJSONObject("title");
				Iterator<?> keys = jsonTitles.keys();
		        while( keys.hasNext() ){
		            String key = (String) keys.next();
		            Lang l = new Lang(key,jsonTitles.getString(key));
					titles.add(l);
		        }
				dm.setTitles(titles);
				dm.setShortname(json_obj.getString("shortname"));
				dm.setVersionId(json_obj.getDouble("version"));
				dm.setDownloadUrl(json_obj.getString("url"));
				dm.setInstalled(db.isInstalled(dm.getShortname()));
				dm.setToUpdate(db.toUpdate(dm.getShortname(), dm.getVersionId()));
				modules.add(dm);
			}
			
			DownloadListAdapter mla = new DownloadListAdapter(this, modules);
			ListView listView = (ListView) findViewById(R.id.module_list);
			listView.setAdapter(mla);

		} catch (Exception e) {
			e.printStackTrace();
			BugSenseHandler.sendException(e);
			UIUtils.showAlert(this, R.string.loading, R.string.error_processing_response);
		}
		db.close();

	}

	public void moduleListComplete(Payload response) {
		// close dialog and process results
		pDialog.dismiss();
		if(response.result){
			try {
				json = new JSONObject(response.resultResponse);
				refreshModuleList();
			} catch (JSONException e) {
				BugSenseHandler.sendException(e);
				UIUtils.showAlert(this, R.string.loading, R.string.error_connection);
				e.printStackTrace();
			}
		} else {
			UIUtils.showAlert(this, R.string.loading, response.resultResponse, new Callable<Boolean>() {
				
				public Boolean call() throws Exception {
					DownloadActivity.this.finish();
					return true;
				}
			});
		}

	}

}
