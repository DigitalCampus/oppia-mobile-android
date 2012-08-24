package org.digitalcampus.mtrain.widgets;

import java.util.Locale;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.application.MTrain;
import org.digitalcampus.mtrain.application.Tracker;
import org.digitalcampus.mtrain.model.Module;
import org.digitalcampus.mtrain.utils.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.bugsense.trace.BugSenseHandler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class PageWidget extends WidgetFactory {

	private static final String TAG = "PageWidget";
	private Context ctx;
	private Module module;
	private org.digitalcampus.mtrain.model.Activity activity;
	private long startTimestamp = System.currentTimeMillis()/1000;
	private SharedPreferences prefs;

	public PageWidget(Context context, Module module, org.digitalcampus.mtrain.model.Activity activity) {
		super(context, module, activity);
		this.ctx = context;
		this.module = module;
		this.activity = activity;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		View vv = super.getLayoutInflater().inflate(R.layout.widget_page, null);
		super.getLayout().addView(vv);

		// get the location data
		WebView wv = (WebView) ((Activity) context).findViewById(R.id.page_webview);
		// TODO error check here that the file really exists first
		// TODO error check that location is in the hashmap
		String url = "file://" + module.getLocation() + "/" + activity.getLocation(prefs.getString("prefLanguage", Locale.getDefault().getLanguage()));

		Log.v(TAG, "Loading: " + url);
		wv.loadUrl(url);
		
		// set up the page to intercept videos
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				if (url.contains("/video/")) {
					Log.d(TAG, "Intercepting click on video url: " + url);
					// extract video name from url
					int startPos = url.indexOf("/video/") + 7;
					String videoFileName = url.substring(startPos, url.length());
					Log.d(TAG, videoFileName);

					// check video file exists
					boolean exists = FileUtils.mediaFileExists(videoFileName);
					if (exists) {
						// launch intent to play video
						Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
						Uri data = Uri.parse(MTrain.MEDIA_PATH + videoFileName);
						// TODO check that the file really is video/mp4 and not another video type
						intent.setDataAndType(data, "video/mp4");
						// track that the video has been played (or at least clicked on)
						Tracker t = new Tracker(ctx);
						t.mediaPlayed(PageWidget.this.module.getModId(), PageWidget.this.activity.getDigest(), videoFileName);
						ctx.startActivity(intent);
					} else {
						// TODO lang string
						Toast.makeText(ctx, "Media file: '" + videoFileName + "' not found.", Toast.LENGTH_LONG).show();
					}
					return true;
				} else {
					Log.d(TAG, "Not doing anything with click");
					// return false so should not override url loading
					return false;
				}

			}
		});
	}

	public boolean isComplete(){
		long endTimestamp = System.currentTimeMillis()/1000;
		long diff = endTimestamp - startTimestamp;
		if(diff >= MTrain.PAGE_READ_TIME){
			return true;
		} else {
			return false;
		}
	}
	
	public long getTimeTaken(){
		long endTimestamp = System.currentTimeMillis()/1000;
		long diff = endTimestamp - startTimestamp;
		if(diff >= MTrain.PAGE_READ_TIME){
			return diff;
		} else {
			return 0;
		}
	}
	
	public JSONObject getActivityCompleteData(){
		JSONObject obj = new JSONObject();
		try {
			obj.put("activity", "completed");
			obj.put("timetaken", this.getTimeTaken());
			String lang = prefs.getString("prefLanguage", Locale.getDefault().getLanguage());
			obj.put("lang", lang);
		} catch (JSONException e) {
			e.printStackTrace();
			BugSenseHandler.log(TAG, e);
		}
		
		return obj;
	}
}
