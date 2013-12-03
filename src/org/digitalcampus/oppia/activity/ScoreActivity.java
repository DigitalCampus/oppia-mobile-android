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
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ScoreActivity extends AppActivity {

	public static final String TAG = ScoreActivity.class.getSimpleName();
	private SharedPreferences prefs;
	private static WebView webView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scorecard);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
		this.getScorecard();
		
	}
	
	private void getScorecard(){
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		webView = (WebView) findViewById(R.id.scorecard_webview);
		webView.setWebViewClient(new ScoreCardWebViewClient());
		webView.getSettings().setJavaScriptEnabled(true);
		String url = "";
		if(ConnectionUtils.isNetworkConnected(this)){
			url = prefs.getString(getString(R.string.prefs_server), getString(R.string.prefServer)) + "mobile/scorecard/?";
			url += "username=" + prefs.getString(getString(R.string.prefs_username), "");
			url += "&api_key=" + prefs.getString(getString(R.string.prefs_api_key), "");
		} else {
			String lang = prefs.getString(getString(R.string.prefs_language), Locale.getDefault().getLanguage());
        	url = "file:///android_asset/" + FileUtils.getLocalizedFilePath(ScoreActivity.this,lang,"scorecard_not_available.html");
		}
		webView.loadUrl(url);
	}
	
	private class ScoreCardWebViewClient extends WebViewClient{
		@Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        	String lang = prefs.getString(getString(R.string.prefs_language), Locale.getDefault().getLanguage());
        	String url = "file:///android_asset/" + FileUtils.getLocalizedFilePath(ScoreActivity.this,lang,"scorecard_not_available.html");
        	webView.loadUrl(url);
        }
	}
	
}
