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


import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.MobileLearning;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class StartUpActivity extends Activity{

	public final static String TAG = StartUpActivity.class.getSimpleName();
	private static final int splashDisplayFor = 2000;
	private boolean showSplash = true;
	private TextView tvProgress;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.start_up);
        tvProgress = (TextView) this.findViewById(R.id.start_up_progress);
        
        if(showSplash){
        	displaySplashScreen();
        } else {
        	endSplashScreen();
        }
        
        // set up local dirs
 		if(!MobileLearning.createDirs()){
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setCancelable(false);
 			builder.setTitle(R.string.error);
 			builder.setMessage(R.string.error_sdcard);
 			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					StartUpActivity.this.finish();
 				}
 			});
 			builder.show();
 			return;
 		}
	}
	
    private void displaySplashScreen() {        
        // create a thread that counts up to the timeout
        Thread t = new Thread() {
            int count = 0;
            @Override
            public void run() {
                try {
                    super.run();
                    while (count < splashDisplayFor) {
                        sleep(100);
                        count += 100;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endSplashScreen();
                }
            }
        };
        t.start();
    }
	
    private void updateProgress(String text){
    	if(tvProgress != null){
    		tvProgress.setText(text);
    	}
    }
	
	private void endSplashScreen() {
        // launch new activity and close splash screen
        startActivity(new Intent(StartUpActivity.this, OppiaMobileActivity.class));
        finish();
    }
}
