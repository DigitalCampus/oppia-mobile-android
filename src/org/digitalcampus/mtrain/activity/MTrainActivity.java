package org.digitalcampus.mtrain.activity;

import java.util.ArrayList;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.application.DbHelper;
import org.digitalcampus.mtrain.application.MTrain;
import org.digitalcampus.mtrain.model.Module;
import org.digitalcampus.mtrain.task.InstallModules;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MTrainActivity extends Activity {

	static Handler myHandler;
	ProgressDialog myProgress;
	public static final String TAG = "MTrainActivity";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //set up local dirs
        MTrain.createMTrainDirs();
        
        /* grab the anc.zip (from 192.168.1.35) and unpack
        try {
			new DefaultHttpClient().execute(new HttpGet("http://192.168.1.35/mtrain/modules/anc.zip")).getEntity().writeTo(new FileOutputStream(new File(mTrain.DOWNLOAD_PATH,"anc.zip")));
		} catch (ClientProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/

        // install any new modules
        // TODO show info to user that we're checking for new modules
        // TODO? scan already extracted modules and install these
    	InstallModules imTask = new InstallModules(MTrainActivity.this);
    	imTask.execute();
    	
    	LinearLayout modulesLL = (LinearLayout) findViewById(R.id.modules);
    	DbHelper db = new DbHelper(this);
    	
    	// TODO show message if no modules installed yet
    	ArrayList<Module> modules = db.getModules();
    	for(Module m: modules){
    		Button b = new Button(this);
    		b.setTag(m);
    		b.setTypeface(Typeface.DEFAULT_BOLD);
        	b.setTextSize(20);
        	b.setText(m.getTitle());
        	b.setOnClickListener(new View.OnClickListener() {
             	public void onClick(View v) {
             		Intent i = new Intent(MTrainActivity.this, ModuleIndexActivity.class);
             		Bundle tb = new Bundle();
             		tb.putSerializable(Module.TAG, (Module) v.getTag());
    				i.putExtras(tb);
             		startActivity(i);
             	}
             });
        	modulesLL.addView(b);
    	}
    	db.close();
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

}
