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
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.digitalcampus.mobile.learning.BuildConfig;
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
import org.digitalcampus.oppia.utils.TextUtilsJava;
import org.digitalcampus.oppia.utils.resources.ExternalResourceOpener;
import org.digitalcampus.oppia.utils.resources.JSInterface;
import org.digitalcampus.oppia.utils.resources.JSInterfaceForBackwardsCompat;
import org.digitalcampus.oppia.utils.resources.JSInterfaceForInlineInput;
import org.digitalcampus.oppia.utils.resources.JSInterfaceForResourceImages;
import org.digitalcampus.oppia.utils.storage.FileUtils;
import org.digitalcampus.oppia.utils.storage.Storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;

import com.google.android.material.snackbar.Snackbar;

public class PageWidget extends BaseWidget implements JSInterfaceForInlineInput.OnInputEnteredListener {

	public static final String TAG = PageWidget.class.getSimpleName();

	private static final String VIDEO_SUBPATH = "/video/";
	private static final String RESOURCE_SUBPATH = "/resources/";

	private WebView webview;

	private final List<JSInterface> jsInterfaces = new ArrayList<>();
	private final List<String> inlineInput = new ArrayList<>();
	private List<String> pageResources = new ArrayList<>();
	private final List<String> openedResources = new ArrayList<>();

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
		outState.putSerializable(BaseWidget.WIDGET_CONFIG, (Serializable) getWidgetConfig());
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// get the location data
		String url = course.getLocation() + activity.getLocation(prefLang);
		int defaultFontSize = Integer.parseInt(prefs.getString(PrefsActivity.PREF_TEXT_SIZE, "16"));

		webview = super.getActivity().findViewById(activity.getActId());
		webview.getSettings().setDefaultFontSize(defaultFontSize);
		webview.getSettings().setAllowFileAccess(true);

		try {
			String contents = FileUtils.readFile(url);
			pageResources = ExternalResourceOpener.getResourcesFromContent(contents);

			webview.getSettings().setJavaScriptEnabled(true);

			JSInterfaceForBackwardsCompat backwardsCompatJSInterface = new JSInterfaceForBackwardsCompat(getContext());
			jsInterfaces.add(backwardsCompatJSInterface);
			webview.addJavascriptInterface(backwardsCompatJSInterface, backwardsCompatJSInterface.getInterfaceExposedName());

            //We inject the interface to launch intents from the HTML
			JSInterfaceForResourceImages imagesJSInterface = new JSInterfaceForResourceImages(getContext(), course.getLocation());
			jsInterfaces.add(imagesJSInterface);
            webview.addJavascriptInterface(imagesJSInterface, imagesJSInterface.getInterfaceExposedName());

			JSInterfaceForInlineInput inputJSInterface = new JSInterfaceForInlineInput(getContext());
			inputJSInterface.setOnInputEnteredListener(this);
			jsInterfaces.add(inputJSInterface);
            webview.addJavascriptInterface(inputJSInterface, inputJSInterface.getInterfaceExposedName());


			webview.loadDataWithBaseURL("file://" + course.getLocation() + File.separator, contents, "text/html", "utf-8", null);
		} catch (IOException e) {
			webview.loadUrl("file://" + url);
		}


