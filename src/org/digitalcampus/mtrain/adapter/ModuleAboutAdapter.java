package org.digitalcampus.mtrain.adapter;

import java.util.ArrayList;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.activity.ModuleAboutActivity.Prop;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ModuleAboutAdapter extends ArrayAdapter<Prop> {

	public static final String TAG = "ModuleAboutAdapter";

	private final Context context;
	private ArrayList<Prop> propsList;
	
	public ModuleAboutAdapter(Activity context, ArrayList<Prop> propsList) {
		super(context, R.layout.module_about_list_row, propsList);
		this.context = context;
		this.propsList = propsList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.module_about_list_row, parent, false);
	    Prop p = propsList.get(position);
	    TextView key = (TextView) rowView.findViewById(R.id.module_about_key);
	    key.setText(p.key);
	    
	    TextView value = (TextView) rowView.findViewById(R.id.module_about_value);
	    value.setText(p.value);
	    return rowView;
	}

}