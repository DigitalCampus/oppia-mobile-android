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

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.Tracker;
import org.digitalcampus.oppia.gamification.GamificationEngine;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class UrlWidget extends BaseWidget {

	public static final String TAG = UrlWidget.class.getSimpleName();	
	
	public static UrlWidget newInstance(Activity activity, Course course, boolean isBaseline) {
		UrlWidget myFragment = new UrlWidget();

		Bundle args = new Bundle();
		args.putSerializable(Activity.TAG, activity);
		args.putSerializable(Course.TAG, course);
		args.putBoolean(CourseActivity.BASELINE_TAG, isBaseline);
		myFragment.setArguments(args);

		return myFragment;
	}

	public UrlWidget() {
		// Required empty public constructor
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		course = (Course) getArguments().getSerializable(Course.TAG);
		activity = (org.digitalcampus.oppia.model.Activity) getArguments().getSerializable(org.digitalcampus.oppia.model.Activity.TAG);
		this.setIsBaseline(getArguments().getBoolean(CourseActivity.BASELINE_TAG));

		View vv = inflater.inflate(R.layout.widget_url, container, false);
		vv.setId(activity.getActId());
		if ((savedInstanceState != null) && (savedInstanceState.getSerializable("widget_config") != null)){
			setWidgetConfig((HashMap<String, Object>) savedInstanceState.getSerializable("widget_config"));
		}
		return vv;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) { 
		super.onActivityCreated(savedInstanceState);

		// show description if any
		String desc = activity.getDescription(prefLang);
		TextView descTV = getView().findViewById(R.id.widget_url_description);
		if ((desc != null) && desc.length() > 0){
			descTV.setText(desc);
		} else {
			descTV.setVisibility(View.GONE);
		}
		
		WebView wv = getView().findViewById(R.id.widget_url_webview);
		int defaultFontSize = Integer.parseInt(prefs.getString(PrefsActivity.PREF_TEXT_SIZE, "16"));
		wv.getSettings().setDefaultFontSize(defaultFontSize);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.setWebViewClient(new WebViewClient() {
			/**      
			 * @deprecated (replace as soon as possible)
			 */
	        @Override
			@Deprecated
	        public boolean shouldOverrideUrlLoading(WebView view, String url)
	        {
	            return false;
	        }
	    });
		wv.loadUrl(activity.getLocation(prefLang));

	}
	
	
	@Override
	public boolean getActivityCompleted() {
		return false;
	}

	@Override
	public void saveTracker(){
		long timetaken = this.getSpentTime();
		if (timetaken < App.URL_READ_TIME) {
			return;
		}
		Tracker t = new Tracker(super.getActivity());
		JSONObject obj = new JSONObject();
		
		// add in extra meta-data
		try {
			MetaDataUtils mdu = new MetaDataUtils(super.getActivity());
			obj.put("timetaken", timetaken);
			obj = mdu.getMetaData(obj);
			obj.put("lang", prefLang);

			GamificationEngine gamificationEngine = new GamificationEngine(getActivity());
			GamificationEvent gamificationEvent = gamificationEngine.processEventURLActivity(this.course, this.activity);

			// if it's a baseline activity then assume completed
			if(this.isBaseline){
				t.saveTracker(course.getCourseId(), activity.getDigest(), obj, true, gamificationEvent);
			} else {
				t.saveTracker(course.getCourseId(), activity.getDigest(), obj, this.getActivityCompleted(), gamificationEvent);
			}
		} catch (JSONException jsone) {
			Log.d(TAG,"Error generating json for url widget", jsone);
			Mint.logException(jsone);
		} catch (NullPointerException npe){
			Log.d(TAG,"Null pointer in generating json for url widget", npe);
			Mint.logException(npe);
		}
	}

	@Override
	public String getContentToRead() {
		return null;
	}

	@Override
	public HashMap<String, Object> getWidgetConfig() {
		return null;
	}

	@Override
	public void setWidgetConfig(HashMap<String, Object> config) {
		// do nothing
	}

}
