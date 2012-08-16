package org.digitalcampus.mtrain.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.adapter.ModuleAboutAdapter;
import org.digitalcampus.mtrain.adapter.ModuleListAdapter;
import org.digitalcampus.mtrain.model.Module;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

public class ModuleAboutActivity extends Activity {

	public static final String TAG = "ModuleAboutActivity";
	private Module module;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_module_about);
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			module = (Module) bundle.getSerializable(Module.TAG);
			setTitle(module.getTitle());
		}
		Log.d(TAG,module.getLocation());
		
		TextView titleTV = (TextView) findViewById(R.id.module_title);
		titleTV.setText(module.getTitle());
		
		TextView versionTV = (TextView) findViewById(R.id.module_versionid);
		versionTV.setText(module.getProps().get("versionid"));
		
		TextView shortnameTV = (TextView) findViewById(R.id.module_shortname);
		shortnameTV.setText(module.getProps().get("shortname"));
		
		//HashMap<String,String> temp = (HashMap<String,String>) module.getProps().clone();
		//temp.remove("title");
		//temp.remove("shortname");
		//temp.remove("versionid");
		
		ArrayList<Prop> list = new ArrayList<Prop>();

		Iterator<Entry<String,String>> it = module.getProps().entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, String> pairs = it.next();
	        Log.d(TAG,pairs.getKey() + " = " + pairs.getValue());
	        if(!pairs.getKey().equals("title") && !pairs.getKey().equals("versionid") && !pairs.getKey().equals("shortname")){
		        Prop p = new Prop();
		        p.key = pairs.getKey();
		        p.value = pairs.getValue();
		        list.add(p);
	        }
	    }
	    
	    ModuleAboutAdapter mla = new ModuleAboutAdapter(this, list);
		ListView listView = (ListView) findViewById(R.id.module_about_list);
		listView.setAdapter(mla);

	}
	
	public class Prop{
		public String key;
		public String value;
	}
}
