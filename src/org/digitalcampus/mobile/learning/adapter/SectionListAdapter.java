package org.digitalcampus.mobile.learning.adapter;

import java.util.ArrayList;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.activity.ModuleActivity;
import org.digitalcampus.mobile.learning.model.Module;
import org.digitalcampus.mobile.learning.model.Section;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SectionListAdapter extends ArrayAdapter<Section> {

	public static final String TAG = "SectionListAdapter";
	public static final String TAG_PLACEHOLDER = "placeholder";
	
	private final Context ctx;
	private final ArrayList<Section> sectionList;
	private SharedPreferences prefs;
	private Module module;

	public SectionListAdapter(Activity context, Module module, ArrayList<Section> sectionList) {
		super(context, R.layout.section_list_row, sectionList);
		this.ctx = context;
		this.sectionList = sectionList;
		this.module = module;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
	    LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    
	    View rowView = inflater.inflate(R.layout.section_list_row, parent, false);
	    TextView sectionTitle = (TextView) rowView.findViewById(R.id.section_title);
	    
	    Section s = sectionList.get(position);
	    sectionTitle.setText(s.getTitle(prefs.getString("prefLanguage", Locale.getDefault().getLanguage())));
	    
	    ProgressBar pb = (ProgressBar) rowView.findViewById(R.id.section_progress_bar);
	    pb.setProgress((int) s.getProgress());
	    
	    rowView.setTag(sectionList.get(position));
	    
	    /* set image
	    if(s.getImageFile() != null){
	    	ImageView iv = (ImageView) rowView.findViewById(R.id.section_image);
	    	String path = module.getLocation() + "/" + s.getImageFile();
	    	Bitmap bm = ImageUtils.LoadBMPsdcard(path, ctx.getResources(), R.drawable.default_icon_section);
	    	iv.setImageBitmap(bm);
	    }*/
	    
	    // now set up the hozontal activity list
	    LinearLayout ll = (LinearLayout) rowView.findViewById(R.id.section_activities);
	    for(int i=0 ; i<s.getActivities().size(); i++){
		    View horizRowItem = inflater.inflate(R.layout.section_horizonal_item, parent, false);
		    
		    ll.addView(horizRowItem);
		    
		    TextView tv = (TextView) horizRowItem.findViewById(R.id.activity_title);
		    tv.setText(s.getActivities().get(i).getTitle(prefs.getString("prefLanguage", Locale.getDefault().getLanguage())));
		    
		    // set image
		    ImageView iv = (ImageView) horizRowItem.findViewById(R.id.activity_image);
	    	iv.setImageBitmap(s.getActivities().get(i).getImageFile(module.getLocation(), ctx.getResources()));
	    	
	    	LinearLayout activityObject = (LinearLayout) horizRowItem.findViewById(R.id.activity_object);
	    	activityObject.setTag(R.id.TAG_SECTION_ID, s);
	    	activityObject.setTag(R.id.TAG_PLACEHOLDER_ID, i);
		    // set clicker
	    	activityObject.setClickable(true);
	    	activityObject.setSelected(true);
	    	activityObject.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					Section s = (Section) v.getTag(R.id.TAG_SECTION_ID); 
					int placeholder = (Integer) v.getTag(R.id.TAG_PLACEHOLDER_ID);
					Intent i = new Intent(ctx, ModuleActivity.class);
					Bundle tb = new Bundle();
					tb.putSerializable(Section.TAG, (Section) s);
					tb.putSerializable(Module.TAG, (Module) module);
					tb.putSerializable(SectionListAdapter.TAG_PLACEHOLDER, (Integer) placeholder);
					i.putExtras(tb);
	         		ctx.startActivity(i);
				}
		    });
	    }
	    
	    return rowView;
	}
	
}

