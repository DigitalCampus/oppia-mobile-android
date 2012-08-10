package org.digitalcampus.mtrain.activity;

import java.util.ArrayList;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.adapter.SectionListAdapter;
import org.digitalcampus.mtrain.application.MTrain;
import org.digitalcampus.mtrain.model.Module;
import org.digitalcampus.mtrain.model.Section;
import org.digitalcampus.mtrain.utils.ModuleXMLReader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ModuleIndexActivity extends Activity {

	public static final String TAG = "ModuleIndexActivity";
	
	protected Module module;
	private ModuleXMLReader mxr;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_index);
        
        Bundle bundle = this.getIntent().getExtras(); 
        if(bundle != null) {
        	module = (Module) bundle.getSerializable(Module.TAG);
        	setTitle(module.getTitle());
        	TextView tv = (TextView) this.findViewById(R.id.module_title);
        	tv.setText(module.getTitle());
        	
        	mxr = new ModuleXMLReader(module.getLocation()+"/"+ MTrain.MODULE_XML);
        	ArrayList<Section> sections = mxr.getSections(module.getModId());
        	
        	ListView listView = (ListView) findViewById(R.id.section_list);
        	SectionListAdapter sla = new SectionListAdapter(this, sections);
        	listView.setAdapter(sla); 
        	
        	listView.setOnItemClickListener(new OnItemClickListener() {

    			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    				view.setBackgroundResource(R.drawable.background_gradient);
    				Section s = (Section) view.getTag();    				
    				Intent i = new Intent(ModuleIndexActivity.this, ModuleActivity.class);
    				Bundle tb = new Bundle();
    				tb.putSerializable(Section.TAG, (Section) s);
    				tb.putSerializable(Module.TAG, (Module) module);
    				i.putExtras(tb);
             		startActivity(i);
    			}
    		});
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_module_index, menu);
        return true;
    }

    
}
