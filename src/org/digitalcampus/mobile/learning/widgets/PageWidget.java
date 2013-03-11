package org.digitalcampus.mobile.learning.widgets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.application.DbHelper;
import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.application.Tracker;
import org.digitalcampus.mobile.learning.model.Media;
import org.digitalcampus.mobile.learning.model.Module;
import org.digitalcampus.mobile.learning.utils.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;

public class PageWidget extends WidgetFactory {

	private static final String TAG = PageWidget.class.getSimpleName();
	private Context ctx;
	private Module module;
	private org.digitalcampus.mobile.learning.model.Activity activity;
	private long startTimestamp = System.currentTimeMillis()/1000;
	private long mediaStartTimeStamp;
	private boolean mediaPlaying = false;
	private String mediaFileName;
	private SharedPreferences prefs;
	private WebView wv;
	private BufferedReader br;
	
	public PageWidget(Context context, Module module, org.digitalcampus.mobile.learning.model.Activity activity) {
		super(context, module, activity);
		this.ctx = context;
		this.module = module;
		this.activity = activity;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		View vv = super.getLayoutInflater().inflate(R.layout.widget_page, null);
		super.getLayout().addView(vv);
	
		// get the location data
		wv = (WebView) ((Activity) context).findViewById(R.id.page_webview);
		wv.setBackgroundColor(0x00000000);
		// hack to get transparent background on webviews
		if (Build.VERSION.SDK_INT >= 11){ // Android v3.0+
			try {
				Method method = View.class.getMethod("setLayerType", int.class, Paint.class);
				method.invoke(wv, 1, new Paint()); // 1 = LAYER_TYPE_SOFTWARE (API11)
			} catch (Exception e) {
				
			}
		}
		// TODO error check here that the file really exists first
		// TODO error check that location is in the hashmap
		String url = module.getLocation() + "/" + activity.getLocation(prefs.getString("prefLanguage", Locale.getDefault().getLanguage()));
		try {
			String content =  "<html><head>";
			content += "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>";
			content += "<link href='file:///android_asset/www/style.css' rel='stylesheet' type='text/css'/>";
			content += "</head>";
			content += FileUtils.readFile(url);
			content += "</html>";
			wv.loadDataWithBaseURL("file://" + module.getLocation() + "/", content, "text/html", "utf-8", null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			wv.loadUrl("file://" + url);
		}
		
		
		// set up the page to intercept videos
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				if (url.contains("/video/")) {
					Log.d(TAG, "Intercepting click on video url: " + url);
					// extract video name from url
					int startPos = url.indexOf("/video/") + 7;
					mediaFileName = url.substring(startPos, url.length());

					// check video file exists
					boolean exists = FileUtils.mediaFileExists(mediaFileName);
					if (exists) {
						// launch intent to play video
						Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
						Uri data = Uri.parse(MobileLearning.MEDIA_PATH + mediaFileName);
						
						// TODO check that the file really is video/mp4 and not another video type
						
						intent.setDataAndType(data, "video/mp4");
						
						mediaPlaying = true;
						mediaStartTimeStamp = System.currentTimeMillis()/1000;
	
						ctx.startActivity(intent);
					} else {
						Toast.makeText(ctx, ctx.getString(R.string.error_media_not_found,mediaFileName), Toast.LENGTH_LONG).show();
					}
					return true;
				} else {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					Uri data = Uri.parse(url);
					intent.setData(data);
					ctx.startActivity(intent);
					// launch action in mobile browser - not the webview
					// return true so doesn't follow link within webview
					return true;
				}

			}
		});
	}
	
	public boolean isComplete(){
		// only show as being complete if all the videos on this page have been played
		if(this.activity.hasMedia()){
			Log.d(TAG,"This page has media....");
			ArrayList<Media> mediaList = this.activity.getMedia();
			boolean completed = true;
			DbHelper db = new DbHelper(this.ctx);
			Log.d(TAG,"Searching media....");
			for (Media m: mediaList){
				Log.d(TAG,"Checking...."+m.getFilename());
				if(!db.digestInLog(this.module.getModId(), m.getDigest())){
					Log.d(TAG,"digest not in log");
					Log.d(TAG,String.valueOf(this.module.getModId()));
					Log.d(TAG,m.getDigest());
					completed = false;
				}
			}
			db.close();
			if(!completed){
				return false;
			}
		}
		
		long endTimestamp = System.currentTimeMillis()/1000;
		long diff = endTimestamp - startTimestamp;
		if(diff >= MobileLearning.PAGE_READ_TIME){
			return true;
		} else {
			return false;
		}
	}
	
	public long getTimeTaken(){
		long endTimestamp = System.currentTimeMillis()/1000;
		long diff = endTimestamp - startTimestamp;
		if(diff >= MobileLearning.PAGE_READ_TIME){
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
			BugSenseHandler.sendException(e);
		}
		
		return obj;
	}

	@Override
	public String getContentToRead() {
		File f = new File ("/"+ module.getLocation() + "/" + activity.getLocation(prefs.getString("prefLanguage", Locale.getDefault().getLanguage())));
		StringBuilder text = new StringBuilder();
		try {
		    br = new BufferedReader(new FileReader(f));
		    String line;

		    while ((line = br.readLine()) != null) {
		        text.append(line);
		    }
		}
		catch (IOException e) {
		    return "";
		}
		return android.text.Html.fromHtml(text.toString()).toString();
	}


	public void mediaStopped() {
		if(mediaPlaying){
			Log.d(TAG,"Media was playing...");
			long mediaEndTimeStamp = System.currentTimeMillis()/1000;
			long timeTaken = mediaEndTimeStamp - mediaStartTimeStamp;
			Log.d(TAG,"video playing for:" + String.valueOf(timeTaken));
			mediaPlaying = false;
			// track that the video has been played (or at least clicked on)
			Tracker t = new Tracker(ctx);
			// digest should be that of the video not the page
			for(Media m: PageWidget.this.activity.getMedia()){
				if(m.getFilename().equals(mediaFileName)){
					Log.d(TAG,"media digest:" + m.getDigest());
					Log.d(TAG,"media file:" + mediaFileName);
					t.mediaPlayed(PageWidget.this.module.getModId(), m.getDigest(), mediaFileName, timeTaken);
				}
			}
		}
		
	}

	@Override
	public boolean getMediaPlaying() {
		return this.mediaPlaying;
	}

	@Override
	public long getMediaStartTime() {
		return this.mediaStartTimeStamp;
	}

	@Override
	public void setMediaPlaying(boolean playing) {
		this.mediaPlaying = playing;
		
	}

	@Override
	public void setMediaStartTime(long mediaStartTime) {
		this.mediaStartTimeStamp = mediaStartTime;
		
	}

	@Override
	public String getMediaFileName() {
		return this.mediaFileName;
	}

	@Override
	public void setMediaFileName(String mediaFileName) {
		this.mediaFileName = mediaFileName;	
	}


}
