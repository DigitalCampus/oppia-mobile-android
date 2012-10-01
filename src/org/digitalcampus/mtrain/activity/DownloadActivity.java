package org.digitalcampus.mtrain.activity;

import java.util.ArrayList;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.adapter.DownloadListAdapter;
import org.digitalcampus.mtrain.application.DbHelper;
import org.digitalcampus.mtrain.application.MTrain;
import org.digitalcampus.mtrain.listener.GetModuleListListener;
import org.digitalcampus.mtrain.model.Lang;
import org.digitalcampus.mtrain.model.Module;
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
		// Create an array of Modules, that will be put to our ListActivity

		DbHelper db = new DbHelper(this);
		try {
			ArrayList<Module> modules = new ArrayList<Module>();
			
			for (int i = 0; i < (json.length()); i++) {
				JSONObject json_obj = json.getJSONObject(i);
				Module dm = new Module();
				// TODO LANG
				ArrayList<Lang> titles = new ArrayList<Lang>();
				Lang l = new Lang("en",json_obj.getString("title"));
				titles.add(l);
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
			BugSenseHandler.log(TAG, e);
			MTrain.showAlert(this, R.string.loading, R.string.error_processing_response);
		}
		db.close();

	}

	public void moduleListComplete(String response) {
		// close dialog and process results
		pDialog.dismiss();
		try {
			json = new JSONArray(response);
			refreshModuleList();
		} catch (JSONException e) {
			BugSenseHandler.log(TAG, e);
			MTrain.showAlert(this, R.string.loading, response);
			e.printStackTrace();
		}

	}

}
