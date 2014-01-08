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

import com.bugsense.trace.BugSenseHandler;

public class PageWidget extends WidgetFactory {

	public static final String TAG = PageWidget.class.getSimpleName();
	private boolean mediaPlaying = false;
	private long mediaStartTimeStamp;
	private String mediaFileName;
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
		View vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.widget_page, null);
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
				+ activity.getLocation(prefs.getString(super.getActivity().getString(R.string.prefs_language), Locale.getDefault()
						.getLanguage()));
		try {
			wv.loadDataWithBaseURL("file://" + course.getLocation() + "/", FileUtils.readFile(url), "text/html",
					"utf-8", null);
		} catch (IOException e) {
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
						Toast.makeText(PageWidget.super.getActivity(), PageWidget.super.getActivity().getString(R.string.error_media_not_found, mediaFileName),
								Toast.LENGTH_LONG).show();
						return true;
					}

					String mimeType = FileUtils.getMimeType(MobileLearning.MEDIA_PATH + mediaFileName);
					if (!FileUtils.supportedMediafileType(mimeType)) {
						Toast.makeText(PageWidget.super.getActivity(), PageWidget.super.getActivity().getString(R.string.error_media_unsupported, mediaFileName),
								Toast.LENGTH_LONG).show();
						return true;
					}

					// check user has app installed to play the video
					// launch intent to play video
					Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
					Uri data = Uri.parse(MobileLearning.MEDIA_PATH + mediaFileName);
					intent.setDataAndType(data, "video/mp4");

					PackageManager pm = PageWidget.super.getActivity().getPackageManager();

					List<ResolveInfo> infos = pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
					boolean appFound = false;
					for (ResolveInfo info : infos) {
						IntentFilter filter = info.filter;
						if (filter != null && filter.hasAction(Intent.ACTION_VIEW)) {
							// Found an app with the right intent/filter
							appFound = true;
						}
					}
					if (!appFound) {
						Toast.makeText(PageWidget.super.getActivity(),
								PageWidget.super.getActivity().getString(R.string.error_media_app_not_found), Toast.LENGTH_LONG)
								.show();
						return true;
					}
					PageWidget.this.mediaPlaying = true;
					PageWidget.this.mediaStartTimeStamp = System.currentTimeMillis() / 1000;
					PageWidget.super.getActivity().startActivity(intent);

					return true;
				} else {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					Uri data = Uri.parse(url);
					intent.setData(data);
					PageWidget.super.getActivity().startActivity(intent);
					// launch action in mobile browser - not the webview
					// return true so doesn't follow link within webview
					return true;
				}

			}
		});
	}


	@Override
	public void onResume() {
		super.onResume();
		if (mediaPlaying) {
			this.mediaStopped();
		} 
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
			String lang = prefs.getString(super.getActivity().getString(R.string.prefs_language), Locale.getDefault().getLanguage());
			obj.put("lang", lang);
			obj.put("readaloud", readAloud);
			// if it's a baseline activity then assume completed
			if (this.isBaseline) {
				t.saveTracker(course.getModId(), activity.getDigest(), obj, true);
			} else {
				t.saveTracker(course.getModId(), activity.getDigest(), obj, this.getActivityCompleted());
			}
		} catch (JSONException e) {
			// Do nothing
		// sometimes get null pointer exception for the MetaDataUtils if the screen is rotated rapidly
		} catch (NullPointerException npe){
			//do nothing
		}
	}

	private void mediaStopped() {
		if (mediaPlaying) {
			long mediaEndTimeStamp = System.currentTimeMillis() / 1000;
			long timeTaken = mediaEndTimeStamp - mediaStartTimeStamp;
			Log.d(TAG, "video playing for:" + String.valueOf(timeTaken));
			mediaPlaying = false;
			// track that the video has been played (or at least clicked on)
			Tracker t = new Tracker(super.getActivity());
			// digest should be that of the video not the page
			for (Media m : PageWidget.this.activity.getMedia()) {
				if (m.getFilename().equals(mediaFileName)) {
					Log.d(TAG, "media digest:" + m.getDigest());
					Log.d(TAG, "media file:" + mediaFileName);
					Log.d(TAG, "media length:" + m.getLength());
					boolean completed = false;
					if (timeTaken >= m.getLength()) {
						completed = true;
					}
					JSONObject data = new JSONObject();
					try {
						data.put("media", "played");
						data.put("mediafile", mediaFileName);
						data.put("timetaken", timeTaken);
						String lang = prefs.getString(super.getActivity().getString(R.string.prefs_language), Locale.getDefault()
								.getLanguage());
						data.put("lang", lang);
					} catch (JSONException e) {
						if (!MobileLearning.DEVELOPER_MODE) {
							BugSenseHandler.sendException(e);
						} else {
							e.printStackTrace();
						}
					}
					MetaDataUtils mdu = new MetaDataUtils(super.getActivity());
					// add in extra meta-data
					try {
						data = mdu.getMetaData(data);
					} catch (JSONException e) {
						// Do nothing
					}
					t.saveTracker(PageWidget.this.course.getModId(), m.getDigest(), data, completed);
				}
			}
		}

	}

	@Override
	public void setWidgetConfig(HashMap<String, Object> config) {
		if (config.containsKey("Media_Playing")) {
			this.setMediaPlaying((Boolean) config.get("Media_Playing"));
		}
		if (config.containsKey("Media_StartTime")) {
			this.setMediaStartTime((Long) config.get("Media_StartTime"));
		}
		if (config.containsKey("Media_File")) {
			this.setMediaFileName((String) config.get("Media_File"));
		}
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
		config.put("Media_Playing", this.getMediaPlaying());
		config.put("Media_StartTime", this.getMediaStartTime());
		config.put("Media_File", this.getMediaFileName());
		config.put("Activity_StartTime", this.getStartTime());
		config.put("Activity", this.activity);
		config.put("Course", this.course);
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

	public String getContentToRead() {
		File f = new File("/"
				+ course.getLocation()
				+ "/"
				+ activity.getLocation(prefs.getString(super.getActivity().getString(R.string.prefs_language), Locale.getDefault()
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
