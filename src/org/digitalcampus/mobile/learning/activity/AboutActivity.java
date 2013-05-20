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

import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

public class AboutActivity extends AppActivity {

	public static final String TAG = AboutActivity.class.getSimpleName();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
		this.drawHeader();
		
		WebView wv = (WebView) findViewById(R.id.about_webview);
		String url = "file:///android_asset/www/en/about.html";
		wv.loadUrl(url);
		
		
		TextView versionNo = (TextView)  findViewById(R.id.about_versionno);
		try {
			String no = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
			versionNo.setText(getString(R.string.version,no));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			BugSenseHandler.sendException(e);
		}
		
	}
}
