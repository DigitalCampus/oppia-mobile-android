package org.digitalcampus.oppia.widgets;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.activity.CourseActivity;
import org.digitalcampus.oppia.application.MobileLearning;
import org.digitalcampus.oppia.gesture.ResourceGestureDetector;
import org.digitalcampus.oppia.model.Course;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.bugsense.trace.BugSenseHandler;

public class ResourceWidget extends WidgetFactory {

	public static final String TAG = ResourceWidget.class.getSimpleName();
	private Context ctx;
	private org.digitalcampus.oppia.model.Activity activity;
	private SharedPreferences prefs;
	private long startTimestamp = System.currentTimeMillis()/1000;
	private GestureDetector resourceGestureDetector;
	private OnTouchListener resourceGestureListener; 		
	
	public ResourceWidget(Context context, Course module, org.digitalcampus.oppia.model.Activity activity) {
		super(context, module, activity);
		this.ctx = context;
		this.activity = activity;
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
		OnResourceClickListener orcl = new OnResourceClickListener();
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isBaselineActivity() {
		// TODO Auto-generated method stub
		return false;
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
	
	private class OnResourceClickListener implements OnClickListener{

		public void onClick(View v) {
			File file = (File) v.getTag();
			// check the file is on the file system (should be but just in case)
			if(!file.exists()){
				Toast.makeText(ResourceWidget.this.ctx, ResourceWidget.this.ctx.getString(R.string.error_resource_not_found,file.getName()), Toast.LENGTH_LONG).show();
				return;
			} 
			Uri targetUri = Uri.fromFile(file);
			
			// check there is actually an app installed to open this filetype
			
			Intent intent = new Intent();
			intent.setAction(android.content.Intent.ACTION_VIEW);
			intent.setDataAndType(targetUri, ResourceWidget.this.activity.getMimeType());
			
			PackageManager pm = ResourceWidget.this.ctx.getPackageManager();

			List<ResolveInfo> infos = pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
			boolean appFound = false;
			for (ResolveInfo info : infos) {
				IntentFilter filter = info.filter;
				if (filter != null && filter.hasAction(Intent.ACTION_VIEW)) {
					// Found an app with the right intent/filter
					appFound = true;
				}
			}

			if(appFound){
				ResourceWidget.this.ctx.startActivity(intent);
			} else {
				Toast.makeText(ResourceWidget.this.ctx, ResourceWidget.this.ctx.getString(R.string.error_resource_app_not_found,file.getName()), Toast.LENGTH_LONG).show();
			}
			return;
		}
		
	}

}
