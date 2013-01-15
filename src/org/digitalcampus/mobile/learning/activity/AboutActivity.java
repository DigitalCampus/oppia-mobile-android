package org.digitalcampus.mobile.learning.activity;

import java.lang.reflect.Method;

import org.digitalcampus.mobile.learning.R;

import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

public class AboutActivity extends AppActivity {

	public static final String TAG = "AboutActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		this.drawHeader();
		
		WebView wv = (WebView) findViewById(R.id.about_webview);
		wv.setBackgroundColor(0x00000000);
		// hack to get transparent background on webviews
		if (Build.VERSION.SDK_INT >= 11){ // Android v3.0+
			try {
				Method method = View.class.getMethod("setLayerType", int.class, Paint.class);
				method.invoke(wv, 1, new Paint()); // 1 = LAYER_TYPE_SOFTWARE (API11)
			} catch (Exception e) {
				
			}
		}
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
