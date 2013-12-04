package org.digitalcampus.oppia.widgets;

import java.io.File;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.oppia.listener.OnResourceClickListener;
import org.digitalcampus.oppia.model.Course;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class ResourceWidget extends WidgetFactory {

	public static final String TAG = ResourceWidget.class.getSimpleName();
	private Context ctx;	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(super.getActivity());
		course = (Course) getArguments().getSerializable(Course.TAG);
		activity = (org.digitalcampus.oppia.model.Activity) getArguments().getSerializable(org.digitalcampus.oppia.model.Activity.TAG);
		ctx = super.getActivity();
		View vv = super.getLayoutInflater(savedInstanceState).inflate(R.layout.widget_resource, null);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		vv.setLayoutParams(lp);
		return vv;
	}
	 
	@Override
	public void onActivityCreated(Bundle savedInstanceState) { 
		 super.onActivityCreated(savedInstanceState);
		
		LinearLayout ll = (LinearLayout) ((android.app.Activity) ctx).findViewById(R.id.widget_resource_object);
		String fileUrl = course.getLocation() + activity.getLocation(prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage()));
		Log.d(TAG,fileUrl);
		// show description if any
		String desc = activity.getDescription(prefs.getString(ctx.getString(R.string.prefs_language), Locale.getDefault().getLanguage()));

		TextView descTV = (TextView) ((android.app.Activity) ctx).findViewById(R.id.widget_resource_description);
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
			//ll.addView(iv, lp);
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
	public boolean getActivityCompleted() {
		// TODO Auto-generated method stub
		return false;
	}
}
