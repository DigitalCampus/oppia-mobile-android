/* 
 * This file is part of OppiaMobile - https://digital-campus.org/
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

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.exception.UserNotFoundException;
import org.digitalcampus.oppia.model.User;
import org.digitalcampus.oppia.utils.ConnectionUtils;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.util.Locale;

public class MonitorActivity extends AppActivity {
	
	public static final String TAG = MonitorActivity.class.getSimpleName();
	private WebView webView = null;
	private SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_monitor);
        ActionBar actionBar = getActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		webView = new WebView(this);
		
		LinearLayout ll = (LinearLayout) findViewById(R.id.monitor_layout);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		webView.setLayoutParams(lp);
		ll.addView(webView);
		if (savedInstanceState != null) {
			webView.restoreState(savedInstanceState);
		} else {
			webView.getSettings().setJavaScriptEnabled(true);
			webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
			webView.setWebViewClient(new MonitorWebViewClient());
			this.loadMonitor();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		webView.saveState(outState);
	} 
	
	private void loadMonitor(){ 
		String url;
		if(ConnectionUtils.isNetworkConnected(this)){
			DbHelper db = new DbHelper(this);
			User u;
			try {
				u = db.getUser(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
				url = prefs.getString(PrefsActivity.PREF_SERVER, getString(R.string.prefServer)) + "mobile/monitor/?";
				url += "username=" + u.getUsername();
				url += "&api_key=" + u.getApiKey();
			} catch (UserNotFoundException e) {
				String lang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
	        	url = Storage.getLocalizedFilePath(MonitorActivity.this, lang, "monitor_not_available.html");
			}
			DatabaseManager.getInstance().closeDatabase();
			
		} else {
			String lang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
        	url = Storage.getLocalizedFilePath(MonitorActivity.this,lang,"monitor_not_available.html");
		}
		webView.loadUrl(url);
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
		int itemId = item.getItemId();
		if (itemId == R.id.menu_return) {
			this.finish();
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	private class MonitorWebViewClient extends WebViewClient{
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return false;
		}
		
		@Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        	String lang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
        	String url = Storage.getLocalizedFilePath(MonitorActivity.this,lang,"monitor_not_available.html");
        	webView.loadUrl(url);
        }
	}

}
