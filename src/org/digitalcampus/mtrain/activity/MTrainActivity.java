package org.digitalcampus.mtrain.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.application.mTrain;
import org.digitalcampus.mtrain.utils.ZipUtility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;

public class MTrainActivity extends Activity {

	static Handler myHandler;
	ProgressDialog myProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //set up local dirs
        mTrain.createMTrainDirs();
        
        // grab the anc.zip (from 192.168.1.35) and unpack
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
		}

        ZipUtility.unzipFiles(mTrain.DOWNLOAD_PATH, "anc.zip", mTrain.MODULES_PATH);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    
    
	
}
