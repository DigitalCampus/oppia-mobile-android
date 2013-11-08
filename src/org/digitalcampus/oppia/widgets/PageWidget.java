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

package org.digitalcampus.oppia.widgets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.Tracker;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.model.Module;
import org.digitalcampus.oppia.utils.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;

public class PageWidget extends WidgetFactory {

	public static final String TAG = PageWidget.class.getSimpleName();
	private Context ctx;
	private Module module;
	private org.digitalcampus.oppia.model.Activity activity;
	private long startTimestamp = System.currentTimeMillis()/1000;
	private long mediaStartTimeStamp;
	private boolean mediaPlaying = false;
	private String mediaFileName;
	private SharedPreferences prefs;
	private WebView wv;
	private BufferedReader br;
	private boolean readAloud = false;
	
	
	public PageWidget(Context context, Module module, org.digitalcampus.oppia.model.Activity activity) {
		super(context, module, activity);
		this.ctx = context;
		this.module = module;
		this.activity = activity;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		View vv = super.getLayoutInflater().inflate(R.layout.widget_page, null);
		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		super.getLayout().addView(vv);
		vv.setLayoutParams(lp);
		
		wv = (WebView) ((Activity) context).findViewById(R.id.page_webview);
		// get the location data
		String url = module.getLocation() + activity.getLocation(prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage()));
		// find if there is specific stylesheet in course package
		String styleLocation = "file:///android_asset/www/style.css";
		File styleSheet = new File(module.getLocation() + "/style.css");
		if(styleSheet.exists()){
			styleLocation = module.getLocation() + "/style.css";
		} 
		try {
			String content =  "<html><head>";
			content += "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>";
			content += "<link href='" + styleLocation + "' rel='stylesheet' type='text/css'/>";
			content += "</head>";
			content += FileUtils.readFile(url);
			content += "</html>";
			wv.loadDataWithBaseURL("file://" + module.getLocation() + "/", content, "text/html", "utf-8", null);
		} catch (IOException e) {
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
					if (!exists) {
						Toast.makeText(ctx, ctx.getString(R.string.error_media_not_found,mediaFileName), Toast.LENGTH_LONG).show();
						return true;
					}
					
					String mimeType = FileUtils.getMimeType(MobileLearning.MEDIA_PATH + mediaFileName);
					if(!FileUtils.supportedMediafileType(mimeType)){
						Toast.makeText(ctx, ctx.getString(R.string.error_media_unsupported, mediaFileName), Toast.LENGTH_LONG).show();
						return true;
					}
					
					// check user has app installed to play the video
					// launch intent to play video
					Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
					Uri data = Uri.parse(MobileLearning.MEDIA_PATH + mediaFileName);
					intent.setDataAndType(data, "video/mp4");
					
					PackageManager pm = PageWidget.this.ctx.getPackageManager();

					List<ResolveInfo> infos = pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
					boolean appFound = false;
					for (ResolveInfo info : infos) {
						IntentFilter filter = info.filter;
						if (filter != null && filter.hasAction(Intent.ACTION_VIEW)) {
							// Found an app with the right intent/filter
							appFound = true;
						}
					}
					if (!appFound){
						Toast.makeText(PageWidget.this.ctx, PageWidget.this.ctx.getString(R.string.error_media_app_not_found), Toast.LENGTH_LONG).show();
						return true;
					}
					
					mediaPlaying = true;
					mediaStartTimeStamp = System.currentTimeMillis()/1000;
					ctx.startActivity(intent);
					
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
	
	public boolean activityHasTracker(){
		long endTimestamp = System.currentTimeMillis()/1000;
		long diff = endTimestamp - startTimestamp;
		if(diff >= MobileLearning.PAGE_READ_TIME){
			return true;
		} else {
			return false;
		}
	}
	
	public boolean activityCompleted(){
		// only show as being complete if all the videos on this page have been played
		if(this.activity.hasMedia()){
			ArrayList<Media> mediaList = this.activity.getMedia();
			boolean completed = true;
			DbHelper db = new DbHelper(this.ctx);
			for (Media m: mediaList){
				if(!db.activityCompleted(this.module.getModId(), m.getDigest())){
					completed = false;
				}
			}
			db.close();
			if(!completed){
				return false;
			}
		}
		return true;
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
	
	public JSONObject getTrackerData(){
		JSONObject obj = new JSONObject();
		try {
			obj.put("timetaken", this.getTimeTaken());
			String lang = prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage());
			obj.put("lang", lang);
			obj.put("readaloud",readAloud);
		} catch (JSONException e) {
			e.printStackTrace();
			BugSenseHandler.sendException(e);
		}
		
		return obj;
	}

	@Override
	public String getContentToRead() {
		File f = new File ("/"+ module.getLocation() + "/" + activity.getLocation(prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage())));
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


	private void mediaStopped() {
		if(mediaPlaying){
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
					Log.d(TAG,"media length:" + m.getLength());
					boolean completed = false;
					if(timeTaken >= m.getLength()){
						completed = true;
					}
					t.mediaPlayed(PageWidget.this.module.getModId(), m.getDigest(), mediaFileName, timeTaken, completed);
				}
			}
		}
		
	}

	@Override
	public void setWidgetConfig(HashMap<String,Object> config) {
		if (config.containsKey("Media_Playing")){
			this.setMediaPlaying((Boolean) config.get("Media_Playing"));
		}
		if (config.containsKey("Media_StartTime")){
			this.setMediaStartTime((Long) config.get("Media_StartTime"));
		}
		if (config.containsKey("Media_File")){
			this.setMediaFileName((String) config.get("Media_File"));
		}
		this.mediaStopped();
	}
	
	@Override
	public HashMap<String, Object> getWidgetConfig() {
		HashMap<String, Object> config = new HashMap<String, Object>();
		config.put("Media_Playing", this.getMediaPlaying());
		config.put("Media_StartTime", this.getMediaStartTime());
		config.put("Media_File", this.getMediaFileName());
		return config;
	}
	
	private boolean getMediaPlaying() {
		return this.mediaPlaying;
	}

	private long getMediaStartTime() {
		return this.mediaStartTimeStamp;
	}

	private void setMediaPlaying(boolean playing) {
		this.mediaPlaying = playing;
	}

	private void setMediaStartTime(long mediaStartTime) {
		this.mediaStartTimeStamp = mediaStartTime;
	}

	private String getMediaFileName() {
		return this.mediaFileName;
	}

	private void setMediaFileName(String mediaFileName) {
		this.mediaFileName = mediaFileName;	
	}
	
	@Override
	public void setStartTime(long startTime) {
		this.startTimestamp = startTime;
		
	}

	@Override
	public long getStartTime() {
		return this.startTimestamp;
	}

	@Override
	public void setReadAloud(boolean reading){
		this.readAloud = reading;
	}
	
	@Override
	public boolean getReadAloud(){
		return readAloud;
	}

	@Override
	public void setBaselineActivity(boolean baseline) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isBaselineActivity() {
		// TODO Auto-generated method stub
		return false;
	}

	

}
