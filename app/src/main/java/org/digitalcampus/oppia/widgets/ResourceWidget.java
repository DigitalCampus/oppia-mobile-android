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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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

import com.splunk.mint.Mint;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.application.App;
import org.digitalcampus.oppia.application.Tracker;
import org.digitalcampus.oppia.gamification.GamificationEngine;
import org.digitalcampus.oppia.gamification.GamificationServiceDelegate;
import org.digitalcampus.oppia.model.Activity;
import org.digitalcampus.oppia.model.Course;
import org.digitalcampus.oppia.model.GamificationEvent;
import org.digitalcampus.oppia.utils.MetaDataUtils;
import org.digitalcampus.oppia.utils.resources.ExternalResourceOpener;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ResourceWidget extends BaseWidget {

	public static final String TAG = ResourceWidget.class.getSimpleName();


	private static final String PROPERTY_RESOURCE_VIEWING = "Resource_Viewing";
    private static final String PROPERTY_RESOURCE_STARTTIME = "Resource_StartTime";
    private static final String PROPERTY_RESOURCE_FILENAME = "Resource_FileName";

    private static final String STR_WIDGET = "widget_";
	private static final String STR_ACT_STARTTIME = "_Activity_StartTime";
	private static final String STR_RESOURCE_VIEWING = "_Resource_Viewing";
	private static final String STR_RESOURCE_STARTTIME = "_Resource_StartTime";
	private static final String STR_RESOURCE_FILENAME = "_Resource_FileName";

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
		// Required empty public constructor
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		course = (Course) getArguments().getSerializable(Course.TAG);
		activity = (org.digitalcampus.oppia.model.Activity) getArguments().getSerializable(org.digitalcampus.oppia.model.Activity.TAG);
		this.setIsBaseline(getArguments().getBoolean(CourseActivity.BASELINE_TAG));

		View vv = inflater.inflate(R.layout.widget_resource, container, false);
		vv.setId(activity.getActId());
		if ((savedInstanceState != null) && (savedInstanceState.getSerializable(BaseWidget.WIDGET_CONFIG) != null)){
			setWidgetConfig((HashMap<String, Object>) savedInstanceState.getSerializable(BaseWidget.WIDGET_CONFIG));
		}
		return vv;
	}
	 
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(BaseWidget.WIDGET_CONFIG, (Serializable) this.getWidgetConfig());
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) { 
		super.onActivityCreated(savedInstanceState);

		LinearLayout ll = getView().findViewById(R.id.widget_resource_object);
		String fileUrl = course.getLocation() + activity.getLocation(prefLang);

		// show description if any
		String desc = activity.getDescription(prefLang);
		TextView descTV = getView().findViewById(R.id.widget_resource_description);
		if ((desc != null) && desc.length() > 0){
			descTV.setText(desc);
		} else {
			descTV.setVisibility(View.GONE);
		}
		
		File file = new File(fileUrl);
		setResourceFileName(file.getName());
		OnResourceClickListener orcl = new OnResourceClickListener(super.getActivity());
		// show image files
		if (activity.getMimeType().equals("image/jpeg") || activity.getMimeType().equals("image/png")){
			ImageView iv = new ImageView(super.getActivity());
			Bitmap myBitmap = BitmapFactory.decodeFile(fileUrl);
			iv.setImageBitmap(myBitmap);
			LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
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
		editor.putLong(STR_WIDGET+activity.getDigest()+STR_ACT_STARTTIME, this.getStartTime());
		editor.putBoolean(STR_WIDGET+activity.getDigest()+STR_RESOURCE_VIEWING, this.isResourceViewing());
		editor.putLong(STR_WIDGET+activity.getDigest()+STR_RESOURCE_STARTTIME, this.getResourceStartTime());
		editor.putString(STR_WIDGET+activity.getDigest()+STR_RESOURCE_FILENAME, this.getResourceFileName());
		editor.apply();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// check to see if the vars are stored in shared prefs
		if(prefs.contains(STR_WIDGET+activity.getDigest()+STR_ACT_STARTTIME)){
			this.setStartTime(prefs.getLong(STR_WIDGET+activity.getDigest()+STR_ACT_STARTTIME, System.currentTimeMillis()/1000));
		}
		if(prefs.contains(STR_WIDGET+activity.getDigest()+STR_RESOURCE_VIEWING)){
			this.setResourceViewing(prefs.getBoolean(STR_WIDGET+activity.getDigest()+STR_RESOURCE_VIEWING, false));
		}
		if(prefs.contains(STR_WIDGET+activity.getDigest()+STR_RESOURCE_STARTTIME)){
			this.setResourceStartTime(prefs.getLong(STR_WIDGET+activity.getDigest()+STR_RESOURCE_STARTTIME, System.currentTimeMillis()/1000));
		}
		if(prefs.contains(STR_WIDGET+activity.getDigest()+STR_RESOURCE_FILENAME)){
			this.setResourceFileName(prefs.getString(STR_WIDGET+activity.getDigest()+STR_RESOURCE_FILENAME, ""));
		}
		
		if (isResourceViewing()) {
			this.resourceStopped();
		} 
		// clear the shared prefs
		Editor editor = prefs.edit();
		Map<String,?> keys = prefs.getAll();

		for(Map.Entry<String,?> entry : keys.entrySet()){
			if (entry.getKey().startsWith(STR_WIDGET+activity.getDigest())){
				editor.remove(entry.getKey());
			}            
		 }
		editor.apply();
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
				data.put("lang", prefLang);
			} catch (JSONException e) {
				Mint.logException(e);
				Log.d(TAG, "JSONException", e);
			}

			// add in extra meta-data
			try {
				MetaDataUtils mdu = new MetaDataUtils(super.getActivity());
				data = mdu.getMetaData(data);
			} catch (JSONException e) {
				Mint.logException(e);
				Log.d(TAG, "JSONException", e);
			}

			GamificationEngine gamificationEngine = new GamificationEngine( getActivity());
			GamificationEvent gamificationEvent = gamificationEngine.processEventResourceStoppedActivity();

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
		if (activity == null || timetaken < App.RESOURCE_READ_TIME) {
			return;
		}

		new GamificationServiceDelegate(getActivity())
				.createActivityIntent(course, activity, getActivityCompleted(), isBaseline)
				.registerResourceEvent(timetaken);
	}

	@Override
	public HashMap<String, Object> getWidgetConfig() {
		HashMap<String, Object> config = new HashMap<>();
		config.put(BaseWidget.PROPERTY_ACTIVITY_STARTTIME, this.getStartTime());
		config.put(PROPERTY_RESOURCE_VIEWING, this.isResourceViewing());
		config.put(PROPERTY_RESOURCE_STARTTIME, this.getResourceStartTime());
		config.put(PROPERTY_RESOURCE_FILENAME, this.getResourceFileName());
		return config;
	}

	@Override
	public void setWidgetConfig(HashMap<String, Object> config) {
		if (config.containsKey(BaseWidget.PROPERTY_ACTIVITY_STARTTIME)){
			this.setStartTime((Long) config.get(BaseWidget.PROPERTY_ACTIVITY_STARTTIME));
		}
		if (config.containsKey(PROPERTY_RESOURCE_VIEWING)){
			this.setResourceViewing((Boolean) config.get(PROPERTY_RESOURCE_VIEWING));
		}
		if (config.containsKey(PROPERTY_RESOURCE_STARTTIME)){
			this.setResourceStartTime((Long) config.get(PROPERTY_RESOURCE_STARTTIME));
		}
		if (config.containsKey(PROPERTY_RESOURCE_FILENAME)){
			this.setResourceFileName((String) config.get(PROPERTY_RESOURCE_FILENAME));
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

		private Context ctx;
		
		public OnResourceClickListener(Context ctx){
			this.ctx = ctx;
		}

		public void onClick(View v) {
			File file = (File) v.getTag();
			// check the file is on the file system (should be but just in case)
			if(!file.exists()){
				Toast.makeText(ctx, ctx.getString(R.string.error_resource_not_found, file.getName()), Toast.LENGTH_LONG).show();
				return;
			}
            Intent intent = ExternalResourceOpener.getIntentToOpenResource(ctx, file);
            if(intent != null){
                ResourceWidget.this.setResourceViewing(true);
                ResourceWidget.this.setResourceStartTime(System.currentTimeMillis()/1000);
                ctx.startActivity(intent);
            } else {
                Toast.makeText(ctx,
                        ctx.getString(R.string.error_resource_app_not_found, file.getName()),
                        Toast.LENGTH_LONG).show();
            }
		}
		
	}
	
}
