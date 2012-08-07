package org.digitalcampus.mtrain.widgets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.model.Module;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.Xml.Encoding;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class PageWidget extends WidgetFactory{

	private static final String TAG = "PageWidget";
	
	public PageWidget(Context context, Module module, org.digitalcampus.mtrain.model.Activity activity) {
		super(context, module, activity);
		View vv = super.getLayoutInflater().inflate(R.layout.widget_page, null);
		super.getLayout().addView(vv);
		
		// get the location data
		WebView wv = (WebView) ((Activity) context).findViewById(R.id.page_webview);
		//TODO error check here that the file really exists first
		
		String url = "file://"+ module.getLocation() + "/" + activity.getActivity().get("location");
		
		// for testing load file and display in log (for checking the character encoding)
		/*String data = "";
		
		File file = new File(module.getLocation() + "/" + activity.getActivity().get("location"));
		 try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
		    while ((line = reader.readLine()) != null){
		    	data += line;
		    	Log.d(TAG,line);
		    }
		    reader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		Log.v(TAG,"Loading: " + url);
		wv.loadUrl(url);
		//wv.loadData(data, "text/html", Encoding.UTF_8.toString());
		
		// set up the page to intercept videos
		wv.setWebViewClient(new WebViewClient() {
		    @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
		      Log.d(TAG, "Panel2Builder... webview URL:" + url);

		      if (url.contains("/video/")) {
		        Log.d(TAG, "Intercepting click on video url");
		        //extract video name from url
		        
		        
		       // check video file exists
		        
		        return true;
		      }
		      else {
		        Log.d(TAG, "Not doing anything with click");
		        return false;
		      }

		    }
		  });
	}

}
