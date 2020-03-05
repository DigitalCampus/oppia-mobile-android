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

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.CourseMetaPage;
import org.digitalcampus.oppia.utils.storage.FileUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Locale;

public class CourseMetaPageActivity extends AppActivity {

	private Course course;
	private CourseMetaPage cmp;

	@Override
	public void onStart() {
		super.onStart();
		initialize();
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_metapage);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		String prefLang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            course = (Course) bundle.getSerializable(Course.TAG);
            int pageID = bundle.getInt(CourseMetaPage.TAG);
            cmp = course.getMetaPage(pageID);
        }
		
		TextView titleTV = findViewById(R.id.course_title);
		TextView versionTV = findViewById(R.id.course_versionid);
		TextView shortnameTV = findViewById(R.id.course_shortname);
		String title = cmp.getLang(prefLang).getContent();
		titleTV.setText(title);
		BigDecimal big = new BigDecimal(course.getVersionId());
		versionTV.setText(big.toString());
		shortnameTV.setText(course.getShortname());
		
		WebView wv = this.findViewById(R.id.metapage_webview);
		String url = course.getLocation() + File.separator +cmp.getLang(prefLang).getLocation();
		
		try {
			String content =  "<html><head>";
			content += "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>";
			content += "<link href='file:///android_asset/www/style.css' rel='stylesheet' type='text/css'/>";
			content += "</head>";
			content += FileUtils.readFile(url);
			content += "</html>";
			wv.loadDataWithBaseURL("file://" + course.getLocation() + File.separator, content, "text/html", "utf-8", null);
		} catch (IOException e) {
			Mint.logException(e);
			Log.d(TAG, "IOException: ", e);
			wv.loadUrl("file://" + url);
		}
	    
	}
	
}
