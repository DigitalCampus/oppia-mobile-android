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

package org.digitalcampus.mobile.learning.widgets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.activity.ModuleActivity;
import org.digitalcampus.mobile.learning.application.DbHelper;
import org.digitalcampus.mobile.learning.application.MobileLearning;
import org.digitalcampus.mobile.learning.application.Tracker;
import org.digitalcampus.mobile.learning.gesture.ResourceGestureDetector;
import org.digitalcampus.mobile.learning.model.Media;
import org.digitalcampus.mobile.learning.model.Module;
import org.digitalcampus.mobile.learning.utils.FileUtils;
import org.digitalcampus.mquiz.MQuiz;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;

public class PageWidget extends WidgetFactory {

	public static final String TAG = PageWidget.class.getSimpleName();
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
	private boolean readAloud = false;
	private GestureDetector pageGestureDetector;
	private OnTouchListener pageGestureListener;
	
	
	public PageWidget(Context context, Module module, org.digitalcampus.mobile.learning.model.Activity activity) {
		super(context, module, activity);
		this.ctx = context;
		this.module = module;
		this.activity = activity;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		
		pageGestureDetector = new GestureDetector((Activity) context, new ResourceGestureDetector((ModuleActivity) context));
		pageGestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				try {
					// TODO - for some reason unless this is in a try/catch block it will fail with NullPointerException
					return pageGestureDetector.onTouchEvent(event);
				} catch (Exception e){
					return false;
				}
			}
		};
		View vv = super.getLayoutInflater().inflate(R.layout.widget_page, null);
		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		super.getLayout().addView(vv);
		vv.setLayoutParams(lp);
		
		wv = (WebView) ((Activity) context).findViewById(R.id.page_webview);
		wv.setOnTouchListener(pageGestureListener);
		// get the location data
		String url = module.getLocation() + "/" + activity.getLocation(prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage()));
		try {
			String content =  "<html><head>";
			content += "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>";
			content += "<link href='file:///android_asset/www/style.css' rel='stylesheet' type='text/css'/>";
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
					if (exists) {
						
						String mimeType = FileUtils.getMimeType(MobileLearning.MEDIA_PATH + mediaFileName);
						if(FileUtils.supportedMediafileType(mimeType)){
							// launch intent to play video
							Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
							Uri data = Uri.parse(MobileLearning.MEDIA_PATH + mediaFileName);
							intent.setDataAndType(data, "video/mp4");
							mediaPlaying = true;
							mediaStartTimeStamp = System.currentTimeMillis()/1000;
							ctx.startActivity(intent);
						} else {
							Toast.makeText(ctx, ctx.getString(R.string.error_media_unsupported, mediaFileName), Toast.LENGTH_LONG).show();
						}
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


	public void mediaStopped() {
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
	public MQuiz getMQuiz() {
		//do nothing
		return null;
	}

	@Override
	public void setMQuiz(MQuiz mquiz) {
		// do nothing
		
	}

}
