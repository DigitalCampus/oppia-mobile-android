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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.Tracker;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.utils.FileUtils;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

public class PageWidget extends WidgetFactory {

	public static final String TAG = PageWidget.class.getSimpleName();
	private Context ctx;
	private String mediaFileName;
	private WebView wv;
	
	
	 @Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
		course = (Course) getArguments().getSerializable(Course.TAG);
		activity = (Activity) getArguments().getSerializable(Activity.TAG);
		this.setIsBaseline(getArguments().getBoolean(CourseActivity.BASELINE_TAG));
		ctx = super.getActivity();
		View vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.widget_page, null);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		vv.setLayoutParams(lp);
		return vv;
	}
	
	 
	 @Override
	 public void onActivityCreated(Bundle savedInstanceState) { 
		 super.onActivityCreated(savedInstanceState);
		 wv = (WebView) ((android.app.Activity) ctx).findViewById(R.id.page_webview);
			// get the location data
			String url = course.getLocation() + activity.getLocation(prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage()));
			try {
				wv.loadDataWithBaseURL("file://" + course.getLocation() + "/", FileUtils.readFile(url), "text/html", "utf-8", null);
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

	@Override
	public void onStop() {
		this.saveTracker();
		super.onStop();
	}
	 
	@Override
	protected boolean getActivityCompleted() {
		// only show as being complete if all the videos on this page have been
		// played
		if (this.activity.hasMedia()) {
			ArrayList<Media> mediaList = this.activity.getMedia();
			boolean completed = true;
			DbHelper db = new DbHelper(this.ctx);
			for (Media m : mediaList) {
				if (!db.activityCompleted(this.course.getModId(), m.getDigest())) {
					completed = false;
				}
			}
			db.close();
			if (!completed) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	protected void saveTracker(){
		long timetaken = System.currentTimeMillis()/1000 - startTime;
		this.startTime = System.currentTimeMillis()/1000;
		// only save tracker if over the time 
		if (timetaken < MobileLearning.PAGE_READ_TIME) {
			return;
		}
		Tracker t = new Tracker(ctx);
		JSONObject obj = new JSONObject();
		MetaDataUtils mdu = new MetaDataUtils(ctx);
		// add in extra meta-data
		try {
			obj.put("timetaken", timetaken);
			obj = mdu.getMetaData(obj);
			String lang = prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage());
			obj.put("lang", lang);
             //obj.put("readaloud",readAloud);
		} catch (JSONException e) {
			// Do nothing
		} 
		// if it's a baseline activity then assume completed
		if(this.isBaseline){
			t.saveTracker(course.getModId(), activity.getDigest(), obj, true);
		} else {
			t.saveTracker(course.getModId(), activity.getDigest(), obj, this.getActivityCompleted());
		}
	}
}
