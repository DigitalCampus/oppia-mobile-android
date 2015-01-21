/* 
 * This file is part of OppiaMobile - https://digital-campus.org/
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
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.DatabaseManager;
import org.digitalcampus.oppia.application.DbHelper;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.Tracker;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.digitalcampus.oppia.utils.mediaplayer.VideoPlayerActivity;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ActivityNotFoundException;
import android.content.Intent;
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
	private WebView wv;

	
	public static PageWidget newInstance(Activity activity, Course course, boolean isBaseline) {
		PageWidget myFragment = new PageWidget();
	    Bundle args = new Bundle();
	    args.putSerializable(Activity.TAG, activity);
	    args.putSerializable(Course.TAG, course);
	    args.putBoolean(CourseActivity.BASELINE_TAG, isBaseline);
	    myFragment.setArguments(args);

	    return myFragment;
	}

	public PageWidget(){
		
	}


	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
		View vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.fragment_webview, null);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		vv.setLayoutParams(lp);
		activity = (Activity) getArguments().getSerializable(Activity.TAG);
		course = (Course) getArguments().getSerializable(Course.TAG);
		isBaseline = getArguments().getBoolean(CourseActivity.BASELINE_TAG);
		vv.setId(activity.getActId());
		if ((savedInstanceState != null) && (savedInstanceState.getSerializable("widget_config") != null)){
			setWidgetConfig((HashMap<String, Object>) savedInstanceState.getSerializable("widget_config"));
		}
		return vv;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("widget_config", getWidgetConfig());
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		wv = (WebView) super.getActivity().findViewById(activity.getActId());
		// get the location data
		String url = course.getLocation()
				+ activity.getLocation(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault()
						.getLanguage()));
		
		int defaultFontSize = Integer.parseInt(prefs.getString(PrefsActivity.PREF_TEXT_SIZE, "16"));
		wv.getSettings().setDefaultFontSize(defaultFontSize);

		try {
			wv.getSettings().setJavaScriptEnabled(true); 
			wv.loadDataWithBaseURL("file://" + course.getLocation() + File.separator, FileUtils.readFile(url), "text/html",
					"utf-8", null);
		} catch (IOException e) {
			wv.loadUrl("file://" + url);
		}

		// set up the page to intercept videos
		wv.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				Log.d(TAG,url);
				if (url.contains("/video/") && !url.contains("vimeo.com/video/")) {
					// extract video name from url
					int startPos = url.indexOf("/video/") + 7;
					String mediaFileName = url.substring(startPos, url.length());

					// check video file exists
					boolean exists = FileUtils.mediaFileExists(PageWidget.super.getActivity(), mediaFileName);
					if (!exists) {
						Toast.makeText(PageWidget.super.getActivity(), PageWidget.super.getActivity().getString(R.string.error_media_not_found, mediaFileName),
								Toast.LENGTH_LONG).show();
						return true;
					}

					String mimeType = FileUtils.getMimeType(FileUtils.getMediaPath(PageWidget.super.getActivity()) + mediaFileName);

					if (!FileUtils.supportedMediafileType(mimeType)) {
						Toast.makeText(PageWidget.super.getActivity(), PageWidget.super.getActivity().getString(R.string.error_media_unsupported, mediaFileName),
								Toast.LENGTH_LONG).show();
						return true;
					}
					
					Intent intent = new Intent(PageWidget.super.getActivity(), VideoPlayerActivity.class);
					Bundle tb = new Bundle();
					tb.putSerializable(VideoPlayerActivity.MEDIA_TAG, mediaFileName);
					tb.putSerializable(Activity.TAG, activity);
					tb.putSerializable(Course.TAG, course);
					intent.putExtras(tb);
					startActivity(intent);
					return true;
					
				} else {
					
					try {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						Uri data = Uri.parse(url);
						intent.setData(data);
						PageWidget.super.getActivity().startActivity(intent);
					} catch (ActivityNotFoundException anfe) {
						// do nothing
					}
					// launch action in mobile browser - not the webview
					// return true so doesn't follow link within webview
					return true;
				}

			}
		});
	}

	public void setIsBaseline(boolean isBaseline) {
		this.isBaseline = isBaseline;
	}
	
	protected boolean getActivityCompleted() {
		// only show as being complete if all the videos on this page have been
		// played
		if (this.activity.hasMedia()) {
			ArrayList<Media> mediaList = this.activity.getMedia();
			boolean completed = true;
			DbHelper db = new DbHelper(super.getActivity());
			long userId = db.getUserId(prefs.getString(PrefsActivity.PREF_USER_NAME, ""));
			for (Media m : mediaList) {
				if (!db.activityCompleted(this.course.getCourseId(), m.getDigest(), userId)) {
					completed = false;
				}
			}
			DatabaseManager.getInstance().closeDatabase();
			return completed;
		} else {
			return true;
		}
	}

	public void saveTracker() {
		long timetaken = System.currentTimeMillis() / 1000 - this.getStartTime();
		// only save tracker if over the time
		if (timetaken < MobileLearning.PAGE_READ_TIME) {
			return;
		}
		Tracker t = new Tracker(super.getActivity());
		JSONObject obj = new JSONObject();
		
		// add in extra meta-data
		try {
			MetaDataUtils mdu = new MetaDataUtils(super.getActivity());
			obj.put("timetaken", timetaken);
			obj = mdu.getMetaData(obj);
			String lang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
			obj.put("lang", lang);
			obj.put("readaloud", readAloud);
			// if it's a baseline activity then assume completed
			if (this.isBaseline) {
				t.saveTracker(course.getCourseId(), activity.getDigest(), obj, true);
			} else {
				t.saveTracker(course.getCourseId(), activity.getDigest(), obj, this.getActivityCompleted());
			}
		} catch (JSONException e) {
			// Do nothing
			// sometimes get null pointer exception for the MetaDataUtils if the screen is rotated rapidly
		} catch (NullPointerException npe){
			//do nothing
		}
	}

	@Override
	public void setWidgetConfig(HashMap<String, Object> config) {
		if (config.containsKey("Activity_StartTime")) {
			this.setStartTime((Long) config.get("Activity_StartTime"));
		}
		if (config.containsKey("Activity")) {
			activity = (Activity) config.get("Activity");
		}
		if (config.containsKey("Course")) {
			course = (Course) config.get("Course");
		}
	}

	@Override
	public HashMap<String, Object> getWidgetConfig() {
		HashMap<String, Object> config = new HashMap<String, Object>();
		config.put("Activity_StartTime", this.getStartTime());
		config.put("Activity", this.activity);
		config.put("Course", this.course);
		return config;
	}

	public String getContentToRead() {
		File f = new File(File.separator
				+ course.getLocation()
				+ File.separator
				+ activity.getLocation(prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault()
						.getLanguage())));
		StringBuilder text = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));
			String line;

			while ((line = br.readLine()) != null) {
				text.append(line);
			}
			br.close();
		} catch (IOException e) {
			return "";
		}
		return android.text.Html.fromHtml(text.toString()).toString();
	}
}
