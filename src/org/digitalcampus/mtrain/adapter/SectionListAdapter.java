package org.digitalcampus.mtrain.adapter;

import java.util.ArrayList;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.model.Section;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SectionListAdapter extends ArrayAdapter<Section> {

	public static final String TAG = "SectionListAdapter";

	private final Context context;
	private final ArrayList<Section> sectionList;

	public SectionListAdapter(Activity context, ArrayList<Section> sectionList) {
		super(context, R.layout.section_list_row, sectionList);
		this.context = context;
		this.sectionList = sectionList;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
	    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    
	    View rowView = inflater.inflate(R.layout.section_list_row, parent, false);
	    TextView sectionTitle = (TextView) rowView.findViewById(R.id.section_title);
	    
	    Section s = sectionList.get(position);
	    sectionTitle.setText(s.getTitle());
	    
	    TextView sectionNo = (TextView) rowView.findViewById(R.id.section_number);
	    sectionNo.setText(String.valueOf(s.getSectionId()));
	    
	    ProgressBar pb = (ProgressBar) rowView.findViewById(R.id.section_progress_bar);
	    pb.setProgress((int) s.getProgress());
	    
	    rowView.setTag(sectionList.get(position));
	    return rowView;
	}
	
}

