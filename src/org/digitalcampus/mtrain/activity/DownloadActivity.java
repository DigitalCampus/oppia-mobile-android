package org.digitalcampus.mtrain.activity;

import java.util.ArrayList;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.adapter.DownloadListAdapter;
import org.digitalcampus.mtrain.application.DbHelper;
import org.digitalcampus.mtrain.application.MTrain;
import org.digitalcampus.mtrain.listener.GetModuleListListener;
import org.digitalcampus.mtrain.task.GetModuleListTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bugsense.trace.BugSenseHandler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ListView;

public class DownloadActivity extends Activity implements GetModuleListListener {

	public static final String TAG = "DownloadActivity";

	private ProgressDialog pDialog;
	private SharedPreferences prefs;
	private JSONArray json;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		setContentView(R.layout.activity_download);
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
		String[] url = new String[1];

		url[0] = prefs.getString("prefServer", getString(R.string.prefServerDefault)) + MTrain.SERVER_MODULES_PATH;
		task.setGetModuleListListener(this);
		task.execute(url);
	}

	public void refreshModuleList() {
		// process the response and display on screen in listview
		// Create an array of Strings, that will be put to our ListActivity

		try {

			ArrayList<DownloadModule> modules = new ArrayList<DownloadModule>();
			DbHelper db = new DbHelper(this);
			for (int i = 0; i < (json.length()); i++) {
				JSONObject json_obj = json.getJSONObject(i);
				DownloadModule dm = new DownloadModule();
				dm.title = json_obj.getString("title");
				dm.shortname = json_obj.getString("shortname");
				dm.version = json_obj.getDouble("version");
				dm.downloadUrl = json_obj.getString("url");
				dm.installed = db.isInstalled(dm.shortname);
				dm.toUpdate = db.toUpdate(dm.shortname, dm.version);
				modules.add(dm);
			}
			db.close();

			DownloadListAdapter mla = new DownloadListAdapter(this, modules);
			ListView listView = (ListView) findViewById(R.id.module_list);
			listView.setAdapter(mla);

		} catch (Exception e) {
			e.printStackTrace();
			BugSenseHandler.log(TAG, e);
			MTrain.showAlert(this, R.string.close, R.string.error_processing_response);
		}

	}

	// TODO make this into proper class (with getters/setters etc)
	public class DownloadModule {
		public String title;
		public Double version;
		public String shortname;
		public String downloadUrl;
		public boolean installed = false;
		public boolean toUpdate = false;
	}

	public void moduleListComplete(String response) {
		// close dialog and process results
		pDialog.dismiss();
		try {
			json = new JSONArray(response);
			refreshModuleList();
		} catch (JSONException e) {
			BugSenseHandler.log(TAG, e);
			MTrain.showAlert(this, R.string.close, response);
			e.printStackTrace();
		}

	}

}
