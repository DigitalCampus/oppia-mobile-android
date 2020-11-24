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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.SessionManager;
import org.digitalcampus.oppia.database.DbHelper;
import org.digitalcampus.oppia.gamification.GamificationServiceDelegate;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.Media;
import org.digitalcampus.oppia.utils.resources.JSInterfaceForResourceImages;
import org.digitalcampus.oppia.utils.storage.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;

public class PageWidget extends BaseWidget {

	public static final String TAG = PageWidget.class.getSimpleName();

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
        // Required empty public constructor
	}


	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View vv = inflater.inflate(R.layout.fragment_webview, container, false);

		activity = (Activity) getArguments().getSerializable(Activity.TAG);
		course = (Course) getArguments().getSerializable(Course.TAG);
		isBaseline = getArguments().getBoolean(CourseActivity.BASELINE_TAG);
		vv.setId(activity.getActId());
		if ((savedInstanceState != null) && (savedInstanceState.getSerializable(BaseWidget.WIDGET_CONFIG) != null)){
			setWidgetConfig((HashMap<String, Object>) savedInstanceState.getSerializable(BaseWidget.WIDGET_CONFIG));
		}
		return vv;
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(BaseWidget.WIDGET_CONFIG, (Serializable) getWidgetConfig());
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		WebView wv = super.getActivity().findViewById(activity.getActId());
		// get the location data
		String url = course.getLocation() + activity.getLocation(prefLang);
		
		int defaultFontSize = Integer.parseInt(prefs.getString(PrefsActivity.PREF_TEXT_SIZE, "16"));
		wv.getSettings().setDefaultFontSize(defaultFontSize);

		try {
			wv.getSettings().setJavaScriptEnabled(true);
            //We inject the interface to launch intents from the HTML
            wv.addJavascriptInterface(
                    new JSInterfaceForResourceImages(this.getActivity(), course.getLocation()),
                    JSInterfaceForResourceImages.INTERFACE_EXPOSED_NAME);

			wv.loadDataWithBaseURL("file://" + course.getLocation() + File.separator, FileUtils.readFile(url), "text/html", "utf-8", null);
		} catch (IOException e) {
			wv.loadUrl("file://" + url);
		}


		wv.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                //We execute the necessary JS code to bind click on images with our JavascriptInterface
                view.loadUrl(JSInterfaceForResourceImages.JS_INJECTION);
            }

            // set up the page to intercept videos
			/**      
			 * @deprecated (replace as soon as possible)
			 */
			@Override
			@Deprecated
			public boolean shouldOverrideUrlLoading(WebView view, String url) {

				if (url.contains("/video/")) {
					// extract video name from url
					int startPos = url.indexOf("/video/") + 7;
					String mediaFileName = url.substring(startPos);
					PageWidget.super.startMediaPlayerWithFile(mediaFileName);
                    return true;
					
				} else {
					
					try {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						Uri data = Uri.parse(url);
						intent.setData(data);
						PageWidget.super.getActivity().startActivity(intent);
						return true;
					} catch (ActivityNotFoundException anfe) {
						Log.d(TAG,"Activity not found", anfe);
					}
					return false;
				}
			}
		});
	}

	@Override
	public void setIsBaseline(boolean isBaseline) {
		this.isBaseline = isBaseline;
	}
	
	public boolean getActivityCompleted() {
		// only show as being complete if all the videos on this page have been played
		if (this.activity.hasMedia()) {
			ArrayList<Media> mediaList = (ArrayList<Media>) this.activity.getMedia();
			boolean completed = true;
			DbHelper db = DbHelper.getInstance(super.getActivity());
			long userId = db.getUserId(SessionManager.getUsername(getActivity()));
			for (Media m : mediaList) {
				if (!db.activityCompleted(this.course.getCourseId(), m.getDigest(), userId)) {
					completed = false;
				}
			}
			return completed;
		} else {
			return true;
		}
	}

	public void saveTracker() {
		if (activity == null){
			return;
		}
		long timetaken = this.getSpentTime();
		// only save tracker if over the time
		if (timetaken < App.PAGE_READ_TIME) {
			return;
		}

		new GamificationServiceDelegate(getActivity())
			.createActivityIntent(course, activity, getActivityCompleted(), isBaseline)
			.registerPageActivityEvent(timetaken, readAloud);
	}

	@Override
	public void setWidgetConfig(HashMap<String, Object> config) {
		if (config.containsKey(BaseWidget.PROPERTY_ACTIVITY_STARTTIME)) {
			this.setStartTime((Long) config.get(BaseWidget.PROPERTY_ACTIVITY_STARTTIME));
		}
		if (config.containsKey(BaseWidget.PROPERTY_ACTIVITY)) {
			activity = (Activity) config.get(BaseWidget.PROPERTY_ACTIVITY);
		}
		if (config.containsKey(BaseWidget.PROPERTY_COURSE)) {
			course = (Course) config.get(BaseWidget.PROPERTY_COURSE);
		}
	}

	@Override
	public HashMap<String, Object> getWidgetConfig() {
		HashMap<String, Object> config = new HashMap<>();
		config.put(BaseWidget.PROPERTY_ACTIVITY_STARTTIME, this.getStartTime());
		config.put(BaseWidget.PROPERTY_ACTIVITY, this.activity);
		config.put(BaseWidget.PROPERTY_COURSE, this.course);
		return config;
	}

	public String getContentToRead() {
		File f = new File(File.separator + course.getLocation() + File.separator
				+ activity.getLocation(prefLang));
		StringBuilder text = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(f))){
			String line;
			while ((line = reader.readLine()) != null) {
				text.append(line);
			}
		} catch (IOException e) {
			Log.e(TAG, "getContentToRead: ", e);
			return "";
		}

		return HtmlCompat.fromHtml(text.toString(), HtmlCompat.FROM_HTML_MODE_LEGACY).toString();
	}


}
