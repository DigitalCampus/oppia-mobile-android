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
		String url = "file:///android_asset/www/about.html";
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
