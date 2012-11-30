package org.digitalcampus.mobile.learning.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.adapter.SectionListAdapter;
import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.model.Module;
import org.digitalcampus.mobile.learning.model.Section;
import org.digitalcampus.mobile.learning.utils.ImageUtils;
import org.digitalcampus.mobile.learning.utils.ModuleXMLReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ModuleIndexActivity extends Activity {

	public static final String TAG = "ModuleIndexActivity";
	
	private Module module;
	private ModuleXMLReader mxr;
	private ArrayList<Section> sections;
	private SharedPreferences prefs;
	
	private HashMap<String, String> langMap = new HashMap<String, String>();
	private String[] langArray;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_index);
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        Bundle bundle = this.getIntent().getExtras(); 
        if(bundle != null) {
        	module = (Module) bundle.getSerializable(Module.TAG);
        	
        }
    	mxr = new ModuleXMLReader(module.getLocation()+"/"+ MobileLearning.MODULE_XML);
    }

    @Override
	public void onStart() {
		super.onStart();
		sections = mxr.getSections(module.getModId(),ModuleIndexActivity.this);
		rebuildLangs();
		setTitle(module.getTitle(prefs.getString("prefLanguage", Locale.getDefault().getLanguage())));
    	
		TextView tv = (TextView) this.findViewById(R.id.module_title);
    	tv.setText(module.getTitle(prefs.getString("prefLanguage", Locale.getDefault().getLanguage())));
		
    	// set image
		if(module.getImageFile() != null){
			ImageView iv = (ImageView) this.findViewById(R.id.module_image);
			String path = module.getLocation() + "/" + module.getImageFile();
			Bitmap bm = ImageUtils.LoadBMPsdcard(path, this.getResources(), R.drawable.default_icon_module);
			iv.setImageBitmap(bm);
		}
    	
    	ListView listView = (ListView) findViewById(R.id.section_list);
    	SectionListAdapter sla = new SectionListAdapter(this, module, sections);
    	listView.setAdapter(sla); 
    	
    	/*listView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				Section s = (Section) view.getTag();    
				Intent i = new Intent(ModuleIndexActivity.this, ModuleActivity.class);
				Bundle tb = new Bundle();
				tb.putSerializable(Section.TAG, (Section) s);
				tb.putSerializable(Module.TAG, (Module) module);
				i.putExtras(tb);
         		startActivity(i);
			}
		});*/
	
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_module_index, menu);
        return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	Intent i;
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_module_about:
				i = new Intent(this, ModuleAboutActivity.class);
				Bundle tb = new Bundle();
				tb.putSerializable(Module.TAG, module);
				i.putExtras(tb);
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
    
    private void rebuildLangs() {
		// recreate langMap
		langMap = new HashMap<String, String>();
		Iterator<String> itr = module.getAvailableLangs().iterator();
		while (itr.hasNext()) {
			String lang = itr.next();
			Locale l = new Locale(lang);
			String langDisp = l.getDisplayLanguage(l);
			langMap.put(langDisp, lang);
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
						onStart();
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
