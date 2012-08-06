package org.digitalcampus.mtrain.activity;

import java.util.ArrayList;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.application.MTrain;
import org.digitalcampus.mtrain.model.Module;
import org.digitalcampus.mtrain.model.Section;
import org.digitalcampus.mtrain.utils.ModuleXMLReader;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class ModuleIndexActivity extends Activity {

	public static final String TAG = "ModuleIndexActivity";
	
	private Module module;
	private ModuleXMLReader mxr;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module_index);
        
        LinearLayout sectionsLL = (LinearLayout) findViewById(R.id.sections);
        Bundle bundle = this.getIntent().getExtras(); 
        if(bundle != null) {
        	module = (Module) bundle.getSerializable(Module.TAG);
        	Log.d(TAG,module.getLocation()+"/"+ MTrain.MODULE_XML);
        	setTitle(module.getTitle());
        	
        	mxr = new ModuleXMLReader(module.getLocation()+"/"+ MTrain.MODULE_XML);
        	
        	ArrayList<Section> sections = mxr.getSections(module.getModId());
        	for(Section s: sections){
        		Log.d(TAG,s.getTitle());
        		Button b = new Button(this);
        		b.setTag(s);
        		b.setTypeface(Typeface.DEFAULT_BOLD);
            	b.setTextSize(20);
            	b.setText(s.getTitle());
            	b.setOnClickListener(new View.OnClickListener() {
                 	public void onClick(View v) {
                 		Intent i = new Intent(ModuleIndexActivity.this, ModuleActivity.class);
                 		Bundle tb = new Bundle();
                 		tb.putSerializable(Section.TAG, (Section) v.getTag());
        				i.putExtras(tb);
                 		startActivity(i);
                 	}
                 });
            	sectionsLL.addView(b);
        	}
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_module_index, menu);
        return true;
    }

    
}
