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

package org.digitalcampus.mobile.learning.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.R.id;
import org.digitalcampus.mobile.learning.adapter.ModuleListAdapter;
import org.digitalcampus.mobile.learning.application.DbHelper;
import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.exception.ModuleNotFoundException;
import org.digitalcampus.mobile.learning.listener.InstallModuleListener;
import org.digitalcampus.mobile.learning.listener.ScanMediaListener;
import org.digitalcampus.mobile.learning.model.MessageFeed;
import org.digitalcampus.mobile.learning.model.Module;
import org.digitalcampus.mobile.learning.task.InstallDownloadedModulesTask;
import org.digitalcampus.mobile.learning.task.Payload;
import org.digitalcampus.mobile.learning.task.ScanMediaTask;
import org.digitalcampus.mobile.learning.utils.FileUtils;
import org.digitalcampus.mobile.learning.utils.ModuleXMLReader;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

public class MobileLearningActivity extends AppActivity implements InstallModuleListener,
		OnSharedPreferenceChangeListener, ScanMediaListener {

	public static final String TAG = MobileLearningActivity.class.getSimpleName();
	private SharedPreferences prefs;
	private Module tempMod;
	private ArrayList<String> langSet = new ArrayList<String>();
	private HashMap<String, String> langMap = new HashMap<String, String>();
	private String[] langArray;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		BugSenseHandler.initAndStartSession(this, MobileLearning.BUGSENSE_API_KEY);
		setContentView(R.layout.activity_main);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefs.registerOnSharedPreferenceChangeListener(this);
		PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
		
		this.drawHeader();
		this.drawMessages();
		
		// set preferred lang to the default lang
		if (prefs.getString("prefLanguage", "").equals("")) {
			Editor editor = prefs.edit();
			editor.putString("prefLanguage", Locale.getDefault().getLanguage());
			editor.commit();
		}

		// set up local dirs
		if(!MobileLearning.createDirs()){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setCancelable(false);
			builder.setTitle(R.string.error);
			builder.setMessage(R.string.error_sdcard);
			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					MobileLearningActivity.this.finish();
				}
			});
			builder.show();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		if (!MobileLearning.isLoggedIn(this)) {
			startActivity(new Intent(MobileLearningActivity.this, LoginActivity.class));
			return;
		}
		
		// install any new modules
		// TODO show info to user that we're checking for new modules
		// TODO? scan already extracted modules and install these
		File dir = new File(MobileLearning.DOWNLOAD_PATH);
		String[] children = dir.list();
		if (children != null) {
			InstallDownloadedModulesTask imTask = new InstallDownloadedModulesTask(MobileLearningActivity.this);
			imTask.setInstallerListener(this);
			imTask.execute();
		}
	}

	@Override
	public void onResume(){
		super.onResume();
		DbHelper db = new DbHelper(this);
		MessageFeed mf = db.getMessageFeed();
		db.close();
		this.updateMessages(mf);
		this.updateHeader();
	}
	
	@Override
	public void onPause(){
		this.stopMessages();
		super.onPause();
	}
	
	private void displayModules() {

		DbHelper db = new DbHelper(this);
		ArrayList<Module> modules = db.getModules();
		db.close();
		ArrayList<Module> removeModules = new ArrayList<Module>();
		for (Module m : modules) {
			try {
				ModuleXMLReader mxr = new ModuleXMLReader(m.getLocation() + "/" + MobileLearning.MODULE_XML);
				m.setTitles(mxr.getTitles());
				ArrayList<String> mLangs = mxr.getLangs();
				m.setAvailableLangs(mLangs);
				// add these langs to the global available langs
				langSet.addAll(mLangs);
				m.setProps(mxr.getMeta());
				m.setImageFile(mxr.getModuleImage());
				m.setMedia(mxr.getMedia());
			} catch (ModuleNotFoundException mnfe){
				// remove from database
				mnfe.deleteModule(this, m.getModId());
				removeModules.add(m);
			}
		}
		
		for(Module m: removeModules){
			// remove from current list
			modules.remove(m);
			
		}

		// rebuild langs
		rebuildLangs();

		LinearLayout llLoading = (LinearLayout) this.findViewById(R.id.loading_modules);
		llLoading.setVisibility(View.GONE);
		LinearLayout llNone = (LinearLayout) this.findViewById(R.id.no_modules);
		if (modules.size() > 0) {
			llNone.setVisibility(View.GONE);
		} else {
			llNone.setVisibility(View.VISIBLE);
			Button manageBtn = (Button) this.findViewById(R.id.manage_modules_btn);
			manageBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					startActivity(new Intent(MobileLearningActivity.this, DownloadActivity.class));
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
				Intent i = new Intent(MobileLearningActivity.this, ModuleIndexActivity.class);
				Bundle tb = new Bundle();
				tb.putSerializable(Module.TAG, m);
				i.putExtras(tb);
				startActivity(i);
			}
		});

		// scan media
		this.scanMedia(modules);
	}

	private void scanMedia(ArrayList<Module> modules) {
		ArrayList<Object> media = new ArrayList<Object>();

		for (Module m : modules) {
			media.addAll(m.getMedia());
		}
		ScanMediaTask task = new ScanMediaTask();
		Payload p = new Payload(0, media);
		task.setScanMediaListener(this);
		task.execute(p);

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
		case R.id.menu_logout:
			logout();
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
		// only show if at least one language
		if (i > 0) {
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
	}

	private void logout() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setTitle(R.string.logout);
		builder.setMessage(R.string.logout_confirm);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// wipe activity data
				DbHelper db = new DbHelper(MobileLearningActivity.this);
				db.onLogout();
				db.close();

				// wipe user prefs
				Editor editor = prefs.edit();
				editor.putString("prefsUsername", "");
				editor.putString("prefApiKey", "");
				editor.commit();

				// restart this activity
				MobileLearningActivity.this.onStart();

			}
		});
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				return; // do nothing
			}
		});
		builder.show();
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
				DbHelper db = new DbHelper(MobileLearningActivity.this);
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
				DbHelper db = new DbHelper(MobileLearningActivity.this);
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

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.d(TAG, key + " changed");
		if(key.equalsIgnoreCase("prefServer")){
			Editor editor = sharedPreferences.edit();
			if(!sharedPreferences.getString("prefServer", "").endsWith("/")){
				String newServer = sharedPreferences.getString("prefServer", "")+"/";
				editor.putString("prefServer", newServer);
		    	editor.commit();
			}
		}
	}

	public void downloadComplete() {
		// do nothing

	}

	public void downloadProgressUpdate(String msg) {
		// do nothing

	}

	public void scanStart() {
		TextView tv = (TextView) this.findViewById(id.home_message);
		tv.setText(this.getString(R.string.info_scan_media_start));
	}

	public void scanProgressUpdate(String msg) {
		TextView tv = (TextView) this.findViewById(id.home_message);
		tv.setText(this.getString(R.string.info_scan_media_checking, msg));
	}

	public void scanComplete(Payload response) {
		LinearLayout ll = (LinearLayout) this.findViewById(id.home_messages);
		TextView tv = (TextView) this.findViewById(id.home_message);
		Button btn = (Button) this.findViewById(R.id.message_action_button);
		
		if (response.responseData.size() > 0) {
			ll.setVisibility(View.VISIBLE);
			tv.setText(this.getString(R.string.info_scan_media_missing));
			btn.setText(this.getString(R.string.scan_media_download_button));
			btn.setTag(response.responseData);
			btn.setOnClickListener(new OnClickListener() {

				public void onClick(View view) {
					@SuppressWarnings("unchecked")
					ArrayList<Object> m = (ArrayList<Object>) view.getTag();
					Intent i = new Intent(MobileLearningActivity.this, DownloadMediaActivity.class);
					Bundle tb = new Bundle();
					tb.putSerializable(DownloadMediaActivity.TAG, m);
					i.putExtras(tb);
					startActivity(i);
				}
			});

		} else {
			ll.setVisibility(View.GONE);
			tv.setText("");
			btn.setText("");
			btn.setOnClickListener(null);
			btn.setTag(null);
		}
	}

}
