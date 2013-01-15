package org.digitalcampus.mobile.learning.activity;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.application.Header;

import android.app.Activity;
import android.util.Log;

public class AppActivity extends Activity {
	
	public static final String TAG = AppActivity.class.getSimpleName();
	
	private Header header;
	
	public void drawHeader() {
		try {
			header = (Header) findViewById(R.id.header);
			header.initHeader();
		} catch (NullPointerException npe) {
			// do nothing
			Log.d(TAG,"header not found");
		}
	}
	
	public Header getHeader(){
		return this.header;
	}

}
