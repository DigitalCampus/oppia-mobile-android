package org.digitalcampus.mobile.learning.adapter;

import java.util.ArrayList;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.model.Module;
import org.digitalcampus.mobile.learning.model.Section;
import org.digitalcampus.mobile.learning.utils.ImageUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SectionListAdapter extends ArrayAdapter<Section> {

	public static final String TAG = "SectionListAdapter";

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
	    
	    // set image
	    if(s.getImageFile() != null){
	    	ImageView iv = (ImageView) rowView.findViewById(R.id.section_image);
	    	String path = module.getLocation() + "/" + s.getImageFile();
	    	Bitmap bm = ImageUtils.LoadBMPsdcard(path, ctx.getResources(), R.drawable.section_default_icon);
	    	iv.setImageBitmap(bm);
	    }
	    return rowView;
	}
	
}

