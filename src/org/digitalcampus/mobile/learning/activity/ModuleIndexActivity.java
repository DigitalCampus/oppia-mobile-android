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

import java.util.ArrayList;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.adapter.SectionListAdapter;
import org.digitalcampus.mobile.learning.model.Activity;
import org.digitalcampus.mobile.learning.model.Lang;
import org.digitalcampus.mobile.learning.model.Module;
import org.digitalcampus.mobile.learning.model.ModuleMetaPage;
import org.digitalcampus.mobile.learning.model.Section;
import org.digitalcampus.mobile.learning.utils.ImageUtils;
import org.digitalcampus.mobile.learning.utils.ModuleXMLReader;

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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ModuleIndexActivity extends AppActivity {

	public static final String TAG = ModuleIndexActivity.class.getSimpleName();
	
	private Module module;
	private ModuleXMLReader mxr;
	private ArrayList<Section> sections;
	private SharedPreferences prefs;
	private ArrayList<String> langs = new ArrayList<String>();
	private Lang[] langArray;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_index);
        
        this.drawHeader();
	    
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        Bundle bundle = this.getIntent().getExtras(); 
        if(bundle != null) {
        	module = (Module) bundle.getSerializable(Module.TAG);
        	mxr = new ModuleXMLReader(module.getModuleXMLLocation());
        	module.setMetaPages(mxr.getMetaPages());
        	
        	String digest = (String) bundle.getSerializable("JumpTo");
        	if(digest != null){
        		// code to directly jump to a specific activity
        		sections = mxr.getSections(module.getModId(),ModuleIndexActivity.this);
        		for(Section s: sections){
        			for(int i=0 ; i<s.getActivities().size(); i++){
        				Activity a = s.getActivities().get(i);
        				if(a.getDigest().equals(digest)){
        					Intent intent = new Intent(this, ModuleActivity.class);
        					Bundle tb = new Bundle();
        					tb.putSerializable(Section.TAG, (Section) s);
        					tb.putSerializable(Module.TAG, (Module) module);
        					tb.putSerializable(SectionListAdapter.TAG_PLACEHOLDER, (Integer) i);
        					intent.putExtras(tb);
        	         		startActivity(intent);
        				}
        			}
        		}
        		
        	}
        }
    	
    }

    @Override
	public void onStart() {
		super.onStart();
		sections = mxr.getSections(module.getModId(),ModuleIndexActivity.this);
		rebuildLangs();
		setTitle(module.getTitle(prefs.getString("prefLanguage", Locale.getDefault().getLanguage())));
    	
		TextView tv = (TextView) getHeader().findViewById(R.id.page_title);
    	tv.setText(module.getTitle(prefs.getString("prefLanguage", Locale.getDefault().getLanguage())));
		
    	// set image
		if(module.getImageFile() != null){
			ImageView iv = (ImageView) getHeader().findViewById(R.id.page_icon);
			Bitmap bm = ImageUtils.LoadBMPsdcard(module.getImageFile(), this.getResources(), R.drawable.default_icon_module);
			iv.setImageBitmap(bm);
		}
    	
    	ListView listView = (ListView) findViewById(R.id.section_list);
    	SectionListAdapter sla = new SectionListAdapter(this, module, sections);
    	listView.setAdapter(sla); 
	
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.clear();
        getMenuInflater().inflate(R.menu.activity_module_index, menu);
        ArrayList<ModuleMetaPage> ammp = module.getMetaPages();
        int order = 104;
        for(ModuleMetaPage mmp: ammp){
        	String title = mmp.getLang(prefs.getString("prefLanguage", Locale.getDefault().getLanguage())).getContent();
        	menu.add(0,mmp.getId(),order, title).setIcon(android.R.drawable.ic_menu_info_details);
        	order++;
        }
        return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
    	Intent i;
    	Bundle tb = new Bundle();
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_language:
				createLanguageDialog();
				return true;
			case R.id.menu_help:
				startActivity(new Intent(this, HelpActivity.class));
				return true;
			default:
				i = new Intent(this, ModuleMetaPageActivity.class);
				tb.putSerializable(Module.TAG, module);
				tb.putSerializable(ModuleMetaPage.TAG, item.getItemId());
				i.putExtras(tb);
				startActivity(i);
				return true;
				
				//return super.onOptionsItemSelected(item);
		}
	}
    
    private void rebuildLangs() {
		
    	ArrayList<Lang> mlangs = module.getLangs();
		for(Lang l: mlangs){
			if(!langs.contains(l.getLang())){
				langs.add(l.getLang());
			}
		}
	}
    
    private void createLanguageDialog() {
    	int selected = -1;
		langArray = new Lang[langs.size()];
		int i = 0;
		for (String s: langs) {
			Locale loc = new Locale(s);
			String langDisp = loc.getDisplayLanguage(loc);
			Lang l = new Lang(s,langDisp);
			langArray[i] = l;
			if (s.equals(prefs.getString("prefLanguage", Locale.getDefault().getLanguage()))) {
				selected = i;
			}
			i++;
		}
		
		String[] array = new String[langs.size()];
		for(int j=0;j<langs.size(); j++){
			array[j] = langArray[j].getContent();
		}
		
		
		// only show if at least one language
		if (i > 0) {
			ArrayAdapter<String> arr = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice,array);

			AlertDialog mAlertDialog = new AlertDialog.Builder(this)
					.setSingleChoiceItems(arr, selected, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							String newLang = langArray[whichButton].getLang();
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

    
}
