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

package org.digitalcampus.oppia.fragments;

import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.utils.ConnectionUtils;
import org.digitalcampus.oppia.utils.FileUtils;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout.LayoutParams;

public class ScorecardFragment extends Fragment{

	public static final String TAG = ScorecardFragment.class.getSimpleName();
	private WebView webView;
	private SharedPreferences prefs;
	
	public static ScorecardFragment newInstance() {
		ScorecardFragment myFragment = new ScorecardFragment();
	    return myFragment;
	}

	public ScorecardFragment(){
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
		View vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_scorecard, null);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		vv.setLayoutParams(lp);
		return vv;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
		webView = (WebView) super.getActivity().findViewById(R.id.scorecard_fragment_webview);
		webView.setWebViewClient(new ScoreCardWebViewClient());
		webView.getSettings().setJavaScriptEnabled(true);
		String lang = prefs.getString(getString(R.string.prefs_language), Locale.getDefault().getLanguage());
		String url = "";
		url = FileUtils.getLocalizedFilePath(super.getActivity(),lang,"webview_loading.html");
		webView.loadUrl(url);
		if(ConnectionUtils.isNetworkConnected(super.getActivity())){
			url = prefs.getString(getString(R.string.prefs_server), getString(R.string.prefServer)) + "mobile/scorecard/?";
			url += "username=" + prefs.getString("prefUsername", "");
			url += "&api_key=" + prefs.getString("prefApiKey", "");
		} else {
        	url = FileUtils.getLocalizedFilePath(super.getActivity(),lang,"scorecard_not_available.html");
		}
		webView.loadUrl(url);

	}

	private class ScoreCardWebViewClient extends WebViewClient{
		@Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        	String lang = ScorecardFragment.this.prefs.getString(getString(R.string.prefs_language), Locale.getDefault().getLanguage());
        	String url = FileUtils.getLocalizedFilePath(ScorecardFragment.this.getActivity(),lang,"scorecard_not_available.html");
        	webView.loadUrl(url);
        }
	}
}
