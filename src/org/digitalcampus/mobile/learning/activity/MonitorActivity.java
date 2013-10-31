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
package org.digitalcampus.mobile.learning.activity;


import org.digitalcampus.mobile.learning.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MonitorActivity extends AppActivity {
	
	public static final String TAG = MonitorActivity.class.getSimpleName();
	private WebView wv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitor);
		this.drawHeader();
	}

	@Override
	public void onStart() {
		super.onStart();
		this.loadMonitor();
	}
	
	private void loadMonitor(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String url = prefs.getString(getString(R.string.prefs_server), getString(R.string.prefServer)) + "mobile/monitor/?";
		url += "username=" + prefs.getString(getString(R.string.prefs_username), "");
		url += "&api_key=" + prefs.getString(getString(R.string.prefs_api_key), "");
		wv = (WebView) findViewById(R.id.monitor_webview);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.loadUrl(url);
		
		// set up the page to intercept videos
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return false;
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    // Check if the key event was the Back button and if there's history
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && wv.canGoBack()) {
	        wv.goBack();
	        return true;
	    }
	    // If it wasn't the Back key or there's no web page history, bubble up to the default
	    // system behavior (probably exit the activity)
	    return super.onKeyDown(keyCode, event);
	}

}
