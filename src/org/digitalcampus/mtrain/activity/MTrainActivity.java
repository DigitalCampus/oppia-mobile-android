package org.digitalcampus.mtrain.activity;

import java.util.ArrayList;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.application.DbHelper;
import org.digitalcampus.mtrain.application.MTrain;
import org.digitalcampus.mtrain.model.Module;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
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

        
        
    }

    protected void onStart(){
    	super.onStart();
    	
    	// install any new modules
        // TODO show info to user that we're checking for new modules
    	MTrain mt = new MTrain(MTrainActivity.this);
    	mt.installNewDownloads();
    	
    	LinearLayout modulesLL = (LinearLayout) findViewById(R.id.modules);
    	DbHelper db = new DbHelper(this);
    	ArrayList<Module> modules = db.getModules();
    	for(Module m: modules){
    		Log.d(TAG,m.getTitle());
    	}
    	db.close();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

}