		webview.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url) {
                //We execute the necessary JS code to bind our JavascriptInterfaces
                for (JSInterface jsInterface : jsInterfaces){
					view.loadUrl(jsInterface.getJavascriptInjection());
				}
				view.evaluateJavascript("changeAudioSource('" + Storage.getMediaPath(getContext()) + "')", null);
            }

            // set up the page to intercept videos
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				return handleUrl(request.getUrl().getPath());
			}

			private boolean handleUrl(final String url){
				if (url.contains(VIDEO_SUBPATH)) {
					// extract video name from url
					int startPos = url.indexOf(VIDEO_SUBPATH) + VIDEO_SUBPATH.length();
					String mediaFileName = url.substring(startPos);
					PageWidget.super.startMediaPlayerWithFile(mediaFileName);
					return true;

				} else if (url.contains(RESOURCE_SUBPATH)){
					openResourceExternally(url);
				}
				return false;
			}
		});
	}

	@Override
	public void setIsBaseline(boolean isBaseline) {
		this.isBaseline = isBaseline;
	}

	@Override
	protected void onTextSizeChanged(int fontSize) {
		webview.getSettings().setDefaultFontSize(fontSize);
	}

	private void openResourceExternally(String fileUrl){
		File fileToOpen = new File(fileUrl);
		try {
			Intent intent = ExternalResourceOpener.getIntentToOpenResource(getContext(), fileToOpen);
			if(intent != null){
				PageWidget.super.getActivity().startActivity(intent);
				openedResources.add(fileToOpen.getName());
			} else {
				showResourceOpenerNotFoundMessage(fileToOpen);
			}
		} catch (ActivityNotFoundException anfe) {
			Log.d(TAG,"Activity not found", anfe);
		}
	}

	private void showResourceOpenerNotFoundMessage(File fileToOpen){
		String filename = fileToOpen.getName();
		Intent installAppIntent = ExternalResourceOpener.getIntentToInstallAppForResource(getContext(), fileToOpen);

		Snackbar snackbar = Snackbar.make(getView(),
				getContext().getString(R.string.external_resource_app_missing, filename), Snackbar.LENGTH_LONG);
		snackbar.show();
		snackbar.setActionTextColor(Color.WHITE);

		if (installAppIntent != null){
			snackbar.setAction(R.string.external_resource_install_app, v -> {
				PageWidget.super.getActivity().startActivity(installAppIntent);
			});
		}
	}

	public boolean activityMediaCompleted() {
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

	public boolean getActivityCompleted(){
		if (!this.activityMediaCompleted()){
			return false;
		}

		if (!pageResources.isEmpty() && BuildConfig.PAGE_COMPLETION_VIEW_FILE){
			//If there are files, they have to be opened to mark the activity has completed
			for (String resource : pageResources){
				if (!openedResources.contains(resource)){
					return false;
				}
			}
		}

		long timetaken = this.getSpentTime();
		if (BuildConfig.PAGE_COMPLETED_METHOD.equals(App.PAGE_COMPLETED_METHOD_TIME_SPENT)){
			Log.d(TAG, "Time spent: " + timetaken + " | Min time (fixed): " + BuildConfig.PAGE_COMPLETED_TIME_SPENT);
			return timetaken > BuildConfig.PAGE_COMPLETED_TIME_SPENT;
		}

		if (BuildConfig.PAGE_COMPLETED_METHOD.equals(App.PAGE_COMPLETED_METHOD_WPM)){
			Activity a = DbHelper.getInstance(getContext()).getActivityByDigest(activity.getDigest());
			int minTimeToRead = (a.getWordCount() * 60) / BuildConfig.PAGE_COMPLETED_WPM;
			Log.d(TAG, "Time spent: " + timetaken + " | Min time (WPM): " + minTimeToRead);
			return timetaken > minTimeToRead;
		}
		return timetaken > App.PAGE_READ_TIME;

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

		GamificationServiceDelegate delegate = new GamificationServiceDelegate(getActivity())
				.createActivityIntent(course, activity, getActivityCompleted(), isBaseline);
		if (!inlineInput.isEmpty()){
			delegate.addExtraEventData("inline_input", TextUtilsJava.join(",", inlineInput));
		}
		if (!openedResources.isEmpty()){
			delegate.addExtraEventData("opened_resources", TextUtilsJava.join(",", openedResources));
		}
		delegate.registerPageActivityEvent(timetaken, readAloud);
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

	@Override
	public void inlineInputReceived(String input) {
		if (!inlineInput.contains(input)){
			inlineInput.add(input);
		}
	}
}
