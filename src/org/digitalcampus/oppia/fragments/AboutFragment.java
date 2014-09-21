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
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.utils.FileUtils;

import com.bugsense.trace.BugSenseHandler;

import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class AboutFragment extends Fragment{

	public static final String TAG = AboutFragment.class.getSimpleName();
	private WebView webView;
	private SharedPreferences prefs;
	
	public static AboutFragment newInstance() {
		AboutFragment myFragment = new AboutFragment();
	    return myFragment;
	}

	public AboutFragment(){
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
		View vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_about, null);
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
		webView = (WebView) super.getActivity().findViewById(R.id.about_webview);
		String lang = prefs.getString("prefLanguage", Locale.getDefault().getLanguage());
		String url = FileUtils.getLocalizedFilePath(super.getActivity(),lang, "about.html");

		int defaultFontSize = Integer.parseInt(prefs.getString("prefTextSize", "16"));
		webView.getSettings().setDefaultFontSize(defaultFontSize);
		
		webView.loadUrl(url);
		
		TextView versionNo = (TextView) super.getActivity().findViewById(R.id.about_versionno);
		try {
			String no = super.getActivity().getPackageManager().getPackageInfo(super.getActivity().getPackageName(), 0).versionName;
			versionNo.setText(getString(R.string.version,no));
		} catch (NameNotFoundException e) {
			if(!MobileLearning.DEVELOPER_MODE){
				BugSenseHandler.sendException(e);
			} else {
				e.printStackTrace();
			}
		}
	}
}
