package org.digitalcampus.mobile.learning.activity;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.application.Header;

import android.app.Activity;

public class AppActivity extends Activity {
	
	public static final String TAG = AppActivity.class.getSimpleName();
	
	private Header header;
	
	public void drawHeader() {
		try {
			header = (Header) findViewById(R.id.header);
			header.initHeader(this);
		} catch (NullPointerException npe) {
			// do nothing
		}
	}
	
	public Header getHeader(){
		return this.header;
	}
	
	public void updateHeader(){
		try {
			header.updateHeader(this);
		} catch (Exception npe) {
			// do nothing
		}
	}

}
