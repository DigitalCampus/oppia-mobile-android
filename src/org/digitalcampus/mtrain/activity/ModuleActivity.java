package org.digitalcampus.mtrain.activity;

import java.util.ArrayList;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.R.layout;
import org.digitalcampus.mtrain.R.menu;
import org.digitalcampus.mtrain.model.Module;
import org.digitalcampus.mtrain.model.Section;
import org.digitalcampus.mtrain.widgets.PageWidget;
import org.digitalcampus.mtrain.widgets.QuizWidget;
import org.digitalcampus.mtrain.widgets.WidgetFactory;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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
    	//TODO log that activity has been visited
    	ArrayList<org.digitalcampus.mtrain.model.Activity> acts = section.getActivities();
    	TextView tb = (TextView) this.findViewById(R.id.module_activity_title);
    	
    	tb.setText(acts.get(this.currentActivityNo).getActivity().get("title"));
    	
    	if(acts.get(this.currentActivityNo).getActType().equals("page")){
    		currentActivity = (PageWidget) new PageWidget(ModuleActivity.this, module, acts.get(this.currentActivityNo).getActivity());
    	}
    	if(acts.get(this.currentActivityNo).getActType().equals("quiz")){
    		currentActivity = (QuizWidget) new QuizWidget(ModuleActivity.this, module, acts.get(this.currentActivityNo).getActivity());
    	}
    	this.setUpNav();
    }
    
    private void setUpNav(){
    	Button prevB = (Button) ModuleActivity.this.findViewById(R.id.prev_btn);
    	Button nextB = (Button) ModuleActivity.this.findViewById(R.id.next_btn);
    	if(this.hasPrev()){
    		prevB.setEnabled(true);
    		prevB.setOnClickListener(new View.OnClickListener() {
             	public void onClick(View v) {
             		currentActivityNo--;
             		loadActivity();
             	}
             });
    	} else {
    		prevB.setEnabled(false);
    	}
    	
    	if(this.hasNext()){
    		nextB.setEnabled(true);
    		nextB.setOnClickListener(new View.OnClickListener() {
             	public void onClick(View v) {
             		currentActivityNo++;
             		loadActivity();
             	}
             });
    	} else {
    		nextB.setEnabled(false);
    	}
    }
    
    private boolean hasPrev(){
    	if(this.currentActivityNo == 0){
    		return false;
    	}
    	return true;
    }
    private boolean hasNext(){
    	int noActs = section.getActivities().size();
    	if(this.currentActivityNo + 1 == noActs){
    		return false;
    	} else {
    		return true;
    	}
    }
    
}
