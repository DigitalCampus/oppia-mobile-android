package org.digitalcampus.mtrain.widgets;

import java.util.HashMap;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.model.Module;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;

public class PageWidget extends WidgetFactory{

	private static final String TAG = "PageWidget";
	
	public PageWidget(Context context, Module module, HashMap<String,String> data) {
		super(context, module, data);
		View vv = super.getLayoutInflater().inflate(R.layout.widget_page, null);
		super.getLayout().addView(vv);
		
		// get the location data
		WebView wv = (WebView) ((Activity) context).findViewById(R.id.page_webview);
		String url = "file://"+ module.getLocation() + "/" + data.get("location");
		Log.d(TAG,url);
		wv.loadUrl(url);
	}

}
