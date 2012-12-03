package org.digitalcampus.mobile.learning.activity;

import java.util.ArrayList;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.model.Media;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;

public class DownloadMediaActivity extends Activity {

	public static final String TAG = "DownloadMediaActivity";
	private ArrayList<Object> missingMedia = new ArrayList<Object>();

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download_media);
		Bundle bundle = this.getIntent().getExtras();
		if (bundle != null) {
			missingMedia = (ArrayList<Object>) bundle.getSerializable(DownloadMediaActivity.TAG);
		}
		
		WebView wv = (WebView) findViewById(R.id.download_media_webview);
		String url = "file:///android_asset/www/download_media.html";
		wv.loadUrl(url);
		
		WebView wvml = (WebView) findViewById(R.id.download_media_list_webview);
		String strData = "<ul>";
		for(Object o: missingMedia){
			Media m = (Media) o;
			Log.d(TAG,m.getFilename());
			strData += "<li><a href=''>"+m.getFilename()+"</a></li>";
		}
		strData += "</ul>";
		wvml.loadData(strData,"text/html","utf-8");
		
	}
	
	
}
