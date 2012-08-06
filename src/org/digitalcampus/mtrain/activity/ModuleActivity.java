package org.digitalcampus.mtrain.activity;

import java.util.ArrayList;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.R.layout;
import org.digitalcampus.mtrain.R.menu;
import org.digitalcampus.mtrain.model.Module;
import org.digitalcampus.mtrain.model.Section;
import org.digitalcampus.mtrain.widgets.PageWidget;
import org.digitalcampus.mtrain.widgets.WidgetFactory;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class ModuleActivity extends Activity {

	public static final String TAG = "ModuleActivity";
	private Section section;
	private Module module;
	private int currentActivityNo = 0;
	private WidgetFactory currentActivity;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module);
        Bundle bundle = this.getIntent().getExtras(); 
        if(bundle != null) {
        	section = (Section) bundle.getSerializable(Section.TAG);
        	module = (Module) bundle.getSerializable(Module.TAG);
        	Log.d(TAG,"title:" + module.getTitle());
        	Log.d(TAG,"title:" + section.getTitle());
        	setTitle(section.getTitle());
        	loadActivity();
        }
    }

   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_module, menu);
        return true;
    }
    
    private void loadActivity(){
    	ArrayList<org.digitalcampus.mtrain.model.Activity> acts = section.getActivities();
    	TextView tb = (TextView) this.findViewById(R.id.module_activity_title);
    	
    	tb.setText(acts.get(this.currentActivityNo).getActivity().get("title"));
    	
    	if(acts.get(this.currentActivityNo).getActType().equals("page")){
    		currentActivity = (PageWidget) new PageWidget(ModuleActivity.this, module, acts.get(this.currentActivityNo).getActivity());
    	}
    }

    
}
