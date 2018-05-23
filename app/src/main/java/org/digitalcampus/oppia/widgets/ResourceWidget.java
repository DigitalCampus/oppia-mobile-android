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

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.activity.PrefsActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.application.Tracker;
import org.digitalcampus.oppia.gamification.GamificationEngine;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.digitalcampus.oppia.utils.resources.ExternalResourceOpener;
import org.digitalcampus.oppia.utils.storage.Storage;
import org.json.JSONException;
import org.json.JSONObject;

import com.splunk.mint.Mint;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class ResourceWidget extends WidgetFactory {

	public static final String TAG = ResourceWidget.class.getSimpleName();	
	private boolean resourceViewing = false;
	private long resourceStartTime;
	private String resourceFileName;
	
	public static ResourceWidget newInstance(Activity activity, Course course, boolean isBaseline) {
		ResourceWidget myFragment = new ResourceWidget();

		Bundle args = new Bundle();
		args.putSerializable(Activity.TAG, activity);
		args.putSerializable(Course.TAG, course);
		args.putBoolean(CourseActivity.BASELINE_TAG, isBaseline);
		myFragment.setArguments(args);

		return myFragment;
	}

	public ResourceWidget() {

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
		course = (Course) getArguments().getSerializable(Course.TAG);
		activity = (org.digitalcampus.oppia.model.Activity) getArguments().getSerializable(org.digitalcampus.oppia.model.Activity.TAG);
		this.setIsBaseline(getArguments().getBoolean(CourseActivity.BASELINE_TAG));
		View vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.widget_resource, null);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		vv.setLayoutParams(lp);
		vv.setId(activity.getActId());
		if ((savedInstanceState != null) && (savedInstanceState.getSerializable("widget_config") != null)){
			setWidgetConfig((HashMap<String, Object>) savedInstanceState.getSerializable("widget_config"));
		}
		return vv;
	}
	 
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("widget_config", this.getWidgetConfig());
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) { 
		super.onActivityCreated(savedInstanceState);

		String lang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault().getLanguage());
		LinearLayout ll = (LinearLayout) getView().findViewById(R.id.widget_resource_object);
		String fileUrl = course.getLocation() + activity.getLocation(lang);

		// show description if any
		String desc = activity.getMultiLangInfo().getDescription(lang);
		TextView descTV = (TextView) getView().findViewById(R.id.widget_resource_description);
		if ((desc != null) && desc.length() > 0){
			descTV.setText(desc);
		} else {
			descTV.setVisibility(View.GONE);
		}
		
		File file = new File(fileUrl);
		setResourceFileName(file.getName());
		OnResourceClickListener orcl = new OnResourceClickListener(super.getActivity(),activity.getMimeType());
		// show image files
		if (activity.getMimeType().equals("image/jpeg") || activity.getMimeType().equals("image/png")){
			ImageView iv = new ImageView(super.getActivity());
			Bitmap myBitmap = BitmapFactory.decodeFile(fileUrl);
			iv.setImageBitmap(myBitmap);
			LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			ll.addView(iv, lp);
			iv.setTag(file);
			iv.setOnClickListener(orcl);
		} else {
		// add button to open other filetypes in whatever app the user has installed as default for that filetype
			Button btn = new Button(super.getActivity());
			btn.setText(super.getActivity().getString(R.string.widget_resource_open_file,file.getName()));
			btn.setTextAppearance(super.getActivity(), R.style.ButtonText);
			ll.addView(btn);
			btn.setTag(file);
			btn.setOnClickListener(orcl);
		}
	}

	@Override
	public void onPause(){
		super.onPause();
		Editor editor = prefs.edit();
		editor.putLong("widget_"+activity.getDigest()+"_Activity_StartTime", this.getStartTime());
		editor.putBoolean("widget_"+activity.getDigest()+"_Resource_Viewing", this.isResourceViewing());
		editor.putLong("widget_"+activity.getDigest()+"_Resource_StartTime", this.getResourceStartTime());
		editor.putString("widget_"+activity.getDigest()+"_Resource_FileName", this.getResourceFileName());
		editor.commit();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// check to see if the vars are stored in shared prefs
		if(prefs.contains("widget_"+activity.getDigest()+"_Activity_StartTime")){
			this.setStartTime(prefs.getLong("widget_"+activity.getDigest()+"_Activity_StartTime", System.currentTimeMillis()/1000));
		}
		if(prefs.contains("widget_"+activity.getDigest()+"_Resource_Viewing")){
			this.setResourceViewing(prefs.getBoolean("widget_"+activity.getDigest()+"_Resource_Viewing", false));
		}
		if(prefs.contains("widget_"+activity.getDigest()+"_Resource_StartTime")){
			this.setResourceStartTime(prefs.getLong("widget_"+activity.getDigest()+"_Resource_StartTime", System.currentTimeMillis()/1000));
		}
		if(prefs.contains("widget_"+activity.getDigest()+"_Resource_FileName")){
			this.setResourceFileName(prefs.getString("widget_"+activity.getDigest()+"_Resource_FileName", ""));
		}
		
		if (isResourceViewing()) {
			this.resourceStopped();
		} 
		// clear the shared prefs
		Editor editor = prefs.edit();
		Map<String,?> keys = prefs.getAll();

		for(Map.Entry<String,?> entry : keys.entrySet()){
			if (entry.getKey().startsWith("widget_"+activity.getDigest())){
				editor.remove(entry.getKey());
			}            
		 }
		editor.commit();
	}
	
	private void resourceStopped() {
		if (resourceViewing) {
			long resourceEndTime = System.currentTimeMillis() / 1000;
			long timeTaken = resourceEndTime - this.getResourceStartTime();
			resourceViewing = false;
			// track that the resource has been viewed (or at least clicked on)
			Tracker t = new Tracker(super.getActivity());
			JSONObject data = new JSONObject();
			try {
				data.put("resource", "viewed");
				data.put("resourcefile", getResourceFileName());
				data.put("timetaken", timeTaken);
				String lang = prefs.getString(PrefsActivity.PREF_LANGUAGE, Locale.getDefault()
						.getLanguage());
				data.put("lang", lang);
			} catch (JSONException e) {
				Mint.logException(e);
				e.printStackTrace();
			}
			MetaDataUtils mdu = new MetaDataUtils(super.getActivity());
			// add in extra meta-data
			try {
				data = mdu.getMetaData(data);
			} catch (JSONException e) {
				// Do nothing
			}

			GamificationEngine gamificationEngine = new GamificationEngine();
			GamificationEvent gamificationEvent = gamificationEngine.processEventResourceStoppedActivity(this.course, this.activity);

			t.saveTracker(course.getCourseId(), activity.getDigest(), data, true, gamificationEvent);

		}

	}
	
	@Override
	public boolean getActivityCompleted() {
		return true;
	}
	
	@Override
	public void saveTracker(){
		long timetaken = this.getSpentTime();
		if (timetaken < MobileLearning.RESOURCE_READ_TIME) {
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

            GamificationEngine gamificationEngine = new GamificationEngine();
            GamificationEvent gamificationEvent = gamificationEngine.processEventResourceActivity(this.course, this.activity);
			// if it's a baseline activity then assume completed
			if(this.isBaseline){
				t.saveTracker(course.getCourseId(), activity.getDigest(), obj, true, gamificationEvent);
			} else {
				t.saveTracker(course.getCourseId(), activity.getDigest(), obj, this.getActivityCompleted(), gamificationEvent);
			}
		} catch (JSONException e) {
			// Do nothing
		} catch (NullPointerException npe){
			//do nothing
		}
	}

	@Override
	public HashMap<String, Object> getWidgetConfig() {
		HashMap<String, Object> config = new HashMap<String, Object>();
		config.put("Activity_StartTime", this.getStartTime());
		config.put("Resource_Viewing", this.isResourceViewing());
		config.put("Resource_StartTime", this.getResourceStartTime());
		config.put("Resource_FileName", this.getResourceFileName());
		return config;
	}

	@Override
	public void setWidgetConfig(HashMap<String, Object> config) {
		if (config.containsKey("Activity_StartTime")){
			this.setStartTime((Long) config.get("Activity_StartTime"));
		}
		if (config.containsKey("Resource_Viewing")){
			this.setResourceViewing((Boolean) config.get("Resource_Viewing"));
		}
		if (config.containsKey("Resource_StartTime")){
			this.setResourceStartTime((Long) config.get("Resource_StartTime"));
		}
		if (config.containsKey("Resource_FileName")){
			this.setResourceFileName((String) config.get("Resource_FileName"));
		}
	}
	
	@Override
	public String getContentToRead() {
		return null;
	}

	private boolean isResourceViewing() {
		return resourceViewing;
	}

	private void setResourceViewing(boolean resourceViewing) {
		this.resourceViewing = resourceViewing;
	}

	private long getResourceStartTime() {
		return resourceStartTime;
	}

	private void setResourceStartTime(long resourceStartTime) {
		this.resourceStartTime = resourceStartTime;
	}

	private String getResourceFileName() {
		return resourceFileName;
	}

	private void setResourceFileName(String resourceFileName) {
		this.resourceFileName = resourceFileName;
	}
	
	private class OnResourceClickListener implements OnClickListener{

		private Context _ctx;
		private String type;
		
		public OnResourceClickListener(Context ctx, String type){
			this._ctx = ctx;
			this.type = type;
		}

		public void onClick(View v) {
			File file = (File) v.getTag();
			// check the file is on the file system (should be but just in case)
			boolean exists = Storage.mediaFileExists(_ctx, file.getName());
			if(!exists){
				Toast.makeText(_ctx, _ctx.getString(R.string.error_resource_not_found,file.getName()), Toast.LENGTH_LONG).show();
				return;
			}
            Intent intent = ExternalResourceOpener.getIntentToOpenResource(_ctx, file);
            if(intent != null){
                ResourceWidget.this.setResourceViewing(true);
                ResourceWidget.this.setResourceStartTime(System.currentTimeMillis()/1000);
                _ctx.startActivity(intent);
            } else {
                Toast.makeText(_ctx,
                        _ctx.getString(R.string.error_resource_app_not_found, file.getName()),
                        Toast.LENGTH_LONG).show();
            }
		}
		
	}
	
}
