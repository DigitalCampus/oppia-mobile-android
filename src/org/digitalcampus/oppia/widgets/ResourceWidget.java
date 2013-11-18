package org.digitalcampus.oppia.widgets;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.gesture.ResourceGestureDetector;
import org.digitalcampus.oppia.listener.OnResourceClickListener;
import org.digitalcampus.oppia.model.Course;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.bugsense.trace.BugSenseHandler;

public class ResourceWidget extends WidgetFactory {

	public static final String TAG = ResourceWidget.class.getSimpleName();
	private Context ctx;
	private SharedPreferences prefs;
	private long startTimestamp = System.currentTimeMillis()/1000;
	private GestureDetector resourceGestureDetector;
	private OnTouchListener resourceGestureListener; 
	private boolean isBaselineActivity = false;
	
	public ResourceWidget(Context context, Course module, org.digitalcampus.oppia.model.Activity activity) {
		super(context, module, activity);
		this.ctx = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(this.ctx);
		
		resourceGestureDetector = new GestureDetector((Activity) context, new ResourceGestureDetector((CourseActivity) context));
		resourceGestureListener = new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				try {
					// for some reason unless this is in a try/catch block it will fail with NullPointerException
					return resourceGestureDetector.onTouchEvent(event);
				} catch (Exception e){
					return false;
				}
			}
		};
		
		View vv = super.getLayoutInflater().inflate(R.layout.widget_resource, null);
		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		super.getLayout().addView(vv,lp);
		vv.setOnTouchListener(resourceGestureListener);
		
		LinearLayout ll = (LinearLayout) ((android.app.Activity) context).findViewById(R.id.widget_resource_object);
		String fileUrl = module.getLocation() + activity.getLocation(prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage()));
		Log.d(TAG,fileUrl);
		// show description if any
		String desc = activity.getDescription(prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage()));

		TextView descTV = (TextView) ((android.app.Activity) context).findViewById(R.id.widget_resource_description);
		if (desc.length() > 0){
			descTV.setText(desc);
		} else {
			descTV.setVisibility(View.GONE);
		}
		
		File file = new File(fileUrl);
		OnResourceClickListener orcl = new OnResourceClickListener(this.ctx,activity.getMimeType());
		// show image files
		if (activity.getMimeType().equals("image/jpeg") || activity.getMimeType().equals("image/png")){
			ImageView iv = new ImageView(this.ctx);
			Bitmap myBitmap = BitmapFactory.decodeFile(fileUrl);
			iv.setImageBitmap(myBitmap);
			ll.addView(iv, lp);
			iv.setTag(file);
			iv.setOnClickListener(orcl);
		} else {
		// add button to open other filetypes in whatever app the user has installed as default for that filetype
			Button btn = new Button(this.ctx);
			btn.setText(this.ctx.getString(R.string.widget_resource_open_file,file.getName()));
			btn.setTextAppearance(this.ctx, R.style.ButtonText);
			ll.addView(btn);
			btn.setTag(file);
			btn.setOnClickListener(orcl);
		}
	}

	@Override
	public boolean activityHasTracker() {
		long endTimestamp = System.currentTimeMillis()/1000;
		long diff = endTimestamp - startTimestamp;
		if(diff >= MobileLearning.PAGE_READ_TIME){
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean getActivityCompleted() {
		return true;
	}
	
	@Override
	public void setActivityCompleted(boolean completed){
		//do nothing
	}

	@Override
	public long getTimeTaken() {
		long endTimestamp = System.currentTimeMillis()/1000;
		long diff = endTimestamp - startTimestamp;
		if(diff >= MobileLearning.PAGE_READ_TIME){
			return diff;
		} else {
			return 0;
		}
	}

	private void setStartTime(long startTime) {
		this.startTimestamp = startTime;
	}

	private long getStartTime() {
		return this.startTimestamp;
	}

	@Override
	public JSONObject getTrackerData() {
		JSONObject obj = new JSONObject();
		try {
			obj.put("timetaken", this.getTimeTaken());
			String lang = prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage());
			obj.put("lang", lang);
		} catch (JSONException e) {
			e.printStackTrace();
			BugSenseHandler.sendException(e);
		}
		
		return obj;
	}

	@Override
	public String getContentToRead() {
		return "";
	}

	@Override
	public void setReadAloud(boolean reading) {
	
	}

	@Override
	public boolean getReadAloud() {
		return false;
	}

	@Override
	public void setBaselineActivity(boolean baseline) {
		this.isBaselineActivity = baseline;
	}

	@Override
	public boolean isBaselineActivity() {
		return this.isBaselineActivity;
	}

	@Override
	public HashMap<String, Object> getWidgetConfig() {
		HashMap<String, Object> config = new HashMap<String, Object>();
		config.put("Activity_StartTime", this.getStartTime());
		return config;
	}

	@Override
	public void setWidgetConfig(HashMap<String, Object> config) {
		if (config.containsKey("Activity_StartTime")){
			this.setStartTime((Long) config.get("Activity_StartTime"));
		}
	}
}
