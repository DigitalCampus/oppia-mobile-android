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


import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.utils.ConnectionUtils;
import org.digitalcampus.oppia.utils.FileUtils;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MonitorActivity extends AppActivity {
	
	public static final String TAG = MonitorActivity.class.getSimpleName();
	private WebView webView;
	private SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitor);
		this.drawHeader();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	public void onStart() {
		super.onStart();
		this.loadMonitor();
	}
	
	private void loadMonitor(){ 
		webView = (WebView) findViewById(R.id.monitor_webview);
		webView.setWebViewClient(new MonitorWebViewClient());
		webView.getSettings().setJavaScriptEnabled(true);
		
		String url = "";
		if(ConnectionUtils.isNetworkConnected(this)){
			url = prefs.getString(getString(R.string.prefs_server), getString(R.string.prefServer)) + "mobile/monitor/?";
			url += "username=" + prefs.getString(getString(R.string.prefs_username), "");
			url += "&api_key=" + prefs.getString(getString(R.string.prefs_api_key), "");
		} else {
			String lang = prefs.getString(getString(R.string.prefs_language), Locale.getDefault().getLanguage());
        	url = "file:///android_asset/" + FileUtils.getLocalizedFilePath(MonitorActivity.this,lang,"monitor_not_available.html");
		}
		webView.loadUrl(url);
		
		
		// set up the page to intercept videos
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				return false;
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    // Check if the key event was the Back button and if there's history
	    if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
	    	webView.goBack();
	        return true;
	    }
	    // If it wasn't the Back key or there's no web page history, bubble up to the default
	    // system behaviour (probably exit the activity)
	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_monitor, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.menu_return:
				this.finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	private class MonitorWebViewClient extends WebViewClient{
		@Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        	String lang = prefs.getString(getString(R.string.prefs_language), Locale.getDefault().getLanguage());
        	String url = "file:///android_asset/" + FileUtils.getLocalizedFilePath(MonitorActivity.this,lang,"monitor_not_available.html");
        	webView.loadUrl(url);
        }
	}

}
