package org.digitalcampus.mobile.learning.activity;

import org.digitalcampus.mobile.learning.R;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class HelpActivity extends Activity {

	public static final String TAG = "HelpActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		
		WebView wv = (WebView) findViewById(R.id.about_webview);
		String url = "file:///android_asset/www/help.html";
		wv.loadUrl(url);
		
	}
}

