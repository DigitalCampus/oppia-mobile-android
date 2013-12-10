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
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.utils.FileUtils;

import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.webkit.WebView;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

public class AboutActivity extends AppActivity {

	public static final String TAG = AboutActivity.class.getSimpleName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String url = "file:///android_asset/" + FileUtils.getLocalizedFilePath(this,prefs.getString(getString(R.string.prefs_language), Locale.getDefault().getLanguage()) , "about.html");
		WebView wv = (WebView) findViewById(R.id.about_webview);
		wv.loadUrl(url);
		
		TextView versionNo = (TextView)  findViewById(R.id.about_versionno);
		try {
			String no = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
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
