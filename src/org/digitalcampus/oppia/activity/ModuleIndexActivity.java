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

package org.digitalcampus.oppia.activity;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.adapter.SectionListAdapter;
import org.digitalcampus.oppia.exception.InvalidXMLException;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Module;
import org.digitalcampus.oppia.model.ModuleMetaPage;
import org.digitalcampus.oppia.model.Section;
import org.digitalcampus.oppia.utils.ImageUtils;
import org.digitalcampus.oppia.utils.ModuleXMLReader;
import org.digitalcampus.oppia.utils.UIUtils;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ModuleIndexActivity extends AppActivity {

	public static final String TAG = ModuleIndexActivity.class.getSimpleName();
	
	private Module module;
	private ModuleXMLReader mxr;
	private ArrayList<Section> sections;
	private SharedPreferences prefs;
	private Activity baselineActivity;
	private AlertDialog aDialog;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_index);
        
        this.drawHeader();
	    
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
        
        Bundle bundle = this.getIntent().getExtras(); 
        if(bundle != null) {
        	module = (Module) bundle.getSerializable(Module.TAG);
        	try {
				mxr = new ModuleXMLReader(module.getModuleXMLLocation());
			
	        	module.setMetaPages(mxr.getMetaPages());
	        	
	        	this.checkBaseline();
	        	
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
        	} catch (InvalidXMLException e) {
        		UIUtils.showAlert(this, R.string.error, R.string.error_reading_xml, new Callable<Boolean>() {
    				public Boolean call() throws Exception {
    					ModuleIndexActivity.this.finish();
    					return true;
    				}
    			});
			}
        }
    	
    }

    @Override
	public void onStart() {
		super.onStart();
		sections = mxr.getSections(module.getModId(),ModuleIndexActivity.this);
		setTitle(module.getTitle(prefs.getString(getString(R.string.prefs_language), Locale.getDefault().getLanguage())));
    	
		TextView tv = (TextView) getHeader().findViewById(R.id.page_title);
    	tv.setText(module.getTitle(prefs.getString(getString(R.string.prefs_language), Locale.getDefault().getLanguage())));
		
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
   	public void onResume() {
    	super.onResume();
   		if(aDialog == null){
   			this.checkBaseline();
   		} else {
   			aDialog.show();
   		}
   		
    }
    
    @Override
	public void onPause() {
		if(aDialog != null){
			aDialog.dismiss();
		}
		super.onPause();
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.clear();
        getMenuInflater().inflate(R.menu.activity_module_index, menu);
        ArrayList<ModuleMetaPage> ammp = module.getMetaPages();
        int order = 104;
        for(ModuleMetaPage mmp: ammp){
        	String title = mmp.getLang(prefs.getString(getString(R.string.prefs_language), Locale.getDefault().getLanguage())).getContent();
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
		}
	}
    
    private void createLanguageDialog() {
    	UIUtils ui = new UIUtils();
    	ui.createLanguageDialog(this, module.getLangs(), prefs, new Callable<Boolean>() {	
			public Boolean call() throws Exception {
				ModuleIndexActivity.this.onStart();
				return true;
			}
		});
	}
    
    private void checkBaseline(){
    	ArrayList<Activity> baselineActs = mxr.getBaselineActivities(module.getModId());
    	Log.d(TAG,"No baseline activities: " + baselineActs.size());
    	// TODO how to handle if more than one baseline activity
    	for(Activity a: baselineActs){
    		if(!a.getCompleted()){
    			this.baselineActivity = a;
    			Log.d(TAG,"adding dialog");
    			aDialog = new AlertDialog.Builder(this).create();
    			aDialog.setCancelable(false);
    			aDialog.setTitle(R.string.alert_pretest);
    			aDialog.setMessage(this.getString(R.string.alert_pretest_summary));

    			aDialog.setButton(DialogInterface.BUTTON_NEGATIVE, (CharSequence) this.getString(R.string.open), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
    					Intent intent = new Intent(ModuleIndexActivity.this, BaselineActivity.class);
    					Bundle tb = new Bundle();
    					tb.putSerializable(Activity.TAG, ModuleIndexActivity.this.baselineActivity);
    					intent.putExtras(tb);
    	         		startActivity(intent);	
					}
    			});
    			aDialog.setButton(DialogInterface.BUTTON_POSITIVE,(CharSequence) this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
    					ModuleIndexActivity.this.finish();
    				}
    			});
    			aDialog.show();
    		}
    	}
    }
    
}
