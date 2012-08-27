package org.digitalcampus.mtrain.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.adapter.ModuleListAdapter;
import org.digitalcampus.mtrain.application.DbHelper;
import org.digitalcampus.mtrain.application.MTrain;
import org.digitalcampus.mtrain.listener.InstallModuleListener;
import org.digitalcampus.mtrain.model.Module;
import org.digitalcampus.mtrain.service.TrackerService;
import org.digitalcampus.mtrain.task.InstallModulesTask;
import org.digitalcampus.mtrain.utils.FileUtils;
import org.digitalcampus.mtrain.utils.ModuleXMLReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.bugsense.trace.BugSenseHandler;

public class MTrainActivity extends Activity implements InstallModuleListener, OnSharedPreferenceChangeListener {

	public static final String TAG = "MTrainActivity";
	private SharedPreferences prefs;
	private Module tempMod;
	private TreeSet<String> langSet = new TreeSet<String>();
	private HashMap<String, String> langMap = new HashMap<String, String>();
	private String[] langArray;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		BugSenseHandler.setup(this, MTrain.BUGSENSE_API_KEY);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
		
		// set preferred lang to the deafult lang
		if (prefs.getString("prefLanguage", "").equals("")) {
			Editor editor = prefs.edit();
			editor.putString("prefLanguage", Locale.getDefault().getLanguage());
			editor.commit();
		}

		// set up local dirs
		MTrain.createMTrainDirs();

