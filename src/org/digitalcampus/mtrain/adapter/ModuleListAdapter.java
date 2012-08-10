package org.digitalcampus.mtrain.adapter;

import java.util.ArrayList;

import org.digitalcampus.mtrain.R;
import org.digitalcampus.mtrain.model.Module;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ModuleListAdapter extends ArrayAdapter<Module> {

	public static final String TAG = "ModuleListAdapter";

	private final Context context;
	private final ArrayList<Module> moduleList;

	public ModuleListAdapter(Activity context, ArrayList<Module> moduleList) {
		super(context, R.layout.module_list_row, moduleList);
		this.context = context;
		this.moduleList = moduleList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    
	    View rowView = inflater.inflate(R.layout.module_list_row, parent, false);
	    TextView textView = (TextView) rowView.findViewById(R.id.module_title);
	    textView.setText(moduleList.get(position).getTitle());
	    rowView.setTag(moduleList.get(position));
	    return rowView;
	}

}
