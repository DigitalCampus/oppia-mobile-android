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

package org.digitalcampus.oppia.fragments;

import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.util.Locale;

public class AboutFragment extends AppFragment {

	private SharedPreferences prefs;
	
	public static AboutFragment newInstance() {
	    return new AboutFragment();
	}

	public AboutFragment(){
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
		return inflater.inflate(R.layout.fragment_about, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
		WebView webView = super.getActivity().findViewById(R.id.about_webview);
		String lang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
		String url = Storage.getLocalizedFilePath(super.getActivity(), lang, "about.html");

		int defaultFontSize = Integer.parseInt(prefs.getString(PrefsActivity.PREF_TEXT_SIZE, "16"));
		webView.getSettings().setDefaultFontSize(defaultFontSize);
		
		webView.loadUrl(url);
		
		TextView versionNo = super.getActivity().findViewById(R.id.about_versionno);
		try {
			String no = super.getActivity().getPackageManager().getPackageInfo(super.getActivity().getPackageName(), 0).versionName;
			versionNo.setText(getString(R.string.version,no));
		} catch (NameNotFoundException e) {
			Mint.logException(e);
			Log.d(TAG, "Error getting version name: ", e);
		}
	}
}