		// install any new modules
		// TODO show info to user that we're checking for new modules
		// TODO? scan already extracted modules and install these
		File dir = new File(MTrain.DOWNLOAD_PATH);
		String[] children = dir.list();
		if (children != null) {
			InstallModulesTask imTask = new InstallModulesTask(MTrainActivity.this);
			imTask.setInstallerListener(this);
			imTask.execute();
		}
		doBindService();
	}

	@Override
	public void onStart() {
		super.onStart();
		if (!isLoggedIn()) {
			startActivity(new Intent(MTrainActivity.this, LoginActivity.class));
			return;
		}
		displayModules();
	}

	public void onDestroy() {
		unbindService(mConnection);
		super.onDestroy();
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			((TrackerService.MyBinder) binder).getService();
		}

		public void onServiceDisconnected(ComponentName className) {
			// s = null;
		}
	};

	void doBindService() {
		Intent i = new Intent(this, TrackerService.class);
		bindService(i, mConnection, Context.BIND_AUTO_CREATE);
	}

	private void displayModules() {

		DbHelper db = new DbHelper(this);
		ArrayList<Module> modules = db.getModules();
		db.close();
		for (Module m : modules) {
			ModuleXMLReader mxr = new ModuleXMLReader(m.getLocation() + "/" + MTrain.MODULE_XML);
			m.setTitles(mxr.getTitles());
			TreeSet<String> mLangs = mxr.getLangs();
			m.setAvailableLangs(mLangs);
			// add these langs to the global available langs
			langSet.addAll(mLangs);
			m.setProps(mxr.getMeta());
		}

		// rebuild Langs
		rebuildLangs();

		LinearLayout ll = (LinearLayout) this.findViewById(R.id.no_modules);
		if (modules.size() > 0) {
			ll.setVisibility(View.GONE);
		} else {
			ll.setVisibility(View.VISIBLE);
			Button manageBtn = (Button) this.findViewById(R.id.manage_modules_btn);
			manageBtn.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					startActivity(new Intent(MTrainActivity.this, DownloadActivity.class));
				}
			});

		}

		ModuleListAdapter mla = new ModuleListAdapter(this, modules);
		ListView listView = (ListView) findViewById(R.id.module_list);
		listView.setAdapter(mla);
		registerForContextMenu(listView);

		listView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Module m = (Module) view.getTag();
				Intent i = new Intent(MTrainActivity.this, ModuleIndexActivity.class);
				Bundle tb = new Bundle();
				tb.putSerializable(Module.TAG, m);
				i.putExtras(tb);
				startActivity(i);
			}
		});
	}

	private void rebuildLangs() {
		// recreate langMap
		langMap = new HashMap<String, String>();
		Iterator<String> itr = langSet.iterator();
		while (itr.hasNext()) {
			String lang = itr.next();
			Locale l = new Locale(lang);
			String langDisp = l.getDisplayLanguage(l);
			langMap.put(langDisp, lang);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_about:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		case R.id.menu_download:
			startActivity(new Intent(this, DownloadActivity.class));
			return true;
		case R.id.menu_settings:
			Intent i = new Intent(this, PrefsActivity.class);
			startActivity(i);
			return true;
		case R.id.menu_language:
			createLanguageDialog();
			return true;
		case R.id.menu_help:
			startActivity(new Intent(this, HelpActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void createLanguageDialog() {
		int selected = -1;
		// TODO this is all quite untidy - fix it up!

		langArray = new String[langMap.size()];
		int i = 0;
		for (Map.Entry<String, String> entry : langMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			langArray[i] = key;
			if (value.equals(prefs.getString("prefLanguage", Locale.getDefault().getLanguage()))) {
				selected = i;
			}
			i++;
		}

		AlertDialog mAlertDialog = new AlertDialog.Builder(this)
				.setSingleChoiceItems(langArray, selected, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String newLang = langMap.get(langArray[whichButton]);
						Editor editor = prefs.edit();
						editor.putString("prefLanguage", newLang);
						editor.commit();
						dialog.dismiss();
						displayModules();
					}
				}).setTitle(getString(R.string.change_language))
				.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// do nothing
					}

				}).create();
		mAlertDialog.show();
	}

	public void installComplete() {
		Log.d(TAG, "Listener says install complete");
		displayModules();

	}

	public void installProgressUpdate(String msg) {
		Log.d(TAG, "Listener sent message:" + msg);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.module_context_menu, menu);
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		tempMod = (Module) info.targetView.getTag();
		switch (item.getItemId()) {
		case R.id.module_context_delete:
			confirmModuleDelete();
			return true;
		case R.id.module_context_reset:
			confirmModuleReset();
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private void confirmModuleDelete() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setTitle(R.string.module_context_delete);
		builder.setMessage(R.string.module_context_delete_confirm);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// continue with delete
				Log.d(TAG,
						"deleting:"
								+ tempMod.getTitle(prefs.getString("prefLanguage", Locale.getDefault().getLanguage())));
				// remove db records
				DbHelper db = new DbHelper(MTrainActivity.this);
				db.deleteModule(tempMod.getModId());
				db.close();
				// remove files
				Log.d(TAG, "deleting:" + tempMod.getLocation());
				File f = new File(tempMod.getLocation());
				FileUtils.deleteDir(f);
				displayModules();
			}
		});
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				tempMod = null;
			}
		});
		builder.show();
	}

	private void confirmModuleReset() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setTitle(R.string.module_context_reset);
		builder.setMessage(R.string.module_context_reset_confirm);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG,
						"resetting:"
								+ tempMod.getTitle(prefs.getString("prefLanguage", Locale.getDefault().getLanguage())));
				DbHelper db = new DbHelper(MTrainActivity.this);
				db.resetModule(tempMod.getModId());
				db.close();
				displayModules();
			}
		});
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				tempMod = null;
			}
		});
		builder.show();
	}

	public void manageBtnClick(View view) {
		startActivity(new Intent(this, DownloadActivity.class));
	}

	public boolean isLoggedIn() {
		String username = prefs.getString("prefUsername", "");
		String password = prefs.getString("prefPassword", "");
		if (username.equals("") || password.equals("")) {
			return false;
		} else {
			return true;
		}
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.d(TAG, key + " changed");
	}

	public void downloadComplete() {
		// do nothing
		
	}

	public void downloadProgressUpdate(String msg) {
		// do nothing
		
	}

}
