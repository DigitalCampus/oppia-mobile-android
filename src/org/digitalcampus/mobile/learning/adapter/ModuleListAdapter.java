package org.digitalcampus.mobile.learning.adapter;

import java.util.ArrayList;
import java.util.Locale;

import org.digitalcampus.mobile.learning.R;
import org.digitalcampus.mobile.learning.model.Module;
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

public class ModuleListAdapter extends ArrayAdapter<Module> {

	public static final String TAG = "ModuleListAdapter";

	private final Context ctx;
	private final ArrayList<Module> moduleList;
	private SharedPreferences prefs;
	
	public ModuleListAdapter(Activity context, ArrayList<Module> moduleList) {
		super(context, R.layout.module_list_row, moduleList);
		this.ctx = context;
		this.moduleList = moduleList;
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View rowView = inflater.inflate(R.layout.module_list_row, parent, false);
	    Module m = moduleList.get(position);
	    rowView.setTag(m);
	    
	    TextView moduleTitle = (TextView) rowView.findViewById(R.id.module_title);
	    moduleTitle.setText(m.getTitle(prefs.getString("prefLanguage", Locale.getDefault().getLanguage())));
	    
	    ProgressBar pb = (ProgressBar) rowView.findViewById(R.id.module_progress_bar);
	    pb.setProgress((int) m.getProgress());
	    
		// set image
		if(m.getImageFile() != null){
			ImageView iv = (ImageView) rowView.findViewById(R.id.module_image);
			String path = m.getLocation() + "/" + m.getImageFile();
			Bitmap bm = ImageUtils.LoadBMPsdcard(path, ctx.getResources(), R.drawable.module_default_icon);
			iv.setImageBitmap(bm);
		}
	    return rowView;
	}

}
